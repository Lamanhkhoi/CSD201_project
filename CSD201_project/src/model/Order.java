package model;

import java.time.LocalDateTime;
import structures.LinkedList;

public class Order {

    private String orderId;                 
    private String customerName;           
    private String phone;                  
    private String address;                 
    private LocalDateTime createdDate;      
    private LocalDateTime expectedDate;    
    private LocalDateTime latestDate;      
    private String status;                  // Trạng thái: Pending, Waiting, Ready, Delivery, Cancel, Completed
    private double totalAmount;             
    private LinkedList<OrderItem> itemsToPick; 
    private int retryCount;                 // Số lần bốc kho thử lại (Chặn vòng lặp vô hạn khi thiếu hàng kéo dài)

    public Order() {
        this.itemsToPick = new LinkedList<>();
        this.retryCount = 0;
    }

    public Order(String orderId, String customerName, String phone, String address,
            LocalDateTime createdDate, LocalDateTime expectedDate, LocalDateTime latestDate,
            String status, double totalAmount, LinkedList<OrderItem> itemsToPick) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.phone = phone;
        this.address = address;
        this.createdDate = createdDate;
        this.expectedDate = expectedDate;
        this.latestDate = latestDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.itemsToPick = itemsToPick;
        this.retryCount = 0;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(LocalDateTime expectedDate) {
        this.expectedDate = expectedDate;
    }

    public LocalDateTime getLatestDate() {
        return latestDate;
    }

    public void setLatestDate(LocalDateTime latestDate) {
        this.latestDate = latestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LinkedList<OrderItem> getItemsToPick() {
        return itemsToPick;
    }

    public void setItemsToPick(LinkedList<OrderItem> itemsToPick) {
        this.itemsToPick = itemsToPick;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    // Tăng số lần thử bốc kho lên 1 đơn vị khi xử lý thất bại dở dang.
    public void incrementRetryCount() {
        this.retryCount++;
    }
}
