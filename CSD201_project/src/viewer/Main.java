package viewer;

import controller.MainController;
import controller.ProductController;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== KHỞI CHẠY HỆ THỐNG QUẢN LÝ KHO VÀ ĐƠN HÀNG CHUYÊN SÂU ===");

        // Khởi tạo lõi hệ thống trung tâm (RAM và nạp File được tự động gọi ở đây)
        MainController mainController = new MainController();

        Object[] mainOptions = {
            "Quản lý Sản phẩm (Product Management)",
            "Quản lý Kho hàng (Inventory Batch Management)",
            "Quản lý Đơn hàng (Order Management)",
            "Thoát chương trình (Exit)"
        };

        while (true) {
            System.out.println("\n===== BÀN ĐIỀU KHIỂN HỆ THỐNG TRUNG TÂM (MAIN MENU) =====");
            int choice = Menu.getChoice(mainOptions);

            switch (choice) {
                case 1:
                    ProductController productController = new ProductController();
                    productController.runMenu();
                    System.out.println("Mở Phân hệ Sản phẩm (Chưa cài đặt)...");
                    break;
                case 2:
                    System.out.println("Mở Phân hệ Kho hàng (Chưa cài đặt)...");
                    break;
                case 3:
                    OrderView orderView = new OrderView(mainController.getOrderController());
                    orderView.displaySubMenu();
                    break;
                case 4:
                    System.out.println("Cảm ơn bạn đã sử dụng hệ thống. Tắt nguồn RAM!");
                    System.exit(0);
                default:
                    System.out.println("Vui lòng chọn từ 1 đến 4!");
            }
        }
    }
}
