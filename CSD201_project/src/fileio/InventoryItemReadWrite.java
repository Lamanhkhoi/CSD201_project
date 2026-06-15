/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fileio;

import model.InventoryItem;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InventoryItemReadWrite implements IFileReadWrite<InventoryItem, List<InventoryItem>> {

    private static final String FILE_PATH = "inventory.txt";

    @Override
    public List<InventoryItem> read() {
        List<InventoryItem> list = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 7) { // Dựa theo toString() của Model gồm 7 trường
                    String batchId = parts[0];
                    String sku = parts[1];
                    int quantity = Integer.parseInt(parts[2]);
                    LocalDate rDate = LocalDate.parse(parts[3]);
                    LocalDate eDate = LocalDate.parse(parts[4]);
                    String location = parts[5];
                    // parts[6] là status, nhưng ta dùng constructor 6 tham số đã có
                    
                    InventoryItem item = new InventoryItem(batchId, sku, quantity, rDate, eDate, location);
                    item.setStatus(parts[6]); // Gán lại status
                    list.add(item);
                }
            }
        } catch (Exception e) {
            System.out.println("-> [Lỗi] Không thể đọc file inventory.txt: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean write(List<InventoryItem> listToSave) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (InventoryItem item : listToSave) {
                pw.println(item.toString()); // Ghi đè cấu trúc 7 trường xuống file
            }
            return true;
        } catch (Exception e) {
            System.out.println("-> [Lỗi] Không thể ghi file inventory.txt: " + e.getMessage());
            return false;
        }
    }
}
