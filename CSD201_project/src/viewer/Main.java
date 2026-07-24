package viewer;

import controller.MainController;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== KHỞI CHẠY HỆ THỐNG QUẢN LÝ KHO VÀ ĐƠN HÀNG CHUYÊN SÂU ===");
        
        MainController mainController = new MainController();

        ProductView productView = new ProductView(mainController, mainController.getProductController());
        InventoryView inventoryView = new InventoryView(mainController, mainController.getInventoryController());
        OrderView orderView = new OrderView(mainController, mainController.getOrderController());
        TransactionView transactionView = new TransactionView(mainController, mainController.getTransactionController());

        Object[] mainOptions = {
            "Quản lý Sản phẩm (Product Management)",
            "Quản lý Tồn kho (Inventory Management)",
            "Quản lý Đơn hàng (Order Management)",
            "Xem Nhật ký kho (Transaction History)",
            "Thoát chương trình (Exit)"
        };

        while (true) {
            System.out.println("\n===== HỆ THỐNG QUẢN LÝ TRUNG TÂM =====");
            int choice = Menu.getChoice(mainOptions);

            switch (choice) {
                case 1:
                    productView.displaySubMenu();
                    break;
                case 2:
                    inventoryView.displaySubMenu();
                    break;
                case 3:
                    orderView.displaySubMenu();
                    break;
                case 4:
                    transactionView.displaySubMenu();
                    break;
                case 5:
                    System.out.println("Cảm ơn bạn đã sử dụng hệ thống. Tắt nguồn RAM!");
                    System.exit(0);
                default:
                    System.out.println("Vui lòng chọn từ 1 đến 5!");
            }
        }
    }
}
