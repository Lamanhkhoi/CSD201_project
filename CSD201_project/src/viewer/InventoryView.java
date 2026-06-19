package viewer;

import controller.MainController;
import controller.InventoryItemController;
import model.InventoryItem;
import utilities.Inputter;
import java.time.LocalDate;
import java.util.List;

public class InventoryView {

    private final MainController mainController;
    private final InventoryItemController inventoryController;

    public InventoryView(MainController mainController, InventoryItemController inventoryController) {
        this.mainController = mainController;
        this.inventoryController = inventoryController;
    }

    public void displaySubMenu() {
        Object[] options = {
            "Nhập kho lô hàng mới (Receive Stock)",
            "Hiển thị & Sắp xếp tồn kho (Display & Sort)",
            "Quay lại Menu chính (Back to Main Menu)"
        };

        while (true) {
            System.out.println("\n===== PHÂN HỆ QUẢN LÝ TỒN KHO =====");
            int choice = Menu.getChoice(options);

            switch (choice) {
                case 1:
                    uiReceiveStock();
                    break;
                case 2:
                    uiDisplayInventory();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    private void uiReceiveStock() {
        System.out.println("\n--- THÊM LÔ HÀNG MỚI ---");
        String batchId = Inputter.inputStr("Nhập mã lô hàng: ");
        String sku = Inputter.inputStr("Nhập mã SKU: ");
        int quantity = Inputter.inputInt("Nhập số lượng: ");
        LocalDate rDate = LocalDate.now();
        LocalDate eDate = Inputter.inputDateNullable("Nhập ngày hết hạn (dd/MM/yyyy): ");
        String location = Inputter.inputStr("Nhập vị trí kệ: ");

        InventoryItem item = new InventoryItem(batchId, sku, quantity, rDate, eDate, location);
        boolean success = inventoryController.receiveNewItem(item);

        if (success) {
            System.out.println("Thành công: Đã nhập kho lô hàng mới lên RAM.");
            // Hỏi lưu file vật lý phương án B
            mainController.saveInventory();
        } else {
            System.out.println("Thất bại: Mã lô hàng bị trùng lặp!");
        }
    }

    private void uiDisplayInventory() {
        List<InventoryItem> displayList = inventoryController.getAllInventoryList();
        if (displayList.isEmpty()) {
            System.out.println("Kho hàng hiện đang trống!");
        } else {
            for (InventoryItem item : displayList) {
                System.out.println(item.toString());
            }
        }
    }
}
