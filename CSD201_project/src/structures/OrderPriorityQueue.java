package structures;

import model.Order;

public class OrderPriorityQueue {

    private static class Node {

        Order data;
        Node next;

        Node(Order data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node head;
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

    public Order peek() {
        if (isEmpty()) {
            return null;
        }
        return head.data;
    }

    public void enqueue(Order order) {
        Node newNode = new Node(order);
        if (head == null || compareOrders(order, head.data) < 0) {
            newNode.next = head;
            head = newNode;
            size++;
            return;
        }

        Node current = head;
        while (current.next != null && compareOrders(current.next.data, order) <= 0) {
            current = current.next; 
        }

        newNode.next = current.next;
        current.next = newNode;
        size++;
    }

    public Order dequeueMin() {
        if (isEmpty()) {
            return null;
        }

        Order minOrder = head.data; 
        head = head.next;           
        size--;
        return minOrder;
    }

    private int compareOrders(Order first, Order second) {
        return first.getExpectedDate().compareTo(second.getExpectedDate());
    }

    public void clear() {
        this.head = null;
        this.size = 0;
    }

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
