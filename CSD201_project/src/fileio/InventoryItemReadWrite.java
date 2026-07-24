package fileio;

import model.InventoryBatch;
import model.InventoryItem;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class InventoryItemReadWrite implements IFileReadWrite<InventoryBatch, List<InventoryBatch>> {

    private static final String FILE_PATH = "data/inventory.txt";
    private static final String EMPTY_MARKER = "-";

    @Override
    public List<InventoryBatch> read() throws Exception {
        List<InventoryBatch> resultList = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
            return resultList;
        }

        LinkedHashMap<String, InventoryBatch> batchMap = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(";");
                if (parts.length != 6) {
                    System.out.println("-> [Cảnh báo] Bỏ qua dòng sai định dạng trong inventory.txt: " + line);
                    continue;
                }

                String batchId = parts[0];
                String sku = parts[1];
                String slotId = parts[2];

                InventoryBatch batch = batchMap.get(batchId);
                if (batch == null) {
                    batch = new InventoryBatch(batchId, sku);
                    batchMap.put(batchId, batch);
                }

                if (EMPTY_MARKER.equals(slotId)) {
                    continue;
                }

                int quantity = Integer.parseInt(parts[3]);
                LocalDate receiveDate = LocalDate.parse(parts[4]);
                LocalDate expiryDate = LocalDate.parse(parts[5]);

                InventoryItem lot = new InventoryItem(slotId, quantity, receiveDate, expiryDate);
                batch.addExistingLot(lot);

                restoreSlotSequence(batch, slotId);
            }
        }

        resultList.addAll(batchMap.values());
        return resultList;
    }

    private void restoreSlotSequence(InventoryBatch batch, String slotId) {
        try {
        int i = slotId.length();
        while (i > 0 && Character.isDigit(slotId.charAt(i - 1))) {
            i--;
        }
        if (i == slotId.length()) {
            return;
        }
        int sequenceNumber = Integer.parseInt(slotId.substring(i));
        if (sequenceNumber > batch.getSlotSequence()) {
            batch.setSlotSequence(sequenceNumber);
        }
    } catch (NumberFormatException e) {
    }
    }

    @Override
    public boolean write(List<InventoryBatch> batchesToSave) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (InventoryBatch batch : batchesToSave) {
                List<InventoryItem> lots = batch.getAllLots();

                if (lots.isEmpty()) {
                    pw.println(String.format("%s;%s;%s;%s;%s;%s",
                            batch.getBatchId(), batch.getSku(),
                            EMPTY_MARKER, EMPTY_MARKER, EMPTY_MARKER, EMPTY_MARKER));
                    continue;
                }

                for (InventoryItem lot : lots) {
                    pw.println(String.format("%s;%s;%s;%d;%s;%s",
                            batch.getBatchId(), batch.getSku(),
                            lot.getSlotId(), lot.getQuantity(), lot.getReceiveDate(), lot.getExpiryDate()));
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("-> [Lỗi] Không thể ghi file inventory.txt: " + e.getMessage());
            return false;
        }
    }
}