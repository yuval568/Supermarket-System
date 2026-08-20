package employees.repository;

import employees.domain.*;
import employees.dto.DeliveryDTO;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DeliveryRepository {

    private final Connection connection;

    public DeliveryRepository() throws SQLException {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    // Saves a new delivery to the database
    public void insert(Delivery delivery) throws SQLException {
        String sql = """
                INSERT INTO deliveries (delivery_id, date, shift_type, branch_id, truck_type, driver_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, delivery.getDeliveryId());
            stmt.setString(2, delivery.getDate().toString());
            stmt.setString(3, delivery.getShiftType().name());
            stmt.setString(4, delivery.getBranch().getBranchId());
            stmt.setString(5, delivery.getTruckType().name());
            stmt.setString(6, delivery.getDriver() != null ? delivery.getDriver().getId() : null);
            stmt.executeUpdate();
        }
    }

    // Updates the driver of a delivery
    public void updateDriver(String deliveryId, String driverId) throws SQLException {
        String sql = "UPDATE deliveries SET driver_id = ? WHERE delivery_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, driverId);
            stmt.setString(2, deliveryId);
            stmt.executeUpdate();
        }
    }

    // Returns all deliveries from the database
    public List<Delivery> findAll(BranchRepository branchRepo,
                                  EmployeeRepository empRepo) throws SQLException {
        List<Delivery> deliveries = new ArrayList<>();
        String sql = "SELECT * FROM deliveries";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                DeliveryDTO dto = mapRowToDTO(rs);
                Delivery delivery = dtoToDelivery(dto, branchRepo, empRepo);
                if (delivery != null) deliveries.add(delivery);
            }
        }
        return deliveries;
    }

    // Returns all deliveries for a specific branch
    public List<Delivery> findByBranch(String branchId, BranchRepository branchRepo,
                                       EmployeeRepository empRepo) throws SQLException {
        List<Delivery> deliveries = new ArrayList<>();
        String sql = "SELECT * FROM deliveries WHERE branch_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DeliveryDTO dto = mapRowToDTO(rs);
                    Delivery delivery = dtoToDelivery(dto, branchRepo, empRepo);
                    if (delivery != null) deliveries.add(delivery);
                }
            }
        }
        return deliveries;
    }

    // Returns a specific delivery by ID
    public Delivery findById(String deliveryId, BranchRepository branchRepo,
                             EmployeeRepository empRepo) throws SQLException {
        String sql = "SELECT * FROM deliveries WHERE delivery_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, deliveryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    DeliveryDTO dto = mapRowToDTO(rs);
                    return dtoToDelivery(dto, branchRepo, empRepo);
                }
            }
        }
        return null;
    }

    // Checks if a delivery exists for a specific shift
    public boolean existsForShift(LocalDate date, ShiftType shiftType,
                                  String branchId) throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM deliveries
                WHERE date = ? AND shift_type = ? AND branch_id = ?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, date.toString());
            stmt.setString(2, shiftType.name());
            stmt.setString(3, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Checks if a delivery ID already exists
    public boolean exists(String deliveryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM deliveries WHERE delivery_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, deliveryId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Maps a database row to a DeliveryDTO
    private DeliveryDTO mapRowToDTO(ResultSet rs) throws SQLException {
        DeliveryDTO dto = new DeliveryDTO();
        dto.deliveryId = rs.getString("delivery_id");
        dto.date = rs.getString("date");
        dto.shiftType = rs.getString("shift_type");
        dto.branchId = rs.getString("branch_id");
        dto.truckType = rs.getString("truck_type");
        dto.driverId = rs.getString("driver_id");
        return dto;
    }

    // Converts a DeliveryDTO to a Delivery domain object
    private Delivery dtoToDelivery(DeliveryDTO dto, BranchRepository branchRepo,
                                   EmployeeRepository empRepo) throws SQLException {
        try {
            Branch branch = branchRepo.findById(dto.branchId);
            if (branch == null) {
                System.out.println("Error: Branch not found for delivery.");
                return null;
            }

            Delivery delivery = new Delivery(
                    dto.deliveryId,
                    LocalDate.parse(dto.date),
                    ShiftType.valueOf(dto.shiftType),
                    branch,
                    TruckType.valueOf(dto.truckType)
            );

            // Load driver if exists
            if (dto.driverId != null) {
                Employee driver = empRepo.findById(dto.driverId, branchRepo);
                if (driver != null) {
                    delivery.setDriver(driver);
                }
            }

            return delivery;
        } catch (Exception e) {
            System.out.println("Error converting DTO to Delivery: " + e.getMessage());
            return null;
        }
    }
}