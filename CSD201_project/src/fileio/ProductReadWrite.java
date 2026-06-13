package fileio;

import java.io.*;
import model.Product;
import structures.SinglyLinkedList;

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
                // Cập nhật: Phải đủ 5 phần tử (thêm cột giá) mới tiến hành ép kiểu
                if (parts.length == 5) {
                    try {
                        double price = Double.parseDouble(parts[4].trim());
                        Product p = new Product(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(), price);
                        list.addLast(p);
                    } catch (NumberFormatException e) {
                        System.out.println("Cảnh báo: Lỗi ép kiểu giá tiền ở dòng: " + line);
                    }
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
                // Nối thêm giá tiền vào cuối chuỗi
                bw.write(p.getSku() + ";" + p.getName() + ";" + p.getCategory() + ";" + p.getSupplier() + ";" + p.getPrice());
                bw.newLine();
                current = current.getNext();
            }
            return true;
        }
    }
}