/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package structures;

import java.util.List;
import java.util.ArrayList;
import model.InventoryItem;

/**
 *
 * @author Admin
 */
public class SlotPriorityQueue {
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
 
    public SlotPriorityQueue() {
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
 
    public void enqueue(InventoryItem slot) {
        Node newNode = new Node(slot);
 
        if (head == null || compareLots(slot, head.data) < 0) {
            newNode.next = head;
            head = newNode;
            size++;
            return;
        }
 
        Node current = head;
        while (current.next != null && compareLots(current.next.data, slot) <= 0) {
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
 
        InventoryItem minSlot = head.data;
        head = head.next;
        size--;
        return minSlot;
    }

    public boolean removeById(String SlotId) {
        if (isEmpty() || SlotId == null) {
            return false;
        }

        if (head.data.getSlotId().equals(SlotId)) {
            head = head.next;
            size--;
            return true;
        }

        Node prev = head;
        Node current = head.next;
        while (current != null) {
            if (current.data.getSlotId().equals(SlotId)) {
                prev.next = current.next;
                size--;
                return true;
            }
            prev = current;
            current = current.next;
        }
 
        return false; 
    }

    public List<InventoryItem> toList() {
        List<InventoryItem> result = new ArrayList<>();
        Node current = head;
        while (current != null) {
            result.add(current.data);
            current = current.next;
        }
        return result;
    }

    private int compareLots(InventoryItem first, InventoryItem second) {
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
}
