package structures;

import model.InventoryItem;

public class InventoryPriorityQueue {
    private static class Node {

        InventoryItem data;
        Node next;

        Node(InventoryItem data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node head;
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

    public InventoryItem peek() {
        if (isEmpty()) {
            return null;
        }
        return head.data;
    }

    public void enqueue(InventoryItem item) {
        Node newNode = new Node(item);

        if (head == null || compareItems(item, head.data) < 0) {
            newNode.next = head;
            head = newNode;
            size++;
            return;
        }

        Node current = head;
        while (current.next != null && compareItems(current.next.data, item) <= 0) {
            current = current.next;
        }

        newNode.next = current.next;
        current.next = newNode;
        size++;
    }

    public InventoryItem dequeueMin() {
        if (isEmpty()) {
            return null;
        }

        InventoryItem minItem = head.data; 
        head = head.next;                  
        size--;
        return minItem;
    }

    private int compareItems(InventoryItem first, InventoryItem second) {
        int dateCompare = first.getExpiryDate().compareTo(second.getExpiryDate());
        if (dateCompare != 0) {
            return dateCompare;
        }
        return first.getReceiveDate().compareTo(second.getReceiveDate());
    }

    public void clear() {
        this.head = null; 
        this.size = 0;
    }

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
