package employees.presentation;

import employees.repository.DatabaseManager;
import employees.service.DeliveryService;
import employees.service.EmployeeService;
import employees.service.ShiftService;

import java.util.Scanner;

public class EmployeeMain {

    private static final String MANAGER_PASSWORD = "ILoveNituz";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Loading system data...");
        try {
            // Step 1: Initialize EmployeeService first (loads branches and employees from DB)
            EmployeeService.getInstance();

            // Step 2: Load shifts (needs employees and branches to already exist)
            ShiftService.getInstance().loadFromDatabase();

            // Step 3: Load deliveries (needs shifts, employees and branches to already exist)
            DeliveryService.getInstance().loadFromDatabase();

            // Step 4: Load sample data only if DB is empty (first run)
            if (EmployeeService.getInstance().getAllBranches().isEmpty()) {
                DataUI.loadSampleData();
                System.out.println("Sample data loaded successfully.");
            } else {
                System.out.println("Data loaded from database successfully.");
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not load data. " + e.getMessage());
            System.out.println("The system will start empty.");
        }

        // Create UI instances
        ManagerUI managerUI = new ManagerUI(scanner);
        EmployeeUI employeeUI = new EmployeeUI(scanner);

        // Main menu loop
        while (true) {
            System.out.println("\nSupermarket HR System");
            System.out.println("1. Manager login");
            System.out.println("2. Employee login");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    if (authenticateManager(scanner)) {
                        managerUI.start();
                    }
                    break;
                case "2":
                    employeeUI.login();
                    break;
                case "0":
                    System.out.println("Goodbye!");
                    try {
                        DatabaseManager.getInstance().close();
                    } catch (Exception e) {
                        System.out.println("Warning: Could not close database connection.");
                    }
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static boolean authenticateManager(Scanner scanner) {
        System.out.print("Enter manager password: ");
        String password = scanner.nextLine().trim();
        if (password.equals(MANAGER_PASSWORD)) {
            return true;
        }
        System.out.println("Error: Incorrect password.");
        return false;
    }
}