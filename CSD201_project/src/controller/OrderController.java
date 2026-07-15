package controller;

import model.Order;
import model.OrderItem;
import model.InventoryItem;
import structures.LinkedList;
import structures.InventoryPriorityQueue;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import model.Transaction;
import structures.OrderPriorityQueue;

public class OrderController {

    private final List<Order> allOrdersList;
    private final HashMap<String, InventoryItem> globalInventoryMap;
    private final InventoryPriorityQueue globalExpiryHeap;
    private final OrderPriorityQueue waitingOrderFEFOQueue;
    private final TransactionController tranController;

    public OrderController(List<Order> allOrdersList,
            HashMap<String, InventoryItem> globalInventoryMap,
            InventoryPriorityQueue globalExpiryHeap,
            TransactionController transactionController) {
        this.allOrdersList = allOrdersList;
        this.globalInventoryMap = globalInventoryMap;
        this.globalExpiryHeap = globalExpiryHeap;
        this.waitingOrderFEFOQueue = new OrderPriorityQueue();
        this.tranController = transactionController;
    }

    public void initializeData(List<Order> loadedOrders) {
        allOrdersList.clear();
        LocalDateTime now = LocalDateTime.now();
        for (Order order : loadedOrders) {
            if (!order.getStatus().equals("Completed") && !order.getStatus().equals("Delivery") && order.getLatestDate().isBefore(now)) {
                order.setStatus("Cancel");
                System.out.println("System: Auto-Cancelled overdue order [" + order.getOrderId() + "] during setup.");
            }
            allOrdersList.add(order);
        }
    }

    public boolean registerNewOrder(Order newOrder) {
        if (!tryAtomicReservation(newOrder)) {
            return false;
        }
        newOrder.setStatus("Ready");
        newOrder.setIsActive(true);
        allOrdersList.add(newOrder);
        waitingOrderFEFOQueue.enqueue(newOrder);
        return true;
    }

    private boolean tryAtomicReservation(Order order) {
        LinkedList<OrderItem> requiredItems = order.getItemsToPick();

        for (int i = 0; i < requiredItems.size(); i++) {
            OrderItem orderItem = requiredItems.get(i);
            InventoryItem batch = findBatchBySku(orderItem.getSku());
            if (batch == null || batch.getQuantity() < orderItem.getQuantity()) {
                System.out.println("-> [THẤT BẠI] Đơn " + order.getOrderId() + " không đủ hàng cho SKU [" + orderItem.getSku() + "]");
                return false;
            }
        }

        for (int i = 0; i < requiredItems.size(); i++) {
            OrderItem orderItem = requiredItems.get(i);
            InventoryItem batch = findBatchBySku(orderItem.getSku());
            batch.setQuantity(batch.getQuantity() - orderItem.getQuantity());

            String txId = "TX-" + batch.getBatchId();
            Transaction newTx = new Transaction(txId, order.getOrderId(), "EXPORT",
                    batch.getSku(), batch.getBatchId(), orderItem.getQuantity(), LocalDateTime.now());
            tranController.addTransaction(newTx);

            if (batch.getQuantity() == 0) {
                globalInventoryMap.remove(batch.getBatchId());
            }
        }

        globalExpiryHeap.clear();
        globalExpiryHeap.addAll(globalInventoryMap.values());
        return true;
    }

    private InventoryItem findBatchBySku(String sku) {
        for (InventoryItem item : globalInventoryMap.values()) {
            if (item.getSku().trim().equalsIgnoreCase(sku.trim())) {
                return item;
            }
        }
        return null;
    }

    public boolean updateOrderStatusManual(String orderId, String newStatus) {
        Order order = findOrderInList(orderId);
        if (order == null) {
            return false;
        }

        if (order.getLatestDate().isBefore(LocalDateTime.now())) {
            System.out.println("Error: Đơn hàng đã quá hạn trễ nhất. Khóa trạng thái!");
            return false;
        }
        if (order.getStatus().equals("Completed")) {
            System.out.println("Error: Đơn hàng đã ở trạng thái Completed. Không thể chỉnh sửa!");
            return false;
        }

        order.setStatus(newStatus);
        return true;
    }

    public boolean updateOrder(Order updatedOrder) {
        if (updatedOrder == null) {
            return false;
        }

        Order existingOrder = findOrderInList(updatedOrder.getOrderId());

        if (existingOrder == null) {
            return false;
        }

        if (existingOrder.getStatus().equalsIgnoreCase("Completed")) {
            System.out.println("Error: Completed order cannot be updated.");
            return false;
        }

        if (existingOrder.getLatestDate().isBefore(LocalDateTime.now())) {
            System.out.println("Error: Order has expired.");
            return false;
        }
        existingOrder.setCustomerName(updatedOrder.getCustomerName());
        existingOrder.setPhone(updatedOrder.getPhone());
        existingOrder.setAddress(updatedOrder.getAddress());
        existingOrder.setCreatedDate(updatedOrder.getCreatedDate());
        existingOrder.setExpectedDate(updatedOrder.getExpectedDate());
        existingOrder.setLatestDate(updatedOrder.getLatestDate());

        return true;
    }

    public boolean deleteOrder(String orderId) {
        Order order = findOrderInList(orderId);
        if (order == null) {
            return false;
        }
        if (order.getStatus().equalsIgnoreCase("Completed")) {
            System.out.println("Error: Completed order cannot be deleted.");
            return false;
        }
        order.setIsActive(false);
        return true;
    }

    public List<Order> getAllOrdersList() {
        List<Order> activeOrders = new ArrayList<>();
        for (Order order : allOrdersList) {
            if (order.isActive()) {
                activeOrders.add(order);
            }
        }
        return activeOrders;
    }

    public Order getOrderById(String id) {
        return findOrderInList(id);
    }

    private Order findOrderInList(String orderId) {
        for (Order o : allOrdersList) {
            if (o.getOrderId().equalsIgnoreCase(orderId) && o.isActive()) {
                return o;
            }
        }
        return null;
    }
}
