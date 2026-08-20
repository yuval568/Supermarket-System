package inventory.presentation;

import inventory.service.InventoryService;

import java.util.Scanner;

public class LoginUI {

    private InventoryService service;
    private Scanner scanner;
    private String managerPassword;
    private String employeePassword;

    public LoginUI(InventoryService inventoryService, Scanner scanner) {

        this.service = inventoryService;
        this.managerPassword = "admin123";
        this.employeePassword = "super123";
        this.scanner = scanner;
    }


    public void start() {
        boolean isRunning = true;

        while (isRunning) {
            System.out.println("\nInventory ");
            System.out.print("Please enter your password / 'exit' to quit: ");

            String input = scanner.nextLine();

            if (input.equals("exit")) {
                System.out.println("Exiting the system");
                isRunning = false;
            } else if (input.equals(this.managerPassword)) {
                showManagerMenu();
            } else if (input.equals(this.employeePassword)) {
                showEmployeeMenu();
            } else {
                System.out.println("Invalid password. Please try again.");
            }
        }
    }
    private void showManagerMenu() {
        ManagerUI managerUI = new ManagerUI(service, scanner);
        managerUI.start();
    }

    private void showEmployeeMenu() {
        EmployeeUI employeeUI = new EmployeeUI(service, scanner);
        employeeUI.start();
    }
}


