package inventory.service;

import inventory.domain.InventoryItem;
import inventory.domain.Item;
import inventory.domain.LowStockAlert;
import inventory.domain.Order;
import inventory.domain.OrderItem;
import inventory.domain.OrderType;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OrderService {

    private InventoryService inventoryService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private int periodicOrderIntervalMinutes = 5;
    private java.time.LocalDateTime lastPeriodicCheckTime = null;
    
    private Thread periodicThread;
    private Thread automaticShortageThread;

    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public void setPeriodicOrderIntervalMinutes(int minutes) {
        this.periodicOrderIntervalMinutes = minutes;
        SystemLogger.log("[OrderService] Periodic order interval updated to " + minutes + " minutes.");
    }

    public int getPeriodicOrderIntervalMinutes() {
        return periodicOrderIntervalMinutes;
    }

    public java.time.LocalDateTime getLastPeriodicCheckTime() {
        return lastPeriodicCheckTime;
    }

    public void startPeriodicOrderTimer() {
        if (periodicThread != null && periodicThread.isAlive()) return;
        periodicThread = new Thread(() -> {
            long lastRun = System.currentTimeMillis();
            while (true) {
                try {
                    try {
                        long current = System.currentTimeMillis();
                        if (current - lastRun >= periodicOrderIntervalMinutes * 60 * 1000L) {
                            if (inventoryService != null) {
                                lastPeriodicCheckTime = java.time.LocalDateTime.now();
                                SystemLogger.log("[Periodic Order Monitor] Running periodic order check...");

                                inventoryService.triggerPeriodicOrders(LocalDate.now().plusDays(1));
                            }
                            lastRun = current;
                        }
                        Thread.sleep(1000); // Check every second to quickly respond to interval changes
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        SystemLogger.log("[Periodic Order Monitor] Stopped.");
                        break;
                    } catch (Exception e) {
                        SystemLogger.log("[Periodic Order Monitor] Error during periodic order: " + e.getMessage());
                        // Continue running - don't let a single failure kill the timer
                    }
                } catch (Exception fatal) {
                    SystemLogger.log("[Periodic Order Monitor] Fatal error: " + fatal.getMessage());
                }
            }
        });
        periodicThread.setDaemon(true);
        periodicThread.setName("periodic-order-monitor");
        periodicThread.start();
    }

    public void automaticShortageOrder() {
        if (automaticShortageThread != null && automaticShortageThread.isAlive()) return;
        automaticShortageThread = new Thread(() -> {
            while (true) {
                try {
                    if (inventoryService != null) {
                        List<LowStockAlert> unprocessed = inventoryService.getUnprocessedAlerts();

                        for (LowStockAlert alert : unprocessed) {
                            int itemId = alert.getInventoryItem().getItem().getId();
                            int quantityNeeded = alert.getQuantityDeficit() + 50;
                            Order order = makeBestOrder(itemId, quantityNeeded);
                            if (order != null) {
                                inventoryService.saveOrder(order);
                            }
                            inventoryService.markAlertProcessed(alert.getAlertId());
                        }
                    }

                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    SystemLogger.log("[Order Monitor] Stopped.");
                    break;
                }
            }
        });
        automaticShortageThread.setDaemon(true);
        automaticShortageThread.setName("automatic-shortage-order");
        automaticShortageThread.start();
    }

    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        if (periodicThread != null) {
            periodicThread.interrupt();
        }
        if (automaticShortageThread != null) {
            automaticShortageThread.interrupt();
        }
    }

    private Order makeBestOrder(int itemId, int quantityNeeded) {
        if (quantityNeeded <= 0) {
            return null;
        }

        Item item = inventoryService.findById(itemId);
        double basePrice = (item != null) ? item.getCostPrice() : 5.0;

        String bestSupplier = SupplierDummy.getBestDeal(itemId, quantityNeeded);
        double totalPrice = SupplierDummy.calculateFinalPrice(basePrice, quantityNeeded, bestSupplier);
        double unitPrice = totalPrice / quantityNeeded;

        Order order = new Order(bestSupplier, OrderType.SHORTAGE);
        order.addItem(itemId, quantityNeeded, unitPrice);
        sendOrder(order);

        SystemLogger.log(String.format("[Order Monitor] Order ID %d created for item %d from %s (qty: %d, total: %.2f ILS)",
                order.getId(), itemId, bestSupplier, quantityNeeded, totalPrice));

        return order;
    }

    public List<Order> generatePeriodicOrder(List<InventoryItem> allItems, LocalDate deliveryDate) {

        Map<String, Order> supplierOrdersMap = new HashMap<>();
        List<Order> generatedOrders = new ArrayList<>();

        SystemLogger.log("[Automated System] Generating periodic restock orders for delivery on: " + deliveryDate);

        List<String> eligibleSuppliers = getEligibleSuppliersForPeriodicOrder();

        if (eligibleSuppliers.isEmpty()) {
            SystemLogger.log("[Periodic Order System] No suppliers meet the 1-day advance notice requirement. Skipping periodic order.");
            return generatedOrders;
        }

        for (InventoryItem iItem : allItems) {
            int itemId = iItem.getItem().getId();
            int currentQty = iItem.getTotalQuantity();
            int minQty = iItem.getItem().getMinQuantity();

            // Periodic orders always order a fixed batch, regardless of current stock
            int quantityToOrder = 50;
            int projectedStock = currentQty + quantityToOrder;

            double basePrice = iItem.getItem().getCostPrice();

            String bestSupplier = getBestEligibleSupplier(itemId, quantityToOrder, basePrice, eligibleSuppliers);
            double totalPrice = SupplierDummy.calculateFinalPrice(basePrice, quantityToOrder, bestSupplier);
            double unitPrice = totalPrice / quantityToOrder;

            supplierOrdersMap.computeIfAbsent(bestSupplier, s -> new Order(s, OrderType.PERIODIC))
                    .addItem(itemId, quantityToOrder, unitPrice);

            SystemLogger.log(String.format("  [PERIODIC] Item: %-30s (ID: %d) -> Supplier: %-10s | To order: %d | Projected: %d",
                    iItem.getItem().getName(), itemId, bestSupplier, quantityToOrder, projectedStock));
        }

        if (supplierOrdersMap.isEmpty()) {
            SystemLogger.log("[Automated System] All items are sufficiently stocked. No periodic orders needed.");
        } else {
            for (Order order : supplierOrdersMap.values()) {
                sendOrder(order);
                generatedOrders.add(order);
            }
        }

        return generatedOrders;
    }

    private List<String> getEligibleSuppliersForPeriodicOrder() {
        LocalDate today = LocalDate.now();
        List<String> eligibleSuppliers = new ArrayList<>();
        List<String> allSuppliers = SupplierDummy.getSuppliers(0);

        for (String supplier : allSuppliers) {

            List<DayOfWeek> deliveryDays = SupplierDummy.getDeliveryDays(supplier);

            long minDaysBetween = Long.MAX_VALUE;

            for (DayOfWeek deliveryDay : deliveryDays) {
                LocalDate nextDelivery = today.with(TemporalAdjusters.nextOrSame(deliveryDay));
                if (nextDelivery.equals(today)) {
                    nextDelivery = nextDelivery.plusWeeks(1);
                }
                long daysBetween = ChronoUnit.DAYS.between(today, nextDelivery);
                if (daysBetween >= 1 && daysBetween < minDaysBetween) {
                    minDaysBetween = daysBetween;
                }
            }

            if (minDaysBetween >= 1 && minDaysBetween < Long.MAX_VALUE) {
                eligibleSuppliers.add(supplier);
            }
        }
        return eligibleSuppliers;
    }

    private String getBestEligibleSupplier(int itemId, int quantity, double basePrice, List<String> eligibleSuppliers) {
        String best = null;
        double lowestPrice = Double.MAX_VALUE;

        for (String supplier : eligibleSuppliers) {
            double price = SupplierDummy.calculateFinalPrice(basePrice, quantity, supplier);
            if (price < lowestPrice) {
                lowestPrice = price;
                best = supplier;
            }
        }

        // If prices are identical, pick randomly
        if (best == null) {
            best = eligibleSuppliers.get(new Random().nextInt(eligibleSuppliers.size()));
        }
        return best;
    }

    private void sendOrder(Order order) {
        StringBuilder itemsStr = new StringBuilder();
        for (OrderItem item : order.getOrderItems()) {
            itemsStr.append(item.getItemId()).append(" (qty ").append(item.getQuantity()).append("), ");
        }
        if (itemsStr.length() > 0) {
            itemsStr.setLength(itemsStr.length() - 2);
        }

        SystemLogger.log(String.format("[Order Sent] ID: %d | Supplier: %s | Total: %.2f ILS | Items: [%s]",
                order.getId(), order.getSupplierName(), order.getTotalPrice(), itemsStr.toString()));

        order.markAsSent();
        if (inventoryService != null) {
            inventoryService.updateOrderStatus(order.getId(), inventory.domain.OrderStatus.SENT);
        }

        int delaySeconds = 15;
        String supplier = order.getSupplierName();
        if ("HADADI".equalsIgnoreCase(supplier)) {
            delaySeconds = 10;
        } else if ("LESHEM".equalsIgnoreCase(supplier)) {
            delaySeconds = 20;
        } else if ("TAKACH".equalsIgnoreCase(supplier)) {
            delaySeconds = 15;
        } else if ("BARGILOVSKI".equalsIgnoreCase(supplier)) {
            delaySeconds = 12;
        }

        SystemLogger.log(String.format("[Delivery System] Order %d from %s is on its way. Estimated arrival: %d seconds.",
                order.getId(), supplier, delaySeconds));

        scheduler.schedule(() -> receiveOrder(order), delaySeconds, TimeUnit.SECONDS);
    }

    private void receiveOrder(Order order) {
        SystemLogger.log(String.format("[Order Delivered] ID: %d | Supplier: %s", order.getId(), order.getSupplierName()));

        if (inventoryService != null) {
            inventoryService.receivedOrder(order);
        }
        order.setStatus(inventory.domain.OrderStatus.DELIVERED);
        if (inventoryService != null) {
            inventoryService.updateOrderStatus(order.getId(), inventory.domain.OrderStatus.DELIVERED);
        }
    }

    public Order placeManualOrder(int itemId, int quantity, LocalDate deliveryDate) {
        if (deliveryDate == null) {
            // Immediate order
            SystemLogger.log("[Manual System] Placing immediate manual order for item " + itemId + " (qty: " + quantity + ")");
            Order order = makeBestOrder(itemId, quantity);
            if (order != null && inventoryService != null) {
                inventoryService.saveOrder(order);
            }
            return order;
        } else {
            // Future order
            SystemLogger.log("[Manual System] Placing future manual order for item " + itemId + " (qty: " + quantity + ") for delivery on " + deliveryDate);

            Item item = inventoryService.findById(itemId);
            double basePrice = (item != null) ? item.getCostPrice() : 5.0;

            List<String> allSuppliers = SupplierDummy.getSuppliers(itemId);
            List<String> eligibleSuppliers = new ArrayList<>();
            for (String supplier : allSuppliers) {
                List<DayOfWeek> days = SupplierDummy.getDeliveryDays(supplier);
                if (days.contains(deliveryDate.getDayOfWeek())) {
                    eligibleSuppliers.add(supplier);
                }
            }

            if (eligibleSuppliers.isEmpty()) {
                throw new IllegalArgumentException("No supplier is available to deliver on " + deliveryDate.getDayOfWeek());
            }

            String bestSupplier = getBestEligibleSupplier(itemId, quantity, basePrice, eligibleSuppliers);
            double totalPrice = SupplierDummy.calculateFinalPrice(basePrice, quantity, bestSupplier);
            double unitPrice = totalPrice / quantity;

            Order order = new Order(bestSupplier, OrderType.MANUAL);
            order.addItem(itemId, quantity, unitPrice);
            sendOrder(order);
            if (inventoryService != null) {
                inventoryService.saveOrder(order);
            }
            return order;
        }
    }
}
