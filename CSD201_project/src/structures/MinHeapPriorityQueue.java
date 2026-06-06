package structures;

import java.util.Comparator;

public class MinHeapPriorityQueue<E> {
    private ArrayList<E> heap;
    private final Comparator<E> comparator;

    public MinHeapPriorityQueue(Comparator<E> comparator) {
        this.heap = new ArrayList<>();
        this.comparator = comparator;
    }

    public int size() { return heap.size(); }
    public boolean isEmpty() { return heap.isEmpty(); }

    protected int parent(int j) { return (j - 1) / 2; }
    protected int left(int j) { return 2 * j + 1; }
    protected int right(int j) { return 2 * j + 2; }
    protected boolean hasLeft(int j) { return left(j) < heap.size(); }
    protected boolean hasRight(int j) { return right(j) < heap.size(); }

    protected void swap(int i, int j) {
        E temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    // Đẩy phần tử lên để cân bằng lại cây nhị phân: O(log n)
    protected void upheap(int j) {
        while (j > 0) {
            int p = parent(j);
            if (comparator.compare(heap.get(j), heap.get(p)) >= 0) break;
            swap(j, p);
            j = p;
        }
    }

    // Kéo phần tử xuống để cân bằng lại cây nhị phân: O(log n)
    protected void downheap(int j) {
        while (hasLeft(j)) {
            int leftIndex = left(j);
            int smallChildIndex = leftIndex;
            if (hasRight(j)) {
                int rightIndex = right(j);
                if (comparator.compare(heap.get(leftIndex), heap.get(rightIndex)) > 0) {
                    smallChildIndex = rightIndex;
                }
            }
            if (comparator.compare(heap.get(smallChildIndex), heap.get(j)) >= 0) break;
            swap(j, smallChildIndex);
            j = smallChildIndex;
        }
    }

    // Thêm phần tử mới vào hàng đợi ưu tiên: O(log n)
    public void enqueue(E element) {
        heap.add(heap.size(), element); // Thêm vào vị trí cuối mảng
        upheap(heap.size() - 1);       // Tiến hành vun nền lên
    }

    // Lấy ra và xóa phần tử có độ ưu tiên cao nhất (nhỏ nhất): O(log n)
    public E dequeueMin() {
        if (heap.isEmpty()) return null;
        E answer = heap.get(0);
        swap(0, heap.size() - 1);       // Tráo phần tử đầu với phần tử cuối mảng
        heap.remove(heap.size() - 1);   // Xóa phần tử cuối
        downheap(0);                    // Tiến hành vun nền xuống
        return answer;
    }

    public E peekMin() {
        if (heap.isEmpty()) return null;
        return heap.get(0);
    }

    // Xuất chuỗi các phần tử cách nhau bằng dấu ";"
    public String toFileString() {
        return heap.toFileString();
    }
}