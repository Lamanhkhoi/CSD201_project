package model;

public class Product {

    private String sku;
    private String name;
    private String category;
    private String supplier;
    private double price; // Biến mới thêm vào

    public Product() {
    }

    public Product(String sku, String name, String category, String supplier, double price) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.supplier = supplier;
        this.price = price;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        // Thêm cột giá tiền với format hiển thị dấu phẩy hàng nghìn
        return String.format("%-15s | %-25s | %-15s | %-20s | %,12.0f VNĐ", sku, name, category, supplier, price);
    }
}