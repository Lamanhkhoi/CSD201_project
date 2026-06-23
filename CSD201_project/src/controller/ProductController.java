package controller;

import model.Product;
import fileio.ProductReadWrite;
import structures.SinglyLinkedList;
import utilities.StorageHandler;

public class ProductController {
    private SinglyLinkedList<Product> productList;
    private final ProductReadWrite fileIO;
    private final StorageHandler<Product, SinglyLinkedList<Product>> storageHandler; 

    // SỬA: Nhận lại danh sách từ bên ngoài (MainController) truyền vào để đồng bộ bộ nhớ RAM
    public ProductController(SinglyLinkedList<Product> productList) {
        this.productList = productList;
        this.fileIO = new ProductReadWrite();
        this.storageHandler = new StorageHandler<>(fileIO);
    }

    public SinglyLinkedList<Product> getProductList() {
        return this.productList;
    }

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

    public boolean addProduct(Product newProduct) {
        if (findProductBySku(newProduct.getSku()) != null) {
            return false;
        }
        productList.addLast(newProduct);
        return true;
    }

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

    // Giữ lại phương thức này phòng trường hợp bạn muốn dùng lưu độc lập sau này
    public void askToSaveData() {
        storageHandler.askAndSave(productList);
    }
}