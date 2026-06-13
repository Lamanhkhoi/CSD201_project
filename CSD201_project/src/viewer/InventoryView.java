package viewer;

import controller.InventoryItemController;


public class InventoryView {
    private final InventoryItemController inventoryController;

    public InventoryView(InventoryItemController inventoryController) {
        this.inventoryController = inventoryController;
    }

    public void displaySubMenu() {
        Object[] options = {
            "1/ Nhập kho lô hàng mới (Receive Stock)",
            "2/ Xuất kho tự động FEFO (Pick Stock)",
            "3/ Điều chuyển vị trí kệ (Relocate Stock)",
            "4/ Tìm kiếm lô hàng trong kho (Search)",
            "5/ Cảnh báo hàng cận date/hết hạn (Expiry Alerts)",
            "6/ Hiển thị & Sắp xếp tồn kho (Display & Sort)",
            "7/ Quay lại Menu chính (Back to Main Menu)"
        };
        
        
        while (true) {
            System.out.println("\n===== PHÂN HỆ QUẢN LÝ TỒN KHO (INVENTORY) =====");
            int choice = Menu.getChoice(options);

            switch (choice) {
                case 1:
                    inventoryController;
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    break;
                case 7:
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }


}
