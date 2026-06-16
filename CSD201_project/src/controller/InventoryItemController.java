package controller;

import java.time.LocalDateTime;
import model.InventoryItem;
import structures.PriorityQueue;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import model.Transaction;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class InventoryItemController {

    private HashMap<String, InventoryItem> inventoryMap;
    private PriorityQueue<InventoryItem> expiryHeap;
    private List<InventoryItem> inventoryList;
    private final TransactionController transactionController;

    // Thay đổi Constructor nhận đúng 4 tham số dữ liệu thô từ Main truyền vào
    public InventoryItemController(HashMap<String, InventoryItem> inventoryMap, PriorityQueue<InventoryItem> expiryHeap,
            List<InventoryItem> inventoryList, TransactionController transactionController) {
        this.inventoryMap = inventoryMap;
        this.expiryHeap = expiryHeap;
        this.inventoryList = inventoryList;
        this.transactionController = transactionController;
    }

    // ==============================================================
    // LOGIC XỬ LÝ NGHIỆP VỤ (BUSINESS LOGIC)
    // ==============================================================
    public boolean receiveNewItem(InventoryItem newItem) {
        // Kiểm tra trùng mã lô hàng để tránh ghi đè dữ liệu map
        if (inventoryMap.containsKey(newItem.getBatchId())) {
            System.out.println("-> [Lỗi] Mã lô hàng '" + newItem.getBatchId() + "' đã tồn tại trên hệ thống!");
            return false;
        }

        System.out.println("-> [Hệ thống] Đang xử lý đưa lô hàng vào các bộ nhớ tạm...");
        inventoryMap.put(newItem.getBatchId(), newItem);
        inventoryList.add(newItem);
        expiryHeap.enqueue(newItem);
        System.out.println("-> [Hệ thống] Nhập kho thành công mã lô: " + newItem.getBatchId());
        // === TỰ ĐỘNG TẠO TRANSACTION NHẬP KHO ===
        String txId = "TX-IMP-" + System.currentTimeMillis() + "-" + newItem.getBatchId();

        // Vì nhập trực tiếp từ nhà cung cấp chưa có đơn xuất, orderId ta truyền "N/A"
        Transaction newTx = new Transaction(
                txId, // 1. transactionId (String)
                "N/A", // 2. orderId (String)
                "IMPORT", // 3. type (String)
                newItem.getSku(), // 4. sku (String)
                newItem.getBatchId(), // 5. batchId (String)
                newItem.getQuantity(), // 6. quantity (int)
                LocalDateTime.now() // 7. date (LocalDateTime)
        );

        // Đẩy thẳng sang SinglyLinkedList trên RAM của bạn
        this.transactionController.addTransaction(newTx);
        System.out.println("-> [Hệ thống] Đã tự động lập biên bản nhật ký nhập kho (" + txId + ").");
        // =========================================================
        return true; 
    }

    public boolean updateItemLocation(String batchId, String newLocation) {
        System.out.println("-> [Hệ thống] Đang tìm kiếm lô hàng...");
        InventoryItem targetItem = inventoryMap.get(batchId);

        // Kiểm tra xem mã lô hàng có thực sự tồn tại trong kho không
        if (targetItem == null) {
            System.out.println("-> [Lỗi] Không tìm thấy mã lô hàng '" + batchId + "' trên hệ thống!");
            return false;
        }

        //Lưu lại vị trí cũ để in thông báo, sau đó cập nhật vị trí mới
        String oldLocation = targetItem.getLocation();
        targetItem.setLocation(newLocation);
        System.out.println("-> [Thành công] Đã dời lô hàng " + batchId + " từ kệ [" + oldLocation + "] sang kệ [" + newLocation + "].");
        return true;
    }

    // --- CÁC HÀM TRA CỨU ĐỂ VIEW SỬ DỤNG ---
    public InventoryItem getBatchById(String batchId) {
        return inventoryMap.get(batchId);
    }

    public List<InventoryItem> getBatchesBySku(String sku) {
        List<InventoryItem> result = new ArrayList<>();
        // Quét toàn bộ kho hàng
        for (InventoryItem item : inventoryList) {
            if (item.getSku().equalsIgnoreCase(sku)) {
                result.add(item);
            }
        }

        return result;
    }

    public List<InventoryItem> getAlertItems(int daysThreshold) {
        List<InventoryItem> alertList = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (InventoryItem item : inventoryList) {
            long daysUntilExpiry = ChronoUnit.DAYS.between(today, item.getExpiryDate());
            if (daysUntilExpiry <= daysThreshold) {
                alertList.add(item);
            }
        }
        return alertList;
    }

    public List<InventoryItem> getAllInventoryList() {
        return this.inventoryList;
    }

    // Trả về danh sách đã sắp xếp theo Số lượng
    public List<InventoryItem> getInventorySortedByQuantity() {
        List<InventoryItem> sortedList = new ArrayList<>(this.inventoryList);
        sortedList.sort(new Comparator<InventoryItem>() {
            @Override
            public int compare(InventoryItem o1, InventoryItem o2) {
                return Integer.compare(o1.getQuantity(), o2.getQuantity());
            }
        });
        return sortedList;
    }

    // Trả về danh sách đã sắp xếp theo Ngày hết hạn (FEFO)
    public List<InventoryItem> getInventorySortedByExpiryDate() {
        List<InventoryItem> sortedList = new ArrayList<>(this.inventoryList);
        sortedList.sort(new Comparator<InventoryItem>() {
            @Override
            public int compare(InventoryItem o1, InventoryItem o2) {
                return o1.getExpiryDate().compareTo(o2.getExpiryDate());
            }
        });
        return sortedList;
    }
}
