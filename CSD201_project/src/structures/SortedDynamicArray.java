package structures;

import java.util.Comparator;

public class SortedDynamicArray<E> extends ArrayList<E> {
    private final Comparator<E> comparator;

    public SortedDynamicArray(Comparator<E> comparator) {
        super();
        this.comparator = comparator;
    }

    public SortedDynamicArray(int capacity, Comparator<E> comparator) {
        super(capacity);
        this.comparator = comparator;
    }

    // Hàm chèn phần tử mới vào mảng luôn đảm bảo tính chất sắp xếp
    public void insertSorted(E item) {
        int low = 0;
        int high = size() - 1;
        int insertIndex = size(); 

        // Thuật toán Tìm kiếm nhị phân (Binary Search) tìm vị trí chèn lý tưởng: O(log n)
        while (low <= high) {
            int mid = (low + high) / 2;
            int compareResult = comparator.compare(get(mid), item);

            if (compareResult > 0) {
                insertIndex = mid; 
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        // Gọi hàm add từ lớp cha để thực hiện dịch chuyển mảng và chèn: O(n)
        this.add(insertIndex, item);
    }
    
    // Tìm kiếm nhị phân một phần tử mẫu dựa trên Comparator
    public int binarySearch(E target) {
        int low = 0;
        int high = size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            int comp = comparator.compare(get(mid), target);
            if (comp == 0) return mid;
            else if (comp < 0) low = mid + 1;
            else high = mid - 1;
        }
        return -1; // Không tìm thấy
    }
}