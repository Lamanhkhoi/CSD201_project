package test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * THỬ NGHIỆM VÀ SO SÁNH HIỆU NĂNG ĐỘC LẬP (PERFORMANCE BENCHMARK)
 * 
 * So sánh 2 Cấu trúc dữ liệu cho Hàng đợi ưu tiên (Priority Queue):
 *  1. Sorted Singly Linked List (Cấu trúc hiện tại trong đồ án - O(N) Enqueue, O(1) Dequeue)
 *  2. Binary Min-Heap (Cấu trúc đối chứng - O(log N) Enqueue, O(log N) Dequeue)
 * 
 * Chạy trên các quy mô dữ liệu: N = 100, N = 500, N = 1,000 (và N = 10,000 mở rộng).
 * File này chạy độc lập hoàn toàn, KHÔNG ảnh hưởng tới mã nguồn chính.
 */
public class PerformanceBenchmark {

    // Mock Item đại diện cho Lô hàng kho để đo hiệu năng
    static class DummyBatchItem {
        String id;
        int quantity;
        LocalDate expiryDate;
        LocalDate receiveDate;

        public DummyBatchItem(String id, int quantity, LocalDate expiryDate, LocalDate receiveDate) {
            this.id = id;
            this.quantity = quantity;
            this.expiryDate = expiryDate;
            this.receiveDate = receiveDate;
        }

        // So sánh tiêu chí FEFO: Hạn dùng trước -> Ngày nhập trước
        public int compareTo(DummyBatchItem other) {
            int dateCompare = this.expiryDate.compareTo(other.expiryDate);
            if (dateCompare != 0) return dateCompare;
            return this.receiveDate.compareTo(other.receiveDate);
        }
    }

    // =========================================================================
    // CTDL 1: SORTED SINGLY LINKED LIST (Cấu trúc hiện tại của dự án)
    // =========================================================================
    static class LinkedListPriorityQueue {
        private static class Node {
            DummyBatchItem data;
            Node next;
            Node(DummyBatchItem data) { this.data = data; }
        }

        private Node head;
        private int size;

        public LinkedListPriorityQueue() {
            this.head = null;
            this.size = 0;
        }

        // Enqueue - O(N): Duyệt tìm vị trí chèn đúng thứ tự FEFO
        public void enqueue(DummyBatchItem item) {
            Node newNode = new Node(item);
            if (head == null || item.compareTo(head.data) < 0) {
                newNode.next = head;
                head = newNode;
                size++;
                return;
            }

            Node current = head;
            while (current.next != null && current.next.data.compareTo(item) <= 0) {
                current = current.next;
            }
            newNode.next = current.next;
            current.next = newNode;
            size++;
        }

        // DequeueMin - O(1): Lấy lô cận date ở head ra ngay lập tức
        public DummyBatchItem dequeueMin() {
            if (isEmpty()) return null;
            DummyBatchItem item = head.data;
            head = head.next;
            size--;
            return item;
        }

        public boolean isEmpty() { return size == 0; }
        public int size() { return size; }
    }

    // =========================================================================
    // CTDL 2: BINARY MIN-HEAP (Cấu trúc đối chứng trên mảng)
    // =========================================================================
    static class BinaryMinHeapPriorityQueue {
        private DummyBatchItem[] heap;
        private int size;

        public BinaryMinHeapPriorityQueue(int capacity) {
            this.heap = new DummyBatchItem[capacity + 10];
            this.size = 0;
        }

        // Enqueue - O(log N): Thêm vào cuối và Heapify Up
        public void enqueue(DummyBatchItem item) {
            ensureCapacity();
            size++;
            heap[size] = item;
            swim(size);
        }

        // DequeueMin - O(log N): Đưa phần tử cuối lên root và Heapify Down
        public DummyBatchItem dequeueMin() {
            if (isEmpty()) return null;
            DummyBatchItem min = heap[1];
            swap(1, size);
            heap[size] = null;
            size--;
            sink(1);
            return min;
        }

