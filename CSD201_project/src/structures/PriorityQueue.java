package structures;

import java.util.Comparator;

/*
 Hàng đợi ưu tiên (Min-Priority Queue) cài đặt theo dạng Node (Danh sách liên kết đơn có sắp xếp).
 Danh sách luôn được duy trì theo thứ tự tăng dần: phần tử có độ ưu tiên cao nhất
 (nhỏ nhất) luôn nằm ở đầu danh sách (head), nên dequeueMin chỉ cần gỡ head ra.
 */
public class PriorityQueue<E> {

    // Lớp Node nội bộ: mỗi Node chứa dữ liệu và con trỏ trỏ tới Node kế tiếp
    private static class Node<E> {

        E data;
        Node<E> next;

        Node(E data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<E> head; // Con trỏ trỏ tới phần tử có độ ưu tiên cao nhất (nhỏ nhất)
    private int size;
    private Comparator<E> comparator;

    public PriorityQueue(Comparator<E> comparator) {
        this.head = null;
        this.size = 0;
        this.comparator = comparator;
    }

    public PriorityQueue() {
        this.head = null;
        this.size = 0;
        this.comparator = null;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Comparator<E> getComparator() {
        return this.comparator;
    }

    // Xem phần tử có độ ưu tiên cao nhất (đầu danh sách) mà không xóa nó
    public E peek() {
        if (isEmpty()) {
            return null;
        }
        return head.data;
    }

    /*
     Thêm phần tử mới vào đúng vị trí đã sắp xếp trong danh sách liên kết.
     Duyệt từ head cho đến khi gặp Node có độ ưu tiên thấp hơn (lớn hơn) rồi chèn vào trước Node đó.
     Nếu độ ưu tiên bằng nhau thì chèn vào sau để giữ thứ tự vào trước - ra trước (FIFO).
     */
    public void enqueue(E element) {
        Node<E> newNode = new Node<>(element);

        // Trường hợp 1: Danh sách rỗng hoặc phần tử mới nhỏ hơn head -> chèn vào đầu
        if (head == null || compareElements(element, head.data) < 0) {
            newNode.next = head;
            head = newNode;
            size++;
            return;
        }

        // Trường hợp 2: Duyệt tìm vị trí chèn ở giữa hoặc cuối danh sách
        Node<E> current = head;
        while (current.next != null && compareElements(current.next.data, element) <= 0) {
            current = current.next; // Tiếp tục đi tới khi Node kế tiếp vẫn nhỏ hơn hoặc bằng phần tử mới
        }

        // Chèn newNode vào giữa current và current.next
        newNode.next = current.next;
        current.next = newNode;
        size++;
    }

    /*
     Lấy ra và xóa bỏ phần tử có độ ưu tiên cao nhất. Vì danh sách luôn được
     sắp xếp sẵn nên chỉ cần gỡ Node đầu (head) ra là xong.
     */
    public E dequeueMin() {
        if (isEmpty()) {
            return null;
        }

        E minElement = head.data; // Lưu vết phần tử nhỏ nhất ở đầu danh sách để trả về
        head = head.next;         // Dời con trỏ head sang Node kế tiếp, Node cũ sẽ bị thu hồi bộ nhớ
        size--;
        return minElement;
    }

    // Tự động quyết định cách so sánh dựa trên việc có Comparator hay không
    private int compareElements(E first, E second) {
        if (this.comparator != null) {
            return this.comparator.compare(first, second);
        }
        // Nếu không có Comparator, ép kiểu phần tử về Comparable giống hệt java.util
        return ((Comparable<? super E>) first).compareTo(second);
    }

    // Xóa sạch tất cả các phần tử trong hàng đợi
    public void clear() {
        this.head = null; // Ngắt con trỏ head, toàn bộ chuỗi Node sẽ được Garbage Collector dọn dẹp
        this.size = 0;
    }

    // Thêm toàn bộ các phần tử từ một danh sách (Iterable) vào hàng đợi
    public void addAll(Iterable<? extends E> elements) {
        if (elements == null) {
            return;
        }
        for (E element : elements) {
            if (element != null) {
                enqueue(element);
            }
        }
    }
}