package employees.service;

import employees.domain.*;
import employees.repository.BranchRepository;
import employees.repository.DeliveryRepository;
import employees.repository.EmployeeRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DeliveryService {

    private static DeliveryService instance = null;
    private final DeliveryRepository deliveryRepo;
    private List<Delivery> allDeliveries;

    private DeliveryService() {
        try {
            this.deliveryRepo = new DeliveryRepository();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database: " + e.getMessage());
        }
        this.allDeliveries = new ArrayList<>();
    }

    public static DeliveryService getInstance() {
        if (instance == null) {
            instance = new DeliveryService();
        }
        return instance;
    }

    // Loads all deliveries from the database into memory
    // Called after EmployeeService is initialized so branches and employees exist
    public void loadFromDatabase() {
        try {
            BranchRepository branchRepo = new BranchRepository();
            EmployeeRepository empRepo = new EmployeeRepository();
            this.allDeliveries = deliveryRepo.findAll(branchRepo, empRepo);
        } catch (SQLException e) {
            System.out.println("Warning: Could not load deliveries from database: " + e.getMessage());
            this.allDeliveries = new ArrayList<>();
        }
    }

    // Creates a new delivery and adds the required workers to the shift
    public void addDelivery(String deliveryId, LocalDate date, ShiftType shiftType,
                            Branch branch, TruckType truckType) throws Exception {

        for (Delivery d : allDeliveries) {
            if (d.getDeliveryId().equals(deliveryId)) {
                throw new Exception("Error: Delivery with ID '" + deliveryId + "' already exists.");
            }
        }

        Shift shift = ShiftService.getInstance()
                .getShiftsByDateAndBranch(date, branch)
                .stream()
                .filter(s -> s.getType() == shiftType)
                .findFirst()
                .orElse(null);

        if (shift == null) {
            throw new Exception("Error: No " + shiftType + " shift on " + date + " for this branch.");
        }

        Delivery delivery = new Delivery(deliveryId, date, shiftType, branch, truckType);
        allDeliveries.add(delivery);

        // Save to database
        deliveryRepo.insert(delivery);

        ShiftService.getInstance().addDeliveryRequirementsToShift(date, shiftType, branch);
    }

    // Connects a driver to a specific delivery
    public void assignDriver(String deliveryId, String driverId) throws Exception {

        Delivery delivery = getDeliveryById(deliveryId);
        if (delivery == null) {
            throw new Exception("Error: Delivery not found.");
        }

        Employee driver = EmployeeService.getInstance().getEmployeeById(driverId);
        if (driver == null) {
            throw new Exception("Error: Employee not found.");
        }
        if (!driver.isActive()) {
            throw new Exception("Error: " + driver.getFullName() + " is not active.");
        }

        Role driverRole = EmployeeService.getInstance().getRoleFromPool("Driver");
        if (!driver.isQualifiedFor(driverRole)) {
            throw new Exception("Error: " + driver.getFullName() + " is not certified as Driver.");
        }

        Shift shift = ShiftService.getInstance()
                .getShiftsByDateAndBranch(delivery.getDate(), delivery.getBranch())
                .stream()
                .filter(s -> s.getType() == delivery.getShiftType())
                .findFirst()
                .orElse(null);

        if (shift == null || !shift.isEmployeeAssigned(driver)) {
            throw new Exception("Error: Driver must be assigned to the shift before being assigned to a delivery.");
        }

        delivery.setDriver(driver);

        // Update driver in database
        deliveryRepo.updateDriver(deliveryId, driverId);
    }

    // Checks if there is any delivery for a specific shift
    public boolean hasDeliveryForShift(LocalDate date, ShiftType shiftType, Branch branch) {
        for (Delivery d : allDeliveries) {
            if (d.getDate().equals(date) && d.getShiftType() == shiftType && d.getBranch().equals(branch)) {
                return true;
            }
        }
        return false;
    }

    // Returns a delivery by ID
    public Delivery getDeliveryById(String deliveryId) {
        for (Delivery d : allDeliveries) {
            if (d.getDeliveryId().equals(deliveryId)) {
                return d;
            }
        }
        return null;
    }

    // Returns all deliveries
    public List<Delivery> getAllDeliveries() {
        return allDeliveries;
    }

    // Returns all deliveries for a specific branch
    public List<Delivery> getDeliveriesByBranch(String branchId) {
        List<Delivery> result = new ArrayList<>();
        for (Delivery d : allDeliveries) {
            if (d.getBranch().getBranchId().equals(branchId)) {
                result.add(d);
            }
        }
        return result;
    }
}