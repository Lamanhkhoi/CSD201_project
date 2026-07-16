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

    private String lotId;
    private int quantity;
    private LocalDate receiveDate; // Ngày nhập kho của riêng lô này
    private LocalDate expiryDate;       // Trạng thái (AVAILABLE, DAMAGED,...)

    public InventoryItem() {
    }

    public InventoryItem(String lotId, int quantity, LocalDate receiveDate, LocalDate expiryDate) {
        this.lotId = lotId;
        this.quantity = quantity;
        this.receiveDate = receiveDate;
        this.expiryDate = expiryDate;
    }

    public String getLotId() {
        return lotId;
    }

    public void setLotId(String lotId) {
        this.lotId = lotId;
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
        return String.format("%s;%d;%s;%s",lotId, quantity, receiveDate, expiryDate);
    }
}
    