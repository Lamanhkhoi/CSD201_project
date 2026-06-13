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
public class InventoryItem implements Comparable<InventoryItem>{

    private String batchId;
    private String sku;
    private int quantity;
    private LocalDate receiveDate; // Ngày nhập kho
    private LocalDate expiryDate;
    private String location;
    private String status;         // Trạng thái (AVAILABLE, DAMAGED,...)

    public InventoryItem() {
    }

    public InventoryItem(String batchId, String sku, int quantity, LocalDate receiveDate, LocalDate expiryDate, String location) {
        this.batchId = batchId;
        this.sku = sku;
        this.quantity = quantity;
        this.receiveDate = receiveDate;
        this.expiryDate = expiryDate;
        this.location = location;
        this.status = "AVAILABLE";
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int compareTo(InventoryItem other) {
        // Tiêu chí 1: Ngày hết hạn (Ngày nào nhỏ hơn/gần hơn thì đứng trước)
        int dateCompare = this.expiryDate.compareTo(other.expiryDate);

        if (dateCompare != 0) {
            return dateCompare;
        }

        // Tiêu chí 2: Nếu cùng ngày hết hạn, ưu tiên lô nhập kho trước (FIFO)
        return this.receiveDate.compareTo(other.receiveDate);
    }

    @Override
    public String toString() {
        return String.format("%s;%s;%d;%s;%s;%s;%s",
                batchId, sku, quantity, receiveDate, expiryDate, location, status);
    }
}
    