        private void swim(int k) {
            while (k > 1 && heap[k].compareTo(heap[k / 2]) < 0) {
                swap(k, k / 2);
                k = k / 2;
            }
        }

        private void sink(int k) {
            while (2 * k <= size) {
                int j = 2 * k;
                if (j < size && heap[j + 1].compareTo(heap[j]) < 0) j++;
                if (heap[k].compareTo(heap[j]) <= 0) break;
                swap(k, j);
                k = j;
            }
        }

        private void swap(int i, int j) {
            DummyBatchItem temp = heap[i];
            heap[i] = heap[j];
            heap[j] = temp;
        }

        private void ensureCapacity() {
            if (size >= heap.length - 1) {
                DummyBatchItem[] old = heap;
                heap = new DummyBatchItem[old.length * 2];
                System.arraycopy(old, 0, heap, 0, old.length);
            }
        }

        public boolean isEmpty() { return size == 0; }
        public int size() { return size; }
    }

    // =========================================================================
    // HAM MAIN CHẠY THỬ NGHIỆM
    // =========================================================================
    public static void main(String[] args) {
        System.out.println("=========================================================================================");
        System.out.println("              CHƯƠNG TRÌNH ĐO VÀ SO SÁNH HIỆU NĂNG THỰC NGHIỆM (CSD201)");
        System.out.println("  So sánh: Sorted Singly Linked List (Hiện tại) VS Binary Min-Heap (Đối chứng)");
        System.out.println("=========================================================================================\n");

        int[] sampleSizes = {100, 500, 1000, 10000};

        // 1. JVM Warm-up (Khởi động JVM JIT compiler để đo chính xác)
        System.out.println("[+] Đang chạy Warm-up JVM...");
        runBenchmarkSuite(50, false);
        System.out.println("[+] Warm-up hoàn tất! Bắt đầu đo chính thức.\n");

        // 2. Chạy thử nghiệm chính thức trên từng quy mô dữ liệu
        for (int N : sampleSizes) {
            System.out.println("=========================================================================================");
            System.out.printf(" >>> THỬ NGHIỆM VỚI QUY MÔ DỮ LIỆU N = %,d LÔ HÀNG <<<\n", N);
            System.out.println("=========================================================================================");
            runBenchmarkSuite(N, true);
            System.out.println();
        }
    }

