package inventory.domain;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Order {

    private static int idCounter = 1000;

    private int id;
    private String supplierName;
    private Map<Integer, OrderItem> itemsMap;
    private double totalPrice;
    private OrderStatus status;
    private OrderType orderType;

    public Order(String supplierName) {
        this(supplierName, OrderType.MANUAL);
    }

    public Order(String supplierName, OrderType orderType) {
        this.id = idCounter++;
        this.supplierName = supplierName;
        this.itemsMap = new HashMap<>();
        this.totalPrice = 0.0;
        this.status = OrderStatus.PENDING;
        this.orderType = orderType;
    }

    public static void updateIdCounter(int maxId) {
        if (maxId >= idCounter) {
            idCounter = maxId + 1;
        }
    }

    public static void setCounter(int counter) { idCounter = counter; }

    public void addItem(int itemId, int quantity, double pricePerUnit) {
        if (itemsMap.containsKey(itemId)) {
            OrderItem existing = itemsMap.get(itemId);
            int newQuantity = existing.getQuantity() + quantity;
            itemsMap.put(itemId, new OrderItem(itemId, newQuantity, pricePerUnit));
        } else {
            itemsMap.put(itemId, new OrderItem(itemId, quantity, pricePerUnit));
        }
        recalculateTotalPrice();
    }

    private void recalculateTotalPrice() {
        this.totalPrice = 0.0;
        for (OrderItem item : itemsMap.values()) {
            this.totalPrice += item.getTotalLinePrice();
        }
    }

    public void markAsSent() {
        if (this.itemsMap.isEmpty())
            throw new IllegalStateException("Cannot send an empty order. Please add items first.");
        this.status = OrderStatus.SENT;
    }



    public int getId()                         { return id; }
    public String getSupplierName()            { return supplierName; }
    public double getTotalPrice()              { return totalPrice; }
    public OrderStatus getStatus()             { return status; }
    public void setStatus(OrderStatus status)  { this.status = status; }
    public OrderType getOrderType()            { return orderType; }
    public void setOrderType(OrderType type)   { this.orderType = type; }


    public Collection<OrderItem> getOrderItems() {
        return itemsMap.values();
    }


    public Map<Integer, Integer> getItemsWithQuantities() {
        Map<Integer, Integer> result = new HashMap<>();
        for (Map.Entry<Integer, OrderItem> entry : itemsMap.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getQuantity());
        }
        return result;
    }
}