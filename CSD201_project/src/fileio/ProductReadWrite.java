package fileio;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import model.Product;

public class ProductReadWrite implements IFileReadWrite<Product> {
    
    // Đường dẫn lưu file tương tự như các phân hệ khác của nhóm bạn
    private final String filePath = "data/products.txt";

    @Override
    public List<Product> read() throws Exception {
        List<Product> list = new ArrayList<>();
        File file = new File(filePath);
        
        // Nếu file chưa tồn tại thì tự động tạo thư mục và file mới
        if (!file.exists()) {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
            return list;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Tách các trường bằng dấu chấm phẩy (;) cho đồng bộ với cấu trúc chung
                String[] parts = line.split(";");
                if (parts.length == 4) {
                    Product p = new Product(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim());
                    list.add(p);
                }
            }
        }
        return list;
    }

    @Override
    public boolean write(List<Product> list) throws Exception {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (Product p : list) {
                bw.write(p.getSku() + ";" + p.getName() + ";" + p.getCategory() + ";" + p.getSupplier());
                bw.newLine();
            }
            return true;
        }
    }
}