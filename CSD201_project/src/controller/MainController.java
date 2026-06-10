package controller;

import model.Order;
import model.InventoryItem;
import fileio.IFileReadWrite;
import fileio.OrderReadWrite;
import java.util.HashMap;
import java.util.List;

public class MainController {

    private final OrderController orderController;
    private final IFileReadWrite<Order> orderFileHandler;

    public MainController() {
        // Giả lập cấu trúc kho vật lý trống từ thành viên khác
        HashMap<String, InventoryItem> globalInventoryMap = new HashMap<>();
        java.util.PriorityQueue<InventoryItem> globalExpiryHeap = new java.util.PriorityQueue<>();

        this.orderFileHandler = new OrderReadWrite();
        this.orderController = new OrderController(globalInventoryMap, globalExpiryHeap, orderFileHandler);

        // Chịu trách nhiệm Load file dữ liệu hệ thống lúc khởi động
        loadSystemData();
    }

    private void loadSystemData() {
        try {
            List<Order> loadedOrders = orderFileHandler.read();
            orderController.initializeData(loadedOrders);
            System.out.println("System Core: Order entries successfully initialized.");
        } catch (Exception e) {
            System.out.println("System Core Warning: Failed to boot file database. " + e.getMessage());
        }
    }

    public OrderController getOrderController() {
        return orderController;
    }
}
