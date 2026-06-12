package model;

public class Product {
    private String sku;
    private String name;
    private String category;
    private String supplier;

    // Constructor không tham số
    public Product() {
    }

    // Constructor đầy đủ tham số
    public Product(String sku, String name, String category, String supplier) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.supplier = supplier;
    }

    // Các phương thức Getter và Setter theo đặc tả
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

    @Override
    public String toString() {
        return String.format("%-15s | %-25s | %-15s | %-20s", sku, name, category, supplier);
    }
}