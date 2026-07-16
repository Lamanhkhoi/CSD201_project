package fileio;

import model.InventoryBatch;
import model.InventoryItem;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/*
 Định dạng dòng (7 cột, phân tách ';'):batchId;sku;isActive;slotId;quantity;receiveDate;expiryDate

 Trường hợp đặc biệt: batch không còn lô hàng con nào (đã xuất hết hoặc vừa
 bị xóa mềm, số lượng = 0) vẫn phải lưu lại 1 dòng "shell" để giữ thông tin
 batchId/sku/isActive - lúc đó 4 cột còn lại được ghi bằng dấu '-'.
 */
public class InventoryItemReadWrite implements IFileReadWrite<InventoryBatch, List<InventoryBatch>> {

    private static final String FILE_PATH = "data/inventory.txt";
    private static final String EMPTY_MARKER = "-";

    @Override
    public List<InventoryBatch> read() throws Exception {
        List<InventoryBatch> resultList = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
            return resultList;
        }

        // Dùng LinkedHashMap để vừa gom nhóm theo batchId, vừa giữ đúng thứ tự xuất hiện trong file
        LinkedHashMap<String, InventoryBatch> batchMap = new LinkedHashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(";");
                if (parts.length != 7) { // Sai định dạng (không đúng 7 cột) -> bỏ qua dòng này
                    System.out.println("-> [Cảnh báo] Bỏ qua dòng sai định dạng trong inventory.txt: " + line);
                    continue;
                }

                String batchId = parts[0];
                String sku = parts[1];
                boolean isActive = Boolean.parseBoolean(parts[2]);
                String slotId = parts[3];

                // Lấy batch đã có trong map, hoặc tạo mới nếu đây là lần đầu gặp batchId này
                InventoryBatch batch = batchMap.get(batchId);
                if (batch == null) {
                    batch = new InventoryBatch(batchId, sku);
                    batchMap.put(batchId, batch);
                }
                batch.setIsActive(isActive);

                // Dòng "shell" (batch rỗng, không có lô hàng) -> không tạo InventoryItem
                if (EMPTY_MARKER.equals(slotId)) {
                    continue;
                }

                int quantity = Integer.parseInt(parts[4]);
                LocalDate receiveDate = LocalDate.parse(parts[5]);
                LocalDate expiryDate = LocalDate.parse(parts[6]);

                InventoryItem lot = new InventoryItem(slotId, quantity, receiveDate, expiryDate);
                batch.addExistingLot(lot); // Dùng slotId có sẵn từ file, KHÔNG sinh slotId mới

                // Khôi phục lại bộ đếm slotSequence để lần nhập tiếp theo không sinh trùng slotId cũ
                restoreSlotSequence(batch, slotId);
            }
        }

        resultList.addAll(batchMap.values());
        return resultList;
    }

    /*
     Trích số thứ tự từ slotId (VD: "BAT001-LOT03" -> 3), rồi cập nhật slotSequence
     của batch nếu số này lớn hơn giá trị đang lưu - đảm bảo sau khi đọc file
     xong, slotSequence luôn bằng đúng số thứ tự lớn nhất đã từng được dùng.
     */
    private void restoreSlotSequence(InventoryBatch batch, String slotId) {
        try {
            int dashLotIndex = slotId.lastIndexOf("-LOT");
            if (dashLotIndex == -1) {
                return;
            }
            int sequenceNumber = Integer.parseInt(slotId.substring(dashLotIndex + 4));
            if (sequenceNumber > batch.getSlotSequence()) {
                batch.setSlotSequence(sequenceNumber);
            }
        } catch (NumberFormatException e) {
            // slotId không đúng định dạng tự sinh (VD: dữ liệu nhập tay) -> bỏ qua, không chặn việc đọc file
        }
    }

    @Override
    public boolean write(List<InventoryBatch> batchesToSave) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (InventoryBatch batch : batchesToSave) {
                List<InventoryItem> lots = batch.getAllLots();

                if (lots.isEmpty()) {
                    // Batch rỗng (đã xuất hết hoặc vừa xóa mềm) -> ghi 1 dòng shell để giữ lại batchId/sku/isActive
                    pw.println(String.format("%s;%s;%s;%s;%s;%s;%s",
                            batch.getBatchId(), batch.getSku(), batch.isIsActive(),
                            EMPTY_MARKER, EMPTY_MARKER, EMPTY_MARKER, EMPTY_MARKER));
                    continue;
                }

                for (InventoryItem lot : lots) {
                    pw.println(String.format("%s;%s;%s;%s;%d;%s;%s",
                            batch.getBatchId(), batch.getSku(), batch.isIsActive(),
                            lot.getSlotId(), lot.getQuantity(), lot.getReceiveDate(),
                            lot.getExpiryDate()));
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("-> [Lỗi] Không thể ghi file inventory.txt: " + e.getMessage());
            return false;
        }
    }
}