package structures;

import model.InventoryItem;

/*
 Hàng đợi ưu tiên (Min-Priority Queue) dành riêng cho InventoryItem,
 cài đặt theo dạng Node (Danh sách liên kết đơn có sắp xếp).

 Danh sách luôn được duy trì theo thứ tự tăng dần: lô hàng có hạn sử dụng
 gần nhất (ưu tiên cao nhất) luôn nằm ở đầu danh sách (head),
 nên dequeueMin chỉ cần gỡ head ra là lấy được lô cần xuất trước (FEFO).

 Logic so sánh được viết cứng bên trong class (không dùng generic/Comparator):
   - Tiêu chí 1: Ngày hết hạn (expiryDate) - ngày nào gần hơn thì đứng trước.
   - Tiêu chí 2: Nếu cùng ngày hết hạn, lô nào nhập kho (receiveDate) trước thì đứng trước.
 */
public class InventoryPriorityQueue {

    // Lớp Node nội bộ: mỗi Node chứa một InventoryItem và con trỏ trỏ tới Node kế tiếp
    private static class Node {

        InventoryItem data;
        Node next;

        Node(InventoryItem data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node head; // Con trỏ trỏ tới lô hàng có độ ưu tiên cao nhất (hết hạn sớm nhất)
    private int size;

    public InventoryPriorityQueue() {
        this.head = null;
        this.size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    // Xem lô hàng có độ ưu tiên cao nhất (đầu danh sách) mà không xóa nó
    public InventoryItem peek() {
        if (isEmpty()) {
            return null;
        }
        return head.data;
    }

    /*
     Thêm lô hàng mới vào đúng vị trí đã sắp xếp trong danh sách liên kết.
     Duyệt từ head cho đến khi gặp Node có độ ưu tiên thấp hơn rồi chèn vào trước Node đó.
     Nếu độ ưu tiên bằng nhau thì chèn vào sau để giữ thứ tự vào trước - ra trước (FIFO).
     */
    public void enqueue(InventoryItem item) {
        Node newNode = new Node(item);

        // Trường hợp 1: Danh sách rỗng hoặc lô mới ưu tiên hơn head -> chèn vào đầu
        if (head == null || compareItems(item, head.data) < 0) {
            newNode.next = head;
            head = newNode;
            size++;
            return;
        }

        // Trường hợp 2: Duyệt tìm vị trí chèn ở giữa hoặc cuối danh sách
        Node current = head;
        while (current.next != null && compareItems(current.next.data, item) <= 0) {
            current = current.next; // Tiếp tục đi tới khi Node kế tiếp vẫn ưu tiên hơn hoặc bằng lô mới
        }

        // Chèn newNode vào giữa current và current.next
        newNode.next = current.next;
        current.next = newNode;
        size++;
    }

    /*
     Lấy ra và xóa bỏ lô hàng có độ ưu tiên cao nhất (hết hạn sớm nhất).
     Vì danh sách luôn được sắp xếp sẵn nên chỉ cần gỡ Node đầu (head) ra là xong.
     */
    public InventoryItem dequeueMin() {
        if (isEmpty()) {
            return null;
        }

        InventoryItem minItem = head.data; // Lưu vết lô hàng ở đầu danh sách để trả về
        head = head.next;                  // Dời head sang Node kế tiếp, Node cũ sẽ bị thu hồi bộ nhớ
        size--;
        return minItem;
    }

    /*
     Logic so sánh độ ưu tiên được viết cứng cho InventoryItem:
     - Trả về số âm nếu 'first' ưu tiên hơn (đứng trước) 'second'.
     - Tiêu chí 1: hạn sử dụng gần hơn thì ưu tiên hơn (FEFO).
     - Tiêu chí 2: cùng hạn sử dụng thì lô nhập kho trước ưu tiên hơn (FIFO).
     */
    private int compareItems(InventoryItem first, InventoryItem second) {
        int dateCompare = first.getExpiryDate().compareTo(second.getExpiryDate());
        if (dateCompare != 0) {
            return dateCompare;
        }
        return first.getReceiveDate().compareTo(second.getReceiveDate());
    }

    // Xóa sạch tất cả các lô hàng trong hàng đợi
    public void clear() {
        this.head = null; // Ngắt con trỏ head, toàn bộ chuỗi Node sẽ được Garbage Collector dọn dẹp
        this.size = 0;
    }

    // Thêm toàn bộ các lô hàng từ một danh sách (Iterable) vào hàng đợi
    public void addAll(Iterable<InventoryItem> items) {
        if (items == null) {
            return;
        }
        for (InventoryItem item : items) {
            if (item != null) {
                enqueue(item);
            }
        }
    }
}
