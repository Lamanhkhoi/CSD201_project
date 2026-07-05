package structures;

import model.Order;

/*
 Hàng đợi ưu tiên (Min-Priority Queue) dành riêng cho Order,
 cài đặt theo dạng Node (Danh sách liên kết đơn có sắp xếp).

 Danh sách luôn được duy trì theo thứ tự tăng dần: đơn hàng có ngày giao
 dự kiến (expectedDate) sớm nhất luôn nằm ở đầu danh sách (head),
 nên dequeueMin chỉ cần gỡ head ra là lấy được đơn cần xử lý trước.

 Logic so sánh được viết cứng bên trong class (không dùng generic/Comparator):
   - Đơn nào có expectedDate sớm hơn thì được ưu tiên xử lý trước.
   - Nếu trùng expectedDate thì giữ thứ tự vào trước - ra trước (FIFO).
 */
public class OrderPriorityQueue {

    // Lớp Node nội bộ: mỗi Node chứa một Order và con trỏ trỏ tới Node kế tiếp
    private static class Node {

        Order data;
        Node next;

        Node(Order data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node head; // Con trỏ trỏ tới đơn hàng có độ ưu tiên cao nhất (ngày giao sớm nhất)
    private int size;

    public OrderPriorityQueue() {
        this.head = null;
        this.size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    // Xem đơn hàng có độ ưu tiên cao nhất (đầu danh sách) mà không xóa nó
    public Order peek() {
        if (isEmpty()) {
            return null;
        }
        return head.data;
    }

    /*
     Thêm đơn hàng mới vào đúng vị trí đã sắp xếp trong danh sách liên kết.
     Duyệt từ head cho đến khi gặp Node có độ ưu tiên thấp hơn rồi chèn vào trước Node đó.
     Nếu độ ưu tiên bằng nhau thì chèn vào sau để giữ thứ tự vào trước - ra trước (FIFO).
     */
    public void enqueue(Order order) {
        Node newNode = new Node(order);

        // Trường hợp 1: Danh sách rỗng hoặc đơn mới ưu tiên hơn head -> chèn vào đầu
        if (head == null || compareOrders(order, head.data) < 0) {
            newNode.next = head;
            head = newNode;
            size++;
            return;
        }

        // Trường hợp 2: Duyệt tìm vị trí chèn ở giữa hoặc cuối danh sách
        Node current = head;
        while (current.next != null && compareOrders(current.next.data, order) <= 0) {
            current = current.next; // Tiếp tục đi tới khi Node kế tiếp vẫn ưu tiên hơn hoặc bằng đơn mới
        }

        // Chèn newNode vào giữa current và current.next
        newNode.next = current.next;
        current.next = newNode;
        size++;
    }

    /*
     Lấy ra và xóa bỏ đơn hàng có độ ưu tiên cao nhất (ngày giao sớm nhất).
     Vì danh sách luôn được sắp xếp sẵn nên chỉ cần gỡ Node đầu (head) ra là xong.
     */
    public Order dequeueMin() {
        if (isEmpty()) {
            return null;
        }

        Order minOrder = head.data; // Lưu vết đơn hàng ở đầu danh sách để trả về
        head = head.next;           // Dời head sang Node kế tiếp, Node cũ sẽ bị thu hồi bộ nhớ
        size--;
        return minOrder;
    }

    /*
     Logic so sánh độ ưu tiên được viết cứng cho Order:
     - Trả về số âm nếu 'first' ưu tiên hơn (đứng trước) 'second'.
     - Đơn có ngày giao dự kiến (expectedDate) sớm hơn thì ưu tiên hơn.
     */
    private int compareOrders(Order first, Order second) {
        return first.getExpectedDate().compareTo(second.getExpectedDate());
    }

    // Xóa sạch tất cả các đơn hàng trong hàng đợi
    public void clear() {
        this.head = null; // Ngắt con trỏ head, toàn bộ chuỗi Node sẽ được Garbage Collector dọn dẹp
        this.size = 0;
    }

    // Thêm toàn bộ các đơn hàng từ một danh sách (Iterable) vào hàng đợi
    public void addAll(Iterable<Order> orders) {
        if (orders == null) {
            return;
        }
        for (Order order : orders) {
            if (order != null) {
                enqueue(order);
            }
        }
    }
}
