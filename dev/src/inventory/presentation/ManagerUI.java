package inventory.presentation;

import inventory.domain.Category;
import inventory.domain.DataLoader;
import inventory.domain.InventoryItem;
import inventory.domain.Item;
import inventory.domain.Order;
import inventory.domain.ShelfLocation;
import inventory.service.InventoryService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ManagerUI {

    private InventoryService service;
    private Scanner scanner;

    public ManagerUI(InventoryService service, Scanner scanner) {

        this.service = service;
        this.scanner = scanner;

    }


    public void start() {
        boolean backToMainMenu = false;

        while (!backToMainMenu) {
            System.out.println("\nManager Menu   ");
            System.out.println("1. Add a new item");

            System.out.println("2. Create discount (Item / Category)");
            System.out.println("3. Update item location");
            System.out.println("4. Update item Price");
            System.out.println("5. Update item minimum quantity");
            System.out.println("6. Add category");
            System.out.println("7. Logout");
            System.out.println("8. Update periodic order frequency (minutes)");
            System.out.println("9. View order history");
            System.out.println("10. Place manual order (Immediate / Future)");
            System.out.print("Please select an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    handleAddItem();
                    break;

                case "2":
                    handleManageDiscounts();
                    break;

                case "3":
                    handleUpdateLocation();
                    break;

                case "4":
                    handleChangePrice();
                    break;

                case "5":
                    handleUpdateMinQuantity();
                    break;

                case "6":
                    handleAddCategory();
                    break;

                case "7":
                    backToMainMenu = true;
                    break;

                case "8":
                    handleUpdatePeriodicFrequency();
                    break;

                case "9":
                    handleSystemLogs();
                    break;

                case "10":
                    handleManualOrder();
                    break;

                default:
                    System.out.println("Please enter a valid option.");
            }
        }

    }

    private void handleAddItem() {

        try {

            System.out.print("Enter item name: ");
            String itemName = scanner.nextLine();

            System.out.print("Enter cost price: ");
            double costPrice = Double.parseDouble(scanner.nextLine());

            System.out.print("Enter selling price: ");
            double sellingPrice = Double.parseDouble(scanner.nextLine());

            System.out.print("Enter manufacturer: ");
            String manufacturer = scanner.nextLine();

            System.out.print("Enter parent category : \n");

            List<Category> allCategories = service.getCategories();
            List<Category> leafCategories = new ArrayList<>();
            for (Category cat : allCategories) {
                if (cat.getSubCategories().isEmpty()) {
                    leafCategories.add(cat);
                }
            }

            if (leafCategories.isEmpty()) {
                System.out.println("Error: Cannot add a new item without categories in the system. Please add categories first (option 10).");
                return;
            }

            for (int i = 0; i < leafCategories.size(); i++) {
                Category current = leafCategories.get(i);
                System.out.println((i + 1) + ". " + getCategoryFullName(current));
            }

            System.out.print("Enter category number: ");
            int choice = Integer.parseInt(scanner.nextLine());
            Category selectedCategory = leafCategories.get(choice - 1);
            System.out.println("Adding to: " + getCategoryFullName(selectedCategory));
            service.addItem(itemName, costPrice, sellingPrice, manufacturer, selectedCategory);
            System.out.println("Item added");
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter a valid number.");
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Error: Invalid category selection.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private String getCategoryFullName(Category category) {
        if (category.getParent() == null) {
            return category.getName();
        }
        return getCategoryFullName(category.getParent()) + " -> " + category.getName();
    }



    private void handleManageDiscounts() {
        System.out.println("\nManage Discounts");

        try {
            System.out.print("Enter discount name: ");
            String discountName = scanner.nextLine();

            System.out.print("Enter discount percentage: ");
            double percentage = Double.parseDouble(scanner.nextLine());

            System.out.print("Enter start date (YYYY-MM-DD): ");
            LocalDate startDate = LocalDate.parse(scanner.nextLine());

            System.out.print("Enter end date (YYYY-MM-DD): ");
            LocalDate endDate = LocalDate.parse(scanner.nextLine());

            System.out.println("\ndiscount to: ");
            System.out.println("1. Specific Item");
            System.out.println("2. Entire Category");
            System.out.print("Select option (1 or 2): ");

            int choice = Integer.parseInt(scanner.nextLine());

            if (choice == 1) {
                System.out.print("Enter Item ID: ");
                int itemId = Integer.parseInt(scanner.nextLine());
                Item item = service.findById(itemId);

                if (item == null) {
                    System.out.println("Error: Item not found.");
                    return;
                }
                service.addDiscount(discountName, percentage, startDate, endDate, item, null);
                System.out.println("Discount created successfully for item: " + item.getName());

            } else if (choice == 2) {
                List<Category> allCategories = service.getCategories();
                if (allCategories == null || allCategories.isEmpty()) {
                    System.out.println("Error: No categories exist.");
                    return;
                }

                System.out.println("\nSelect Category:");
                for (int i = 0; i < allCategories.size(); i++) {
                    Category cat = allCategories.get(i);
                    System.out.println((i + 1) + ". " + getCategoryFullName(cat));
                }
                System.out.print("Enter category number: ");
                int catChoice = Integer.parseInt(scanner.nextLine());
                Category selectedCategory = allCategories.get(catChoice - 1);

                service.addDiscount(discountName, percentage, startDate, endDate, null, selectedCategory);
                System.out.println("Discount created successfully");

            } else {
                System.out.println("Invalid option. Discount creation aborted.");
            }

        } catch (Exception e) {
            System.out.println("Failed to create discount");
        }
    }



    private void handleUpdateLocation() {

        System.out.println("\nUpdate Item Location");
        try {
            System.out.print("Enter Item ID: ");
            int id = Integer.parseInt(scanner.nextLine());
            InventoryItem iItem = service.getInventoryItemByItemId(id);
            if (iItem == null) {
                System.out.println("Item not found.");
                return;
            }
            System.out.println("Current location: " + iItem.getLocation());
            System.out.print("Enter new aisle (e.g. A, B, C): ");
            String aisle = scanner.nextLine();
            System.out.print("Enter row number: ");
            int row = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter shelf number: ");
            int shelf = Integer.parseInt(scanner.nextLine());
            ShelfLocation newLocation = new ShelfLocation(aisle, row, shelf);
            service.updateItemLocation(id, newLocation);
            System.out.println("Location updated to: " + newLocation);
        } catch (Exception e) {
            System.out.println("Update unsuccessful");
        }

    }

    private void handleChangePrice() {

        System.out.println("\n Update Item Price");

        try {
            System.out.print("Enter Item ID: ");
            int id = Integer.parseInt(scanner.nextLine());
            Item item = service.findById(id);

            if (item == null) {
                System.out.println("Item not found.");
                return;
            }

            System.out.println("Selected Item: " + item.getName());
            System.out.println("Current Cost Price: " + item.getCostPrice());
            System.out.println("Current Selling Price: " + item.getSellingPrice());
            System.out.print("Enter new Selling Price : ");
            String sellInput = scanner.nextLine();
            if (!sellInput.trim().isEmpty()) {
                double newSell = Double.parseDouble(sellInput);
                if (newSell < 0) throw new IllegalArgumentException("Price cannot be negative.");
                service.updateItemPrice(id, newSell);
                System.out.println("Selling price updated successfully.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleUpdateMinQuantity() {
        System.out.println("\nUpdate Minimum Quantity");
        try {
            System.out.print("Enter Item ID: ");
            int id = Integer.parseInt(scanner.nextLine());
            Item item = service.findById(id);
            if (item == null) {
                System.out.println("Item not found.");
                return;
            }
            System.out.println("Item: " + item.getName());
            System.out.println("Current Min Quantity: " + item.getMinQuantity());
            System.out.print("Enter new minimum quantity: ");
            int newMin = Integer.parseInt(scanner.nextLine());
            service.updateMinQuantity(id, newMin);
            System.out.println("Minimum quantity updated successfully.");
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter a valid number.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleAddCategory() {
        System.out.println("\nAdd Category");
        try {
            System.out.print("Enter category name: ");
            String name = scanner.nextLine();

            System.out.println("Select parent category (or 0 for no parent):");
            List<Category> allCategories = service.getCategories();
            for (int i = 0; i < allCategories.size(); i++)
                System.out.println((i + 1) + ". " + getCategoryFullName(allCategories.get(i)));

            System.out.print("Enter number: ");
            int choice = Integer.parseInt(scanner.nextLine());

            if (choice == 0) {
                service.addCategory(name);
            } else {
                Category parent = allCategories.get(choice - 1);
                service.addCategory(name, parent);
            }
            System.out.println("Category added successfully.");

        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter a valid number.");
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Error: Invalid category selection.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    private void handleInitialData() {
        try {
            DataLoader loader = new DataLoader(service);
            loader.loadData();
            System.out.println("Data loaded successfully");
        } catch (Exception e) {
            System.out.println("Data wasn't loaded successfully: " + e.getMessage());
        }
    }


    private void handleUpdatePeriodicFrequency() {
        try {
            System.out.println("\nUpdate Periodic Restock Order Frequency");
            System.out.print("Enter new frequency in minutes: ");
            int minutes = Integer.parseInt(scanner.nextLine());
            
            if (minutes <= 0) {
                System.out.println("Error: frequency must be positive.");
                return;
            }

            service.updatePeriodicOrderInterval(minutes);
            System.out.println("Successfully updated periodic order frequency to " + minutes + " minutes.");
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid number format.");
        } catch (Exception e) {
            System.out.println("Error updating periodic order frequency: " + e.getMessage());
        }
    }

    private void handleSystemLogs() {
        System.out.println("\n=================================================");
        System.out.println("                 ORDER HISTORY");
        System.out.println("=================================================");

        int interval = service.getPeriodicOrderIntervalMinutes();
        java.time.LocalDateTime lastCheck = service.getLastPeriodicCheckTime();
        
        String lastCheckStr = (lastCheck != null) 
                ? lastCheck.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) 
                : "Not run yet";

        System.out.println("Periodic order is set to run every " + interval + " minutes.");
        System.out.println("Last background check run at: " + lastCheckStr + "\n");

        List<Order> allOrders = service.getAllOrders();
        if (allOrders.isEmpty()) {
            System.out.println("No orders have been placed yet.");
            System.out.println("=================================================\n");
            return;
        }

        for (Order order : allOrders) {
            StringBuilder itemsStr = new StringBuilder();
            for (inventory.domain.OrderItem oi : order.getOrderItems()) {
                inventory.domain.Item item = service.findById(oi.getItemId());
                String itemName = (item != null) ? item.getName() : "ID " + oi.getItemId();
                itemsStr.append(itemName).append(" x").append(oi.getQuantity()).append(", ");
            }
            if (itemsStr.length() > 0) itemsStr.setLength(itemsStr.length() - 2);

            String typeLabel;
            switch (order.getOrderType()) {
                case PERIODIC:
                    typeLabel = "Periodic";
                    break;
                case SHORTAGE:
                    typeLabel = "Shortage (auto)";
                    break;
                case MANUAL:
                    typeLabel = "Manual";
                    break;
                default:
                    typeLabel = order.getOrderType().name();
            }

            System.out.printf("[%s] Order #%d | Supplier: %s | Items: [%s] | Total: %.2f ILS | Status: %s%n",
                    typeLabel, order.getId(), order.getSupplierName(), itemsStr, order.getTotalPrice(), order.getStatus());
        }

        System.out.println("=================================================\n");
    }

    private void handleManualOrder() {
        try {
            System.out.println("\nPlace Manual Order");
            System.out.print("Enter Item ID to order: ");
            int itemId = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Enter quantity: ");
            int quantity = Integer.parseInt(scanner.nextLine());
            
            System.out.println("Order Type:");
            System.out.println("1. Immediate (ASAP)");
            System.out.println("2. Future (Coordinate a delivery date)");
            System.out.print("Select type: ");
            String type = scanner.nextLine();
            
            LocalDate deliveryDate = null;
            if ("2".equals(type)) {
                System.out.print("Enter desired delivery date (YYYY-MM-DD): ");
                deliveryDate = LocalDate.parse(scanner.nextLine());
                if (!deliveryDate.isAfter(LocalDate.now())) {
                    System.out.println("Error: Future delivery date must be after today.");
                    return;
                }
            } else if (!"1".equals(type)) {
                System.out.println("Error: Invalid order type selected.");
                return;
            }
            
            Order order = service.placeManualOrder(itemId, quantity, deliveryDate);
            if (order != null) {
                System.out.println("Successfully placed order ID " + order.getId() + " from supplier " + order.getSupplierName());
            } else {
                System.out.println("Failed to place order.");
            }
            
        } catch (DateTimeParseException e) {
            System.out.println("Error: Invalid date format. Please use YYYY-MM-DD.");
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid number format.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error placing manual order: " + e.getMessage());
        }
    }
}
