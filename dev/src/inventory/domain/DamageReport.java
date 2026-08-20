package inventory.domain;

import java.time.LocalDate;

public class DamageReport {

    private int id;
    private static int idCounter = 10;
    private int itemId;
    private int quantity;
    private LocalDate reportDate;
    private DamageReason reason;

    public DamageReport (Item item, int quantity, DamageReason reason) {

        if (item == null)
            throw new IllegalArgumentException("Item cannot be null");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive");
        if (reason == null)
            throw new IllegalArgumentException("Reason cannot be null");

        this.itemId = item.getId();
        this.id = idCounter++;
        this.quantity = quantity;
        this.reason = reason;
        this.reportDate = LocalDate.now();
    }

    public int getId() {
        return id;
    }

    public int getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public DamageReason getReason() {
        return reason;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}
