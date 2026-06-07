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
public class InventoryItem {
    private String batchId;
    private String sku;
    private int quantity;
    private LocalDate expiryDate;
    private String location;

    public InventoryItem() {
    }

    public InventoryItem(String batchId, String sku, int quantity, LocalDate expiryDate, String location) {
        this.batchId = batchId;
        this.sku = sku;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.location = location;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    
    
    public Boolean isExpired() {
        return false;
    }
    
}
