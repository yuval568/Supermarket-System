package inventory;

import inventory.domain.DataLoader;
import inventory.presentation.LoginUI;
import inventory.repository.InventoryRepository;
import inventory.service.InventoryService;
import inventory.service.OrderService;
import inventory.repository.DatabaseManager;
import java.util.Scanner;

public class InventoryMain {

    public static void main(String[] args) {

        DatabaseManager.initializeDatabase();

        Scanner scanner = new Scanner(System.in);

        InventoryRepository repo = new InventoryRepository();
        OrderService orderService = new OrderService();
        InventoryService service = new InventoryService(repo, orderService);

        orderService.setInventoryService(service);

        service.startWarehouseMonitor();
        orderService.automaticShortageOrder();
        orderService.startPeriodicOrderTimer();

        System.out.println("Welcome to Inventory System");
        System.out.println("1. Start with data");
        System.out.println("2. Start with empty system");
        String choice = scanner.nextLine();
        if (choice.equals("1")) {
            new DataLoader(service).loadData();
        }

        LoginUI loginUI = new LoginUI(service, scanner);
        loginUI.start();
        scanner.close();
    }
}