    private static void runBenchmarkSuite(int N, boolean printResult) {
        List<DummyBatchItem> dataset = generateRandomDataset(N);

        // ---------------------------------------------------------------------
        // KỊCH BẢN 1: BULK ENQUEUE (Nhập hàng loạt N phần tử vào kho)
        // ---------------------------------------------------------------------
        LinkedListPriorityQueue llQueue1 = new LinkedListPriorityQueue();
        long startLL_Insert = System.nanoTime();
        for (DummyBatchItem item : dataset) {
            llQueue1.enqueue(item);
        }
        long endLL_Insert = System.nanoTime();
        double timeLL_Insert_Ms = (endLL_Insert - startLL_Insert) / 1_000_000.0;

        BinaryMinHeapPriorityQueue heapQueue1 = new BinaryMinHeapPriorityQueue(N);
        long startHeap_Insert = System.nanoTime();
        for (DummyBatchItem item : dataset) {
            heapQueue1.enqueue(item);
        }
        long endHeap_Insert = System.nanoTime();
        double timeHeap_Insert_Ms = (endHeap_Insert - startHeap_Insert) / 1_000_000.0;

        // ---------------------------------------------------------------------
        // KỊCH BẢN 2: BULK DEQUEUE (Xuất kho hàng loạt N phần tử FEFO)
        // ---------------------------------------------------------------------
        long startLL_Delete = System.nanoTime();
        while (!llQueue1.isEmpty()) {
            llQueue1.dequeueMin();
        }
        long endLL_Delete = System.nanoTime();
        double timeLL_Delete_Ms = (endLL_Delete - startLL_Delete) / 1_000_000.0;

        long startHeap_Delete = System.nanoTime();
        while (!heapQueue1.isEmpty()) {
            heapQueue1.dequeueMin();
        }
        long endHeap_Delete = System.nanoTime();
        double timeHeap_Delete_Ms = (endHeap_Delete - startHeap_Delete) / 1_000_000.0;

        // ---------------------------------------------------------------------
        // KỊCH BẢN 3: MIXED WORKLOAD (50% Nhập / 50% Xuất kho thực tế)
        // ---------------------------------------------------------------------
        LinkedListPriorityQueue llQueue2 = new LinkedListPriorityQueue();
        BinaryMinHeapPriorityQueue heapQueue2 = new BinaryMinHeapPriorityQueue(N);

        long startLL_Mixed = System.nanoTime();
        for (int i = 0; i < dataset.size(); i++) {
            llQueue2.enqueue(dataset.get(i));
            if (i % 2 == 1) {
                llQueue2.dequeueMin();
            }
        }
        long endLL_Mixed = System.nanoTime();
        double timeLL_Mixed_Ms = (endLL_Mixed - startLL_Mixed) / 1_000_000.0;

        long startHeap_Mixed = System.nanoTime();
        for (int i = 0; i < dataset.size(); i++) {
            heapQueue2.enqueue(dataset.get(i));
            if (i % 2 == 1) {
                heapQueue2.dequeueMin();
            }
        }
        long endHeap_Mixed = System.nanoTime();
        double timeHeap_Mixed_Ms = (endHeap_Mixed - startHeap_Mixed) / 1_000_000.0;

        // IN KẾT QUẢ
        if (printResult) {
            System.out.printf("| %-30s | %-22s | %-22s | %-12s |\n", "Kịch bản thử nghiệm", "Sorted Linked List", "Binary Min-Heap", "Kết luận");
            System.out.println("|--------------------------------|------------------------|------------------------|--------------|");
            
            printRow("1. Nhập kho hàng loạt (Enqueue)", timeLL_Insert_Ms, timeHeap_Insert_Ms);
            printRow("2. Xuất kho FEFO hàng loạt (Dequeue)", timeLL_Delete_Ms, timeHeap_Delete_Ms);
            printRow("3. Phối hợp Nhập & Xuất kho (Mixed)", timeLL_Mixed_Ms, timeHeap_Mixed_Ms);
        }
    }

    private static void printRow(String scenarioName, double timeLLMs, double timeHeapMs) {
        String winner;
        if (Math.abs(timeLLMs - timeHeapMs) < 0.005) {
            winner = "Ngang ngửa";
        } else if (timeLLMs < timeHeapMs) {
            winner = "Linked List";
        } else {
            winner = "Min-Heap";
        }

        System.out.printf("| %-30s | %18.4f ms | %18.4f ms | %-12s |\n", scenarioName, timeLLMs, timeHeapMs, winner);
    }

    // Sinh dữ liệu ngẫu nhiên giả lập lô hàng kho
    private static List<DummyBatchItem> generateRandomDataset(int size) {
        List<DummyBatchItem> list = new ArrayList<>(size);
        Random rand = new Random(42); // Seed cố định để dữ liệu thử nghiệm công bằng
        LocalDate baseDate = LocalDate.now();

        for (int i = 1; i <= size; i++) {
            String id = String.format("SLOT-%05d", i);
            int qty = rand.nextInt(500) + 1;
            int daysExpiry = rand.nextInt(365) + 1;
            int daysReceive = rand.nextInt(30);

            LocalDate expiryDate = baseDate.plusDays(daysExpiry);
            LocalDate receiveDate = baseDate.minusDays(daysReceive);

            list.add(new DummyBatchItem(id, qty, expiryDate, receiveDate));
        }
        return list;
    }
}
