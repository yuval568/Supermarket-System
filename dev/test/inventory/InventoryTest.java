package inventory;

import inventory.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import inventory.repository.InventoryRepository;
import inventory.service.InventoryService;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryTest {
    InventoryService data;
    InventoryRepository repo;
    inventory.service.OrderService os;
    
    @BeforeEach
    void SetData() {
        inventory.repository.DatabaseManager.initializeDatabase();
        try (java.sql.Connection conn = inventory.repository.DatabaseManager.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM inventory_items");
            stmt.execute("DELETE FROM items");
            stmt.execute("DELETE FROM categories");
            stmt.execute("DELETE FROM discounts");
            stmt.execute("DELETE FROM damage_reports");
            stmt.execute("DELETE FROM low_stock_alerts");
            stmt.execute("DELETE FROM order_items");
            stmt.execute("DELETE FROM orders");
        } catch (Exception e) {
            e.printStackTrace();
        }

        repo = new InventoryRepository();
        os = new inventory.service.OrderService();
        data = new InventoryService(repo, os);
        os.setInventoryService(data);
        
        // We will load data if the DB is empty, otherwise DataLoader safely skips
        new DataLoader(data).loadData();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (os != null) {
            os.shutdown();
        }
        if (data != null) {
            data.shutdown();
        }
    }

    @Test
    void TestBelowQuantityAlert() {
        Item item = repo.findAllItems().get(1);
        item.setMinQuantity(20);
        repo.updateItem(item);
        
        InventoryItem iItem = repo.findInventoryItemByItemId(item.getId());
        iItem.setWareQuantity(21);
        iItem.setShelfQuantity(0);
        repo.updateInventoryItem(iItem);
        
        // 21 >= 20, so shouldn't be below min
        assertFalse(iItem.getTotalQuantity() < item.getMinQuantity(), "Item quantity was not updated properly");
    }

    @Test
    void TestShelfQuantityAfterSale() {
        Item item = repo.findAllItems().get(0);
        InventoryItem iItem = repo.findInventoryItemByItemId(item.getId());
        iItem.setShelfQuantity(10);
        repo.updateInventoryItem(iItem);
        
        int initialQuantity = iItem.getShelfQuantity();
        data.makeSale(item.getId(), 2);
        
        InventoryItem afterSale = repo.findInventoryItemByItemId(item.getId());
        assertEquals(initialQuantity - 2, afterSale.getShelfQuantity(), "Shelf quantity wasn't updated after the sale");
    }

    @Test
    void CalculateFinalPrice() {
        Item item = repo.findAllItems().get(0);
        Category itemCat = repo.findCategoryByName(item.getCategory().getName());
        data.addDiscount("DiscountTest", 20, LocalDate.now(), LocalDate.now().plusDays(7), item, itemCat);
        
        double FinalPrice = data.calculateFinalPrice(item.getId());
        assertEquals(item.getSellingPrice() * 0.8, FinalPrice, 0.01, "The discount was not applied correctly");
    }

    @Test
    void TestDamageReport() {
        Item item = repo.findAllItems().get(0);
        InventoryItem iItem = repo.findInventoryItemByItemId(item.getId());
        iItem.setShelfQuantity(3);
        iItem.setWareQuantity(3);
        repo.updateInventoryItem(iItem);
        
        int quantityBefore = iItem.getTotalQuantity();
        data.reportDamage(item.getId(), 1, 1, DamageReason.EXPIRED);
        
        InventoryItem afterDamage = repo.findInventoryItemByItemId(item.getId());
        assertEquals(quantityBefore - 2, afterDamage.getTotalQuantity(), "Total quantity should decrease by the damaged amount");
    }

    @Test
    void TestMoveToShelfLogic() {
        Item item = repo.findAllItems().get(0);
        InventoryItem iItem = repo.findInventoryItemByItemId(item.getId());
        iItem.setWareQuantity(10);
        iItem.setShelfQuantity(0);
        repo.updateInventoryItem(iItem);
        
        data.moveToShelf(item.getId(), 7);
        
        InventoryItem afterMove = repo.findInventoryItemByItemId(item.getId());
        assertEquals(3, afterMove.getWareQuantity());
        assertEquals(7, afterMove.getShelfQuantity());
    }


    @Test
    void TestReceiveShipmentUpdatesCost() {
        Item item = repo.findAllItems().get(0);
        InventoryItem iItem = repo.findInventoryItemByItemId(item.getId());
        iItem.setWareQuantity(0);
        repo.updateInventoryItem(iItem);
        
        data.receiveShipment(item.getId(), 15.0, 100);
        
        InventoryItem afterShipment = repo.findInventoryItemByItemId(item.getId());
        Item afterItem = repo.findById(item.getId());
        assertEquals(100, afterShipment.getWareQuantity());
        assertEquals(15.0, afterItem.getCostPrice(), 0.01);
    }

    @Test
    void TestDiscountByCategory() {
        Item item = repo.findAllItems().get(0);
        double originalPrice = item.getSellingPrice();
        Category itemCategory = repo.findCategoryByName(item.getCategory().getName());
        
        data.addDiscount("DiscountTest", 10, LocalDate.now(), LocalDate.now().plusDays(7), null, itemCategory);
        double priceAfter = data.calculateFinalPrice(item.getId());
        
        assertNotEquals(originalPrice, priceAfter);
        assertEquals(originalPrice * 0.9, priceAfter, 0.01, "The category discount was not applied to the item");
    }

    @Test
    void TestUpdateItemLocation() {
        Item item = repo.findAllItems().get(0);
        ShelfLocation newLocation = new ShelfLocation("A", 1, 2);
        data.updateItemLocation(item.getId(), newLocation);
        
        InventoryItem afterUpdate = repo.findInventoryItemByItemId(item.getId());
        assertEquals("Aisle: A, Row: 1, Shelf: 2", afterUpdate.getLocation());
    }

    @Test
    void TestHighestDiscountSelection() {
        Item item = repo.findAllItems().get(0);
        Category cat = repo.findCategoryByName(item.getCategory().getName());
        
        data.addDiscount("SmallSale", 10.0, LocalDate.now(), LocalDate.now().plusDays(1), null, cat);
        data.addDiscount("BigSale", 25.0, LocalDate.now(), LocalDate.now().plusDays(1), item, null);
        
        double finalPrice = data.calculateFinalPrice(item.getId());
        double expectedPrice = item.getSellingPrice() * 0.75;
        assertEquals(expectedPrice, finalPrice, 0.01);
    }
    
    @Test
    void TestAddCategory() {
        data.addCategory("TestCategory");
        Category cat = repo.findCategoryByName("TestCategory");
        assertNotNull(cat);
        assertEquals("TestCategory", cat.getName());
    }
    
    @Test
    void TestAddItem() {
        data.addCategory("NewCat");
        Category cat = repo.findCategoryByName("NewCat");
        data.addItem("NewItem", 10.0, 20.0, "Manufacturer", cat);
        
        List<Item> items = repo.findAllItems();
        boolean found = false;
        for (Item i : items) {
            if (i.getName().equals("NewItem")) found = true;
        }
        assertTrue(found, "Newly added item should be found in repository");
    }

    @Test
    void TestMakeSaleInsufficientStock() {
        Item item = repo.findAllItems().get(0);
        InventoryItem iItem = repo.findInventoryItemByItemId(item.getId());
        iItem.setShelfQuantity(2);
        repo.updateInventoryItem(iItem);
        
        assertThrows(IllegalArgumentException.class, () -> {
            data.makeSale(item.getId(), 5);
        });
    }

    @Test
    void TestMoveToShelfInsufficientWarehouse() {
        Item item = repo.findAllItems().get(0);
        InventoryItem iItem = repo.findInventoryItemByItemId(item.getId());
        iItem.setWareQuantity(5);
        repo.updateInventoryItem(iItem);
        
        assertThrows(IllegalArgumentException.class, () -> {
            data.moveToShelf(item.getId(), 10);
        });
    }

    @Test
    void TestReceiveOrderUpdatesWarehouseStock() {
        Item item = repo.findAllItems().get(0);
        InventoryItem iItem = repo.findInventoryItemByItemId(item.getId());
        int initialWarehouse = iItem.getWareQuantity();
        
        Order order = new Order("HADADI", OrderType.MANUAL);
        order.addItem(item.getId(), 10, 5.0);
        repo.saveOrder(order);
        
        data.receivedOrder(order);
        
        InventoryItem afterReceive = repo.findInventoryItemByItemId(item.getId());
        assertEquals(initialWarehouse + 10, afterReceive.getWareQuantity());
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void TestFindNonExistentItem() {
        Item item = data.findById(999999);
        assertNull(item, "Finding a non-existent item should return null");
    }

    @Test
    void TestInvalidDiscountDates() {
        Item item = repo.findAllItems().get(0);
        Category cat = repo.findCategoryByName(item.getCategory().getName());
        
        assertThrows(IllegalArgumentException.class, () -> {
            data.addDiscount("BadDiscount", 10, LocalDate.now().plusDays(2), LocalDate.now(), item, cat);
        }, "Should throw when start date is after end date");
    }

    @Test
    void TestUpdateItemMinQuantity() {
        Item item = repo.findAllItems().get(0);
        data.updateMinQuantity(item.getId(), 999);
        
        Item updated = repo.findById(item.getId());
        assertEquals(999, updated.getMinQuantity());
    }

    @Test
    void TestSystemLogger() {
        inventory.service.SystemLogger.log("Test log entry");
        List<String> logs = inventory.service.SystemLogger.getLogs();
        
        boolean found = false;
        for (String log : logs) {
            if (log.contains("Test log entry")) found = true;
        }
        assertTrue(found, "SystemLogger should record the given string");
    }

    @Test
    void TestProcessLowStockAlert() {
        Item item = repo.findAllItems().get(0);
        InventoryItem iItem = repo.findInventoryItemByItemId(item.getId());
        
        LowStockAlert alert = new LowStockAlert(iItem, 10);
        data.saveLowStockAlert(alert);
        
        List<LowStockAlert> unprocessed = data.getUnprocessedAlerts();
        assertFalse(unprocessed.isEmpty());
        
        data.markAlertProcessed(alert.getAlertId());
        
        List<LowStockAlert> remaining = data.getUnprocessedAlerts();
        for (LowStockAlert remainingAlert : remaining) {
            assertNotEquals(alert.getAlertId(), remainingAlert.getAlertId(), "Alert should no longer be unprocessed");
        }
    }

    @Test
    void TestOrderAddItemLogic() {
        Order order = new Order("TestSupplier", OrderType.MANUAL);
        
        // Add items and verify merging quantities and total price
        order.addItem(1, 10, 5.0);
        order.addItem(2, 5, 10.0);
        order.addItem(1, 5, 5.0); // Should merge with the previous item 1
        
        assertEquals(15, order.getItemsWithQuantities().get(1), "Quantity for item 1 should be merged to 15");
        assertEquals(5, order.getItemsWithQuantities().get(2), "Quantity for item 2 should be 5");
        
        // Total price should be: (15 * 5.0) + (5 * 10.0) = 75.0 + 50.0 = 125.0
        assertEquals(125.0, order.getTotalPrice(), 0.01, "Total price should be correctly calculated");
    }
}
