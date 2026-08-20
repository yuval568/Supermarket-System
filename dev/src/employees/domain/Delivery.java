package employees.domain;

import java.time.LocalDate;

// This class represents a delivery
public class Delivery {

    // The ID of the delivery
    private String deliveryId;

    // The date of the delivery
    private LocalDate date;

    // The shift time (Morning or Evening)
    private ShiftType shiftType;

    // The branch that gets the delivery
    private Branch branch;

    // The type of the truck
    private TruckType truckType;

    // The driver who does the delivery
    private Employee driver;

    // Create a new delivery object
    public Delivery(String deliveryId, LocalDate date, ShiftType shiftType,
                    Branch branch, TruckType truckType) throws Exception {

        // Check if ID is empty
        if (deliveryId == null || deliveryId.trim().isEmpty()) {
            throw new Exception("Error: Delivery ID cannot be empty.");
        }

        // Check if date is missing
        if (date == null) {
            throw new Exception("Error: Date cannot be null.");
        }

        // Check if shift type is missing
        if (shiftType == null) {
            throw new Exception("Error: Shift type cannot be null.");
        }

        // Check if branch is missing
        if (branch == null) {
            throw new Exception("Error: Branch cannot be null.");
        }

        // Check if truck type is missing
        if (truckType == null) {
            throw new Exception("Error: Truck type cannot be null.");
        }

        // Set the values
        this.deliveryId = deliveryId;
        this.date = date;
        this.shiftType = shiftType;
        this.branch = branch;
        this.truckType = truckType;

        // Start with no driver
        this.driver = null;
    }

    // Return a string with the delivery details
    @Override
    public String toString() {
        return "Delivery{" +
                "ID='" + deliveryId + "'" +
                ", Date=" + date +
                ", Shift=" + shiftType +
                ", Branch=" + branch.getName() +
                ", Truck=" + truckType +
                ", Driver=" + (driver != null ? driver.getFullName() : "Not assigned") +
                '}';
    }

    // Get the delivery ID
    public String getDeliveryId() { return deliveryId; }

    // Get the date
    public LocalDate getDate() { return date; }

    // Get the shift type
    public ShiftType getShiftType() { return shiftType; }

    // Get the branch
    public Branch getBranch() { return branch; }

    // Get the truck type
    public TruckType getTruckType() { return truckType; }

    // Get the driver
    public Employee getDriver() { return driver; }

    // Set a new driver for this delivery
    public void setDriver(Employee driver) throws Exception {

        // Check if driver is missing
        if (driver == null) {
            throw new Exception("Error: Driver cannot be null.");
        }

        // Check if the driver has the right license for the truck
        if (driver.getLicenseType() == null || !driver.getLicenseType().equals(this.truckType)) {
            throw new Exception("Error: " + driver.getFullName() +
                    " does not have the required license for truck type: " + this.truckType);
        }

        // Save the driver
        this.driver = driver;
    }
}