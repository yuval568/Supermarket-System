package inventory.domain;

public class InventoryItem {
    private Item item;
    private int shelfQuantity;
    private int wareQuantity;
    private ShelfLocation shelfLocation;

    public InventoryItem(Item item, int shelfQuantity, int wareQuantity) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        this.item = item;
        this.shelfQuantity = shelfQuantity;
        this.wareQuantity = wareQuantity;
    }

    public Item getItem() { return item; }
    public int getShelfQuantity() { return shelfQuantity; }
    public void setShelfQuantity(int shelfQuantity) { this.shelfQuantity = shelfQuantity; }

    public int getWareQuantity() { return wareQuantity; }
    public void setWareQuantity(int wareQuantity) { this.wareQuantity = wareQuantity; }


    public void setWarehouseQuantity(int wareQuantity) { this.wareQuantity = wareQuantity; }

    public int getTotalQuantity() {
        return shelfQuantity + wareQuantity;
    }

    public ShelfLocation getShelfLocation() { return shelfLocation; }
    public void setShelfLocation(ShelfLocation shelfLocation) { this.shelfLocation = shelfLocation; }


    public String getLocation() {
        if (shelfLocation == null) {
            return "Not set";
        }
        return shelfLocation.toString();
    }
}