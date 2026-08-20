package inventory.domain;

 import java.time.LocalDate;

public class Discount {

    private Item item;
    private Category category;
    private String name;
    private int id;
    private double percentage;
    private LocalDate startDate;
    private LocalDate endDate;
    private static int idCounter = 100;

    public Discount (String name, double percentage, LocalDate startDate, LocalDate endDate, Item item, Category category) {

        if (item == null && category == null)
            throw new IllegalArgumentException("Discount must apply to an item or a category");
        if (startDate == null || endDate == null)
            throw new IllegalArgumentException("Invalid Dates");
        if (endDate.isBefore(startDate))
            throw new IllegalArgumentException("End date must be after start date");
        if (percentage < 0 || percentage > 100)
            throw new IllegalArgumentException("Percentage must be between 0 - 100");

        this.id = idCounter++;
        this.name = name;
        this.percentage = percentage;
        this.item = item;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
    }


    public boolean isActive() {

        LocalDate now = LocalDate.now();
        return !now.isAfter(endDate) && !now.isBefore(startDate);
    }

    public double applyDiscount(double price){

        return price * (1 - (this.percentage / 100));
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public double getPercentage() {
        return percentage;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Item getItem() {
        return item;
    }

    public Category getCategory() {
        return category;
    }
}
