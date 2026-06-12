package controller;

import model.Product;
import fileio.ProductReadWrite;
import structures.SinglyLinkedList;
import utilities.StorageHandler;

public class ProductController {
    private SinglyLinkedList<Product> productList;
    private final ProductReadWrite fileIO;
    // Khai báo StorageHandler nhận tham số tập hợp là SinglyLinkedList
    private final StorageHandler<Product, SinglyLinkedList<Product>> storageHandler; 

    public ProductController() {
        this.productList = new SinglyLinkedList<>();
        this.fileIO = new ProductReadWrite();
        this.storageHandler = new StorageHandler<>(fileIO); // Sạch lỗi dòng 18 hoàn toàn
        loadData();
    }

    // Tải dữ liệu trực tiếp từ file vật lý gán vào cấu trúc của nhóm
    private void loadData() {
        try {
            this.productList = fileIO.read();
        } catch (Exception e) {
            System.out.println("Lỗi khi tải dữ liệu sản phẩm: " + e.getMessage());
        }
    }

    // Cung cấp danh sách cho View hiển thị
    public SinglyLinkedList<Product> getProductList() {
        return this.productList;
    }

    // Tìm kiếm sản phẩm phục vụ kiểm tra trùng mã hoặc sửa/xóa
    public Product findProductBySku(String sku) {
        SinglyLinkedList.Node<Product> current = productList.getHead();
        while (current != null) {
            Product p = current.getElement();
            if (p.getSku().equalsIgnoreCase(sku)) {
                return p;
            }
            current = current.getNext();
        }
        return null;
    }

    // Nghiệp vụ thêm sản phẩm
    public boolean addProduct(Product newProduct) {
        if (findProductBySku(newProduct.getSku()) != null) {
            return false;
        }
        productList.addLast(newProduct);
        return true;
    }

    // Nghiệp vụ xóa sản phẩm
    public boolean deleteProduct(String sku) {
        SinglyLinkedList<Product> newList = new SinglyLinkedList<>();
        boolean found = false;
        
        SinglyLinkedList.Node<Product> current = productList.getHead();
        while (current != null) {
            Product p = current.getElement();
            if (p.getSku().equalsIgnoreCase(sku)) {
                found = true;
            } else {
                newList.addLast(p);
            }
            current = current.getNext();
        }

        if (found) {
            this.productList = newList;
            return true;
        }
        return false;
    }

    // Truyền trực tiếp productList vào hệ thống lưu trữ chung mà không cần convert nữa
    public void askToSaveData() {
        storageHandler.askAndSave(productList);
    }
}