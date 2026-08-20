package inventory.domain;

import inventory.service.InventoryService;

public class DataLoader {

    private InventoryService service;

    public DataLoader(InventoryService service) {
        this.service = service;
    }

    /**
     *Parses location code like "1A" into a ShelfLocation.
     * First character = row number, second character = aisle letter.
     */

    private static ShelfLocation loc(String code) {
        int row     = Character.getNumericValue(code.charAt(0));
        String aisle = String.valueOf(code.charAt(1));
        return new ShelfLocation(aisle, row, 1);
    }

    /** Sets shelf/warehouse quantities and persists the change to the DB. */
    private void setup(InventoryItem tempItem, String locCode, int shelf, int warehouse) {
        tempItem.setShelfLocation(loc(locCode));
        tempItem.setShelfQuantity(shelf);
        tempItem.setWarehouseQuantity(warehouse);
        service.updateInventoryItem(tempItem);
    }

    public void loadData() {
        if (service.getCategories() != null && !service.getCategories().isEmpty()) {
            System.out.println(">>> [DataLoader] Database already contains data. Skipping initial load.");
            return;
        }

        Category dairy = service.addCategory("Dairy");
        Category milk = service.addCategory("Milk", dairy);
        Category cheese = service.addCategory("Cheese", dairy);
        Category yogurt = service.addCategory("Yogurt", dairy);

        InventoryItem tempItem;

        ///// Milk /////
        tempItem = service.addItem("Milk 3% Tnuva", 7.4, 7.9, "Tnuva", service.addCategory("1l", milk));
        setup(tempItem, "1A", 20, 80);

        tempItem = service.addItem("Milk 3% Tnuva", 6.2, 6.8, "Tnuva", service.addCategory("750ml", milk));
        setup(tempItem, "1B", 15, 60);

        tempItem = service.addItem("Milk 3% Tara", 7.2, 7.7, "Tara", service.addCategory("1l", milk));
        setup(tempItem, "1C", 20, 80);

        tempItem = service.addItem("Milk 3% Tara", 6.5, 7.3, "Tara", service.addCategory("750ml", milk));
        setup(tempItem, "1D", 15, 60);

        tempItem = service.addItem("Milk 9% Tnuva", 7.4, 7.9, "Tnuva", service.addCategory("1l", milk));
        setup(tempItem, "1E", 15, 50);

        tempItem = service.addItem("Milk 9% Tnuva", 6.2, 6.8, "Tnuva", service.addCategory("750ml", milk));
        setup(tempItem, "1F", 10, 40);

        ///// Cheese /////
        tempItem = service.addItem("Provolone Sliced", 10.9, 12.5, "Gad", service.addCategory("100g", cheese));
        setup(tempItem, "2A", 30, 70);

        tempItem = service.addItem("Roquefort", 15.0, 18.0, "Tnuva", service.addCategory("100g", cheese));
        setup(tempItem, "2B", 20, 40);

        tempItem = service.addItem("Emek Sliced 28%", 13.5, 15.9, "Tnuva", service.addCategory("200g", cheese));
        setup(tempItem, "2C", 25, 75);

        tempItem = service.addItem("Noam Sliced 28%", 12.9, 14.5, "Tara", service.addCategory("200g", cheese));
        setup(tempItem, "2D", 25, 75);

        tempItem = service.addItem("Bulgarian Cheese 5%", 14.0, 16.5, "Gad", service.addCategory("200g", cheese));
        setup(tempItem, "2E", 20, 60);

        tempItem = service.addItem("White Cheese 5%", 4.8, 5.5, "Tnuva", service.addCategory("250g", cheese));
        setup(tempItem, "2F", 30, 70);

        tempItem = service.addItem("Cottage Cheese 5%", 5.8, 6.4, "Tnuva", service.addCategory("250g", cheese));
        setup(tempItem, "2G", 30, 70);

        tempItem = service.addItem("Symphony Cream Cheese", 10.5, 12.0, "Strauss", service.addCategory("250g", cheese));
        setup(tempItem, "2H", 25, 50);

        tempItem = service.addItem("White Cheese 5%", 4.7, 5.4, "Tara", service.addCategory("250g", cheese));
        setup(tempItem, "2I", 25, 60);

        tempItem = service.addItem("White Cheese 5% Family", 8.5, 9.8, "Tnuva", service.addCategory("500g", cheese));
        setup(tempItem, "2J", 20, 50);

        tempItem = service.addItem("Cottage Cheese 5% Big", 10.5, 11.5, "Tnuva", service.addCategory("500g", cheese));
        setup(tempItem, "2K", 20, 50);



        ///// Yogurt /////
        tempItem = service.addItem("Danone Fruit 3%", 4.5, 5.2, "Strauss", service.addCategory("150g", yogurt));
        setup(tempItem, "3A", 30, 70);

        tempItem = service.addItem("Muller Froop", 4.6, 5.3, "Tara", service.addCategory("150g", yogurt));
        setup(tempItem, "3B", 25, 60);

        tempItem = service.addItem("Bio Yogurt 3%", 3.9, 4.5, "Tnuva", service.addCategory("150g", yogurt));
        setup(tempItem, "3C", 25, 60);

        tempItem = service.addItem("Greek Yogurt 7%", 5.5, 6.2, "Strauss", service.addCategory("200g", yogurt));
        setup(tempItem, "3D", 25, 55);

        tempItem = service.addItem("Goat Milk Yogurt", 6.9, 7.8, "Tnuva", service.addCategory("200g", yogurt));
        setup(tempItem, "3E", 20, 50);

        tempItem = service.addItem("Danone Bio 3%", 4.9, 5.6, "Strauss", service.addCategory("200g", yogurt));
        setup(tempItem, "3F", 25, 55);

        tempItem = service.addItem("Up Drink Yogurt", 7.5, 8.5, "Tnuva", service.addCategory("400g", yogurt));
        setup(tempItem, "3G", 15, 40);

        tempItem = service.addItem("Danone Multi", 8.0, 9.2, "Strauss", service.addCategory("400g", yogurt));
        setup(tempItem, "3H", 15, 40);

        tempItem = service.addItem("Bio Yogurt Bucket 3%", 12.9, 14.5, "Tnuva", service.addCategory("850g", yogurt));
        setup(tempItem, "3I", 10, 30);
    }
}
