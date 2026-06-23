package controller;

import model.Order;
import model.OrderItem;
import model.InventoryItem;
import structures.LinkedList;
import structures.PriorityQueue;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Comparator;
import java.util.Collections;
import model.Transaction;

public class OrderController {

    private final List<Order> allOrdersList;
    private final HashMap<String, Order> orderLookupMap;
    private final PriorityQueue<Order> waitingOrderFEFOQueue;

    // Kết nối đến module Kho vật lý của thành viên khác
    private final HashMap<String, InventoryItem> globalInventoryMap;
    private final PriorityQueue<InventoryItem> globalExpiryHeap;
    private final TransactionController tranController;

    public OrderController(List<Order> allOrdersList,
            HashMap<String, InventoryItem> globalInventoryMap,
            PriorityQueue<InventoryItem> globalExpiryHeap,
            TransactionController tranController) {

        this.allOrdersList = allOrdersList;
        this.globalInventoryMap = globalInventoryMap;
        this.globalExpiryHeap = globalExpiryHeap;
        this.tranController = tranController;

        this.orderLookupMap = new HashMap<>();
        this.waitingOrderFEFOQueue = new PriorityQueue<>(new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                return o1.getExpectedDate().compareTo(o2.getExpectedDate());
            }
        });

        rebuildFEFOQueue();
    }

    public void initializeData(List<Order> loadedOrders) {
        this.allOrdersList.clear();
        this.orderLookupMap.clear();
        LocalDateTime now = LocalDateTime.now();

        for (Order order : loadedOrders) {
            if (!order.getStatus().equals("Completed") && !order.getStatus().equals("Delivery")
                    && order.getLatestDate().isBefore(now)) {

                if (order.getStatus().equals("Ready")) {
                    refundItemsToInventory(order);
                }
                order.setStatus("Cancel");
                System.out.println("System: Auto-Cancelled overdue order [" + order.getOrderId() + "] during setup.");
            }

            this.allOrdersList.add(order);
            this.orderLookupMap.put(order.getOrderId(), order);
        }
        rebuildFEFOQueue();
    }

    public void registerNewOrder(Order newOrder) {
        this.allOrdersList.add(newOrder);
        this.orderLookupMap.put(newOrder.getOrderId(), newOrder);
        newOrder.setIsActive(true);

        if (newOrder.getStatus().equals("Pending") || newOrder.getStatus().equals("Waiting")) {
            this.waitingOrderFEFOQueue.enqueue(newOrder);
        }
    }

    private void rebuildFEFOQueue() {
        while (!waitingOrderFEFOQueue.isEmpty()) {
            waitingOrderFEFOQueue.dequeueMin();
        }
        for (Order o : allOrdersList) {
            if (o.isActive() && (o.getStatus().equals("Pending") || o.getStatus().equals("Waiting"))) {
                waitingOrderFEFOQueue.enqueue(o);
            }
        }
    }

    public void processAllWaitingOrders() {
        System.out.println("\n--- RUNNING AUTO FEFO ALLOCATION ---");
        List<Order> deferredList = new ArrayList<>();

        while (!waitingOrderFEFOQueue.isEmpty()) {
            Order currentOrder = waitingOrderFEFOQueue.dequeueMin();

            if (currentOrder.getRetryCount() >= 3) {
                deferredList.add(currentOrder);
                continue;
            }

            boolean success = tryAtomicReservation(currentOrder);

            if (success) {
                currentOrder.setStatus("Ready");
                currentOrder.setRetryCount(0);
                System.out.println("-> Order [" + currentOrder.getOrderId() + "] successfully allocated to READY.");
            } else {
                currentOrder.incrementRetryCount();
                currentOrder.setStatus("Waiting");
                deferredList.add(currentOrder);
                System.out.println("-> Order [" + currentOrder.getOrderId() + "] failed to allocate. Status: WAITING.");
            }
        }

        for (Order o : deferredList) {
            waitingOrderFEFOQueue.enqueue(o);
        }
    }

    private boolean tryAtomicReservation(Order order) {
        System.out.println("[DEBUG] --- KIỂM TRA ĐỒNG BỘ KHO KHI XUẤT ĐƠN " + order.getOrderId() + " ---");
        System.out.println("-> Số lượng lô hàng hiện có trên RAM: " + globalInventoryMap.size());
        for (InventoryItem item : globalInventoryMap.values()) {
            System.out.println("   + Lô: " + item.getBatchId() + " | SKU: " + item.getSku() + " | Số lượng: " + item.getQuantity());
        }

        LinkedList<OrderItem> requiredItems = order.getItemsToPick();
        List<ReservationRecord> tempReservations = new ArrayList<>();

        HashMap<String, Integer> simulationQtyMap = new HashMap<>();
        for (InventoryItem item : globalInventoryMap.values()) {
            simulationQtyMap.put(item.getBatchId(), item.getQuantity());
        }

        for (int i = 0; i < requiredItems.size(); i++) {
            OrderItem orderItem = requiredItems.get(i);
            String targetSku = orderItem.getSku();
            int neededQty = orderItem.getQuantity();

            List<InventoryItem> targetSkuBatches = new ArrayList<>();
            for (InventoryItem item : globalInventoryMap.values()) {
                if (item.getSku().trim().equalsIgnoreCase(targetSku.trim()) && simulationQtyMap.get(item.getBatchId()) > 0) {
                    targetSkuBatches.add(item);
                }
            }

            Collections.sort(targetSkuBatches, new Comparator<InventoryItem>() {
                @Override
                public int compare(InventoryItem o1, InventoryItem o2) {
                    return o1.compareTo(o2);
                }
            });

            int availableStock = 0;
            for (InventoryItem b : targetSkuBatches) {
                availableStock += simulationQtyMap.get(b.getBatchId());
            }

            if (availableStock < neededQty) {
                System.out.println("-> [THẤT BẠI FEFO] Đơn hàng " + order.getOrderId() + " không thể xuất kho!");
                System.out.println("   Mã hàng bị thiếu: [" + targetSku + "]");
                System.out.println("   Số lượng đơn cần: " + neededQty + " | Tổng kho hiện có: " + availableStock);
                return false;
            }

            for (InventoryItem batch : targetSkuBatches) {
                if (neededQty <= 0) {
                    break;
                }

                int currentSimQty = simulationQtyMap.get(batch.getBatchId());
                if (currentSimQty <= neededQty) {
                    neededQty -= currentSimQty;
                    tempReservations.add(new ReservationRecord(batch.getBatchId(), currentSimQty));
                    simulationQtyMap.put(batch.getBatchId(), 0);
                } else {
                    simulationQtyMap.put(batch.getBatchId(), currentSimQty - neededQty);
                    tempReservations.add(new ReservationRecord(batch.getBatchId(), neededQty));
                    neededQty = 0;
                }
            }
        }

        for (ReservationRecord record : tempReservations) {
            InventoryItem realBatch = globalInventoryMap.get(record.batchId);
            realBatch.setQuantity(realBatch.getQuantity() - record.pickedQty);
            String txId = "TX-" + System.currentTimeMillis() + "-" + record.batchId;
            Transaction newTx = new Transaction(
                    txId,
                    order.getOrderId(),
                    "EXPORT",
                    realBatch.getSku(),
                    record.batchId,
                    record.pickedQty,
                    LocalDateTime.now()
            );

            this.tranController.addTransaction(newTx);
            if (realBatch.getQuantity() == 0) {
                globalInventoryMap.remove(record.batchId);
            }
        }

        globalExpiryHeap.clear();
        globalExpiryHeap.addAll(globalInventoryMap.values());
        return true;
    }

    public boolean updateOrderStatusManual(String orderId, String newStatus) {
        Order order = orderLookupMap.get(orderId);
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

        if (newStatus.equalsIgnoreCase("Cancel")
                && (order.getStatus().equals("Ready") || order.getStatus().equals("Delivery"))) {
            refundItemsToInventory(order);
        }

        order.setStatus(newStatus);
        rebuildFEFOQueue();
        return true;
    }

    public boolean updateOrder(Order updatedOrder) {
        if (updatedOrder == null) {
            return false;
        }

        Order existingOrder = orderLookupMap.get(updatedOrder.getOrderId());

        if (existingOrder == null || !existingOrder.isActive()) {
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
        rebuildFEFOQueue();

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

        Order order = orderLookupMap.get(orderId);

        if (order == null) {
            return false;
        }

        if (!order.isActive()) {
            return false;
        }

        if (order.getStatus().equalsIgnoreCase("Completed")) {
            System.out.println("Error: Completed order cannot be deleted.");
            return false;
        }
        if (order.getStatus().equalsIgnoreCase("Ready")
                || order.getStatus().equalsIgnoreCase("Delivery")) {

            refundItemsToInventory(order);
        }

        order.setIsActive(false);

        rebuildFEFOQueue();

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
        Order order = orderLookupMap.get(id);
        if (order == null || !order.isActive()) {
            return null;
        }
        return order;
    }
}

class ReservationRecord {

    String batchId;
    int pickedQty;

    ReservationRecord(String batchId, int pickedQty) {
        this.batchId = batchId;
        this.pickedQty = pickedQty;
    }
}
