package controller;

import model.*;
import fileio.*;
import structures.SinglyLinkedList;
import structures.InventoryPriorityQueue;
import structures.OrderPriorityQueue;
;
import utilities.StorageHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class MainController {

    // 1. Vùng lưu trữ dữ liệu tập trung toàn hệ thống trên RAM
    private SinglyLinkedList<Product> productList;
    private HashMap<String, InventoryItem> inventoryMap;
    private InventoryPriorityQueue expiryHeap;
    private List<InventoryItem> inventoryList;
    private List<Order> allOrdersList;
    private HashMap<String, Order> orderLookupMap;
    private OrderPriorityQueue waitingOrderFEFOQueue;
    private SinglyLinkedList<Transaction> transactionHistory;

    // 2. Các File IO Handlers độc lập
    private final ProductReadWrite productFileIO;
    private final InventoryItemReadWrite inventoryFileIO;
    private final OrderReadWrite orderFileHandler;
    private final TransactionReadWrite tranFileHandler;

    // 3. StorageHandlers
    private final StorageHandler<Product, SinglyLinkedList<Product>> productStorage;
    private final StorageHandler<InventoryItem, List<InventoryItem>> inventoryStorage;
    private final StorageHandler<Order, List<Order>> orderStorage;
    private final StorageHandler<Transaction, SinglyLinkedList<Transaction>> tranStorage;

    // 4. SubController
    private final ProductController productController;
    private final InventoryItemController inventoryController;
    private final OrderController orderController;
    private final TransactionController transactionController;

    public MainController() {
        this.productList = new SinglyLinkedList<>();
        this.inventoryMap = new HashMap<>();
        this.inventoryList = new ArrayList<>();
        this.allOrdersList = new ArrayList<>();
        this.orderLookupMap = new HashMap<>();
        this.transactionHistory = new SinglyLinkedList<>();

        this.expiryHeap = new InventoryPriorityQueue();
        this.waitingOrderFEFOQueue = new OrderPriorityQueue();

        // Khởi tạo tầng Đọc/Ghi file vật lý
        this.productFileIO = new ProductReadWrite();
        this.inventoryFileIO = new InventoryItemReadWrite();
        this.orderFileHandler = new OrderReadWrite();
        this.tranFileHandler = new TransactionReadWrite();

        // Khởi tạo các StorageHandler tương ứng
        this.productStorage = new StorageHandler<>(productFileIO);
        this.inventoryStorage = new StorageHandler<>(inventoryFileIO);
        this.orderStorage = new StorageHandler<>(orderFileHandler);
        this.tranStorage = new StorageHandler<>(tranFileHandler);

        // Đọc toàn bộ dữ liệu từ các file văn bản lên RAM trước khi chạy SubController
        loadAllSystemData();

        // Khởi tạo các SubController bằng cách TRUYỀN THAM CHIẾU DATA VÀO CONSTRUCTOR
        this.productController = new ProductController(this.productList);
        this.transactionController = new TransactionController(this.transactionHistory);
        this.inventoryController = new InventoryItemController(this.inventoryMap, this.expiryHeap, this.inventoryList, this.transactionController);
        this.orderController = new OrderController(this.allOrdersList, this.inventoryMap, this.expiryHeap, this.transactionController);
    }

    private void loadAllSystemData() {
        try {
            this.productList = productFileIO.read();
            System.out.println("System Core: Tải dữ liệu sản phẩm thành công.");
        } catch (Exception e) {
            System.out.println("System Core Warning: Lỗi tải file sản phẩm: " + e.getMessage());
        }

        try {
            this.inventoryList = inventoryFileIO.read();
            for (InventoryItem item : this.inventoryList) {
                this.inventoryMap.put(item.getBatchId(), item);
                if (item.getStatus().equalsIgnoreCase("AVAILABLE")) {
                    this.expiryHeap.enqueue(item);
                }
            }
            System.out.println("System Core: Tải dữ liệu tồn kho thành công.");
        } catch (Exception e) {
            System.out.println("System Core Warning: Lỗi tải file tồn kho: " + e.getMessage());
        }

        try {
            this.allOrdersList = orderFileHandler.read();
            for (Order o : this.allOrdersList) {
                this.orderLookupMap.put(o.getOrderId(), o);
                if (o.getStatus().equalsIgnoreCase("Waiting") || o.getStatus().equalsIgnoreCase("Pending")) {
                    this.waitingOrderFEFOQueue.enqueue(o);
                }
            }
            System.out.println("System Core: Tải dữ liệu đơn hàng thành công.");
        } catch (Exception e) {
            System.out.println("System Core Warning: Lỗi tải file đơn hàng: " + e.getMessage());
        }

        try {
            this.transactionHistory = tranFileHandler.read();
            System.out.println("System Core: Tải dữ liệu nhật ký giao dịch thành công.");
        } catch (Exception e) {
            System.out.println("System Core Warning: Lỗi tải file giao dịch: " + e.getMessage());
        }
    }

    public ProductController getProductController() {
        return productController;
    }

    public InventoryItemController getInventoryController() {
        return inventoryController;
    }

    public OrderController getOrderController() {
        return orderController;
    }

    public TransactionController getTransactionController() {
        return transactionController;
    }

    public boolean saveProducts() {
        System.out.println("Thao tác trên file Product.txt: ");
        return productStorage.askAndSave(productList);
    }

    public boolean saveInventory() {
        System.out.println("Thao tác trên file InventoryItem.txt: ");
        return inventoryStorage.askAndSave(inventoryList);
    }

    public boolean saveOrders() {
        System.out.println("Thao tác trên file Order.txt: ");
        return orderStorage.askAndSave(allOrdersList);
    }

    public boolean saveTransactions() {
        try {
            return tranFileHandler.write(transactionHistory);
        } catch (Exception e) {
            return false;
        }
    }
}
