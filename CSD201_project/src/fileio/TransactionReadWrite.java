/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fileio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import model.Transaction;
import structures.SinglyLinkedList;

/**
 *
 * @author LENOVO
 */
public class TransactionReadWrite implements IFileReadWrite<Transaction, SinglyLinkedList<Transaction>>{
    private final String filePath = "data/transactions.txt";
    @Override
    public SinglyLinkedList<Transaction> read() throws IOException {
        SinglyLinkedList<Transaction> history = new SinglyLinkedList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
            return history;
        }
        try ( BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 7) {
                    Transaction tx = new Transaction(
                            parts[0],
                            parts[1],
                            parts[2],
                            parts[3],
                            parts[4],
                            Integer.parseInt(parts[5]),
                            LocalDateTime.parse(parts[6]));
                    history.addLast(tx);
                }
            }

        } 
        return history;
    }

    @Override
    public boolean write(SinglyLinkedList<Transaction> history) {
        try ( BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            SinglyLinkedList.Node<Transaction> current = history.getHead();
            while (current != null) {
                bw.write(current.getElement().toString());
                bw.newLine();
                current = current.getNext();
            }

//            System.out.println("Transaction history saved.");
            return true;
        } catch (IOException e) {
//            System.out.println("Error writing file: "
//                    + e.getMessage());
            return false;
        }
    }

}
