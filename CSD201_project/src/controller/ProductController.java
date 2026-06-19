package controller;

import model.Product;
import structures.SinglyLinkedList;

public class ProductController {

    private SinglyLinkedList<Product> productList;

    public ProductController(SinglyLinkedList<Product> productList) {
        this.productList = productList;
    }

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
}
