package fileio;

import java.io.*;
import model.Product;
import structures.SinglyLinkedList; // Sử dụng cấu trúc dữ liệu của nhóm

public class ProductReadWrite implements IFileReadWrite<Product, SinglyLinkedList<Product>> {
    
    private final String filePath = "data/products.txt";

    @Override
    public SinglyLinkedList<Product> read() throws Exception {
        SinglyLinkedList<Product> list = new SinglyLinkedList<>();
        File file = new File(filePath);
        
        if (!file.exists()) {
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            file.createNewFile();
            return list;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(";");
                if (parts.length == 4) {
                    Product p = new Product(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim());
                    list.addLast(p); // Dùng addLast của SinglyLinkedList nhóm viết
                }
            }
        }
        return list;
    }

    @Override
    public boolean write(SinglyLinkedList<Product> list) throws Exception {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            SinglyLinkedList.Node<Product> current = list.getHead();
            while (current != null) {
                Product p = current.getElement();
                bw.write(p.getSku() + ";" + p.getName() + ";" + p.getCategory() + ";" + p.getSupplier());
                bw.newLine();
                current = current.getNext(); // Duyệt qua từng Node kế tiếp
            }
            return true;
        }
    }
}