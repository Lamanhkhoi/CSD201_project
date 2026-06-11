package controller;

import model.Product;
import fileio.ProductReadWrite;
import utilities.Inputter;
import utilities.StorageHandler;
import java.util.ArrayList;
import java.util.List;

public class ProductController {
    // Sử dụng java.util.List đồng bộ theo hướng IFileReadWrite
    private ProductReadWrite fileIO;
    private StorageHandler<Product, LinkedList<Product>> storageHandler;

    public ProductController() {
        this.productList = new ArrayList<>();
        this.fileIO = new ProductReadWrite();
        // Khởi tạo StorageHandler truyền vào bộ đọc ghi tương ứng
        this.storageHandler = new StorageHandler<>(fileIO);
        loadData();
    }

    // Tải dữ liệu từ file lên danh sách khi khởi tạo bộ điều khiển
    private void loadData() {
        try {
            this.productList = fileIO.read();
            System.out.println("Hệ thống: Khởi tạo dữ liệu sản phẩm thành công.");
        } catch (Exception e) {
            System.out.println("Lỗi khi tải dữ liệu sản phẩm: " + e.getMessage());
        }
    }

    // Gọi hàm hỏi và lưu có sẵn của nhóm thông qua StorageHandler
    private void askToSaveData() {
        storageHandler.askAndSave(productList);
    }

    // Điều phối Menu hệ thống theo đặc tả phương thức runMenu()
    public void runMenu() {
        int choice;
        do {
            System.out.println("\n====================================");
            System.out.println("     PHÂN HỆ QUẢN LÝ SẢN PHẨM (PRODUCT) ");
            System.out.println("====================================");
            System.out.println("1. Thêm sản phẩm mới (Add Product)");
            System.out.println("2. Cập nhật thông tin sản phẩm (Update Product)");
            System.out.println("3. Xóa sản phẩm khỏi hệ thống (Delete Product)");
            System.out.println("4. Hiển thị danh sách sản phẩm (Display Products)");
            System.out.println("5. Quay lại Menu chính");
            System.out.println("====================================");
            
            // Gọi hàm nhập số nguyên của Inputter nhóm bạn (inputInt hoặc getInt tùy bạn điều chỉnh)
            choice = Inputter.inputInt("Vui lòng chọn chức năng (1-5): ");
            
            switch (choice) {
                case 1:
                    addProduct();
                    break;
                case 2:
                    updateProduct();
                    break;
                case 3:
                    deleteProduct();
                    break;
                case 4:
                    displayProducts();
                    break;
                case 5:
                    System.out.println("Đang quay lại Menu chính...");
                    break;
                default:
                    System.out.println("Lựa chọn không hợp lệ! Vui lòng chọn lại.");
            }
        } while (choice != 5);
    }

    // Chức năng: Thêm sản phẩm mới (Kiểm tra trùng SKU trực tiếp trên List)
    public void addProduct() {
        System.out.println("\n--- [CHỨC NĂNG] THÊM SẢN PHẨM MỚI ---");
        String sku;
        while (true) {
            sku = Inputter.inputStr("Nhập mã SKU sản phẩm: ").trim();
            if (sku.isEmpty()) {
                System.out.println("Thông báo: Mã SKU không được để trống.");
                continue;
            }
            if (findProductBySku(sku) != null) {
                System.out.println("Thông báo: SKU này đã tồn tại trên hệ thống! Vui lòng nhập mã khác.");
                continue;
            }
            break;
        }

        String name = Inputter.inputStr("Nhập tên sản phẩm: ");
        String category = Inputter.inputStr("Nhập danh mục/phân loại: ");
        String supplier = Inputter.inputStr("Nhập nhà cung cấp: ");

        Product newProduct = new Product(sku, name, category, supplier);
        productList.add(newProduct); 
        System.out.println("Thông báo: Thêm sản phẩm mới vào danh sách tạm thời thành công!");

        // Hỏi lưu dữ liệu
        askToSaveData();
    }

    // Chức năng: Cập nhật thông tin sản phẩm dựa trên SKU
    public void updateProduct() {
        System.out.println("\n--- [CHỨC NĂNG] CẬP NHẬT THÔNG TIN SẢN PHẨM ---");
        String sku = Inputter.inputStr("Nhập mã SKU sản phẩm cần sửa: ").trim();
        Product p = findProductBySku(sku);

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
        askToSaveData();
    }

    // Chức năng: Xóa sản phẩm ra khỏi danh sách dựa trên SKU
    public void deleteProduct() {
        System.out.println("\n--- [CHỨC NĂNG] XÓA SẢN PHẨM KHỎI HỆ THỐNG ---");
        String sku = Inputter.inputStr("Nhập mã SKU sản phẩm muốn xóa: ").trim();
        Product p = findProductBySku(sku);

        if (p != null) {
            productList.remove(p); // Tận dụng hàm remove(Object) tối ưu của java.util.List
            System.out.println("Thông báo: Đã xóa sản phẩm thành công khỏi danh sách tạm!");
            askToSaveData();
        } else {
            System.out.println("Thông báo: Không tìm thấy mã SKU: " + sku + " để xóa.");
        }
    }

    // Chức năng: Hiển thị toàn bộ danh sách sản phẩm dạng bảng
    public void displayProducts() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("                           DANH SÁCH SẢN PHẨM TRÊN HỆ THỐNG                     ");
        System.out.println("--------------------------------------------------------------------------------");
        if (productList.isEmpty()) {
            System.out.println("  Danh sách hiện đang trống.");
            System.out.println("--------------------------------------------------------------------------------");
            return;
        }
        System.out.printf("%-15s | %-25s | %-15s | %-20s\n", "SKU", "Tên Sản Phẩm", "Danh Mục", "Nhà Cung Cấp");
        System.out.println("--------------------------------------------------------------------------------");
        
        // Duyệt java.util.List bằng vòng lặp for-each rất gọn gàng
        for (Product p : productList) {
            System.out.println(p);
        }
        System.out.println("--------------------------------------------------------------------------------");
    }

    // Hàm bổ trợ (Helper): Tìm kiếm sản phẩm theo SKU tuần tự trên java.util.List
    private Product findProductBySku(String sku) {
        for (Product p : productList) {
            if (p.getSku().equalsIgnoreCase(sku)) {
                return p;
            }
        }
        return null;
    }
}