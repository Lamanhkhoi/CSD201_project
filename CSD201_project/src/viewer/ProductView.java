package viewer;

import controller.ProductController;
import model.Product;
import structures.SinglyLinkedList;
import utilities.Inputter;

public class ProductView {
    private final ProductController productController;

    public ProductView(ProductController productController) {
        this.productController = productController;
    }

    public void displaySubMenu() {
        Object[] options = {
            "Thêm sản phẩm mới (Add Product)",
            "Cập nhật thông tin sản phẩm (Update Product)",
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
                    uiUpdateProduct();
                    break;
                case 3:
                    uiDeleteProduct();
                    break;
                case 4:
                    uiDisplayProducts();
                    break;
                case 5:
                    System.out.println("Đang quay lại Menu chính...");
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    // 1. NHIỆM VỤ: THÊM SẢN PHẨM MỚI (CÓ VALIDATE FORMAT)
    private void uiAddProduct() {
        System.out.println("\n--- TIẾN HÀNH THÊM SẢN PHẨM MỚI ---");
        String sku;
        while (true) {
            sku = Inputter.inputStr("Nhập mã SKU sản phẩm: ").trim();
            
            // Kiểm tra rỗng
            if (sku.isEmpty()) {
                System.out.println("Thông báo: Mã SKU không được để trống.");
                continue;
            }
            
            // Kiểm tra định dạng Pxxx bằng mẫu chuẩn của nhóm
            if (!sku.matches(utilities.Pattern.PRODUCT_SKU_PATTERN)) {
                System.out.println("Thông báo: Mã SKU sai định dạng! Định dạng chuẩn phải là Pxxx (với xxx là 3 chữ số. Ví dụ mẫu: P001, P123, P999).");
                continue;
            }
            
            // Kiểm tra trùng mã
            if (productController.findProductBySku(sku) != null) {
                System.out.println("Thông báo: SKU này đã tồn tại trên hệ thống! Vui lòng nhập mã khác.");
                continue;
            }
            break;
        }

        String name = Inputter.inputStr("Nhập tên sản phẩm: ");
        String category = Inputter.inputStr("Nhập danh mục/phân loại: ");
        String supplier = Inputter.inputStr("Nhập nhà cung cấp: ");

        Product newProduct = new Product(sku, name, category, supplier);
        if (productController.addProduct(newProduct)) {
            System.out.println("Thông báo: Thêm sản phẩm mới vào danh sách tạm thời thành công!");
            productController.askToSaveData();
        }
    }

    // 2. NHIỆM VỤ: CẬP NHẬT THÔNG TIN (CÓ VALIDATE FORMAT KHI TRA CỨU)
    private void uiUpdateProduct() {
        System.out.println("\n--- CẬP NHẬT THÔNG TIN SẢN PHẨM ---");
        String sku;
        
        // Tiến hành gác cổng format ngay từ bước gõ mã tìm kiếm
        while (true) {
            sku = Inputter.inputStr("Nhập mã SKU sản phẩm cần sửa: ").trim();
            
            if (sku.isEmpty()) {
                System.out.println("Thông báo: Mã SKU không được để trống.");
                continue;
            }
            
            if (!sku.matches(utilities.Pattern.PRODUCT_SKU_PATTERN)) {
                System.out.println("Thông báo: Mã SKU sai định dạng! Định dạng chuẩn phải là Pxxx (với xxx là 3 chữ số. Ví dụ mẫu: P001, P123, P999).");
                continue;
            }
            break;
        }

        Product p = productController.findProductBySku(sku);
        if (p == null) {
            System.out.println("Thông báo: Không tìm thấy sản phẩm có SKU: " + sku);
            return;
        }

        System.out.println("Dữ liệu hiện tại: " + p);
        System.out.println("(Để trống và nhấn Enter nếu bạn muốn giữ lại thông tin cũ)");

        String newName = Inputter.inputStr("Nhập tên mới: ").trim();
        if (!newName.isEmpty()) p.setName(newName);

        String newCategory = Inputter.inputStr("Nhập danh mục mới: ").trim();
        if (!newCategory.isEmpty()) p.setCategory(newCategory);

        String newSupplier = Inputter.inputStr("Nhập nhà cung cấp mới: ").trim();
        if (!newSupplier.isEmpty()) p.setSupplier(newSupplier);

        System.out.println("Thông báo: Cập nhật thông tin hoàn tất!");
        productController.askToSaveData();
    }

    private void uiDeleteProduct() {
        System.out.println("\n--- XÓA SẢN PHẨM KHỎI HỆ THỐNG ---");
        String sku = Inputter.inputStr("Nhập mã SKU sản phẩm muốn xóa: ").trim();

        if (productController.deleteProduct(sku)) {
            System.out.println("Thông báo: Đã xóa sản phẩm thành công!");
            productController.askToSaveData();
        } else {
            System.out.println("Thông báo: Không tìm thấy mã SKU: " + sku + " trên hệ thống.");
        }
    }

    private void uiDisplayProducts() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("                           DANH SÁCH SẢN PHẨM TRÊN HỆ THỐNG                     ");
        System.out.println("--------------------------------------------------------------------------------");
        
        SinglyLinkedList<Product> list = productController.getProductList();
        SinglyLinkedList.Node<Product> current = list.getHead();
        
        if (current == null) {
            System.out.println("  Danh sách hiện đang trống.");
            System.out.println("--------------------------------------------------------------------------------");
            return;
        }
        
        System.out.printf("%-15s | %-25s | %-15s | %-20s\n", "SKU", "Tên Sản Phẩm", "Danh Mục", "Nhà Cung Cấp");
        System.out.println("--------------------------------------------------------------------------------");
        
        while (current != null) {
            System.out.println(current.getElement());
            current = current.getNext();
        }
        System.out.println("--------------------------------------------------------------------------------");
    }
}