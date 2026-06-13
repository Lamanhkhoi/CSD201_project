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

    private void uiAddProduct() {
        System.out.println("\n--- TIẾN HÀNH THÊM SẢN PHẨM MỚI ---");
        String sku;
        while (true) {
            sku = Inputter.inputStr("Nhập mã SKU sản phẩm: ").trim();
            if (sku.isEmpty()) {
                System.out.println("Thông báo: Mã SKU không được để trống.");
                continue;
            }
            if (!sku.matches(utilities.Pattern.PRODUCT_SKU_PATTERN)) {
                System.out.println("Thông báo: Mã SKU sai định dạng! Định dạng chuẩn phải là Pxxx (VD: P001, P123).");
                continue;
            }
            if (productController.findProductBySku(sku) != null) {
                System.out.println("Thông báo: SKU này đã tồn tại trên hệ thống! Vui lòng nhập mã khác.");
                continue;
            }
            break;
        }

        // Bắt buộc nhập Tên sản phẩm
        String name;
        while (true) {
            name = Inputter.inputStr("Nhập tên sản phẩm: ").trim();
            if (name.isEmpty()) {
                System.out.println("Thông báo: Tên sản phẩm không được để trống!");
                continue;
            }
            break;
        }

// Bắt buộc nhập Danh mục
        String category;
        while (true) {
            category = Inputter.inputStr("Nhập danh mục/phân loại: ").trim();
            if (category.isEmpty()) {
                System.out.println("Thông báo: Danh mục không được để trống!");
                continue;
            }
            break;
        }

        String supplier;
        while (true) {
            supplier = Inputter.inputStr("Nhập nhà cung cấp: ").trim();
            if (supplier.isEmpty()) {
                System.out.println("Thông báo: Nhà cung cấp không được để trống!");
                continue;
            }
            break;
        }
        // Vòng lặp yêu cầu nhập giá, chặn số âm
        double price;
        while (true) {
            price = Inputter.inputDouble("Nhập giá sản phẩm (VNĐ): ");

            // Rào chắn 1: Không được âm hoặc bằng 0
            if (price <= 0) {
                System.out.println("Thông báo: Giá sản phẩm phải lớn hơn 0 VNĐ.");
                continue;
            }

            // Rào chắn 2: Giá trị nhỏ nhất ở VNĐ thường làm tròn hàng nghìn
            if (price % 1000 != 0) {
                System.out.println("Thông báo: Số tiền không hợp lý! Vui lòng nhập giá làm tròn đến hàng nghìn (VD: 1000, 150000).");
                continue;
            }

            // Rào chắn 3: Cảnh báo mềm nếu giá quá rẻ (dưới 10.000 VNĐ)
            if (price < 50000) {
                System.out.printf("Cảnh báo: Mức giá %,.0f VNĐ là một mức giá thấp.\n", price);
                String confirm = Inputter.inputStr("Bạn có chắc chắn muốn thiết lập mức giá này không? (Y/N): ").trim();
                if (!confirm.equalsIgnoreCase("Y")) {
                    System.out.println("Vui lòng nhập lại giá.");
                    continue; // Bắt nhập lại
                }
            } // 4. Xác nhận mềm: Giá quá cao (> 10.000.000 VNĐ)
            else if (price > 10000000) {
                System.out.printf("Cảnh báo: Sản phẩm có giá trị rất cao (%,.0f VNĐ).\n", price);
                String confirm = Inputter.inputStr("Bạn có chắc chắn mình không gõ thừa số 0 chứ? (Y/N): ").trim();
                if (!confirm.equalsIgnoreCase("Y")) {
                    System.out.println("Vui lòng nhập lại giá.");
                    continue;
                }
            }

            break; // Vượt qua mọi trạm kiểm soát -> Thoát vòng lặp
        }

        Product newProduct = new Product(sku, name, category, supplier, price);
        if (productController.addProduct(newProduct)) {
            System.out.println("Thông báo: Thêm sản phẩm mới thành công!");
            productController.askToSaveData();
        }
    }

    private void uiUpdateProduct() {
        System.out.println("\n--- CẬP NHẬT THÔNG TIN SẢN PHẨM ---");
        String sku;
        while (true) {
            sku = Inputter.inputStr("Nhập mã SKU sản phẩm cần sửa: ").trim();
            if (sku.isEmpty()) {
                System.out.println("Thông báo: Mã SKU không được để trống.");
                continue;
            }
            if (!sku.matches(utilities.Pattern.PRODUCT_SKU_PATTERN)) {
                System.out.println("Thông báo: Mã SKU sai định dạng! Định dạng chuẩn phải là Pxxx (VD: P001).");
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
        if (!newName.isEmpty()) {
            p.setName(newName);
        }

        String newCategory = Inputter.inputStr("Nhập danh mục mới: ").trim();
        if (!newCategory.isEmpty()) {
            p.setCategory(newCategory);
        }

        String newSupplier = Inputter.inputStr("Nhập nhà cung cấp mới: ").trim();
        if (!newSupplier.isEmpty()) {
            p.setSupplier(newSupplier);
        }

        // Sử dụng InputDoubleNullable để cho phép người dùng ấn Enter bỏ qua
        Double newPrice = Inputter.inputDoubleNullable("Nhập giá mới (VNĐ): ");
        if (newPrice != null) {
            if (newPrice < 0) {
                System.out.println("Thông báo: Giá nhập vào là số âm, hệ thống sẽ giữ nguyên giá cũ.");
            } else {
                p.setPrice(newPrice);
            }
        }

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
        System.out.println("\n------------------------------------------------------------------------------------------------------");
        System.out.println("                                DANH SÁCH SẢN PHẨM TRÊN HỆ THỐNG                                      ");
        System.out.println("------------------------------------------------------------------------------------------------------");

        SinglyLinkedList<Product> list = productController.getProductList();
        SinglyLinkedList.Node<Product> current = list.getHead();

        if (current == null) {
            System.out.println("  Danh sách hiện đang trống.");
            System.out.println("------------------------------------------------------------------------------------------------------");
            return;
        }

        // Kéo dài phần tiêu đề để vừa cột giá
        System.out.printf("%-15s | %-25s | %-15s | %-20s | %-15s\n", "SKU", "Tên Sản Phẩm", "Danh Mục", "Nhà Cung Cấp", "Đơn Giá");
        System.out.println("------------------------------------------------------------------------------------------------------");

        while (current != null) {
            System.out.println(current.getElement());
            current = current.getNext();
        }
        System.out.println("------------------------------------------------------------------------------------------------------");
    }
}
