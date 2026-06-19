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

        if (newOrder.getStatus().equals("Pending") || newOrder.getStatus().equals("Waiting")) {
            this.waitingOrderFEFOQueue.enqueue(newOrder);
        }
    }

    private void rebuildFEFOQueue() {
        // Clear hàng đợi cũ để tránh trùng lặp dữ liệu rác
        while (!waitingOrderFEFOQueue.isEmpty()) {
            waitingOrderFEFOQueue.dequeueMin();
        }
        for (Order o : allOrdersList) {
            if (o.getStatus().equals("Pending") || o.getStatus().equals("Waiting")) {
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

    /**
     * ĐÃ SỬA: Thuật toán bốc kho tự động nguyên tử chuẩn hóa giải thuật mô
     * phỏng
     */
    private boolean tryAtomicReservation(Order order) {
        // --- ĐOẠN CODE IN KIỂM TRA (XÓA SAU KHI ĐÃ CHẠY ĐƯỢC) ---
        System.out.println("[DEBUG] --- KIỂM TRA ĐỒNG BỘ KHO KHI XUẤT ĐƠN " + order.getOrderId() + " ---");
        System.out.println("-> Số lượng lô hàng hiện có trên RAM: " + globalInventoryMap.size());
        for (InventoryItem item : globalInventoryMap.values()) {
            System.out.println("   + Lô: " + item.getBatchId() + " | SKU: " + item.getSku() + " | Số lượng: " + item.getQuantity());
        }
        // -------------------------------------------------------
        LinkedList<OrderItem> requiredItems = order.getItemsToPick();
        List<ReservationRecord> tempReservations = new ArrayList<>();

        // Tạo một bản đồ ảo (Simulation Map) sao chép số lượng hiện tại để trừ nháp trên RAM
        HashMap<String, Integer> simulationQtyMap = new HashMap<>();
        for (InventoryItem item : globalInventoryMap.values()) {
            simulationQtyMap.put(item.getBatchId(), item.getQuantity());
        }

        // BƯỚC 1: CHẠY MÔ PHỎNG KIỂM TRA TOÀN BỘ ĐƠN HÀNG
        for (int i = 0; i < requiredItems.size(); i++) {
            OrderItem orderItem = requiredItems.get(i);
            String targetSku = orderItem.getSku();
            int neededQty = orderItem.getQuantity();

            // Lọc ra các lô hàng thực tế thuộc SKU này
            List<InventoryItem> targetSkuBatches = new ArrayList<>();
            for (InventoryItem item : globalInventoryMap.values()) {
                if (item.getSku().trim().equalsIgnoreCase(targetSku.trim()) && simulationQtyMap.get(item.getBatchId()) > 0) {
                    targetSkuBatches.add(item);
                }
            }

            // Sắp xếp các lô hàng của SKU này theo chiến lược FEFO (Cận hạn dùng lên trước)
            Collections.sort(targetSkuBatches, new Comparator<InventoryItem>() {
                @Override
                public int compare(InventoryItem o1, InventoryItem o2) {
                    return o1.compareTo(o2); // Tận dụng hàm compareTo theo hạn sử dụng có sẵn của bạn
                }
            });

            // Tính tổng số lượng hàng khả dụng trong kho ảo mô phỏng
            int availableStock = 0;
            for (InventoryItem b : targetSkuBatches) {
                availableStock += simulationQtyMap.get(b.getBatchId());
            }

            // RÀNG BUỘC NGUYÊN TỬ: Nếu kho ảo không đủ đáp ứng, lập tức hủy bỏ toàn bộ đơn hàng
            if (availableStock < neededQty) {
                System.out.println("-> [THẤT BẠI FEFO] Đơn hàng " + order.getOrderId() + " không thể xuất kho!");
                System.out.println("   Mã hàng bị thiếu: [" + targetSku + "]");
                System.out.println("   Số lượng đơn cần: " + neededQty + " | Tổng kho hiện có: " + availableStock);
                return false;
            }

            // Tiến hành trừ nháp trên kho ảo mô phỏng
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

        // BƯỚC 2: COMMIT THẬT VÀO HỆ THỐNG & GHI LOG TRANSACTION
        for (ReservationRecord record : tempReservations) {
            InventoryItem realBatch = globalInventoryMap.get(record.batchId);
            realBatch.setQuantity(realBatch.getQuantity() - record.pickedQty);

            // TẠO MÃ GIAO DỊCH DUY NHẤT
            String txId = "TX-" + System.currentTimeMillis() + "-" + record.batchId;

            // ĐÃ SỬA: Ép đúng thứ tự Constructor của bạn: (id, orderId, type, sku, batchId, quantity, date)
            Transaction newTx = new Transaction(
                    txId,
                    order.getOrderId(),
                    "EXPORT",
                    realBatch.getSku(),
                    record.batchId,
                    record.pickedQty,
                    LocalDateTime.now()
            );

            // Đẩy vào danh sách liên kết đơn trên RAM của bạn
            this.tranController.addTransaction(newTx);

            // Nếu lô hàng đã bị bốc hết sạch số lượng, xóa bỏ khỏi bản đồ kho vật lý
            if (realBatch.getQuantity() == 0) {
                globalInventoryMap.remove(record.batchId);
            }
        }

        // ĐỒNG BỘ HÓA LẠI HEAP KHO CHÍNH XÁC SAU KHI CÓ BIẾN ĐỘNG
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

    private void refundItemsToInventory(Order order) {
        // 1. Lấy danh sách các mặt hàng cần hoàn trả từ đơn hàng
        LinkedList<OrderItem> itemsToRefund = order.getItemsToPick();

        // 2. Duyệt qua từng mặt hàng trong đơn
        for (int i = 0; i < itemsToRefund.size(); i++) {
            OrderItem orderItem = itemsToRefund.get(i);
            String sku = orderItem.getSku();
            int refundQty = orderItem.getQuantity();

            // 3. Tìm lô hàng hiện có trong kho (globalInventoryMap) có cùng mã SKU để cộng trả lại
            boolean refunded = false;
            for (model.InventoryItem inventoryItem : globalInventoryMap.values()) {
                if (inventoryItem.getSku().trim().equalsIgnoreCase(sku.trim())) {
                    // Cộng trả lại số lượng vào lô hàng này trên RAM
                    inventoryItem.setQuantity(inventoryItem.getQuantity() + refundQty);
                    refunded = true;
                    break; // Tìm thấy lô hàng cùng SKU đầu tiên là cộng trả vào ngay
                }
            }

            // 4. Trường hợp kho đã sạch bóng SKU này (không tìm thấy lô nào cũ)
            if (!refunded) {
                // Tạo một lô hàng hoàn trả mới và đẩy vào Map kho vật lý
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

        // 5. Đồng bộ hóa lại Heap cận hạn (globalExpiryHeap) của thành viên khác sau khi kho có biến động
        globalExpiryHeap.clear();
        globalExpiryHeap.addAll(globalInventoryMap.values());

        System.out.println("System: Stock items for Order [" + order.getOrderId() + "] refunded successfully.");
    }

    public List<Order> getAllOrdersList() {
        return allOrdersList;
    }

    public Order getOrderById(String id) {
        return orderLookupMap.get(id);
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
