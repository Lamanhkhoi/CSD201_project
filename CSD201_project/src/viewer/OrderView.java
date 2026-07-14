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
import java.util.List;
import model.Product;

public class OrderView {

    private final MainController mainController;
    private final OrderController orderController;

    public OrderView(MainController mainController, OrderController orderController) {
        this.mainController = mainController;
        this.orderController = orderController;
    }

    public void displaySubMenu() {
        Object[] options = {
            "Đặt đơn hàng mới",
            "Hiển thị tất cả đơn hàng",
            "Cập nhật thông tin Đơn hàng",
            "Xóa đơn hàng",
            "Quay lại Menu chính"
        };

        while (true) {
            System.out.println("\n===== PHÂN HỆ QUẢN LÝ ĐƠN HÀNG =====");
            int choice = Menu.getChoice(options);

            switch (choice) {
                case 1:
                    uiCreateOrder();
                    break;
                case 2:
                    printOrderTable(orderController.getAllOrdersList());
                    break;
                case 3:
                    uiUpdateOrder();
                    break;
                case 4:
                    uiDeleteOrder();
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
            orderId = Inputter.inputStr("Nhập mã đơn hàng (ORDxxx): ").toUpperCase();
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

        LocalDate expected;
        LocalDate latest;
        while (true) {
            expected = Inputter.inputDate("Nhập ngày giao hàng dự kiến (dd/MM/yyyy): ");
            latest = Inputter.inputDate("Nhập ngày giao hẹn trễ nhất (dd/MM/yyyy): ");
            if (!latest.isBefore(expected)) {
                break;
            }
            System.out.println("Lỗi: Ngày hẹn trễ nhất phải >= ngày giao dự kiến. Nhập lại.");
        }

        LinkedList<OrderItem> itemsList = new LinkedList<>();
        double totalAmount = 0;
        System.out.println("-> Bắt đầu nhập danh sách mặt hàng SKU cần mua:");
        while (true) {
            String sku;
            Product product;
            while (true) {
                sku = Inputter.inputStr("   Nhập mã hàng (SKU): ").toUpperCase();
                product = mainController.getProductController().findProductBySku(sku);
                if (product == null) {
                    System.out.println("   Mã SKU không tồn tại! Nhập lại.");
                } else {
                    break;
                }
            }
            int qty = Inputter.inputInt("   Nhập số lượng mua: ");
            itemsList.addLast(new OrderItem(sku, qty));
            totalAmount += product.getPrice() * qty;

            String confirm = Inputter.inputStr("Bấm 'Y' để hoàn tất đơn, hoặc phím bất kỳ để tiếp tục nhập SKU: ");
            if (confirm.equalsIgnoreCase("Y")) {
                break;
            }
        }

        Order newOrder = new Order(orderId, name, phone, address,
                LocalDateTime.now(), expected.atStartOfDay(), latest.atStartOfDay(),
                "Pending", totalAmount, itemsList, true);

        orderController.registerNewOrder(newOrder);
        System.out.println("Đăng ký đơn hàng thành công!");
        mainController.saveOrders();
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

    private void uiUpdateOrder() {
        System.out.println("\n========== UPDATE ORDER ==========");
        String id = Inputter.inputStr("Nhập mã đơn hàng cần cập nhật: ").toUpperCase();
        Order oldOrder = orderController.getOrderById(id);
        if (oldOrder == null) {
            System.out.println("Không tìm thấy đơn hàng.");
            return;
        }
        System.out.println("\n(Để trống nếu muốn giữ nguyên giá trị cũ)");
        Order updatedOrder = new Order();
        updatedOrder.setOrderId(oldOrder.getOrderId());

        String customerName = Inputter.inputStr("Tên khách hàng [" + oldOrder.getCustomerName() + "]: ");
        updatedOrder.setCustomerName(customerName.isEmpty() ? oldOrder.getCustomerName() : customerName);

        String phone;
        while (true) {
            phone = Inputter.inputStr("Số điện thoại [" + oldOrder.getPhone() + "]: ");
            if (phone.isEmpty()) {
                phone = oldOrder.getPhone();
                break;
            }
            if (phone.matches(Pattern.PHONE_PATTERN)) {
                break;
            }
            System.out.println("Sai định dạng số điện thoại!");
        }
        updatedOrder.setPhone(phone);

        String address = Inputter.inputStr("Địa chỉ [" + oldOrder.getAddress() + "]: ");
        updatedOrder.setAddress(address.isEmpty() ? oldOrder.getAddress() : address);

        LocalDate createdDate = oldOrder.getCreatedDate().toLocalDate();

        LocalDate expectedDate = Inputter.inputDateNullable("Ngày giao dự kiến [" + oldOrder.getExpectedDate().toLocalDate() + "] (dd/MM/yyyy): ");
        if (expectedDate == null) {
            expectedDate = oldOrder.getExpectedDate().toLocalDate();
        }

        LocalDate latestDate = Inputter.inputDateNullable("Ngày giao trễ nhất [" + oldOrder.getLatestDate().toLocalDate() + "] (dd/MM/yyyy): ");

        if (latestDate == null) {
            latestDate = oldOrder.getLatestDate().toLocalDate();
        }

        if (expectedDate.isBefore(createdDate)) {
            System.out.println("Ngày giao dự kiến phải sau hoặc bằng ngày tạo.");
            return;
        }

        if (latestDate.isBefore(expectedDate)) {
            System.out.println("Ngày giao trễ nhất phải sau hoặc bằng ngày giao dự kiến.");
            return;
        }

        updatedOrder.setCreatedDate(createdDate.atStartOfDay());
        updatedOrder.setExpectedDate(expectedDate.atStartOfDay());
        updatedOrder.setLatestDate(latestDate.atStartOfDay());

        boolean success = orderController.updateOrder(updatedOrder);
        if (success) {
            System.out.println("Cập nhật đơn hàng thành công.");
            mainController.saveOrders();
        } else {
            System.out.println("Cập nhật đơn hàng thất bại.");
        }
    }

    private void uiDeleteOrder() {
        System.out.println("\n========== DELETE ORDER ==========");
        String id = Inputter.inputStr("Nhập mã đơn hàng: ").toUpperCase();
        Order order = orderController.getOrderById(id);
        if (order == null) {
            System.out.println("Không tìm thấy đơn hàng.");
            return;
        }
        String confirm = Inputter.inputStr("Bạn có chắc muốn xóa? (Y/N): ");
        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println("Đã hủy thao tác.");
            return;
        }

        boolean success = orderController.deleteOrder(id);
        if (success) {
            System.out.println("Đã xóa đơn hàng thành công.");
            mainController.saveOrders();
        } else {
            System.out.println("Xóa thất bại.");
        }

    }
}
