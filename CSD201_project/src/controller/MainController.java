package controller;

import model.*;
import fileio.*;
import structures.SinglyLinkedList;
import utilities.StorageHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainController {

    // 1. Vùng lưu trữ dữ liệu tập trung toàn hệ thống trên RAM
    private SinglyLinkedList<Product> productList;
    private HashMap<String, InventoryBatch> inventoryBatchMap;
    private HashMap<String, String> skuToBatchId;
    private List<InventoryBatch> loadedInventoryBatches;
    private List<Order> allOrdersList;
    private SinglyLinkedList<Transaction> transactionHistory;

    // 2. Các File IO Handlers độc lập
    private final ProductReadWrite productFileIO;
    private final InventoryItemReadWrite inventoryFileIO;
    private final OrderReadWrite orderFileHandler;
    private final TransactionReadWrite tranFileHandler;

    // 3. StorageHandlers
    private final StorageHandler<Product, SinglyLinkedList<Product>> productStorage;
    private final StorageHandler<InventoryBatch, List<InventoryBatch>> inventoryStorage;
    private final StorageHandler<Order, List<Order>> orderStorage;
    private final StorageHandler<Transaction, SinglyLinkedList<Transaction>> tranStorage;

    // 4. SubController
    private final ProductController productController;
    private final InventoryItemController inventoryController;
    private final OrderController orderController;
    private final TransactionController transactionController;

    public MainController() {
        this.productList = new SinglyLinkedList<>();
        this.inventoryBatchMap = new HashMap<>();
        this.skuToBatchId = new HashMap<>();
        this.allOrdersList = new ArrayList<>();
        this.transactionHistory = new SinglyLinkedList<>();

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
        this.inventoryController = new InventoryItemController(this.inventoryBatchMap, this.skuToBatchId, this.transactionController);
        this.inventoryController.initializeFromLoadedData(this.loadedInventoryBatches);
        this.orderController = new OrderController(this.allOrdersList, this.inventoryController, this.transactionController);
    }

    private void loadAllSystemData() {
        try {
            this.productList = productFileIO.read();
            System.out.println("System Core: Tải dữ liệu sản phẩm thành công.");
        } catch (Exception e) {
            System.out.println("System Core Warning: Lỗi tải file sản phẩm: " + e.getMessage());
        }

        try {
            this.loadedInventoryBatches = inventoryFileIO.read();

            System.out.println("System Core: Tải dữ liệu tồn kho thành công.");
        } catch (Exception e) {
            System.out.println("System Core Warning: Lỗi tải file tồn kho: " + e.getMessage());
        }

        try {
            this.allOrdersList = orderFileHandler.read();
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
        return inventoryStorage.askAndSave(this.inventoryController.getAllBatchesForSave());
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
