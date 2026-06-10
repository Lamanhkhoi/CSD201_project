package structures;

import java.util.Comparator;

public class PriorityQueue<E> {

    private E[] heap;             
    private int size;             
    private int capacity;         
    private Comparator<E> comparator; 


    public PriorityQueue(Comparator<E> comparator) {
        this.capacity = 16; 
        this.heap = (E[]) new Object[capacity]; 
        this.size = 0;
        this.comparator = comparator;
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

    public E peek() {
        if (isEmpty()) {
            return null;
        }
        return heap[0];
    }

    public void enqueue(E element) {
        if (size == capacity) {
            resize(); // Tự động nhân đôi dung lượng mảng nếu bị tràn
        }
        heap[size] = element; // Đặt phần tử mới vào vị trí trống cuối cùng của mảng
        upHeap(size);         // Thực hiện thuật toán sàng lên (Sift-up/Up-heap)
        size++;
    }

    /*
     Lấy ra và xóa bỏ phần tử có độ ưu tiên cao nhất (gốc Heap index 0). Tự
     động đưa phần tử cuối lên và sàng xuống để tái lập đặc tính Min-Heap.
    */
    public E dequeueMin() {
        if (isEmpty()) {
            return null;
        }

        E minElement = heap[0];      // Lưu vết phần tử nhỏ nhất ở gốc để trả về
        heap[0] = heap[size - 1];     // Đưa phần tử cuối cùng của mảng lên thay thế ở gốc
        heap[size - 1] = null;       // Giải phóng ô nhớ cuối
        size--;

        if (size > 0) {
            downHeap(0);             // Thực hiện thuật toán sàng xuống (Sift-down/Down-heap) từ gốc
        }
        return minElement;
    }

    /*
     Thuật toán sàng lên (Up-Heap / Sift-Up): Di chuyển một nút từ dưới lên
     trên cho đến khi nó lớn hơn hoặc bằng nút cha của nó.
    */
    private void upHeap(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2; // Công thức tìm vị trí nút cha trong mảng phẳng

            // Nếu nút hiện tại lớn hơn hoặc bằng nút cha -> Đã đạt cấu trúc Min-Heap, dừng lại
            if (comparator.compare(heap[index], heap[parentIndex]) >= 0) {
                break;
            }

            swap(index, parentIndex); // Hoán vị với nút cha
            index = parentIndex;      // Tiếp tục kiểm tra ngược lên từ vị trí nút cha cũ
        }
    }

    /*
     Thuật toán sàng xuống (Down-Heap / Sift-Down): Di chuyển một nút từ gốc
     xuống dưới cho đến khi nó nhỏ hơn hoặc bằng cả 2 nút con của nó.
    */
    private void downHeap(int index) {
        while (2 * index + 1 < size) { // Vòng lặp chạy khi nút hiện tại vẫn còn ít nhất 1 nút con trái
            int leftChild = 2 * index + 1;
            int rightChild = leftChild + 1;
            int smallestChild = leftChild; // Giả định ban đầu con trái là nút nhỏ nhất

            // Nếu tồn tại con phải và con phải có độ ưu tiên cao hơn (nhỏ hơn) con trái
            if (rightChild < size && comparator.compare(heap[rightChild], heap[leftChild]) < 0) {
                smallestChild = rightChild; // Cập nhật nút nhỏ nhất là con phải
            }

            // Nếu nút cha hiện tại đã nhỏ hơn hoặc bằng nút con nhỏ nhất -> Đạt cấu trúc, dừng lại
            if (comparator.compare(heap[index], heap[smallestChild]) <= 0) {
                break;
            }

            swap(index, smallestChild); // Hoán vị cha với nút con nhỏ nhất để duy trì đỉnh tối thiểu
            index = smallestChild;      // Tiếp tục hạ xuống kiểm tra ở tầng dưới
        }
    }

    private void swap(int i, int j) {
        E temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    private void resize() {
        capacity *= 2;
        E[] newHeap = (E[]) new Object[capacity];
        System.arraycopy(heap, 0, newHeap, 0, size);
        this.heap = newHeap;
    }
}
