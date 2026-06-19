package controller;

import model.Transaction;
import structures.SinglyLinkedList;

public class TransactionController {

    private SinglyLinkedList<Transaction> transactionHistory;

    public TransactionController(SinglyLinkedList<Transaction> transactionHistory) {
        this.transactionHistory = transactionHistory;
    }

    public SinglyLinkedList<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    public void addTransaction(Transaction tx) {
        if (tx != null) {
            transactionHistory.addLast(tx);
        }
    }

    public void viewHistory() {
        SinglyLinkedList.Node<Transaction> current = transactionHistory.getHead();
        if (current == null) {
            System.out.println("-> [Thông báo] Lịch sử giao dịch hiện đang trống.");
            return;
        }
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
