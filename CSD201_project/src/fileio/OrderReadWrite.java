package fileio;

import model.Order;
import model.OrderItem;
import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import structures.LinkedList;

public class OrderReadWrite implements IFileReadWrite<Order, List<Order>> {
    private final String filePath = "data/orders.txt";

    @Override
    public List<Order> read() throws Exception {
        List<Order> list = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
            return list;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(";");
                
                String orderId = parts[0];
                String customerName = parts[1];
                String phone = parts[2];
                String address = parts[3];
                LocalDateTime createdDate = LocalDateTime.parse(parts[4]);
                LocalDateTime expectedDate = LocalDateTime.parse(parts[5]);
                LocalDateTime latestDate = LocalDateTime.parse(parts[6]);
                String status = parts[7];
                double totalAmount = Double.parseDouble(parts[8]);

                LinkedList<OrderItem> itemsToPick = new LinkedList<>();
                if (parts.length > 9 && !parts[9].equalsIgnoreCase("NONE")) {
                    String[] itemsArray = parts[9].split(",");
                    for (String itemStr : itemsArray) {
                        String[] itemParts = itemStr.split(":");
                        itemsToPick.addLast(new OrderItem(itemParts[0], Integer.parseInt(itemParts[1])));
                    }
                }

                Order order = new Order(orderId, customerName, phone, address, 
                        createdDate, expectedDate, latestDate, status, totalAmount, itemsToPick);
                list.add(order);
            }
        }
        return list;
    }

    @Override
    public boolean write(List<Order> list) throws Exception {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (Order o : list) {
                StringBuilder sb = new StringBuilder();
                sb.append(o.getOrderId()).append(";")
                  .append(o.getCustomerName()).append(";")
                  .append(o.getPhone()).append(";")
                  .append(o.getAddress()).append(";")
                  .append(o.getCreatedDate().toString()).append(";")
                  .append(o.getExpectedDate().toString()).append(";")
                  .append(o.getLatestDate().toString()).append(";")
                  .append(o.getStatus()).append(";")
                  .append(o.getTotalAmount()).append(";");

                LinkedList<OrderItem> items = o.getItemsToPick();
                if (items.isEmpty()) {
                    sb.append("NONE");
                } else {
                    for (int j = 0; j < items.size(); j++) {
                        OrderItem item = items.get(j);
                        sb.append(item.getSku()).append(":").append(item.getQuantity());
                        if (j < items.size() - 1) sb.append(",");
                    }
                }
                bw.write(sb.toString());
                bw.newLine();
            }
            return true;
        }
    }
    
    
}