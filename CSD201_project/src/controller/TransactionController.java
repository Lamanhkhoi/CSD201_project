/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import fileio.IFileReadWrite;
import java.util.Scanner;
import utilities.StorageHandler;
import model.Transaction;
import structures.SinglyLinkedList;

/**
 *
 * @author LENOVO
 */
public class TransactionController {
    private SinglyLinkedList<Transaction> transactionHistory;
    private StorageHandler<Transaction, SinglyLinkedList<Transaction>> storageHandler;
    private IFileReadWrite<Transaction, SinglyLinkedList<Transaction>> fileHandler;
    private final Scanner scanner;
    public TransactionController(IFileReadWrite<Transaction, SinglyLinkedList<Transaction>> fileHandler) {
        this.fileHandler = fileHandler;
        this.transactionHistory = new SinglyLinkedList<>(); // Khởi tạo danh sách rỗng ban đầu
        this.scanner = new Scanner(System.in);
        this.storageHandler = new StorageHandler<>(fileHandler);
    }

    public StorageHandler<Transaction, SinglyLinkedList<Transaction>> getStorageHandler() {
        return storageHandler;
    }

    public SinglyLinkedList<Transaction> getTransactionHistory() {
        return transactionHistory;
    }
    
    
    /**
     * CHỨC NĂNG: Đọc file lưu trữ nền tảng và đồng bộ hóa vào RAM của cấu trúc dữ liệu
     */
    public void loadInitialData() {
        try {
            System.out.println("System: Loading transaction logs from database file...");
            SinglyLinkedList<Transaction> loadedData = fileHandler.read();
            
            if (loadedData != null && loadedData.getHead() != null) {
                this.transactionHistory = loadedData;
                System.out.println("System: Transaction logs successfully synchronized into RAM.");
            } else {
                System.out.println("System: No active transaction log records found. Ready for deployment.");
            }
        } catch (Exception e) {
            System.out.println("System Error: Failed to restore transaction history. " + e.getMessage());
            // Khởi tạo danh sách trống nếu có lỗi xảy ra để tránh crash NullPointerException
            this.transactionHistory = new SinglyLinkedList<>();
        }
    }
    
    
    
     public void addTransaction(Transaction tx) {
        transactionHistory.addLast(tx);
    }

    public void viewHistory() {
        SinglyLinkedList.Node<Transaction> current = transactionHistory.getHead();
        while (current != null) {
            System.out.println(current.getElement());
            current = current.getNext();
        }
    }
    
    public SinglyLinkedList<Transaction> searchTransactions(String keyword) {
        // Tạo một danh sách liên kết đơn mới để chứa các kết quả tìm thấy
        SinglyLinkedList<Transaction> resultList = new SinglyLinkedList<>();
        
        // Con trỏ duyệt từ đầu danh sách lịch sử gốc trên RAM
        SinglyLinkedList.Node<Transaction> current = transactionHistory.getHead();
        
        while (current != null) {
            Transaction tx = current.getElement();
            
            // Nếu SKU hoặc OrderID trùng với từ khóa (không phân biệt chữ hoa thường)
            if (tx.getSku().equalsIgnoreCase(keyword) || tx.getOrderId().equalsIgnoreCase(keyword)) {
                resultList.addLast(tx); // Thêm giao dịch trùng khớp vào danh sách kết quả
            }
            
            current = current.getNext(); // Chuyển sang nút tiếp theo
        }
        
        return resultList; // Trả về danh sách kết quả (nếu không tìm thấy, danh sách sẽ trống)
    }
    
}
