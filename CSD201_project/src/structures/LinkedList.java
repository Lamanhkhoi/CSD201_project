package structures;

public class LinkedList<Order> {

    private static class Node<Order> {

        private Order element;
        private Node<Order> next;

        public Node(Order element, Node<Order> next) {
            this.element = element;
            this.next = next;
        }

        public Order getElement() {
            return element;
        }

        public Node<Order> getNext() {
            return next;
        }

        public void setNext(Node<Order> next) {
            this.next = next;
        }
    }

    private Node<Order> head = null;
    private Node<Order> tail = null;
    private int size = 0;

    public LinkedList() {
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Order first() {
        if (isEmpty()) {
            return null;
        }
        return head.getElement();
    }

    public Order last() {
        if (isEmpty()) {
            return null;
        }
        return tail.getElement();
    }

    public void addFirst(Order element) {
        head = new Node<>(element, head);
        if (size == 0) {
            tail = head;
        }
        size++;
    }

    public void addLast(Order element) {
        Node<Order> newest = new Node<>(element, null);
        if (isEmpty()) {
            head = newest;
        } else {
            tail.setNext(newest);
        }
        tail = newest;
        size++;
    }

    public Order removeFirst() {
        if (isEmpty()) {
            return null;
        }
        Order answer = head.getElement();
        head = head.getNext();
        size--;
        if (size == 0) {
            tail = null;
        }
        return answer;
    }

    public Order get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of bound: " + index);
        }
        Node<Order> current = head;
        for (int i = 0; i < index; i++) {
            current = current.getNext();
        }
        return current.getElement();
    }
}
