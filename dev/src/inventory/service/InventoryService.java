package inventory.service;

import inventory.domain.*;
import inventory.repository.InventoryRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class InventoryService {

    private final InventoryRepository repo;
    private final OrderService orderService;
    private Thread monitorThread;

    private final Warehouse warehouse = new Warehouse(1, "Main Warehouse");

    public InventoryService(InventoryRepository repo, OrderService orderService) {
        this.repo = repo;
        this.orderService = orderService;
    }

    public void shutdown() {
        if (monitorThread != null) {
            monitorThread.interrupt();
        }
    }


    public InventoryItem addItem(String name, double costPrice, double sellingPrice,
                        String manufacturer, Category category) {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("Name cannot be empty");
        if (costPrice < 0 || sellingPrice < 0)
            throw new IllegalArgumentException("Price cannot be negative");
        if (manufacturer == null || manufacturer.isEmpty())
            throw new IllegalArgumentException("Manufacturer cannot be empty");
        if (category == null)
            throw new IllegalArgumentException("Category cannot be null");

        Item item = new Item(name, costPrice, sellingPrice, manufacturer, category);
        repo.saveItem(item);


        InventoryItem inventoryItem = new InventoryItem(item, 0, 0);
        repo.saveInventoryItem(inventoryItem);

        return inventoryItem;
    }

    public void updateInventoryItem(InventoryItem iItem) {
        repo.updateInventoryItem(iItem);
    }

    public Item findById(int id) {
        return repo.findById(id);
    }

    public void updateMinQuantity(int id, int minQuantity) {
        Item item = repo.findById(id);
        if (item == null)
            throw new IllegalArgumentException("Item not found");
        if (minQuantity < 0)
            throw new IllegalArgumentException("Min quantity cannot be negative");
        item.setMinQuantity(minQuantity);
        repo.updateItem(item);
    }

    public void updateItemPrice(int id, double price) {
        Item item = repo.findById(id);
        if (item == null)
            throw new IllegalArgumentException("Item not found");
        if (price < 0)
            throw new IllegalArgumentException("Price cannot be negative");
        item.setSellingPrice(price);
        repo.updateItem(item);
    }

    public void updateItemLocation(int id, ShelfLocation location) {
        InventoryItem iItem = repo.findInventoryItemByItemId(id);
        if (iItem == null)
            throw new IllegalArgumentException("Inventory details not found for this item");
        iItem.setShelfLocation(location);
        repo.updateInventoryItem(iItem);
    }

    public double makeSale(int id, int quantity) {
        Item item = repo.findById(id);
        if (item == null)
            throw new IllegalArgumentException("Item not found");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive");

        InventoryItem iItem = repo.findInventoryItemByItemId(id);
        if (iItem == null)
            throw new IllegalArgumentException("Inventory details not found for this item");
        if (quantity > iItem.getShelfQuantity())
            throw new IllegalArgumentException(
                    "Not enough stock on shelf. Current: " + iItem.getShelfQuantity());

        double unitPrice = calculateFinalPrice(id);
        iItem.setShelfQuantity(iItem.getShelfQuantity() - quantity);
        repo.updateInventoryItem(iItem);

        return unitPrice * quantity;
    }

    public void moveToShelf(int id, int quantity) {
        Item item = repo.findById(id);
        if (item == null)
            throw new IllegalArgumentException("Item not found");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive");

        InventoryItem iItem = repo.findInventoryItemByItemId(id);
        if (iItem == null)
            throw new IllegalArgumentException("Inventory details not found for this item");
        if (iItem.getWareQuantity() < quantity)
            throw new IllegalArgumentException("Not enough stock in warehouse");

        iItem.setWareQuantity(iItem.getWareQuantity() - quantity);
        iItem.setShelfQuantity(iItem.getShelfQuantity() + quantity);
        repo.updateInventoryItem(iItem);
    }

    public void receiveShipment(int id, double newCostPrice, int quantity) {
        Item item = repo.findById(id);
        if (item == null)
            throw new IllegalArgumentException("Item not found");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive");

        item.updateCostPrice(newCostPrice);
        repo.updateItem(item);

        InventoryItem iItem = repo.findInventoryItemByItemId(id);
        if (iItem == null) {
            iItem = new InventoryItem(item, 0, quantity);
            repo.saveInventoryItem(iItem);
        } else {
            iItem.setWareQuantity(iItem.getWareQuantity() + quantity);
            repo.updateInventoryItem(iItem);
        }
    }

    public void reportDamage(int id, int shelfQty, int warehouseQty, DamageReason reason) {
        Item item = repo.findById(id);
        if (item == null)
            throw new IllegalArgumentException("Item not found");
        if (shelfQty < 0 || warehouseQty < 0)
            throw new IllegalArgumentException("Quantity cannot be negative");
        if (shelfQty + warehouseQty == 0)
            throw new IllegalArgumentException("Total quantity must be greater than 0");

        InventoryItem iItem = repo.findInventoryItemByItemId(id);
        if (iItem == null)
            throw new IllegalArgumentException("Inventory details not found for this item");
        if (shelfQty > iItem.getShelfQuantity() || warehouseQty > iItem.getWareQuantity())
            throw new IllegalArgumentException("Cannot report more than current stock");

        iItem.setShelfQuantity(iItem.getShelfQuantity() - shelfQty);
        iItem.setWareQuantity(iItem.getWareQuantity() - warehouseQty);
        repo.updateInventoryItem(iItem);

        DamageReport report = new DamageReport(item, shelfQty + warehouseQty, reason);
        repo.saveDamageReport(report);
    }

 
    public void receivedOrder(Order order) {
        if (order == null || order.getItemsWithQuantities() == null)
            throw new IllegalArgumentException("Order or order items cannot be null");

        for (Map.Entry<Integer, Integer> entry : order.getItemsWithQuantities().entrySet()) {
            int itemId            = entry.getKey();
            int quantityReceived  = entry.getValue();
            Item item             = repo.findById(itemId);
            if (item == null) continue;
            receiveShipment(itemId, item.getCostPrice(), quantityReceived);
        }
        order.setStatus(OrderStatus.DELIVERED);
        repo.saveOrder(order);
    }



    public List<Order> triggerPeriodicOrders(LocalDate deliveryDate) {
        List<InventoryItem> allItems = repo.findAllInventoryItems();
        List<Order> orders = orderService.generatePeriodicOrder(allItems, deliveryDate);
        for (Order order : orders) {
            repo.saveOrder(order);
        }
        return orders;
    }

    public void updatePeriodicOrderInterval(int minutes) {
        if (orderService != null) {
            orderService.setPeriodicOrderIntervalMinutes(minutes);
        }
    }

    public int getPeriodicOrderIntervalMinutes() {
        if (orderService != null) {
            return orderService.getPeriodicOrderIntervalMinutes();
        }
        return -1;
    }

    public java.time.LocalDateTime getLastPeriodicCheckTime() {
        if (orderService != null) {
            return orderService.getLastPeriodicCheckTime();
        }
        return null;
    }

    public Order placeManualOrder(int itemId, int quantity, LocalDate deliveryDate) {
        if (orderService != null) {
            return orderService.placeManualOrder(itemId, quantity, deliveryDate);
        }
        return null;
    }


    public List<InventoryItem> getItemsBelowMinQuantity() {

        List<InventoryItem> allInventoryItems = repo.findAllInventoryItems();
        return warehouse.getItemsBelowMinQuantity(allInventoryItems);
    }

    public List<InventoryItem> getItemsByCategoryName(String name) {
        Category category = getCategoryByName(name);
        if (category == null)
            throw new IllegalArgumentException("Category not found");
        return getItemsByCategory(category);
    }

    public List<InventoryItem> getItemsByCategory(Category category) {
        if (category == null)
            throw new IllegalArgumentException("Category cannot be null");
        List<InventoryItem> categoryItems = new ArrayList<>();
        for (InventoryItem iItem : repo.findAllInventoryItems()) {
            Item item = iItem.getItem();
            if (item.getCategory().equals(category) || item.getCategory().isSonOf(category))
                categoryItems.add(iItem);
        }
        return categoryItems;
    }

    public InventoryItem getInventoryItemByItemId(int id) {
        return repo.findInventoryItemByItemId(id);
    }

    public List<Double> getItemPriceHistory(int id) {
        Item item = repo.findById(id);
        if (item == null)
            throw new IllegalArgumentException("Item not found");
        return item.getLastCostPrices();
    }

    public double calculateFinalPrice(int id) {
        Item item = repo.findById(id);
        if (item == null)
            throw new IllegalArgumentException("Item not found");

        double highestDiscount = 0.0;
        for (Discount discount : repo.findAllDiscounts()) {
            if (!discount.isActive()) continue;
            if (discount.getItem() != null && discount.getItem().getId() == id) {
                highestDiscount = Math.max(highestDiscount, discount.getPercentage());
            } else if (discount.getCategory() != null) {
                Category itemCategory     = item.getCategory();
                Category discountCategory = discount.getCategory();
                if (itemCategory.equals(discountCategory) || itemCategory.isSonOf(discountCategory))
                    highestDiscount = Math.max(highestDiscount, discount.getPercentage());
            }
        }
        return item.getSellingPrice() * (1 - (highestDiscount / 100.0));
    }

    public Category addCategory(String name) {
        return addCategory(name, null);
    }

    public Category addCategory(String name, Category parent) {
        Category existing = repo.findCategoryByName(name);
        if (existing != null) return existing;
        Category newCat = new Category(name, parent);
        repo.saveCategory(newCat);
        return newCat;
    }

    public Category getCategoryByName(String name) {
        return repo.findCategoryByName(name);
    }

    public List<Category> getCategories() {
        return repo.findAllCategories();
    }

    public Discount addDiscount(String name, double percentage, LocalDate startDate,
                                LocalDate endDate, Item item, Category category) {
        Discount discount = new Discount(name, percentage, startDate, endDate, item, category);
        repo.saveDiscount(discount);
        return discount;
    }

    public List<Discount> getDiscounts() {
        return repo.findAllDiscounts();
    }

    public List<Discount> getActiveDiscounts() {
        List<Discount> result = new ArrayList<>();
        for (Discount d : repo.findAllDiscounts())
            if (d.isActive()) result.add(d);
        return result;
    }

    public List<DamageReport> getDamageReports() {
        return repo.findAllDamageReports();
    }



    public void saveLowStockAlert(LowStockAlert alert) {
        repo.saveLowStockAlert(alert);
    }

    public List<LowStockAlert> getUnprocessedAlerts() {
        return repo.findUnprocessedAlerts();
    }

    public void markAlertProcessed(int alertId) {
        repo.updateAlertProcessed(alertId);
    }

    public void saveOrder(Order order) {
        repo.saveOrder(order);
    }

    public void updateOrderStatus(int orderId, inventory.domain.OrderStatus status) {
        repo.updateOrderStatus(orderId, status);
    }

    public List<Order> getAllOrders() {
        return repo.findAllOrders();
    }

    private boolean hasPendingShipment(int itemId) {
        for (Order order : repo.findAllOrders()) {
            if (order.getStatus() == inventory.domain.OrderStatus.SENT) {
                if (order.getItemsWithQuantities().containsKey(itemId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void startWarehouseMonitor() {
        if (monitorThread != null && monitorThread.isAlive()) return;
        monitorThread = new Thread(() -> {
            SystemLogger.log("[Warehouse Monitor] Started. Scanning every 5 seconds.");
            while (true) {
                try {
                    List<InventoryItem> allItems = repo.findAllInventoryItems();
                    List<InventoryItem> lowStockItems = warehouse.getItemsBelowMinQuantity(allItems);

                    for (InventoryItem iItem : lowStockItems) {
                        int itemId = iItem.getItem().getId();

                       
                        if (!repo.hasActiveAlertForItem(itemId) && !hasPendingShipment(itemId)) {
                            int deficit = iItem.getItem().getMinQuantity() - iItem.getTotalQuantity();
                            LowStockAlert alert = new LowStockAlert(iItem, deficit);
                            repo.saveLowStockAlert(alert);
                            alert.triggerAlert();
                        }
                    }

                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    SystemLogger.log("[Warehouse Monitor] Interrupted, stopping.");
                    break;
                } catch (Exception e) {
                    SystemLogger.log("[Warehouse Monitor] Error: " + e.getMessage());
                }
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.setName("warehouse-monitor");
        monitorThread.start();
    }



}