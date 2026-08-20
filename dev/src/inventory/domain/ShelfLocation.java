package inventory.domain;

public class ShelfLocation {

    private static int idCounter = 1;

    private int id;
    private String aisle;
    private int row;
    private int shelf;

    public ShelfLocation (String aisle, int row, int shelf) {

        this.id = idCounter++;
        this.aisle = aisle;
        this.row = row;
        this.shelf = shelf;
    }

    public int getId()      { return id; }
    public String getAisle(){ return aisle; }
    public int getRow()     { return row; }
    public int getShelf()   { return shelf; }

    @Override
    public String toString() {
        return "Aisle: " + aisle + ", Row: " + row + ", Shelf: " + shelf;
    }
}