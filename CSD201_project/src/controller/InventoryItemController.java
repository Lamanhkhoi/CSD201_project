package controller;

import model.InventoryBatch;
import model.InventoryItem;
import model.Transaction;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/*
 Controller quản lý tồn kho theo thiết kế "ngăn tủ" (batchId <-> sku là 1-1):
 - batchMap: tra cứu O(1) theo batchId -> InventoryBatch. Một batchId tồn tại
   trong map nghĩa là nó đang active - KHÔNG còn khái niệm xóa mềm/free-list,
   xóa là gỡ hẳn khỏi map (xóa cứng).
 - skuToBatchId: reverse-index O(1) để biết 1 SKU đang nằm ở batchId nào.
 - batchSequence: bộ đếm sinh batchId mới (BAT001, BAT002...), chỉ tăng dần,
   không bao giờ tái sử dụng số cũ.
 */
public class InventoryItemController {

    private final HashMap<String, InventoryBatch> batchMap;
    private final HashMap<String, String> skuToBatchId;
    private int batchSequence;
    private final TransactionController transactionController;

    public InventoryItemController(HashMap<String, InventoryBatch> batchMap, HashMap<String, String> skuToBatchId,
            TransactionController transactionController) {
        this.batchMap = batchMap;
        this.skuToBatchId = skuToBatchId;
        this.batchSequence = 0;
        this.transactionController = transactionController;
    }

    // Gọi 1 lần sau khi đọc xong dữ liệu từ file: dựng lại skuToBatchId và khôi phục batchSequence
    public void initializeFromLoadedData(List<InventoryBatch> loadedBatches) {
        for (InventoryBatch batch : loadedBatches) {
            batchMap.put(batch.getBatchId(), batch);
            skuToBatchId.put(batch.getSku(), batch.getBatchId());
            updateBatchSequenceFromId(batch.getBatchId());
        }
    }

    // Trích số thứ tự từ batchId (VD: "BAT007" -> 7) để cập nhật batchSequence nếu lớn hơn giá trị hiện có
    private void updateBatchSequenceFromId(String batchId) {
        try {
            String numberPart = batchId.replaceAll("[^0-9]", "");
            if (!numberPart.isEmpty()) {
                int number = Integer.parseInt(numberPart);
                if (number > batchSequence) {
                    batchSequence = number;
                }
            }
        } catch (NumberFormatException e) {
            // batchId không đúng định dạng tự sinh (VD: nhập tay khác chuẩn) -> bỏ qua
        }
    }

    // Luôn sinh batchId hoàn toàn mới - không còn free-list để tái sử dụng
    private String generateNewBatchId() {
        batchSequence++;
        return String.format("BAT%03d", batchSequence);
    }

    // ==============================================================
    // 1. NHẬP KHO
    // ==============================================================
    public boolean receiveStock(String sku, int quantity, LocalDate receiveDate, LocalDate expiryDate) {
        String batchId = skuToBatchId.get(sku);
        InventoryBatch batch;
        boolean isNewBatch = false;

        if (batchId != null) {
            batch = batchMap.get(batchId);
        } else {
            batchId = generateNewBatchId();
            batch = new InventoryBatch(batchId, sku);
            batchMap.put(batchId, batch);
            skuToBatchId.put(sku, batchId);
            isNewBatch = true;
        }

        InventoryItem newLot = batch.addLot(quantity, receiveDate, expiryDate);

        String txId = "TX-IMP-" + System.currentTimeMillis() + "-" + newLot.getSlotId();
        Transaction newTx = new Transaction(txId, "N/A", "IMPORT", sku, newLot.getSlotId(), quantity, LocalDateTime.now());
        transactionController.addTransaction(newTx);

        System.out.println("-> [Hệ thống] Nhập kho thành công: SKU " + sku + " vào "
                + (isNewBatch ? "ngăn tủ mới " : "ngăn tủ đã có ") + batchId + " (lô " + newLot.getSlotId() + ").");
        return true;
    }

