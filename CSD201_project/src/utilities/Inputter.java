package utilities;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;

public class Inputter {
    private static final Scanner sc = new Scanner(System.in);
    // Sử dụng uuuu đi với STRICT là hoàn toàn chính xác cho LocalDate
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    public static String inputStr(String msg) {
        System.out.print(msg);
        return sc.nextLine().trim();
    }

    public static int inputInt(String msg) {
        System.out.print(msg); // Đổi thành print để con trỏ nằm cùng dòng nhắc
        while (true) {
            String s = sc.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter an integer!");
                System.out.print(msg); // In lại dòng nhắc ngay sau thông báo lỗi để người dùng nhập lại
            }
        }
    }

    public static double inputDouble(String msg) {
        System.out.print(msg); // Đổi thành print
        while (true) {
            String s = sc.nextLine().trim();
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a number!");
                System.out.print(msg); // In lại dòng nhắc
            }
        }
    }

    public static LocalDate inputDate(String msg) {
        System.out.print(msg); // Đổi thành print
        while (true) {
            String s = sc.nextLine().trim();
            try {
                return LocalDate.parse(s, FMT);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use dd/MM/yyyy (and valid calendar date).");
                System.out.print(msg); // In lại dòng nhắc
            }
        }
    }

    public static Integer inputIntNullable(String msg) {
        System.out.print(msg);
        while (true) {
            String s = sc.nextLine().trim();
            if (s.isEmpty()) return null;
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter an integer!");
                System.out.print(msg);
            }
        }
    }

    public static Double inputDoubleNullable(String msg) {
        System.out.print(msg);
        while (true) {
            String s = sc.nextLine().trim();
            if (s.isEmpty()) return null;
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a number!");
                System.out.print(msg);
            }
        }
    }

    public static LocalDate inputDateNullable(String msg) {
        System.out.print(msg);
        while (true) {
            String s = sc.nextLine().trim();
            if (s.isEmpty()) return null;
            try {
                return LocalDate.parse(s, FMT);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use dd/MM/yyyy.");
                System.out.print(msg);
            }
        }
    }
}