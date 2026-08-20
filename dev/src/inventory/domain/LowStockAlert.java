package inventory.domain;

import java.util.Date;

public class LowStockAlert {

    private static int idCounter = 1;

    private int alertId;
    private Date alertDate;
    private InventoryItem inventoryItem;
    private int quantityDeficit;
    private boolean processed;

    public LowStockAlert(InventoryItem inventoryItem, int quantityDeficit) {
        if (inventoryItem == null) {
            throw new IllegalArgumentException("InventoryItem cannot be null");
        }
        this.alertId = idCounter++;
        this.alertDate = new Date();
        this.inventoryItem = inventoryItem;
        this.quantityDeficit = quantityDeficit;
        this.processed = false;
    }

    public int getAlertId() { return alertId; }
    public Date getAlertDate() { return alertDate; }
    public InventoryItem getInventoryItem() { return inventoryItem; }
    public int getQuantityDeficit() { return quantityDeficit; }
    public boolean isProcessed() { return processed; }

    public void markProcessed() {
        this.processed = true;
    }

    public void triggerAlert() {
        inventory.service.SystemLogger.log("[LowStockAlert #" + alertId + "] ALERT: Item '"
                + inventoryItem.getItem().getName()
                + "' is below minimum stock level."
                + " Current: " + inventoryItem.getTotalQuantity()
                + " | Minimum: " + inventoryItem.getItem().getMinQuantity()
                + " | Deficit: " + quantityDeficit);
    }
}