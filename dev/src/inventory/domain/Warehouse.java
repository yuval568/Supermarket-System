package inventory.domain;

import java.util.ArrayList;
import java.util.List;

public class Warehouse {
    private int id;
    private String name;

    public Warehouse(int id ,String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public List<InventoryItem> getItemsBelowMinQuantity(List<InventoryItem> liveItemsFromDb) {
        List<InventoryItem> lowStockItems = new ArrayList<>();
        for (InventoryItem iItem : liveItemsFromDb) {
            if (iItem.getTotalQuantity() < iItem.getItem().getMinQuantity()) {
                lowStockItems.add(iItem);
            }
        }
        return lowStockItems;
    }
}