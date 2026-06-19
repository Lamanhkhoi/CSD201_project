package viewer;

import controller.MainController;
import controller.ProductController;
import model.Product;
import structures.SinglyLinkedList;
import utilities.Inputter;

public class ProductView {

    private final MainController mainController;
    private final ProductController productController;

    public ProductView(MainController mainController, ProductController productController) {
    this.mainController = mainController;
    this.productController = productController;
}

    public void displaySubMenu() {
        Object[] options = {
            "Thêm sản phẩm mới (Add Product)",
            "Xóa sản phẩm khỏi hệ thống (Delete Product)",
            "Hiển thị danh sách sản phẩm (Display Products)",
            "Quay lại Menu chính (Back to Main Menu)"
        };

        while (true) {
            System.out.println("\n===== PHÂN HỆ QUẢN LÝ SẢN PHẨM =====");
            int choice = Menu.getChoice(options);

            switch (choice) {
                case 1:
                    uiAddProduct();
                    break;
                case 2:
                    uiDeleteProduct();
                    break;
                case 3:
                    uiDisplayProducts();
                    break;
                case 4:
                    System.out.println("Đang quay lại Menu chính...");
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    private void uiAddProduct() {
        System.out.println("\n--- THÊM SẢN PHẨM MỚI ---");
        String sku = Inputter.inputStr("Nhập mã SKU: ");
        String name = Inputter.inputStr("Nhập tên sản phẩm: ");
        String category = Inputter.inputStr("Nhập danh mục: ");
        String supplier = Inputter.inputStr("Nhập nhà cung cấp: ");
        double price = Inputter.inputDouble("Nhập đơn giá: ");

        Product newProduct = new Product(sku, name, category, supplier, price);
        boolean success = productController.addProduct(newProduct);

        if (success) {
            System.out.println("Thành công: Đã thêm sản phẩm lên bộ nhớ RAM tạm thời.");
            // Triển khai PHƯƠNG ÁN B: View tự gọi MainController kích hoạt hỏi lưu xuống file vật lý
            mainController.saveProducts();
        } else {
            System.out.println("Thất bại: Mã SKU đã tồn tại trên hệ thống!");
        }
    }

    private void uiDeleteProduct() {
        System.out.println("\n--- XÓA SẢN PHẨM ---");
        String sku = Inputter.inputStr("Nhập mã SKU cần xóa: ");
        boolean success = productController.deleteProduct(sku);

        if (success) {
            System.out.println("Thành công: Đã xóa sản phẩm khỏi RAM tạm thời.");
            // Hỏi lưu file vật lý theo phương án B
            mainController.saveProducts();
        } else {
            System.out.println("Thất bại: Không tìm thấy sản phẩm cần xóa.");
        }
    }

    private void uiDisplayProducts() {
        System.out.println("\n------------------------------------------------------------------------------------------------------");
        System.out.println("                                DANH SÁCH SẢN PHẨM TRÊN HỆ THỐNG                                      ");
        System.out.println("------------------------------------------------------------------------------------------------------");

        SinglyLinkedList<Product> list = productController.getProductList();
        SinglyLinkedList.Node<Product> current = list.getHead();

        if (current == null) {
            System.out.println("  Danh sách hiện đang trống.");
            return;
        }

        System.out.printf("%-15s | %-25s | %-15s | %-20s | %-15s\n", "SKU", "Tên Sản Phẩm", "Danh Mục", "Nhà Cung Cấp", "Đơn Giá");
        System.out.println("------------------------------------------------------------------------------------------------------");

        while (current != null) {
            System.out.println(current.getElement());
            current = current.getNext();
        }
    }
}