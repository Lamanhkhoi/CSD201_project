/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDateTime;
import structures.List;

/**
 *
 * @author LENOVO
 */
public class Order {
    private String orderId;
    private String customerName;
    private LocalDateTime orderDate;
    private String address;
    private String status;
    private List<InventoryItem> itemsToPick;

    public Order() {
    }

    public Order(String orderId, String customerName, LocalDateTime orderDate, String address, String status, List<InventoryItem> itemsToPick) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.orderDate = orderDate;
        this.address = address;
        this.status = status;
        this.itemsToPick = itemsToPick;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<InventoryItem> getItemsToPick() {
        return itemsToPick;
    }

    public void setItemsToPick(List<InventoryItem> itemsToPick) {
        this.itemsToPick = itemsToPick;
    }
    
    
}
