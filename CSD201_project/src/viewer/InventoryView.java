package viewer;

import controller.MainController;
import controller.InventoryItemController;
import model.Inventorybatch;
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
            "Nhập kho theo SKU (Receive Stock)",
            "Dời SKU sang ngăn tủ khác (Move Batch)",
            "Xóa SKU khỏi kho (Delete SKU)",
            "Tìm kiếm theo SKU (Search)",
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
                    uiMoveBatch();
                    break;
                case 3:
                    uiDeleteSku();
                    break;
                case 4:
                    uiSearchStock();
                    break;
                case 5:
                    uiExpiryAlerts();
                    break;
                case 6:
                    uiDisplayAndSort();
                    break;
                case 7:
                    return;
                default:
                    System.out.println("-> [Lỗi] Lựa chọn không hợp lệ!");
            }
        }
    }

    // =========================================================
    // 1. NHẬP KHO THEO SKU (không còn nhập batchId/vị trí kệ tay - hệ thống tự cấp ngăn tủ)
    // =========================================================
    private void uiReceiveStock() {
        System.out.println("\n--- NHẬP KHO ---");

        String sku;
        while (true) {
            sku = Inputter.inputStr("Nhập mã SKU (VD: P001): ").toUpperCase();

            if (!sku.matches(Pattern.PRODUCT_SKU_PATTERN)) {
                System.out.println("-> [Lỗi] Sai định dạng! Mã SKU phải đúng dạng P + 3 số.");
                continue;
            }

            if (mainController.getProductController().findProductBySku(sku) == null) {
                System.out.println("-> [Lỗi] TỪ CHỐI! Sản phẩm '" + sku + "' chưa có trong Danh mục Hệ thống.");
                System.out.println("-> Gợi ý: Hãy sang Menu Sản Phẩm để tạo mới trước.");
                return;
            }

            break;
        }

        int quantity = Inputter.inputInt("Nhập số lượng: ");
        LocalDate receiveDate = LocalDate.now(); // Tự động lấy ngày hệ thống làm ngày nhập kho
        LocalDate expiryDate = Inputter.inputDate("Nhập ngày hết hạn (dd/MM/yyyy): ");

        boolean success = inventoryController.receiveStock(sku, quantity, receiveDate, expiryDate);

        if (success && mainController != null) {
            mainController.saveInventory();
        }
    }

    // =========================================================
    // 2. DỜI SKU SANG NGĂN TỦ KHÁC (thay cho "dời vị trí kệ" cũ)
    // =========================================================
    private void uiMoveBatch() {
        System.out.println("\n--- DỜI SKU SANG NGĂN TỦ KHÁC ---");
        String sku = Inputter.inputStr("Nhập mã SKU cần dời: ").toUpperCase();

        if (inventoryController.findBatchBySku(sku) == null) {
            System.out.println("-> [Lỗi] SKU '" + sku + "' hiện không có trong kho.");
            return;
        }

        String targetBatchId = Inputter.inputStr("Nhập mã ngăn tủ đích (VD: BAT005): ").toUpperCase();
        boolean success = inventoryController.moveSkuToBatch(sku, targetBatchId);

        if (success && mainController != null) {
            mainController.saveInventory();
        }
    }

    // =========================================================
    // 3. XÓA MỀM SKU (chỉ khi tổng số lượng = 0)
    // =========================================================
    private void uiDeleteSku() {
        System.out.println("\n--- XÓA SKU KHỎI KHO ---");
        String sku = Inputter.inputStr("Nhập mã SKU cần xóa: ").toUpperCase();

        boolean success = inventoryController.deleteBySku(sku);

        if (success && mainController != null) {
            mainController.saveInventory();
        }
    }

    // =========================================================
    // 4. TÌM KIẾM THEO SKU (batchId <-> sku giờ là 1-1 nên không còn tìm nhiều batch cho 1 SKU)
    // =========================================================
    private void uiSearchStock() {
        System.out.println("\n--- TÌM KIẾM THEO SKU ---");
        String sku = Inputter.inputStr("Nhập mã SKU cần tìm: ").toUpperCase();
        Inventorybatch batch = inventoryController.findBatchBySku(sku);

        if (batch == null) {
            System.out.println("-> [Thông báo] Kho không có SKU nào khớp: " + sku);
            return;
        }

        printBatchDetail(batch);
    }

    // =========================================================
    // 5. CẢNH BÁO CẬN DATE
    // =========================================================
    private void uiExpiryAlerts() {
        System.out.println("\n--- CẢNH BÁO HÀNG CẬN DATE / HẾT HẠN ---");
        int daysThreshold = Inputter.inputInt("Nhập số ngày ngưỡng để cảnh báo (Ví dụ: 30): ");

        List<Inventorybatch> alertList = inventoryController.getAlertBatches(daysThreshold);

        if (alertList.isEmpty()) {
            System.out.println("-> [Thông báo] Tuyệt vời! Kho hàng hiện tại an toàn, không có SKU nào sắp hết hạn trong "
                    + daysThreshold + " ngày tới.");
        } else {
            System.out.println("\n>>> PHÁT HIỆN " + alertList.size() + " NGĂN TỦ CẦN CHÚ Ý <<<");
            for (Inventorybatch batch : alertList) {
                printBatchDetail(batch);
            }
        }
    }

    // =========================================================
    // 6. HIỂN THỊ VÀ SẮP XẾP
    // =========================================================
    private void uiDisplayAndSort() {
        System.out.println("\n--- HIỂN THỊ & SẮP XẾP TỒN KHO ---");
        System.out.println("1. Hiển thị mặc định");
        System.out.println("2. Sắp xếp theo hạn sử dụng gần nhất (FEFO)");

        int sortChoice = Inputter.inputInt("Chọn cách hiển thị (1-2): ");
        List<Inventorybatch> displayList;

        if (sortChoice == 2) {
            displayList = inventoryController.getBatchesSortedByEarliestExpiry();
        } else {
            if (sortChoice != 1) {
                System.out.println("-> [Lỗi] Lựa chọn không hợp lệ. Hệ thống sẽ in theo mặc định.");
            }
            displayList = inventoryController.getAllActiveBatches();
        }

        if (displayList.isEmpty()) {
            System.out.println("-> [Thông báo] Kho hàng hiện đang hoàn toàn trống!");
        } else {
            System.out.println("\n>>> DANH SÁCH TỒN KHO (" + displayList.size() + " NGĂN TỦ) <<<");
            for (Inventorybatch batch : displayList) {
                printBatchDetail(batch);
            }
        }
    }

    // In chi tiết 1 ngăn tủ, gồm toàn bộ lô hàng con bên trong theo thứ tự FEFO
    private void printBatchDetail(Inventorybatch batch) {
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Ngăn tủ: " + batch.getBatchId() + " | SKU: " + batch.getSku()
                + " | Tổng số lượng: " + batch.getTotalQuantity() + " | Số lô: " + batch.lotCount());
        for (InventoryItem lot : batch.getAllLots()) {
            System.out.println("   - Lô " + lot.getSlotId() + " | SL: " + lot.getQuantity()
                    + " | Nhập: " + lot.getReceiveDate() + " | HSD: " + lot.getExpiryDate());
        }
        System.out.println("-------------------------------------------------------------------------");
    }
}