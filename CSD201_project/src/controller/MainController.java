//package controller;
//
//import model.Order;
//import model.InventoryItem;
//import fileio.IFileReadWrite;
//import fileio.OrderReadWrite;
//import fileio.TransactionReadWrite;
//import java.util.HashMap;
//import java.util.List;
//import model.Transaction;
//import structures.SinglyLinkedList;
//
//public class MainController {
//
//    private final OrderController orderController;
//    private final IFileReadWrite<Order, List<Order>> orderFileHandler;
//    private final TransactionController tranController;
//    private final IFileReadWrite<Transaction, SinglyLinkedList<Transaction>> tranFileHandler;
//    public MainController() {
//        // Giả lập cấu trúc kho vật lý trống từ thành viên khác
//        // 1. Khởi tạo cấu trúc kho vật lý giả lập từ thành viên khác
//        HashMap<String, InventoryItem> globalInventoryMap = new HashMap<>();
//        java.util.PriorityQueue<InventoryItem> globalExpiryHeap = new java.util.PriorityQueue<>();
//        
//        // 2. Khởi tạo phân hệ Quản lý file và Bộ điều khiển Nhật ký giao dịch (Transaction) trước
//        this.tranFileHandler = new TransactionReadWrite();
//        // SỬA TẠI ĐÂY: Chỉ truyền duy nhất tranFileHandler, bỏ biến transactionHistory thừa
//        this.tranController = new TransactionController();
//
//        // 3. Khởi tạo phân hệ Quản lý file và Bộ điều khiển Đơn hàng (Order)
//        this.orderFileHandler = new OrderReadWrite();
//        // SỬA TẠI ĐÂY: Tiêm (Inject) thêm 'tranController' vào trong OrderController 
//        // để khi bốc kho FEFO thành công, OrderController có thể tự gọi sang Transaction để ghi nhật ký.
//        this.orderController = new OrderController(globalInventoryMap, globalExpiryHeap, orderFileHandler, tranController);
//        
//        // 4. Chịu trách nhiệm Load file dữ liệu toàn bộ hệ thống lúc khởi động
//        loadSystemData();
//    }
//
//    private void loadSystemData() {
//        try {
//            List<Order> loadedOrders = orderFileHandler.read();
//            orderController.initializeData(loadedOrders);
//            System.out.println("System Core: Order entries successfully initialized.");
//        } catch (Exception e) {
//            System.out.println("System Core Warning: Failed to boot file database. " + e.getMessage());
//        }
//        // SỬA TẠI ĐÂY: Kích hoạt nạp dữ liệu Lịch sử giao dịch cũ từ file lên RAM
////        try {
////            tranController.loadInitialData(); 
////            System.out.println("System Core: Transaction logs successfully initialized.");
////        } catch (Exception e) {
////            System.out.println("System Core Warning: Failed to boot Transaction file database. " + e.getMessage());
////        }
//    }
//
//    public OrderController getOrderController() {
//        return orderController;
//    }
//    
//    public TransactionController getTranController() {
//        return tranController;
//    }
//    
//}
