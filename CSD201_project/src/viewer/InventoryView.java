package viewer;

import controller.MainController;
import controller.InventoryItemController;
import model.InventoryItem;
import utilities.Inputter;
import utilities.Pattern;

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
            "Điều chuyển vị trí kệ (Relocate Stock)",
            "Tìm kiếm lô hàng trong kho (Search)",
            "Cảnh báo hàng cận date/hết hạn (Expiry Alerts)",
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
                    uiRelocateStock();
                    break;
                case 3:
                    uiSearchStock();
                    break;
                case 4:
                    uiExpiryAlerts();
                    break;
                case 5:
                    uiDisplayAndSort();
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    // =========================================================
    // 1. NHẬP KHO
    // =========================================================
    private void uiReceiveStock() {
        System.out.println("\n--- THÊM LÔ HÀNG MỚI ---");
        
        String batchId;
        while (true) {
            batchId = Inputter.inputStr("Nhập mã lô hàng (VD: BAT001): ").toUpperCase();
            if (!batchId.matches(Pattern.BATCH_ID_PATTERN)) {
                System.out.println("-> [Lỗi] Sai định dạng! Mã lô bắt buộc bắt đầu bằng 'BAT' kèm 3 số.");
                continue;
            }
            break;
        }

        String sku;
        while (true) {
            sku = Inputter.inputStr("Nhập mã SKU (VD: p123): ").toUpperCase();
            
            // 1. Kiểm tra định dạng bằng Regex
            if (!sku.matches(Pattern.PRODUCT_SKU_PATTERN)) {
                System.out.println("-> [Lỗi] Sai định dạng! Mã SKU phải dài 3-10 ký tự, gồm chữ in hoa, số và gạch ngang.");
                continue;
            }
            
            // 2. Kiểm tra SKU có tồn tại trong danh mục Product hay chưa (Toàn vẹn tham chiếu)
            if (mainController.getProductController().findProductBySku(sku) == null) {
                 System.out.println("-> [Lỗi] TỪ CHỐI! Sản phẩm '" + sku + "' chưa có trong Danh mục Hệ thống.");
                 System.out.println("-> Gợi ý: Hãy nhập mã SKU khác, hoặc thoát ra sang Menu Sản Phẩm để tạo mới.");
                 return;
            }
            
            break; // Nếu pass qua cả 2 bài test thì cho đi tiếp
        }

        int quantity = Inputter.inputInt("Nhập số lượng: ");
        LocalDate rDate = LocalDate.now(); // Tự động lấy ngày hệ thống làm ngày nhập kho
        LocalDate eDate = Inputter.inputDateNullable("Nhập ngày hết hạn (dd/MM/yyyy): ");
        String location = Inputter.inputStr("Nhập vị trí kệ: ");

        InventoryItem item = new InventoryItem(batchId, sku, quantity, rDate, eDate, location);
        boolean success = inventoryController.receiveNewItem(item);

        if (success) {
            System.out.println("Thành công: Đã nhập kho lô hàng mới lên RAM.");
            // Cập nhật xuống file vật lý thông qua MainController
            if (mainController != null) {
                mainController.saveInventory(); 
            }
        } else {
            System.out.println("Thất bại: Thao tác nhập kho đã bị hủy!");
        }
    }

    // =========================================================
    // 2. ĐIỀU CHUYỂN VỊ TRÍ
    // =========================================================
    private void uiRelocateStock() {
        System.out.println("\n--- ĐIỀU CHUYỂN VỊ TRÍ KỆ HÀNG ---");
        String batchId = Inputter.inputStr("Nhập mã lô hàng cần dời: ").toUpperCase();
        
        if (inventoryController.getBatchById(batchId) == null) {
            System.out.println("-> [Lỗi] Không tìm thấy mã lô hàng '" + batchId + "' trên hệ thống!");
            return;
        }

        String newLoc = Inputter.inputStr("Nhập vị trí kệ mới: ");
        boolean success = inventoryController.updateItemLocation(batchId, newLoc);
        
        if (success && mainController != null) {
            mainController.saveInventory(); // Lưu file sau khi dời thành công
        }
    }

    // =========================================================
    // 3. TÌM KIẾM
    // =========================================================
    private void uiSearchStock() {
        System.out.println("\n--- TÌM KIẾM LÔ HÀNG TRONG KHO ---");
        System.out.println("1. Tìm chính xác theo Mã Lô Hàng (Batch ID)");
        System.out.println("2. Tìm tất cả theo Mã Sản Phẩm (SKU)");
        int searchChoice = Inputter.inputInt("Chọn cách tìm kiếm (1 hoặc 2): ");

        if (searchChoice == 1) {
            String searchBatch = Inputter.inputStr("Nhập mã lô hàng (VD: BAT001): ").toUpperCase();
            InventoryItem foundBatch = inventoryController.getBatchById(searchBatch);
            
            if (foundBatch != null) {
                System.out.println("\n-> ĐÃ TÌM THẤY:");
                System.out.println(foundBatch.toString());
            } else {
                System.out.println("-> [Thông báo] Kho không có lô hàng nào khớp mã: " + searchBatch);
            }

        } else if (searchChoice == 2) {
            String searchSku = Inputter.inputStr("Nhập mã sản phẩm (SKU): ").toUpperCase();
            List<InventoryItem> foundList = inventoryController.getBatchesBySku(searchSku);
            
            if (foundList.isEmpty()) {
                System.out.println("-> [Thông báo] Kho không còn lô hàng nào của sản phẩm: " + searchSku);
            } else {
                System.out.println("\n-> TÌM THẤY " + foundList.size() + " LÔ HÀNG:");
                for (InventoryItem item : foundList) {
                    System.out.println(item.toString());
                }
            }
        } else {
            System.out.println("-> [Lỗi] Lựa chọn không hợp lệ!");
        }
    }

    // =========================================================
    // 4. CẢNH BÁO CẬN DATE
    // =========================================================
    private void uiExpiryAlerts() {
        System.out.println("\n--- CẢNH BÁO HÀNG CẬN DATE / HẾT HẠN ---");
        int daysThreshold = Inputter.inputInt("Nhập số ngày ngưỡng để cảnh báo (Ví dụ: 30): ");
        
        List<InventoryItem> alertList = inventoryController.getAlertItems(daysThreshold);
        
        if (alertList.isEmpty()) {
            System.out.println("-> [Thông báo] Tuyệt vời! Kho hàng hiện tại an toàn, không có sản phẩm nào sắp hết hạn trong " + daysThreshold + " ngày tới.");
        } else {
            System.out.println("\n>>> PHÁT HIỆN " + alertList.size() + " LÔ HÀNG CẦN CHÚ Ý <<<");
            System.out.println("-------------------------------------------------------------------------");
            for (InventoryItem item : alertList) {
                System.out.println(item.toString());
            }
            System.out.println("-------------------------------------------------------------------------");
        }
    }

    // =========================================================
    // 5. HIỂN THỊ VÀ SẮP XẾP
    // =========================================================
    private void uiDisplayAndSort() {
        System.out.println("\n--- HIỂN THỊ & SẮP XẾP TỒN KHO ---");
        System.out.println("1. Hiển thị mặc định (Theo thứ tự nhập kho)");
        System.out.println("2. Sắp xếp theo Số lượng (Ít nhất lên đầu)");
        System.out.println("3. Sắp xếp theo Ngày hết hạn (Cận date lên đầu - FEFO)");
        
        int sortChoice = Inputter.inputInt("Chọn cách hiển thị (1-3): ");
        List<InventoryItem> displayList;

        if (sortChoice == 2) {
            displayList = inventoryController.getInventorySortedByQuantity();
        } else if (sortChoice == 3) {
            displayList = inventoryController.getInventorySortedByExpiryDate();
        } else {
            if (sortChoice != 1) {
                System.out.println("-> [Lỗi] Lựa chọn không hợp lệ. Hệ thống sẽ in theo mặc định.");
            }
            displayList = inventoryController.getAllInventoryList();
        }

        if (displayList.isEmpty()) {
            System.out.println("-> [Thông báo] Kho hàng hiện đang hoàn toàn trống!");
        } else {
            System.out.println("\n>>> DANH SÁCH TỒN KHO (" + displayList.size() + " LÔ HÀNG) <<<");
            System.out.println("-------------------------------------------------------------------------");
            for (InventoryItem item : displayList) {
                System.out.println(item.toString());
            }
            System.out.println("-------------------------------------------------------------------------");
        }
    }
}