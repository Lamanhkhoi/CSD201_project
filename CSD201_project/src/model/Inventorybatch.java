/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDate;
import java.util.List;
import structures.SlotPriorityQueue;

/**
 *
 * @author Admin
 */
public class InventoryBatch {
    private String batchId;
    private String sku;
    private boolean isActive;
    private SlotPriorityQueue slots;
    private int slotSequence;
    
    public InventoryBatch(String batchId, String sku) {
        this.batchId = batchId;
        this.sku = sku;
        this.isActive = true;
        this.slots = new SlotPriorityQueue();
        this.slotSequence = 0;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public SlotPriorityQueue getSlots() {
        return slots;
    }

    public void setSlots(SlotPriorityQueue slots) {
        this.slots = slots;
    }

    public int getSlotSequence() {
        return slotSequence;
    }

    public void setSlotSequence(int slotSequence) {
        this.slotSequence = slotSequence;
    }
    
    /*
    Thêm 1 lô hàng con mới vào batch. slotId được tự sinh tuần tự theo đúng batch (không dùng chung bộ đếm với batch khác).
    */
    public InventoryItem addLot(int quantity, LocalDate receiveDate, LocalDate expiryDate) {
        slotSequence++;
        String lotId = String.format("%s-LOT%02d", batchId, slotSequence);
        InventoryItem newLot = new InventoryItem(lotId, quantity, receiveDate, expiryDate);
        slots.enqueue(newLot);
        return newLot;
    }
    
    /*
    Dùng khi đọc lại dữ liệu từ file: thêm 1 lô đã có sẵn lotId (không sinh slotId mới), tránh trường hợp đọc file lên lại tạo slotId khác với lúc ghi.
     */
    public void addExistingLot(InventoryItem slot) {
        slots.enqueue(slot);
    }
 
    // Xem lô hàng con có hạn sử dụng gần nhất (ưu tiên xuất trước - FEFO)
    public InventoryItem peekEarliestLot() {
        return slots.peek();
    }
 
    // Lấy ra và xóa lô hàng con có hạn sử dụng gần nhất - dùng khi xuất trọn 1 lô
    public InventoryItem dequeueEarliestLot() {
        return slots.dequeueMin();
    }
 
    // Gỡ đúng 1 lô hàng con theo lotId (dùng khi lô về 0, hoặc bị xóa)
    public boolean removeLotById(String lotId) {
        return slots.removeById(lotId);
    }
 
    // Trả về toàn bộ lô hàng con trong batch, theo đúng thứ tự FEFO hiện có
    public List<InventoryItem> getAllLots() {
        return slots.toList();
    }
 
    // Tổng số lượng còn lại trong toàn bộ batch (cộng dồn tất cả lô con)
    public int getTotalQuantity() {
        int total = 0;
        for (InventoryItem lot : slots.toList()) {
            total += lot.getQuantity();
        }
        return total;
    }
 
    // Batch được xem là trống (có thể tái sử dụng cho SKU khác) khi không còn lô hàng con nào
    public boolean isEmpty() {
        return slots.isEmpty();
    }
 
    public int lotCount() {
        return slots.size();
    }
}
