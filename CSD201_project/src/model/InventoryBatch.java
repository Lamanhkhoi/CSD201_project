package model;
import java.time.LocalDate;
import java.util.List;
import structures.SlotPriorityQueue;

public class InventoryBatch {
    private String batchId;
    private String sku;
    private SlotPriorityQueue slots;
    private int slotSequence;

    public InventoryBatch(String batchId, String sku) {
        this.batchId = batchId;
        this.sku = sku;
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

    public InventoryItem addLot(int quantity, LocalDate receiveDate, LocalDate expiryDate) {
        slotSequence++;
        String slotId = String.format("%s-SLOT%03d", batchId, slotSequence);
        InventoryItem newLot = new InventoryItem(slotId, quantity, receiveDate, expiryDate);
        slots.enqueue(newLot);
        return newLot;
    }

    public void addExistingLot(InventoryItem slot) {
        slots.enqueue(slot);
    }

    public InventoryItem peekEarliestLot() {
        return slots.peek();
    }

    public InventoryItem dequeueEarliestLot() {
        return slots.dequeueMin();
    }

    public boolean removeLotById(String lotId) {
        return slots.removeById(lotId);
    }

    public List<InventoryItem> getAllLots() {
        return slots.toList();
    }

    public int getTotalQuantity() {
        int total = 0;
        for (InventoryItem lot : slots.toList()) {
            total += lot.getQuantity();
        }
        return total;
    }

    public boolean isEmpty() {
        return slots.isEmpty();
    }

    public int lotCount() {
        return slots.size();
    }
}