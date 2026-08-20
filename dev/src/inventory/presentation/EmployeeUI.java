package inventory.presentation;

import inventory.domain.DamageReason;
import inventory.domain.InventoryItem;
import inventory.domain.Item;
import inventory.service.InventoryService;

import java.util.Scanner;

public class EmployeeUI {

    private InventoryService service;
    private Scanner scanner;

    public EmployeeUI(InventoryService service, Scanner scanner) {

        this.service = service;
        this.scanner = scanner;
    }

    public void start() {

        boolean logout = false;

        while (!logout) {
            try {
                System.out.println("\nEmployee Menu");
                System.out.println("1. Sell item (Reduce from shelf) ");
                System.out.println("2. Move from Warehouse to Shelf");
                System.out.println("3. Report damaged or expired item");
                System.out.println("4. Logout");
                String choice = scanner.nextLine();

                switch (choice) {

                    case "1":
                        handleSell();
                        break;

                    case "2":
                        handleRestock();
                        break;

                    case "3":
                        handleReportDamage();
                        break;

                    case "4":
                        logout = true;
                        System.out.println("Logging out...");
                        break;

                    default:
                        System.out.println("Please enter a valid option. ");
                }
            } catch (Exception e) {
                System.out.println("Error ");
            }
        }
    }



    private void handleSell() {

        try {
        System.out.println("\nSell Item");
        System.out.print("Enter Item ID: ");
        int id = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter quantity to sell: ");
        int qty = Integer.parseInt(scanner.nextLine());

        double toPay = service.makeSale(id, qty);
        System.out.println("Sale was made, Thank you! . Total to pay: " + String.format("%.2f", toPay) + " ILS");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }



    private void handleRestock() {

        try {
            System.out.println("\nRestock from warehouse");
            System.out.print("Enter Item ID: ");
             int id = Integer.parseInt(scanner.nextLine());
            System.out.print("How many units to move to shelf? ");
            int qty = Integer.parseInt(scanner.nextLine());
            service.moveToShelf(id, qty);
            System.out.println("Move successful.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }




    private void handleReportDamage() {

            try {

                System.out.println("\nReport Damaged or Expired Item");
                System.out.print("Enter Item ID: ");

                int id = Integer.parseInt(scanner.nextLine());
                Item item = service.findById(id);
                if (item == null)
                    throw new RuntimeException("Item not found.");

                InventoryItem iItem = service.getInventoryItemByItemId(id);
                if (iItem == null)
                    throw new RuntimeException("Inventory details not found for this item.");

                System.out.println("Reporting  for: " + item.getName());
                System.out.println("Current Stock -> Shelf: " + iItem.getShelfQuantity() + "  Warehouse: " + iItem.getWareQuantity());

                System.out.println("Select Reason:");
                System.out.println("1. Damaged");
                System.out.println("2. Expired");
                System.out.print("Choice: ");
                String reasonChoice = scanner.nextLine();

                DamageReason reason;
                if (reasonChoice.equals("1")) {
                    reason = DamageReason.DAMAGED;
                } else if (reasonChoice.equals("2")) {
                    reason = DamageReason.EXPIRED;
                } else {
                    throw new RuntimeException("Invalid reason selection. Report cancelled.");
                }

                System.out.print("quantity to reduce from Shelf: ");
                int shelfQty = Integer.parseInt(scanner.nextLine());

                System.out.print("Enter quantity to reduce from Warehouse: ");
                int warehouseQty = Integer.parseInt(scanner.nextLine());

                service.reportDamage(id, shelfQty, warehouseQty, reason);
                System.out.println("Damage report recorded and stock updated.");

            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter valid numbers.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
    }

}

