package controller;

import model.Order;
import model.OrderItem;
import structures.LinkedList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import structures.OrderPriorityQueue;

public class OrderController {

    private final List<Order> allOrdersList;
    private final InventoryItemController inventoryController;
    private final OrderPriorityQueue waitingOrderFEFOQueue;

    public OrderController(List<Order> allOrdersList,
            InventoryItemController inventoryController,
            TransactionController transactionController) {
        this.allOrdersList = allOrdersList;
        this.inventoryController = inventoryController;
        this.waitingOrderFEFOQueue = new OrderPriorityQueue();
    }

    public boolean registerNewOrder(Order newOrder) {
        if (!tryAtomicReservation(newOrder)) {
            return false;
        }
        newOrder.setStatus("Ready");
        newOrder.setIsActive(true);
        allOrdersList.add(newOrder);
        rebuildWaitingQueue();
        return true;
    }

    // Lấy đủ hàng hoặc không lấy gì cả
    private boolean tryAtomicReservation(Order order) {
        LinkedList<OrderItem> requiredItems = order.getItemsToPick();

        // Check đủ hàng cho tất cả SKU trước
        for (int i = 0; i < requiredItems.size(); i++) {
            OrderItem orderItem = requiredItems.get(i);
            if (!inventoryController.hasEnoughStock(orderItem.getSku(), orderItem.getQuantity())) {
                System.out.println("-> [THẤT BẠI] Đơn " + order.getOrderId() + " không đủ hàng cho SKU [" + orderItem.getSku() + "]");
                return false;
            }
        }

        // Đủ hết -> trừ kho thật
        for (int i = 0; i < requiredItems.size(); i++) {
            OrderItem orderItem = requiredItems.get(i);
            inventoryController.deductStock(orderItem.getSku(), orderItem.getQuantity(), order.getOrderId());
        }

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
        
        existingOrder.setCustomerName(updatedOrder.getCustomerName());
        existingOrder.setPhone(updatedOrder.getPhone());
        existingOrder.setAddress(updatedOrder.getAddress());
        existingOrder.setCreatedDate(updatedOrder.getCreatedDate());
        existingOrder.setExpectedDate(updatedOrder.getExpectedDate());
        existingOrder.setLatestDate(updatedOrder.getLatestDate());
        rebuildWaitingQueue();
        return true;
    }

    public boolean deleteOrder(String orderId) {
        Order order = findOrderInList(orderId);
        if (order == null) {
            return false;
        }
        if (order.getStatus().equalsIgnoreCase("Completed")) {
            System.out.println("Lỗi: Đơn Completed không thể xóa.");
            return false;
        }
        order.setIsActive(false);
        rebuildWaitingQueue();
        return true;
    }

    public List<Order> getAllOrdersList() {
        List<Order> activeOrders = new ArrayList<>();
        for (Order order : allOrdersList) {
            if (order.isActive()) {
                activeOrders.add(order);
            }
        }
        activeOrders.sort(Comparator.comparing(Order::getCreatedDate));
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

    public String completeNextOrder() {
        if (waitingOrderFEFOQueue.isEmpty()) {
            return null;
        }
        Order next = waitingOrderFEFOQueue.dequeueMin();
        next.setStatus("Completed");
        return next.getOrderId();
    }

    private void rebuildWaitingQueue() {
        waitingOrderFEFOQueue.clear();
        for (Order o : allOrdersList) {
            if (o.isActive() && o.getStatus().equals("Ready")) {
                waitingOrderFEFOQueue.enqueue(o);
            }
        }
    }
}
