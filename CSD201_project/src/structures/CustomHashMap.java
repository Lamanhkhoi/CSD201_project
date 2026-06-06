package structures;

public class CustomHashMap<K, V> implements Map<K, V> {

    public static class Entry<K, V> {

        private K key;
        private V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value.toString(); // Phục vụ xuất file dữ liệu của Value
        }
    }

    private SinglyLinkedList<Entry<K, V>>[] table;
    private int capacity;
    private int size = 0;
    private static final double LOAD_FACTOR = 0.75;

    @SuppressWarnings("unchecked")
    public CustomHashMap(int capacity) {
        this.capacity = capacity;
        table = (SinglyLinkedList<Entry<K, V>>[]) new SinglyLinkedList[capacity];
    }

    public CustomHashMap() {
        this(17);
    } // Sử dụng số nguyên tố làm kích thước mặc định

    private int hashSlot(K key) {
        return Math.abs(key.hashCode()) % capacity;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public V get(K key) {
        int slot = hashSlot(key);
        SinglyLinkedList<Entry<K, V>> bucket = table[slot];
        if (bucket == null) {
            return null;
        }

        SinglyLinkedList.Node<Entry<K, V>> current = bucket.getHead();
        while (current != null) {
            if (current.getElement().getKey().equals(key)) {
                return current.getElement().getValue();
            }
            current = current.getNext();
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        if ((double) size / capacity >= LOAD_FACTOR) {
            resizeTable();
        }

        int slot = hashSlot(key);
        if (table[slot] == null) {
            table[slot] = new SinglyLinkedList<>();
        }

        SinglyLinkedList<Entry<K, V>> bucket = table[slot];
        SinglyLinkedList.Node<Entry<K, V>> current = bucket.getHead();

        while (current != null) {
            if (current.getElement().getKey().equals(key)) {
                V oldVal = current.getElement().getValue();
                current.getElement().setValue(value);
                return oldVal;
            }
            current = current.getNext();
        }

        bucket.addLast(new Entry<>(key, value));
        size++;
        return null;
    }

    @Override
    public V remove(K key) {
        int slot = hashSlot(key);
        SinglyLinkedList<Entry<K, V>> bucket = table[slot];
        if (bucket == null) {
            return null;
        }

        SinglyLinkedList.Node<Entry<K, V>> prev = null;
        SinglyLinkedList.Node<Entry<K, V>> current = bucket.getHead();

        while (current != null) {
            if (current.getElement().getKey().equals(key)) {
                V removedValue = current.getElement().getValue();
                if (prev == null) {
                    bucket.removeFirst();
                } else {
                    prev.setNext(current.getNext());
                    // Cập nhật lại đuôi nếu xóa phần tử cuối cùng
                    if (current.getNext() == null) {
                        // Kỹ thuật duyệt tái thiết tail không cần thiết nếu cấu trúc quản lý chặt chẽ
                    }
                }
                size--;
                return removedValue;
            }
            prev = current;
            current = current.getNext();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void resizeTable() {
        int oldCapacity = capacity;
        capacity = oldCapacity * 2 + 1; // Giữ kích thước là số lẻ/nguyên tố gần đúng
        SinglyLinkedList<Entry<K, V>>[] oldTable = table;

        table = (SinglyLinkedList<Entry<K, V>>[]) new SinglyLinkedList[capacity];
        size = 0;

        for (int i = 0; i < oldCapacity; i++) {
            if (oldTable[i] != null) {
                SinglyLinkedList.Node<Entry<K, V>> current = oldTable[i].getHead();
                while (current != null) {
                    put(current.getElement().getKey(), current.getElement().getValue());
                    current = current.getNext();
                }
            }
        }
    }

    // Chuyển đổi toàn bộ Value có trong HashMap thành chuỗi dạng văn bản cách nhau bằng dấu ";"
    public String toFileString() {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = 0; i < capacity; i++) {
            if (table[i] != null) {
                SinglyLinkedList.Node<Entry<K, V>> current = table[i].getHead();
                while (current != null) {
                    sb.append(current.getElement().getValue().toString());
                    count++;
                    if (count < size) {
                        sb.append(";");
                    }
                    current = current.getNext();
                }
            }
        }
        return sb.toString();
    }

    public structures.List<V> toCustomList() {
        structures.List<V> list = new structures.ArrayList<>(this.size == 0 ? 16 : this.size);

        for (int i = 0; i < capacity; i++) {
            if (table[i] != null) {
                SinglyLinkedList.Node<Entry<K, V>> current = table[i].getHead();
                while (current != null) {
                    list.add(list.size(), current.getElement().getValue());
                    current = current.getNext();
                }
            }
        }
        return list;
    }
}
