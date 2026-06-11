/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDateTime;

/**
 *
 * @author LENOVO
 */
public class Transaction {
    private String transactionId;
    private String orderId;
    private String type;
    private String sku;
    private String batchId;
    private int quantity;
    private LocalDateTime timestamp;

    public Transaction() {
    }

    public Transaction(String transactionId, String orderId, String type, String sku, String batchId, int quantity, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.type = type;
        this.sku = sku;
        this.batchId = batchId;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

   

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    
    @Override
    public String toString() {
        return String.format("%s;%s;%s;%s;%s;%d;%s", transactionId, orderId, type, sku, batchId, quantity,timestamp);
    }
    
    
}
