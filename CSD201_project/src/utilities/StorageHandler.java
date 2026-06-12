package utilities;

import fileio.IFileReadWrite;
import java.util.List;
import java.util.Scanner;
import structures.SinglyLinkedList;

public class StorageHandler<T, C> {
    private final IFileReadWrite<T, C> fileHandler;
    private final Scanner sc;

    public StorageHandler(IFileReadWrite<T, C> fileHandler) {
        this.fileHandler = fileHandler;
        this.sc = new Scanner(System.in);
    }

    public boolean askAndSave(C listToSave) {
        while (true) {
            System.out.print("Bạn có muốn lưu các thay đổi xuống tệp tin vật lý không? (Y/N): ");
            String input = sc.nextLine().trim().toUpperCase();

            if (input.equals("Y")) {
                try {
                    boolean result = fileHandler.write(listToSave);
                    if (result) {
                        System.out.println("Hệ thống: Dữ liệu đã được ghi nhận và lưu trữ thành công!");
                    }
                    return result;
                } catch (Exception e) {
                    System.out.println("Lỗi nghiêm trọng: Không thể ghi file. Lý do: " + e.getMessage());
                    return false;
                }
            } else if (input.equals("N")) {
                System.out.println("Hệ thống: Hủy bỏ thao tác lưu tệp tin vật lý.");
                return false; 
            } else {
                System.out.println("Lựa chọn không hợp lệ! Vui lòng chỉ nhập 'Y' hoặc 'N'.");
            }
        }
    }
    
    
}