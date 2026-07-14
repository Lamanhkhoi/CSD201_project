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

public class OrderController {

    private final List<Order> allOrdersList;

    // Kết nối đến module Kho vật lý của thành viên khác
    private final HashMap<String, InventoryItem> globalInventoryMap;
    private final InventoryPriorityQueue globalExpiryHeap;

    public OrderController(List<Order> allOrdersList,
            HashMap<String, InventoryItem> globalInventoryMap,
            InventoryPriorityQueue globalExpiryHeap) {

        this.allOrdersList = allOrdersList;
        this.globalInventoryMap = globalInventoryMap;
        this.globalExpiryHeap = globalExpiryHeap;
    }

    public void initializeData(List<Order> loadedOrders) {
        this.allOrdersList.clear();
        LocalDateTime now = LocalDateTime.now();

        for (Order order : loadedOrders) {
            if (!order.getStatus().equals("Completed") && !order.getStatus().equals("Delivery")
                    && order.getLatestDate().isBefore(now)) {
                order.setStatus("Cancel");
                System.out.println("System: Auto-Cancelled overdue order [" + order.getOrderId() + "] during setup.");
            }

            this.allOrdersList.add(order);
        }
    }

    public void registerNewOrder(Order newOrder) {
        this.allOrdersList.add(newOrder);
        newOrder.setIsActive(true);
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

    private void refundItemsToInventory(Order order) {
        LinkedList<OrderItem> itemsToRefund = order.getItemsToPick();

        for (int i = 0; i < itemsToRefund.size(); i++) {
            OrderItem orderItem = itemsToRefund.get(i);
            String sku = orderItem.getSku();
            int refundQty = orderItem.getQuantity();
            boolean refunded = false;
            for (model.InventoryItem inventoryItem : globalInventoryMap.values()) {
                if (inventoryItem.getSku().trim().equalsIgnoreCase(sku.trim())) {
                    inventoryItem.setQuantity(inventoryItem.getQuantity() + refundQty);
                    refunded = true;
                    break;
                }
            }
            if (!refunded) {
                String refundBatchId = "REF-" + order.getOrderId() + "-" + sku;
                InventoryItem newRefundBatch = new InventoryItem(
                        refundBatchId,
                        sku,
                        refundQty,
                        java.time.LocalDate.now(),
                        java.time.LocalDate.now().plusMonths(6),
                        "KỆ-HOÀN"
                );
                globalInventoryMap.put(refundBatchId, newRefundBatch);
            }
        }
        globalExpiryHeap.clear();
        globalExpiryHeap.addAll(globalInventoryMap.values());

        System.out.println("System: Stock items for Order [" + order.getOrderId() + "] refunded successfully.");
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
