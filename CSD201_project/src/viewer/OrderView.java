package viewer;

import controller.MainController;
import controller.OrderController;
import model.Order;
import model.OrderItem;
import structures.LinkedList;
import utilities.Inputter;
import utilities.Pattern;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OrderView {

    private final MainController mainController;
    private final OrderController orderController;

    public OrderView(MainController mainController, OrderController orderController) {
        this.mainController = mainController;
        this.orderController = orderController;
    }

    public void displaySubMenu() {
        Object[] options = {
            "Đặt đơn hàng mới (Register New Order)",
            "Hiển thị & Tìm kiếm Đơn hàng (Báo cáo chuyên sâu)",
            "Cập nhật trạng thái Đơn hàng thủ công",
            "Kích hoạt hệ thống xuất kho tự động (Run Auto FEFO)",
            "Quay lại Menu chính (Back to Main Menu)"
        };

        while (true) {
            System.out.println("\n===== PHÂN HỆ QUẢN LÝ ĐƠN HÀNG =====");
            int choice = Menu.getChoice(options);

            switch (choice) {
                case 1:
                    uiCreateOrder();
                    break;
                case 2:
                    uiDisplayOrdersMenu();
                    break;
                case 3:
                    uiUpdateStatus();
                    break;
                case 4:
                    orderController.processAllWaitingOrders();
                    mainController.saveOrders();
                    mainController.saveInventory();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    private void uiCreateOrder() {
        System.out.println("\n--- TIẾN HÀNH ĐẶT ĐƠN HÀNG MỚI ---");

        String orderId;
        while (true) {
            orderId = Inputter.inputStr("Nhập mã đơn hàng (ORDxxx): ");
            if (orderId.matches(Pattern.ORDER_ID_PATTERN)) {
                if (orderController.getOrderById(orderId) != null) {
                    System.out.println("Mã đơn hàng đã tồn tại trên hệ thống!");
                } else {
                    break;
                }
            } else {
                System.out.println("Sai định dạng! Mã đơn phải bắt đầu bằng 'ORD' và theo sau bởi 3 chữ số.");
            }
        }

        String name = Inputter.inputStr("Nhập tên khách hàng: ");

        String phone;
        while (true) {
            phone = Inputter.inputStr("Nhập số điện thoại (10 số): ");
            if (phone.matches(Pattern.PHONE_PATTERN)) {
                break;
            }
            System.out.println("Sai định dạng số điện thoại Việt Nam!");
        }

        String address = Inputter.inputStr("Nhập địa chỉ giao hàng: ");

        LocalDate expected = Inputter.inputDate("Nhập ngày giao hàng dự kiến (dd/MM/yyyy): ");
        LocalDate latest = Inputter.inputDate("Nhập ngày giao hẹn trễ nhất (dd/MM/yyyy): ");
        double amount = Inputter.inputDouble("Nhập tổng giá trị đơn hàng (VNĐ): ");

        LinkedList<OrderItem> itemsList = new LinkedList<>();
        System.out.println("-> Bắt đầu nhập danh sách mặt hàng SKU cần mua:");
        while (true) {
            String sku = Inputter.inputStr("   Nhập mã hàng (SKU): ");
            int qty = Inputter.inputInt("   Nhập số lượng mua: ");
            itemsList.addLast(new OrderItem(sku, qty));

            String confirm = Inputter.inputStr("Bấm 'Y' để hoàn tất đơn, hoặc phím bất kỳ để tiếp tục nhập SKU: ");
            if (confirm.equalsIgnoreCase("Y")) {
                break;
            }
        }

        Order newOrder = new Order(orderId, name, phone, address,
                LocalDateTime.now(), expected.atStartOfDay(), latest.atStartOfDay(),
                "Pending", amount, itemsList);

        orderController.registerNewOrder(newOrder);
        System.out.println("Đăng ký đơn hàng thành công!");
        mainController.saveOrders();
    }

    private void uiDisplayOrdersMenu() {
        Object[] displayOptions = {
            "Hiển thị TẤT CẢ hóa đơn hiện tại",
            "Tìm kiếm và xem chi tiết theo Mã Đơn hàng (ID)",
            "Lọc hóa đơn theo Trạng thái (Status)",
            "Sắp xếp hiển thị theo Ngày Tạo (Created Date)",
            "Sắp xếp hiển thị theo Ngày Dự Kiến (Expected Date)",
            "Sắp xếp hiển thị theo Ngày Trễ Nhất (Latest Date)",
            "Quay lại Menu Đơn hàng"
        };

        while (true) {
            System.out.println("\n--- TÙY CHỌN HIỂN THỊ & BÁO CÁO ĐƠN HÀNG ---");
            int choice = Menu.getChoice(displayOptions);

            switch (choice) {
                case 1:
                    printOrderTable(orderController.getAllOrdersList());
                    break;
                case 2:
                    displayOrderById();
                    break;
                case 3:
                    displayOrdersByStatus();
                    break;
                case 4:
                    displayOrdersSortedByCreatedDate();
                    break;
                case 5:
                    displayOrdersSortedByExpectedDate();
                    break;
                case 6:
                    displayOrdersSortedByLatestDate();
                    break;
                case 7:
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    // Tìm kiếm chính xác và in cấu trúc cây chi tiết của một Đơn hàng
    private void displayOrderById() {
        String id = Inputter.inputStr("Nhập mã đơn hàng cần tìm: ").toUpperCase();
        Order o = orderController.getOrderById(id);
        if (o == null) {
            System.out.println("Không tìm thấy đơn hàng.");
            return;
        }

        System.out.println("\n=======================================================");
        System.out.println("           CHI TIẾT ĐƠN HÀNG: " + o.getOrderId());
        System.out.println("=======================================================");
        System.out.printf("Khách hàng   : %s\n", o.getCustomerName());
        System.out.printf("Số điện thoại: %s\n", o.getPhone());
        System.out.printf("Địa chỉ nhận : %s\n", o.getAddress());
        System.out.printf("Trạng thái   : [%s]\n", o.getStatus());
        System.out.printf("Tổng hóa đơn : %,.0f VNĐ\n", o.getTotalAmount());
        System.out.println("-------------------------------------------------------");
        System.out.printf("Ngày Tạo đơn : %s\n", o.getCreatedDate().toLocalDate());
        System.out.printf("Giao dự kiến : %s\n", o.getExpectedDate().toLocalDate());
        System.out.printf("Hạn trễ nhất : %s\n", o.getLatestDate().toLocalDate());
        System.out.println("-------------------------------------------------------");
        System.out.println("DANH SÁCH CÁC SKU YÊU CẦU BỐC DỠ KHO:");
        for (int i = 0; i < o.getItemsToPick().size(); i++) {
            OrderItem it = o.getItemsToPick().get(i);
            System.out.printf("  [Mã mặt hàng] SKU: %-15s | [Số lượng] Qty: %d\n", it.getSku(), it.getQuantity());
        }
        System.out.println("=======================================================");
    }

    // Bộ lọc (Filter) tập hợp đơn hàng theo thuộc tính Trạng thái
    private void displayOrdersByStatus() {
        System.out.println("Các trạng thái mẫu: Pending, Waiting, Ready, Delivery, Cancel, Completed");
        String targetStatus = Inputter.inputStr("Nhập trạng thái bạn muốn lọc: ").trim();

        List<Order> allOrders = orderController.getAllOrdersList();
        List<Order> filteredList = new ArrayList<>();

        for (Order o : allOrders) {
            if (o.getStatus().equalsIgnoreCase(targetStatus)) {
                filteredList.add(o);
            }
        }

        if (filteredList.isEmpty()) {
            System.out.println("Không có đơn hàng nào thuộc trạng thái: [" + targetStatus + "]");
        } else {
            System.out.println("\nDANH SÁCH ĐƠN HÀNG ĐÃ ĐƯỢC LỌC THEO TRẠNG THÁI: [" + targetStatus.toUpperCase() + "]");
            printOrderTable(filteredList);
        }
    }

    // Xuất danh sách sắp xếp tăng/giảm dần theo Ngày Tạo (Created Date)
    private void displayOrdersSortedByCreatedDate() {
        List<Order> sortedList = new ArrayList<>(orderController.getAllOrdersList());
        if (sortedList.isEmpty()) {
            System.out.println("Hệ thống trống.");
            return;
        }

        System.out.println("1. Xếp theo Ngày Tạo tăng dần (Cũ nhất trước)\n2. Xếp theo Ngày Tạo giảm dần (Mới nhất trước)");
        int orderChoice = Inputter.inputInt("Lựa chọn kiểu sắp xếp (1-2): ");

        sortedList.sort(new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                return orderChoice == 2 ? o2.getCreatedDate().compareTo(o1.getCreatedDate())
                        : o1.getCreatedDate().compareTo(o2.getCreatedDate());
            }
        });

        System.out.println("\n--- DANH SÁCH BÁO CÁO SẮP XẾP THEO NGÀY TẠO ĐƠN ---");
        printOrderTable(sortedList);
    }

    // Xuất danh sách sắp xếp tăng/giảm dần theo Ngày Giao Dự Kiến (Expected Date)
    private void displayOrdersSortedByExpectedDate() {
        List<Order> sortedList = new ArrayList<>(orderController.getAllOrdersList());
        if (sortedList.isEmpty()) {
            System.out.println("Hệ thống trống.");
            return;
        }

        System.out.println("1. Ngày Dự Kiến tăng dần (Sớm nhất trước)\n2. Ngày Dự Kiến giảm dần (Muộn nhất trước)");
        int orderChoice = Inputter.inputInt("Lựa chọn kiểu sắp xếp (1-2): ");

        sortedList.sort(new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                return orderChoice == 2 ? o2.getExpectedDate().compareTo(o1.getExpectedDate())
                        : o1.getExpectedDate().compareTo(o2.getExpectedDate());
            }
        });

        System.out.println("\n--- DANH SÁCH BÁO CÁO SẮP XẾP THEO NGÀY GIAO DỰ KIẾN ---");
        printOrderTable(sortedList);
    }

    // Xuất danh sách sắp xếp tăng/giảm dần theo Hạn Định Giao Trễ Nhất (Latest Date)
    private void displayOrdersSortedByLatestDate() {
        List<Order> sortedList = new ArrayList<>(orderController.getAllOrdersList());
        if (sortedList.isEmpty()) {
            System.out.println("Hệ thống trống.");
            return;
        }

        System.out.println("1. Hạn trễ nhất tăng dần (Cần ưu tiên xử lý gấp)\n2. Hạn trễ nhất giảm dần");
        int orderChoice = Inputter.inputInt("Lựa chọn kiểu sắp xếp (1-2): ");

        sortedList.sort(new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                return orderChoice == 2 ? o2.getLatestDate().compareTo(o1.getLatestDate())
                        : o1.getLatestDate().compareTo(o2.getLatestDate());
            }
        });

        System.out.println("\n--- DANH SÁCH BÁO CÁO SẮP XẾP THEO HẠN ĐỊNH TRỄ NHẤT ---");
        printOrderTable(sortedList);
    }

    private void printOrderTable(List<Order> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("Không có dữ liệu đơn hàng nào thỏa mãn điều kiện.");
            return;
        }
        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.printf("%-8s | %-16s | %-10s | %-10s | %-10s | %-10s | %-12s\n",
                "Mã Đơn", "Khách Hàng", "Ngày Tạo", "Dự Kiến", "Hạn Trễ", "Trạng Thái", "Tổng Tiền");
        System.out.println("---------------------------------------------------------------------------------------------");
        for (Order o : list) {
            System.out.printf("%-8s | %-16s | %-10s | %-10s | %-10s | %-10s | %,12.0f\n",
                    o.getOrderId(),
                    o.getCustomerName().length() > 16 ? o.getCustomerName().substring(0, 13) + "..." : o.getCustomerName(),
                    o.getCreatedDate().toLocalDate().toString(),
                    o.getExpectedDate().toLocalDate().toString(),
                    o.getLatestDate().toLocalDate().toString(),
                    o.getStatus(),
                    o.getTotalAmount());
        }
        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.printf("TỔNG CỘNG: Tìm thấy %d bản ghi đơn hàng.\n", list.size());
    }

    private void uiUpdateStatus() {
        String id = Inputter.inputStr("Nhập mã đơn hàng cần sửa trạng thái: ");
        if (orderController.getOrderById(id) == null) {
            System.out.println("Thất bại: Mã đơn hàng không tồn tại!");
            return;
        }
        System.out.println("Các trạng thái hợp lệ: Pending, Waiting, Ready, Delivery, Cancel, Completed");
        String status = Inputter.inputStr("Nhập trạng thái mới muốn chuyển đổi: ");
        orderController.updateOrderStatusManual(id, status);
    }
}
