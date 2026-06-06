package utilities;

import fileio.IFileReadWrite;
import structures.List; 
import java.util.Scanner;

public class StorageHandler<T> {
    private final IFileReadWrite<T> fileHandler;
    private final Scanner sc;

    public StorageHandler(IFileReadWrite<T> fileHandler) {
        this.fileHandler = fileHandler;
        this.sc = new Scanner(System.in);
    }

    public boolean askAndSave(List<T> listToSave) {
        while (true) {
            System.out.print("Do you want to save changes? (Y/N): ");
            String input = sc.nextLine().trim().toUpperCase();

            if (input.equals("Y")) {
                try {
                    boolean result = fileHandler.write(listToSave);
                    if (result) {
                        System.out.println("Changes saved successfully to file!");
                    }
                    return result;
                } catch (Exception e) {
                    System.out.println("Critical Error: Cannot write to file. Reason: " + e.getMessage());
                    return false;
                }
            } else if (input.equals("N")) {
                System.out.println("Changes discarded.");
                return true; 
            } else {
                System.out.println("Invalid input! Please enter only 'Y' or 'N'.");
            }
        }
    }
}