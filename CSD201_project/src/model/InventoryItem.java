package model;

import java.time.LocalDate;

public class InventoryItem{

    private String slotId;
    private int quantity;
    private LocalDate receiveDate; 
    private LocalDate expiryDate;   

    public InventoryItem() {
    }

    public InventoryItem(String slotId, int quantity, LocalDate receiveDate, LocalDate expiryDate) {
        this.slotId = slotId;
        this.quantity = quantity;
        this.receiveDate = receiveDate;
        this.expiryDate = expiryDate;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDate getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(LocalDate receiveDate) {
        this.receiveDate = receiveDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    

    @Override
    public String toString() {
        return String.format("%s;%d;%s;%s",slotId, quantity, receiveDate, expiryDate);
    }
}
    