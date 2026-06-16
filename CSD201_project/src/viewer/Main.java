package viewer;

import controller.InventoryItemController;
import controller.OrderController;
import controller.ProductController;
import controller.TransactionController;
import fileio.IFileReadWrite;
import fileio.InventoryItemReadWrite;
import fileio.OrderReadWrite;
import java.util.HashMap;
import java.util.List;
import model.InventoryItem;
import model.Order;
import structures.PriorityQueue;
import utilities.StorageHandler;
import viewer.InventoryView;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== KHỞI CHẠY HỆ THỐNG QUẢN LÝ KHO VÀ ĐƠN HÀNG CHUYÊN SÂU ===");

        // Khởi tạo lõi hệ thống trung tâm (RAM và nạp File được tự động gọi ở đây)
        HashMap<String, InventoryItem> globalInventoryMap = new HashMap<>();
        PriorityQueue<InventoryItem> globalExpiryHeap = new PriorityQueue<>();
        IFileReadWrite<Order, List<Order>> orderFileHandler = new OrderReadWrite();
        // 1. Khởi tạo controller sản phẩm
        ProductController productController = new ProductController();
        TransactionController transactionController = new TransactionController();
        OrderController orderController = new OrderController(globalInventoryMap, globalExpiryHeap, orderFileHandler, transactionController);
        
// 2. Khởi tạo view sản phẩm và tiêm controller vào giống các phân hệ khác
        ProductView productView = new ProductView(productController);
        Object[] mainOptions = {
            "Quản lý Sản phẩm (Product Management)",
            "Quản lý Kho hàng (Inventory Batch Management)",
            "Quản lý Đơn hàng (Order Management)",
            "Quản lý giao dịch (Transaction Management)",
            "Thoát chương trình (Exit)"
        };

        while (true) {
            System.out.println("\n===== BÀN ĐIỀU KHIỂN HỆ THỐNG TRUNG TÂM (MAIN MENU) =====");
            int choice = Menu.getChoice(mainOptions);

            switch (choice) {
                case 1:
                    productView.displaySubMenu(); // Gọi giao diện phụ của phân hệ Product
                    System.out.println("Mở Phân hệ Sản phẩm (Chưa cài đặt)...");
                    break;
                case 2:
                    System.out.println("Mở Phân hệ Kho hàng...");
                    // 1. Khởi tạo đối tượng Đọc/Ghi file
                    InventoryItemReadWrite fileIO = new InventoryItemReadWrite();
                    // 2. Khởi tạo StorageHandler
                    StorageHandler<InventoryItem, java.util.List<InventoryItem>> storage = new StorageHandler<>(fileIO);
                    // 3. Khởi tạo Controller
                    InventoryItemController invController = new InventoryItemController(storage, transactionController);
                    invController.loadData(fileIO.read());

                    // 4. Khởi tạo View truyền Controller vào
                    InventoryView invView = new InventoryView(invController);
                    // 5. Mở màn hình SubMenu
                    invView.displaySubMenu();
                    break;
                case 3:
                    OrderView orderView = new OrderView(orderController);
                    orderView.displaySubMenu();
                    break;
                case 4:
                    TransactionView tranView = new TransactionView(transactionController);
                    tranView.displaySubMenu();
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
