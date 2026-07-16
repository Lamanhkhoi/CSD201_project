/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package structures;

import java.util.List;
import java.util.ArrayList;
import model.InventoryItem;

/**
 *
 * @author Admin
 */
public class SlotPriorityQueue {
    private static class Node {
 
        InventoryItem data;
        Node next;
 
        Node(InventoryItem data) {
            this.data = data;
            this.next = null;
        }
    }
 
    private Node head;
    private int size;
 
    public SlotPriorityQueue() {
        this.head = null;
        this.size = 0;
    }
 
    public int size() {
        return size;
    }
 
    public boolean isEmpty() {
        return size == 0;
    }
 
    // Xem lô hàng con có độ ưu tiên cao nhất (hết hạn sớm nhất) mà không xóa nó
    public InventoryItem peek() {
        if (isEmpty()) {
            return null;
        }
        return head.data;
    }
 
    /*
     Thêm lô hàng con mới vào đúng vị trí đã sắp xếp trong danh sách liên kết, 
     duyệt tìm vị trí chèn, giữ nguyên thứ tự FIFO nếu độ ưu tiên bằng nhau.
    */
    public void enqueue(InventoryItem slot) {
        Node newNode = new Node(slot);
 
        if (head == null || compareLots(slot, head.data) < 0) {
            newNode.next = head;
            head = newNode;
            size++;
            return;
        }
 
        Node current = head;
        while (current.next != null && compareLots(current.next.data, slot) <= 0) {
            current = current.next;
        }
 
        newNode.next = current.next;
        current.next = newNode;
        size++;
    }
 
    // Lấy ra và xóa bỏ lô hàng con có độ ưu tiên cao nhất (hết hạn sớm nhất)
    public InventoryItem dequeueMin() {
        if (isEmpty()) {
            return null;
        }
 
        InventoryItem minSlot = head.data;
        head = head.next;
        size--;
        return minSlot;
    }
 
    /*
     Gỡ ĐÚNG 1 lô hàng con ra khỏi hàng đợi theo lotId, không đụng tới các lô
     khác - dùng khi 1 lô về 0 sau khi xuất hàng, hoặc khi lô bị xóa mềm.
     Vì đây là hàm nội bộ của chính hàng đợi nên tự lo việc nối lại con trỏ
     'next', nơi gọi hàm (InventoryBatch/OrderController) không cần biết gì
     về Node bên trong.
     */
    public boolean removeById(String SlotId) {
        if (isEmpty() || SlotId == null) {
            return false;
        }
 
        // Trường hợp 1: lô cần gỡ chính là head
        if (head.data.getSlotId().equals(SlotId)) {
            head = head.next;
            size--;
            return true;
        }
 
        // Trường hợp 2: duyệt tìm Node đứng trước lô cần gỡ để nối tắt qua nó
        Node prev = head;
        Node current = head.next;
        while (current != null) {
            if (current.data.getSlotId().equals(SlotId)) {
                prev.next = current.next;
                size--;
                return true;
            }
            prev = current;
            current = current.next;
        }
 
        return false; // Không tìm thấy lotId trong hàng đợi này
    }
 
    // Trả về danh sách toàn bộ lô hàng con theo đúng thứ tự FEFO hiện có (không xóa khỏi hàng đợi)
    public List<InventoryItem> toList() {
        List<InventoryItem> result = new ArrayList<>();
        Node current = head;
        while (current != null) {
            result.add(current.data);
            current = current.next;
        }
        return result;
    }
 
    /*
     Logic so sánh độ ưu tiên viết cứng cho StockLot:
     - Tiêu chí 1: hạn sử dụng gần hơn thì ưu tiên hơn (FEFO).
     - Tiêu chí 2: cùng hạn sử dụng thì lô nhập kho trước ưu tiên hơn (FIFO).
     */
    private int compareLots(InventoryItem first, InventoryItem second) {
        int dateCompare = first.getExpiryDate().compareTo(second.getExpiryDate());
        if (dateCompare != 0) {
            return dateCompare;
        }
        return first.getReceiveDate().compareTo(second.getReceiveDate());
    }
 
    public void clear() {
        this.head = null;
        this.size = 0;
    }
}
