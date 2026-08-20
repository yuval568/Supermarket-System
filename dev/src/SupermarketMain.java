import inventory.InventoryMain;
import employees.presentation.EmployeeMain;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.util.Scanner;

public class SupermarketMain {

    public static void main(String[] args) {
        // Both subsystem mains close their Scanner, which would close System.in.
        // Wrapping System.in prevents that so the stream stays open between calls.
        InputStream nonCloseable = new FilterInputStream(System.in) {
            @Override public void close() {}
        };
        System.setIn(nonCloseable);

        Scanner scanner = new Scanner(nonCloseable);

        while (true) {
            System.out.println();
            System.out.println("=== Supermarket Management System ===");
            System.out.println("1. Inventory System");
            System.out.println("2. Employee & HR System");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    InventoryMain.main(args);
                    break;
                case "2":
                    EmployeeMain.main(args);
                    break;
                case "0":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
