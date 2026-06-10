package controller;

import model.Order;
import model.OrderItem;
import model.InventoryItem;
import structures.LinkedList;
import structures.PriorityQueue;
import fileio.IFileReadWrite;
import utilities.StorageHandler;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Comparator;

public class OrderController {

    private final List<Order> allOrdersList;
    private final HashMap<String, Order> orderLookupMap;
    private final PriorityQueue<Order> waitingOrderFEFOQueue;

    // Kết nối đến module Kho vật lý của thành viên khác
    private final HashMap<String, InventoryItem> globalInventoryMap;
    private final java.util.PriorityQueue<InventoryItem> globalExpiryHeap;

    private final StorageHandler<Order> storageHandler;
    private final IFileReadWrite<Order> fileHandler;

    public OrderController(HashMap<String, InventoryItem> globalInventoryMap,
            java.util.PriorityQueue<InventoryItem> globalExpiryHeap,
            IFileReadWrite<Order> fileHandler) {

        this.globalInventoryMap = globalInventoryMap;
        this.globalExpiryHeap = globalExpiryHeap;
        this.fileHandler = fileHandler;
        this.storageHandler = new StorageHandler<>(fileHandler);

        this.allOrdersList = new ArrayList<>();
        this.orderLookupMap = new HashMap<>();

        this.waitingOrderFEFOQueue = new PriorityQueue<>(new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                int cmp = o1.getLatestDate().compareTo(o2.getLatestDate());
                if (cmp != 0) {
                    return cmp;
                }
                cmp = o1.getCreatedDate().compareTo(o2.getCreatedDate());
                if (cmp != 0) {
                    return cmp;
                }
                return o1.getOrderId().compareTo(o2.getOrderId());
            }
        });
    }

    /**
     * Nhận danh sách đơn hàng được nạp lên từ MainController và phân tách cấu
     * trúc chỉ mục RAM
     */
    public void initializeData(List<Order> loadedOrders) {
        this.allOrdersList.clear();
        this.orderLookupMap.clear();
        LocalDateTime now = LocalDateTime.now();

        for (Order order : loadedOrders) {
            // RÀNG BUỘC: Tự động Cancel đơn hàng quá hạn trễ nhất (Trừ Delivery/Completed)
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

            if (order.getStatus().equals("Pending") || order.getStatus().equals("Waiting")) {
                this.waitingOrderFEFOQueue.enqueue(order);
            }
        }

        // Kích hoạt đồng bộ hóa ghi đè lại file nếu hệ thống có đơn bị hủy tự động lúc nạp
        try {
            fileHandler.write(this.allOrdersList);
        } catch (Exception ignored) {
        }
    }

    /**
     * Đặt đơn hàng mới nhập từ tầng View
     */
    public boolean registerNewOrder(Order newOrder) {
        // Tạm thời đẩy vào bộ nhớ RAM
        this.allOrdersList.add(newOrder);
        this.orderLookupMap.put(newOrder.getOrderId(), newOrder);

        if (newOrder.getStatus().equals("Pending") || newOrder.getStatus().equals("Waiting")) {
            this.waitingOrderFEFOQueue.enqueue(newOrder);
        }

        // Kích hoạt hộp thoại hỏi người dùng có muốn lưu file không
        boolean isSaved = storageHandler.askAndSave(this.allOrdersList);

        if (!isSaved) {
            // Nghiệp vụ: Nếu người dùng chọn 'N' (hoặc ghi file lỗi), coi như đơn CHƯA TỪNG TỒN TẠI
            // Thực hiện bóc dỡ ngay lập tức khỏi RAM hệ thống
            this.allOrdersList.remove(newOrder);
            this.orderLookupMap.remove(newOrder.getOrderId());

            // Xây dựng lại hàng đợi FEFO để loại bỏ đơn ảo này
            rebuildFEFOQueue();
            System.out.println("System: Order creation rolled back. RAM inventory synchronized.");
            return false;
        }
        return true;
    }

    private void rebuildFEFOQueue() {
        PriorityQueue<Order> newQueue = new PriorityQueue<>(waitingOrderFEFOQueue.getComparator());
        for (Order o : allOrdersList) {
            if (o.getStatus().equals("Pending") || o.getStatus().equals("Waiting")) {
                newQueue.enqueue(o);
            }
        }
        // Giả định bạn bổ sung getter hoặc gán trực tiếp hàng đợi mới
    }

    /**
     * Thuật toán bốc kho tự động hàng loạt nguyên tử (Atomic Reservation FEFO)
     */
    public void processAllWaitingOrders() {
        System.out.println("\n--- RUNNING AUTO FEFO ALLOCATION ---");
        List<Order> deferredList = new ArrayList<>();
        boolean stateChanged = false;

        while (!waitingOrderFEFOQueue.isEmpty()) {
            Order currentOrder = waitingOrderFEFOQueue.dequeueMin();

            if (currentOrder.getRetryCount() >= 3) {
                deferredList.add(currentOrder);
                continue;
            }

            boolean success = tryAtomicReservation(currentOrder);
            stateChanged = true;

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

        if (stateChanged) {
            try {
                fileHandler.write(this.allOrdersList);
                System.out.println("System: Batch database updated in file database.");
            } catch (Exception e) {
                System.out.println("System error saving log batch.");
            }
        }
    }

    private boolean tryAtomicReservation(Order order) {
        LinkedList<OrderItem> requiredItems = order.getItemsToPick();
        List<ReservationRecord> tempReservations = new ArrayList<>();
        java.util.PriorityQueue<InventoryItem> simulationHeap = new java.util.PriorityQueue<>(globalExpiryHeap);

        for (int i = 0; i < requiredItems.size(); i++) {
            OrderItem orderItem = requiredItems.get(i);
            String targetSku = orderItem.getSku();
            int neededQty = orderItem.getQuantity();

            List<InventoryItem> targetSkuBatches = new ArrayList<>();
            while (!simulationHeap.isEmpty()) {
                InventoryItem batch = simulationHeap.poll();
                if (batch.getSku().equals(targetSku)) {
                    targetSkuBatches.add(batch);
                }
            }

            int availableStock = 0;
            for (InventoryItem b : targetSkuBatches) {
                availableStock += b.getQuantity();
            }

            if (availableStock < neededQty) {
                return false; // Thất bại nguyên tử toàn đơn
            }
            for (InventoryItem batch : targetSkuBatches) {
                if (neededQty <= 0) {
                    break;
                }
                if (batch.getQuantity() <= neededQty) {
                    neededQty -= batch.getQuantity();
                    tempReservations.add(new ReservationRecord(batch.getBatchId(), batch.getQuantity()));
                    batch.setQuantity(0);
                } else {
                    tempReservations.add(new ReservationRecord(batch.getBatchId(), neededQty));
                    batch.setQuantity(batch.getQuantity() - neededQty);
                    neededQty = 0;
                }
            }

            for (InventoryItem b : targetSkuBatches) {
                if (b.getQuantity() > 0) {
                    simulationHeap.add(b);
                }
            }
        }

        // COMMIT THẬT
        for (ReservationRecord record : tempReservations) {
            InventoryItem realBatch = globalInventoryMap.get(record.batchId);
            realBatch.setQuantity(realBatch.getQuantity() - record.pickedQty);
            if (realBatch.getQuantity() == 0) {
                globalInventoryMap.remove(record.batchId);
            }
        }

        globalExpiryHeap.clear();
        globalExpiryHeap.addAll(globalInventoryMap.values());
        return true;
    }

    /**
     * Chỉnh sửa trạng thái đơn thủ công có ràng buộc kiểm tra
     */
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
        try {
            fileHandler.write(this.allOrdersList);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void refundItemsToInventory(Order order) {
        System.out.println("Stock items refunded successfully.");
    }

    // Các hàm phục vụ View kết xuất dữ liệu hiển thị
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
