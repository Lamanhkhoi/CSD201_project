package controller;

import model.InventoryItem;
import model.Order;
import structures.CustomHashMap;
import structures.SortedDynamicArray;
import structures.MinHeapPriorityQueue;
import structures.ArrayList;
import fileio.IFileReadWrite;
import java.util.Comparator;

public class MainController {
    // 1. Vùng bộ nhớ quản lý dữ liệu Kho tập trung (In-Memory Database)
    private CustomHashMap<String, InventoryItem> inventoryMap;
    private SortedDynamicArray<InventoryItem> inventorySortedArray;
    private MinHeapPriorityQueue<InventoryItem> expiryHeap;

    // 2. Thành phần xử lý File Vật lý
    private IFileReadWrite<InventoryItem> inventoryFileHandler;
    
    
    // Bổ sung file txt sau

    // 3. Các Sub-Controller điều khiển chức năng
    private InventoryController inventoryController;
    private OrderController orderController;

    public MainController(IFileReadWrite<InventoryItem> inventoryFileHandler) {
        this.inventoryFileHandler = inventoryFileHandler;
        
        // Bổ sung file txt sau
        this.inventoryMap = new CustomHashMap<>();
        this.inventorySortedArray = new SortedDynamicArray<>(new Comparator<InventoryItem>() {
            @Override
            public int compare(InventoryItem o1, InventoryItem o2) {
                return o1.getSku().compareTo(o2.getSku());
            }
        });

        // Cấu trúc C: Min-Heap luôn ưu tiên hạn sử dụng sớm nhất lên đầu (FEFO)
        this.expiryHeap = new MinHeapPriorityQueue<>(new Comparator<InventoryItem>() {
            @Override
            public int compare(InventoryItem o1, InventoryItem o2) {
                return o1.getExpiryDate().compareTo(o2.getExpiryDate());
            }
        });

        // Tự động gọi hàm nạp toàn bộ file vào RAM khi hệ thống khởi động
        loadAllFiles();
        // Cần tạo thêm controller
        this.inventoryController = new InventoryController(
                inventoryMap, inventorySortedArray, expiryHeap, inventoryFileHandler
        );
        this.orderController = new OrderController(
                inventoryMap, inventorySortedArray, expiryHeap, inventoryFileHandler
        );
    }

    // Hàm load file
    private void loadAllFiles() {
        try {
            System.out.println("System: Initializing and loading data from flat files...");
            ArrayList<InventoryItem> loadedItems = inventoryFileHandler.read();
            
            if (loadedItems != null) {
                for (int i = 0; i < loadedItems.size(); i++) {
                    InventoryItem item = loadedItems.get(i);
                    inventoryMap.put(item.getBatchId(), item);
                    inventorySortedArray.insertSorted(item);
                    expiryHeap.enqueue(item);
                }
            }
            System.out.println("System: Data allocation successfully synchronized to custom structures.");
        } catch (Exception e) {
            System.out.println("System Error during initialization: " + e.getMessage());
        }
    }

    // Ủy quyền trực tiếp cho Sub-Controller xử lý logic và hỏi lưu file
    public boolean executeAddInventoryItem() {
        return inventoryController.addNewInventoryItem();
    }

    //Nhận yêu cầu bốc hàng theo đơn từ giao diện và đẩy về cho OrderController
    public boolean executeProcessOrder(Order order) {
        if (order == null || order.getItemsToPick() == null) {
            return false;
        }
        // Ủy quyền trực tiếp cho Sub-Controller xử lý thuật toán FEFO
        return orderController.processOrderFEFO(order);
    }

    // Tự thêm các Sub Sub-Controller khác
    
    
    // Các hàm getter để MainViewer hoặc các module khác có thể xem trạng thái dữ liệu khi cần
    public CustomHashMap<String, InventoryItem> getInventoryMap() { return inventoryMap; }
    public SortedDynamicArray<InventoryItem> getInventorySortedArray() { return inventorySortedArray; }
    public MinHeapPriorityQueue<InventoryItem> getExpiryHeap() { return expiryHeap; }
}