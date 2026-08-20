package inventory.domain;

public class OrderItem {
    private int itemId;
    private int quantity;
    private double unitPrice;

    public OrderItem(int itemId, int quantity, double unitPrice) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getTotalLinePrice() {
        return quantity * unitPrice;
    }
}