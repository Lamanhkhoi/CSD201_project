package controller;

import model.Order;
import model.OrderItem;
import structures.LinkedList;
import java.time.LocalDateTime;
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
        waitingOrderFEFOQueue.enqueue(newOrder);
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
}