    // ==============================================================
    // 2. CẬP NHẬT VỊ TRÍ (dời SKU sang ngăn tủ khác)
    // ==============================================================
    /*
     Ngăn tủ đích hợp lệ khi CHƯA TỒN TẠI trong batchMap (sẽ được tạo mới).
     Nếu đã tồn tại nghĩa là đang có SKU khác chiếm -> từ chối.
     */
    public boolean moveSkuToBatch(String sku, String targetBatchId) {
        String oldBatchId = skuToBatchId.get(sku);
        if (oldBatchId == null) {
            System.out.println("-> [Lỗi] SKU '" + sku + "' hiện không có trong kho.");
            return false;
        }
        if (oldBatchId.equals(targetBatchId)) {
            System.out.println("-> [Lỗi] Ngăn tủ đích trùng với ngăn tủ hiện tại.");
            return false;
        }
        if (batchMap.containsKey(targetBatchId)) {
            System.out.println("-> [Lỗi] Ngăn tủ '" + targetBatchId + "' đang chứa SKU khác ("
                    + batchMap.get(targetBatchId).getSku() + ").");
            return false;
        }

        InventoryBatch oldBatch = batchMap.get(oldBatchId);
        InventoryBatch targetBatch = new InventoryBatch(targetBatchId, sku);
        batchMap.put(targetBatchId, targetBatch);

        // Chuyển toàn bộ lô hàng con sang ngăn tủ mới - GIỮ NGUYÊN slotId cũ để không mất truy vết lịch sử
        for (InventoryItem lot : oldBatch.getAllLots()) {
            targetBatch.addExistingLot(lot);
        }

        // Ngăn tủ cũ giờ rỗng và không còn ai giữ SKU -> gỡ hẳn khỏi hệ thống (xóa cứng)
        batchMap.remove(oldBatchId);
        skuToBatchId.put(sku, targetBatchId);

        System.out.println("-> [Thành công] Đã dời SKU '" + sku + "' từ ngăn tủ " + oldBatchId
                + " sang " + targetBatchId + ".");
        return true;
    }

    // ==============================================================
    // 3. XÓA (cứng) - chỉ khi tổng số lượng trong ngăn tủ bằng 0
    // ==============================================================
    public boolean deleteBySku(String sku) {
        String batchId = skuToBatchId.get(sku);
        if (batchId == null) {
            System.out.println("-> [Lỗi] SKU '" + sku + "' không có trong kho.");
            return false;
        }

        InventoryBatch batch = batchMap.get(batchId);
        if (batch.getTotalQuantity() != 0) {
            System.out.println("-> [Lỗi] Không thể xóa - ngăn tủ '" + batchId + "' vẫn còn "
                    + batch.getTotalQuantity() + " sản phẩm.");
            return false;
        }

        batchMap.remove(batchId);
        skuToBatchId.remove(sku);

        System.out.println("-> [Thành công] Đã xóa SKU '" + sku + "' và gỡ hẳn ngăn tủ " + batchId + ".");
        return true;
    }

    // ==============================================================
    // 4. CÁC HÀM TRA CỨU / HIỂN THỊ
    // ==============================================================
    public InventoryBatch findBatchBySku(String sku) {
        String batchId = skuToBatchId.get(sku);
        if (batchId == null) {
            return null;
        }
        return batchMap.get(batchId);
    }

    public InventoryBatch findBatchById(String batchId) {
        return batchMap.get(batchId);
    }

    // Toàn bộ ngăn tủ còn hàng (bỏ qua ngăn tủ hết sạch nhưng chưa bị xóa)
    public List<InventoryBatch> getAllActiveBatches() {
        List<InventoryBatch> result = new ArrayList<>();
        for (InventoryBatch batch : batchMap.values()) {
            if (!batch.isEmpty()) {
                result.add(batch);
            }
        }
        return result;
    }

    // Sắp theo hạn sử dụng GẦN NHẤT hiện có trong từng ngăn tủ (lô đầu hàng đợi FEFO của batch đó)
    public List<InventoryBatch> getBatchesSortedByEarliestExpiry() {
        List<InventoryBatch> list = getAllActiveBatches();
        Collections.sort(list, new Comparator<InventoryBatch>() {
            @Override
            public int compare(InventoryBatch a, InventoryBatch b) {
                return a.peekEarliestLot().getExpiryDate().compareTo(b.peekEarliestLot().getExpiryDate());
            }
        });
        return list;
    }

    // Cảnh báo cận date: xét lô sắp hết hạn nhất trong mỗi ngăn tủ
    public List<InventoryBatch> getAlertBatches(int daysThreshold) {
        List<InventoryBatch> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (InventoryBatch batch : getAllActiveBatches()) {
            InventoryItem earliestLot = batch.peekEarliestLot();
            if (earliestLot != null) {
                long daysUntilExpiry = ChronoUnit.DAYS.between(today, earliestLot.getExpiryDate());
                if (daysUntilExpiry <= daysThreshold) {
                    result.add(batch);
                }
            }
        }
        return result;
    }

    // Dùng cho MainController khi cần lưu toàn bộ batch xuống file
    public List<InventoryBatch> getAllBatchesForSave() {
        return new ArrayList<>(batchMap.values());
    }
}