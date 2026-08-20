package inventory.domain;

import java.util.ArrayList;
import java.util.List;

public class Item {

    private String name;
    private static int idCounter = 100000;
    private int id;
    private double costPrice;
    private double sellingPrice;
    private int minQuantity;
    private String manufacturer;
    private Category category;
    private List<Double> lastSellingPrices;
    private List<Double> lastCostPrices;


    public Item(String name, double costPrice, double sellingPrice, String manufacturer, Category category) {

        if (name == null) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        this.id = idCounter++;
        this.name = name;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.minQuantity = 50;
        this.manufacturer = manufacturer;
        this.category = category;
        this.lastSellingPrices = new ArrayList<>();
        this.lastCostPrices = new ArrayList<>();
    }

    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        if (name == null) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.name = name;
    }

    public double getCostPrice() {

        return costPrice;
    }

    public double getSellingPrice() {

        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {

        if (sellingPrice < 0) throw new IllegalArgumentException("Price cannot be negative");

        if (this.sellingPrice > 0) {
            if (lastSellingPrices.size() == 5)
                lastSellingPrices.remove(0);
            lastSellingPrices.add(this.sellingPrice);
        }
        this.sellingPrice = sellingPrice;
    }

    public int getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(int minQuantity) {
        if (minQuantity < 0) throw new IllegalArgumentException("Minimum quantity cannot be negative");
        this.minQuantity = minQuantity;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        if (category == null) throw new IllegalArgumentException("Category cannot be null");
        this.category = category;
    }

    public List<Double> getLastSellingPrices() {
        return lastSellingPrices;
    }

    public List<Double> getLastCostPrices() {
        return lastCostPrices;
    }

    public void updateCostPrice(double newPrice) {
        if (newPrice < 0) throw new IllegalArgumentException("Price cannot be negative");
        if (lastCostPrices.size() == 5) lastCostPrices.remove(0);
        lastCostPrices.add(this.costPrice);
        this.costPrice = newPrice;
    }

    @Override
    public String toString() {
        String categoryName = (category != null) ? category.getFullName() : "None";
        return "ID: " + id + "\n" +
                "Name: " + name + "\n" +
                "Manufacturer: " + manufacturer + "\n" +
                "Cost Price: " + costPrice + "\n" +
                "Selling Price: " + sellingPrice + "\n" +
                "Category: " + categoryName + "\n" +
                "Min qty: " + minQuantity;
    }
}