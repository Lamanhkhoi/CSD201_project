package controller;

import java.time.LocalDateTime;
import utilities.StorageHandler;
import model.InventoryItem;
import structures.PriorityQueue;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import model.Transaction;

public class InventoryItemController {

    private HashMap<String, InventoryItem> inventoryMap;
    private PriorityQueue<InventoryItem> expiryHeap;
    private List<InventoryItem> inventoryList;
    private StorageHandler<InventoryItem, List<InventoryItem>> storageHandler;
    private final TransactionController transactionController;
    public InventoryItemController(StorageHandler<InventoryItem, List<InventoryItem>> storageHandler, TransactionController transactionController) {
        this.storageHandler = storageHandler;
        this.inventoryMap = new HashMap<>();
        this.inventoryList = new ArrayList<>();
        this.transactionController = transactionController;
        this.expiryHeap = new PriorityQueue<>(new Comparator<InventoryItem>() {
            @Override
            public int compare(InventoryItem o1, InventoryItem o2) {
                return o1.compareTo(o2);
            }
        });
    }
    
    // ==============================================================
    // HÀM NẠP DỮ LIỆU TỪ FILE VÀO RAM KHI KHỞI ĐỘNG
    // ==============================================================
    public void loadData(List<InventoryItem> loadedItems) {
        if (loadedItems == null || loadedItems.isEmpty()) {
            System.out.println("-> [Hệ thống] File inventory.txt trống. Đã khởi tạo kho hàng mới.");
            return;
        }

        for (InventoryItem item : loadedItems) {
            this.inventoryMap.put(item.getBatchId(), item);  
            this.inventoryList.add(item);                    
            this.expiryHeap.enqueue(item);                   
        }
        
        System.out.println("-> [Hệ thống] Đã nạp thành công " + loadedItems.size() + " lô hàng từ cơ sở dữ liệu lên RAM.");
    }

    // ==============================================================
    // LOGIC XỬ LÝ NGHIỆP VỤ (BUSINESS LOGIC)
    // ==============================================================
    public void receiveNewItem(InventoryItem newItem) {
        System.out.println("-> [Hệ thống] Đang xử lý đưa lô hàng vào các bộ nhớ tạm...");
        inventoryMap.put(newItem.getBatchId(), newItem);
        inventoryList.add(newItem);
        expiryHeap.enqueue(newItem);
        System.out.println("-> [Hệ thống] Nhập kho thành công mã lô: " + newItem.getBatchId());
        // === ĐOẠN THÊM MỚI 2: TỰ ĐỘNG TẠO TRANSACTION NHẬP KHO ===
       String txId = "TX-IMP-" + System.currentTimeMillis() + "-" + newItem.getBatchId();
        
        // Vì nhập trực tiếp từ nhà cung cấp chưa có đơn xuất, orderId ta truyền "N/A"
        Transaction newTx = new Transaction(
            txId,                         // 1. transactionId (String)
            "N/A",                        // 2. orderId (String)
            "IMPORT",                     // 3. type (String)
            newItem.getSku(),             // 4. sku (String)
            newItem.getBatchId(),         // 5. batchId (String)
            newItem.getQuantity(),        // 6. quantity (int)
            LocalDateTime.now()           // 7. date (LocalDateTime)
        );
        
        // Đẩy thẳng sang SinglyLinkedList trên RAM của bạn
        this.transactionController.addTransaction(newTx);
        System.out.println("-> [Hệ thống] Đã tự động lập biên bản nhật ký nhập kho (" + txId + ").");
        // =========================================================
        triggerSave();
    }

    //code thuật toán lấy hàng cận date từ expiryHeap.dequeueMin()
    public void pickStockAutoFEFO(String sku, int quantityToPick) {
        List<InventoryItem> tempList = new ArrayList<>();

        while (quantityToPick > 0 && !expiryHeap.isEmpty()) {
            InventoryItem currentBatch = expiryHeap.dequeueMin();
            if (!currentBatch.getSku().equalsIgnoreCase(sku)) {
                tempList.add(currentBatch);
                continue;
            }
            if (currentBatch.getQuantity() <= quantityToPick) {
                quantityToPick -= currentBatch.getQuantity();
                currentBatch.setQuantity(0);
                inventoryMap.remove(currentBatch.getBatchId());
                inventoryList.remove(currentBatch);
            } else {
                currentBatch.setQuantity(currentBatch.getQuantity() - quantityToPick);
                quantityToPick = 0;
                tempList.add(currentBatch);
            }
        }
        //Đưa những hàng không dùng đến từ giỏ tạm về lại kho Heap
        for (InventoryItem item : tempList) {
            expiryHeap.enqueue(item);
        }

        //Kiểm tra kết quả và Lưu file
        if (quantityToPick > 0) {
            System.out.println("-> [Cảnh báo] Kho không đủ hàng mã " + sku + "! Khách còn thiếu " + quantityToPick + " sản phẩm.");
        } else {
            System.out.println("-> [Thành công] Đã xuất kho tự động ưu tiên hàng cận date (FEFO) hoàn tất!");
        }

        triggerSave();

    }

    // TODO: Sẽ gọi inventoryMap.get() để sửa
    public void updateItemLocation(String batchId, String newLocation) {
        System.out.println("-> [Hệ thống] Đang tìm kiếm lô hàng...");
        InventoryItem targetItem = inventoryMap.get(batchId);

        // Kiểm tra xem mã lô hàng có thực sự tồn tại trong kho không
        if (targetItem == null) {
            System.out.println("-> [Lỗi] Không tìm thấy mã lô hàng '" + batchId + "' trên hệ thống!");
            return;
        }

        //Lưu lại vị trí cũ để in thông báo, sau đó cập nhật vị trí mới
        String oldLocation = targetItem.getLocation();
        targetItem.setLocation(newLocation);
        System.out.println("-> [Thành công] Đã dời lô hàng " + batchId + " từ kệ [" + oldLocation + "] sang kệ [" + newLocation + "].");
        triggerSave();
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
        return null;
    }

    public List<InventoryItem> getAllInventoryList() {
        return this.inventoryList;
    }

    private void triggerSave() {
        // Hiện tại in Console để test, sau khi ráp file O sẽ mở comment dòng dưới
        storageHandler.askAndSave(this.inventoryList);
        System.out.println("-> [Hệ thống] Đã lưu thông tin.");
    }

}
