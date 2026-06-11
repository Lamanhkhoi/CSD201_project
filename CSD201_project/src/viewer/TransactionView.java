/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package viewer;

import controller.TransactionController;
import model.Transaction;
import structures.SinglyLinkedList;
import utilities.Inputter;

/**
 *
 * @author LENOVO
 */
public class TransactionView {
    private final TransactionController transactionController;

    public TransactionView(TransactionController transactionController) {
        this.transactionController = transactionController;
    }
    
    public void displaySubMenu() {
        Object[] options = {
            "Xem toan bo lich su giao dich kho (View All)",
            "Tra cuu lich su theo Ma don / Ma SKU (Search)",
            "Luu toan bo du lieu giao dich vao file (Save File)",
            "Quay lại Menu chính (Back to Main Menu)"
        };

        while (true) {
            System.out.println("\n===== PHÂN HỆ QUẢN LÝ GIAO DỊCH =====");
            int choice = Menu.getChoice(options);

            switch (choice) {
                case 1:
                    // Gọi hàm hiển thị toàn bộ danh sách liên kết đơn trên RAM
                    transactionController.viewHistory();
                    break;
                case 2:
                   // Chức năng: Tra cứu lịch sử theo Mã đơn / Mã SKU
                    String keyword = Inputter.inputStr("Nhap Ma don hang (Order ID) hoac Ma san pham (SKU) can tim: ");
                    
                    if (!keyword.isEmpty()) {
                        System.out.println("\n--- KET QUA TRA CUU CHO TU KHOA: [" + keyword + "] ---");
                        
                        // Gọi hàm controller mới để nhận về 1 LIST danh sách kết quả
                        SinglyLinkedList<Transaction> results = transactionController.searchTransactions(keyword);
                        
                        // Duyệt danh sách kết quả để hiển thị ra màn hình
                        SinglyLinkedList.Node<Transaction> current = results.getHead();
                        
                        if (current == null) {
                            System.out.println("Khong tim thay bat ky lich su giao dich nao trung khop.");
                        } else {
                            int count = 0;
                            while (current != null) {
                                System.out.println(current.getElement()); // In từng dòng transaction ra
                                count++;
                                current = current.getNext();
                            }
                            System.out.println("-> Tim thay tong cong " + count + " giao dich phu hop.");
                        }
                        System.out.println("-------------------------------------------------------------");
                    } else {
                        System.out.println("=> Thong bao: Tu khoa tim kiem khong duoc de trong!");
                    }
                    break;
                case 3:
                    // CHỨC NĂNG: SỬ DỤNG STORAGEHANDLER THEO YÊU CẦU
                    System.out.println("\nChuong trinh dang chuan bi ghi file...");
                    
                    // 1. Lấy danh sách dữ liệu hiện tại trên RAM
                    SinglyLinkedList<Transaction> currentHistory = transactionController.getTransactionHistory();
                    
                    // 2. Ra lệnh cho storageHandler thực hiện quy trình askAndSave tự động
                    transactionController.getStorageHandler().askAndSave(currentHistory);
                    break;
                case 4:
                    System.out.println("Dang quay lai Menu chinh...");
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }
}
