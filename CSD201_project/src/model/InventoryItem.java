/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDate;

/**
 *
 * @author LENOVO
 */
public class InventoryItem{

    private String slotId;
    private int quantity;
    private LocalDate receiveDate;  // Ngày nhập kho của riêng lô này
    private LocalDate expiryDate;   // Ngày hết hạn của lô hàng    

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
    