package viewer;

import controller.InventoryItemController;
import model.InventoryItem;
import utilities.Inputter;
import java.time.LocalDate;

public class InventoryView {
    private final InventoryItemController inventoryController;

    public InventoryView(InventoryItemController inventoryController) {
        this.inventoryController = inventoryController;
    }

    public void displaySubMenu() {
        Object[] options = {
            "Nhập kho lô hàng mới (Receive Stock)",
            "Xuất kho tự động FEFO (Pick Stock)",
            "Điều chuyển vị trí kệ (Relocate Stock)",
            "Tìm kiếm lô hàng trong kho (Search)",
            "Cảnh báo hàng cận date/hết hạn (Expiry Alerts)",
            "Hiển thị & Sắp xếp tồn kho (Display & Sort)",
            "Quay lại Menu chính (Back to Main Menu)"
        };
        
        while (true) {
            System.out.println("\n===== PHÂN HỆ QUẢN LÝ TỒN KHO (INVENTORY) =====");
            int choice = Menu.getChoice(options); 

            switch (choice) {
                case 1:
                    System.out.println("\n--- THÊM LÔ HÀNG MỚI ---");
                    
                    // 1. Nhập và kiểm tra Batch ID (Mã lô hàng)
                    String batchId;
                    while (true) {
                        batchId = Inputter.inputStr("Nhập mã lô hàng (VD: BAT001): ").toUpperCase();
                        
                        // Kiểm tra Regex
                        if (!batchId.matches(utilities.Pattern.BATCH_ID_PATTERN)) {
                            System.out.println("-> [Lỗi] Sai định dạng! Mã lô bắt buộc bắt đầu bằng 'BAT' kèm 3 số (Ví dụ: BAT001).");
                            continue;
                        }
                        
                        // Kiểm tra trùng lặp
                        if (inventoryController.getBatchById(batchId) != null) {
                            System.out.println("-> [Lỗi] Mã lô hàng này đã tồn tại trên hệ thống!");
                            continue;
                        }
                        break;
                    }

                    // 2. Nhập và kiểm tra SKU (Mã sản phẩm)
                    String sku;
                    while (true) {
                        sku = Inputter.inputStr("Nhập mã sản phẩm (SKU): ").toUpperCase();
                        
                        // Kiểm tra Regex
                        if (!sku.matches(utilities.Pattern.SKU_PATTERN)) {
                            System.out.println("-> [Lỗi] Sai định dạng! Mã SKU phải dài 3-10 ký tự, gồm chữ in hoa, số và gạch ngang (VD: MILK-123).");
                            continue;
                        }
                        break;
                    }

                    // 3. Nhập các thông tin còn lại (Đã được Inputter bảo vệ)
                    int qty = Inputter.inputInt("Nhập số lượng nhập: ");
                    LocalDate rDate = Inputter.inputDate("Nhập ngày nhận kho (dd/MM/yyyy): ");
                    LocalDate eDate = Inputter.inputDate("Nhập ngày hết hạn (dd/MM/yyyy): ");
                    String loc = Inputter.inputStr("Nhập vị trí kệ hàng: ");

                    // Tạo đối tượng và đẩy vào Controller
                    InventoryItem newItem = new InventoryItem(batchId, sku, qty, rDate, eDate, loc);
                    inventoryController.receiveNewItem(newItem);
                    break;

                case 2:
                    System.out.println("\n--- XUẤT KHO TỰ ĐỘNG (FEFO) ---");
                    String searchSku = Inputter.inputStr("Nhập mã sản phẩm (SKU) cần xuất: ").toUpperCase();
                    int pickQty = Inputter.inputInt("Nhập số lượng cần xuất: ");
                    inventoryController.pickStockAutoFEFO(searchSku, pickQty);
                    break;
                    
                // Các case khác tạm thời để trống break;
                case 3:
                    System.out.println("\n--- ĐIỀU CHUYỂN VỊ TRÍ KỆ HÀNG ---");
                    
                    // 1. Nhập và kiểm tra mã Batch ID bằng vòng lặp
                    String relocateBatchId;
                    while (true) {
                        relocateBatchId = Inputter.inputStr("Nhập mã lô hàng cần dời (VD: BAT001): ").toUpperCase();
                        if (!relocateBatchId.matches(utilities.Pattern.BATCH_ID_PATTERN)) {
                            System.out.println("-> [Lỗi] Sai định dạng! Mã lô bắt buộc bắt đầu bằng 'BAT' kèm 3 số.");
                            continue;
                        }
                        break; // Đúng định dạng thì thoát vòng lặp
                    }
                    
                    // 2. Tra cứu xem lô hàng có tồn tại không trước khi bắt người dùng nhập vị trí mới
                    if (inventoryController.getBatchById(relocateBatchId) == null) {
                        System.out.println("-> [Lỗi] Không tìm thấy mã lô hàng '" + relocateBatchId + "' trên hệ thống!");
                        break;
                    }

                    // 3. Nếu tồn tại, cho nhập vị trí mới và gọi Controller
                    String newLoc = Inputter.inputStr("Nhập vị trí kệ mới: ");
                    inventoryController.updateItemLocation(relocateBatchId, newLoc);
                    break;

                case 4:
                    System.out.println("\n--- TÌM KIẾM LÔ HÀNG TRONG KHO ---");
                    System.out.println("1. Tìm chính xác theo Mã Lô Hàng (Batch ID)");
                    System.out.println("2. Tìm tất cả theo Mã Sản Phẩm (SKU)");
                    int searchChoice = Inputter.inputInt("Chọn cách tìm kiếm (1 hoặc 2): ");

                    if (searchChoice == 1) {
                        // TÌM THEO BATCH ID
                        String searchBatch;
                        while (true) {
                            searchBatch = Inputter.inputStr("Nhập mã lô hàng (VD: BAT001): ").toUpperCase();
                            if (!searchBatch.matches(utilities.Pattern.BATCH_ID_PATTERN)) {
                                System.out.println("-> [Lỗi] Sai định dạng! Mã lô bắt buộc bắt đầu bằng 'BAT' kèm 3 số.");
                                continue;
                            }
                            break;
                        }

                        InventoryItem foundBatch = inventoryController.getBatchById(searchBatch);
                        
                        if (foundBatch != null) {
                            System.out.println("\n-> ĐÃ TÌM THẤY:");
                            System.out.println(foundBatch.toString()); // Gọi hàm toString() ở Model để in ra
                        } else {
                            System.out.println("-> [Thông báo] Kho không có lô hàng nào khớp mã: " + searchBatch);
                        }

                    } else if (searchChoice == 2) {
                        // TÌM THEO SKU
                        String skuToSearch; 
                        while (true) {
                            skuToSearch = Inputter.inputStr("Nhập mã sản phẩm (SKU): ").toUpperCase();
                            if (!skuToSearch.matches(utilities.Pattern.SKU_PATTERN)) {
                                System.out.println("-> [Lỗi] Sai định dạng! Mã SKU phải dài 3-10 ký tự, gồm chữ in hoa, số và gạch ngang.");
                                continue;
                            }
                            break;
                        }

                        // Nhớ đổi tên biến truyền vào ở dòng này luôn nhé
                        java.util.List<InventoryItem> foundList = inventoryController.getBatchesBySku(skuToSearch);
                        
                        if (foundList.isEmpty()) {
                            System.out.println("-> [Thông báo] Kho không còn lô hàng nào của sản phẩm: " + skuToSearch);
                        } else {
                            System.out.println("\n-> TÌM THẤY " + foundList.size() + " LÔ HÀNG:");
                            // Duyệt danh sách kết quả và in ra từng dòng
                            for (InventoryItem item : foundList) {
                                System.out.println(item.toString());
                            }
                        }
                    } else {
                        System.out.println("-> [Lỗi] Lựa chọn không hợp lệ!");
                    }
                    break;
                case 5: break;
                case 6: break;
                case 7: return;
                default: System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }
}