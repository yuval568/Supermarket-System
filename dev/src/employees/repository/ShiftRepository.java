package employees.repository;

import employees.domain.*;
import employees.dto.ShiftAssignmentDTO;
import employees.dto.ShiftDTO;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShiftRepository {

    private final Connection connection;

    public ShiftRepository() throws SQLException {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    // Saves a new shift to the database
    public void insert(Shift shift) throws SQLException {
        String sql = """
                INSERT INTO shifts (date, type, branch_id, start_time, end_time)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, shift.getDate().toString());
            stmt.setString(2, shift.getType().name());
            stmt.setString(3, shift.getBranch().getBranchId());
            stmt.setString(4, shift.getStartTime().toString());
            stmt.setString(5, shift.getEndTime().toString());
            stmt.executeUpdate();
        }
        insertRequiredRoles(shift);
    }

    // Saves the required roles for a shift
    public void insertRequiredRoles(Shift shift) throws SQLException {
        String sql = """
                INSERT OR REPLACE INTO shift_required_roles
                (date, type, branch_id, role_name, count) VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Map.Entry<Role, Integer> entry : shift.getRequiredRoles().entrySet()) {
                stmt.setString(1, shift.getDate().toString());
                stmt.setString(2, shift.getType().name());
                stmt.setString(3, shift.getBranch().getBranchId());
                stmt.setString(4, entry.getKey().getRoleName());
                stmt.setInt(5, entry.getValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    // Saves a shift assignment to the database
    public void insertAssignment(LocalDate date, ShiftType type, String branchId,
                                 String employeeId, String roleName) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO shift_assignments
                (date, type, branch_id, employee_id, role_name) VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, date.toString());
            stmt.setString(2, type.name());
            stmt.setString(3, branchId);
            stmt.setString(4, employeeId);
            stmt.setString(5, roleName);
            stmt.executeUpdate();
        }
    }

    // Removes a shift assignment from the database
    public void deleteAssignment(LocalDate date, ShiftType type, String branchId,
                                 String employeeId, String roleName) throws SQLException {
        String sql = """
                DELETE FROM shift_assignments
                WHERE date=? AND type=? AND branch_id=? AND employee_id=? AND role_name=?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, date.toString());
            stmt.setString(2, type.name());
            stmt.setString(3, branchId);
            stmt.setString(4, employeeId);
            stmt.setString(5, roleName);
            stmt.executeUpdate();
        }
    }

    // Removes all assignments for an employee
    public void deleteAllAssignmentsForEmployee(String employeeId) throws SQLException {
        String sql = "DELETE FROM shift_assignments WHERE employee_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employeeId);
            stmt.executeUpdate();
        }
    }

    // Pending shifts management

    public void insertPendingShift(String employeeId, LocalDate date,
                                   ShiftType type, String branchId, String roleName) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO pending_shifts
                (employee_id, date, type, branch_id, role_name, status) VALUES (?, ?, ?, ?, ?, 'PENDING')
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employeeId);
            stmt.setString(2, date.toString());
            stmt.setString(3, type.name());
            stmt.setString(4, branchId);
            stmt.setString(5, roleName);
            stmt.executeUpdate();
        }
    }

    public void updatePendingShiftStatus(String employeeId, LocalDate date,
                                         ShiftType type, String branchId, String status) throws SQLException {
        String sql = """
                UPDATE pending_shifts SET status = ?
                WHERE employee_id = ? AND date = ? AND type = ? AND branch_id = ?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, employeeId);
            stmt.setString(3, date.toString());
            stmt.setString(4, type.name());
            stmt.setString(5, branchId);
            stmt.executeUpdate();
        }
    }

    public void deleteAllPendingForEmployee(String employeeId) throws SQLException {
        String sql = "DELETE FROM pending_shifts WHERE employee_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employeeId);
            stmt.executeUpdate();
        }
    }

    // Returns all shifts from the database
    public List<Shift> findAll(BranchRepository branchRepo,
                               EmployeeRepository empRepo) throws SQLException {
        List<Shift> shifts = new ArrayList<>();
        String sql = "SELECT * FROM shifts";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ShiftDTO dto = mapRowToDTO(rs);
                Shift shift = dtoToShift(dto, branchRepo, empRepo);
                if (shift != null) shifts.add(shift);
            }
        }
        return shifts;
    }

    // Returns all shifts for a specific branch
    public List<Shift> findByBranch(String branchId, BranchRepository branchRepo,
                                    EmployeeRepository empRepo) throws SQLException {
        List<Shift> shifts = new ArrayList<>();
        String sql = "SELECT * FROM shifts WHERE branch_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ShiftDTO dto = mapRowToDTO(rs);
                    Shift shift = dtoToShift(dto, branchRepo, empRepo);
                    if (shift != null) shifts.add(shift);
                }
            }
        }
        return shifts;
    }

    // Returns all shifts for a specific date and branch
    public List<Shift> findByDateAndBranch(LocalDate date, String branchId,
                                           BranchRepository branchRepo,
                                           EmployeeRepository empRepo) throws SQLException {
        List<Shift> shifts = new ArrayList<>();
        String sql = "SELECT * FROM shifts WHERE date = ? AND branch_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, date.toString());
            stmt.setString(2, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ShiftDTO dto = mapRowToDTO(rs);
                    Shift shift = dtoToShift(dto, branchRepo, empRepo);
                    if (shift != null) shifts.add(shift);
                }
            }
        }
        return shifts;
    }

    // Maps a database row to a ShiftDTO
    private ShiftDTO mapRowToDTO(ResultSet rs) throws SQLException {
        ShiftDTO dto = new ShiftDTO();
        dto.date = rs.getString("date");
        dto.type = rs.getString("type");
        dto.branchId = rs.getString("branch_id");
        dto.startTime = rs.getString("start_time");
        dto.endTime = rs.getString("end_time");
        return dto;
    }

    // Maps a database row to a ShiftAssignmentDTO
    private ShiftAssignmentDTO mapAssignmentRowToDTO(ResultSet rs) throws SQLException {
        ShiftAssignmentDTO dto = new ShiftAssignmentDTO();
        dto.date = rs.getString("date");
        dto.shiftType = rs.getString("type");
        dto.branchId = rs.getString("branch_id");
        dto.employeeId = rs.getString("employee_id");
        dto.roleName = rs.getString("role_name");
        return dto;
    }

    // Converts a ShiftDTO to a Shift domain object
    private Shift dtoToShift(ShiftDTO dto, BranchRepository branchRepo,
                             EmployeeRepository empRepo) throws SQLException {
        try {
            LocalDate date = LocalDate.parse(dto.date);
            ShiftType type = ShiftType.valueOf(dto.type);
            LocalTime startTime = LocalTime.parse(dto.startTime);
            LocalTime endTime = LocalTime.parse(dto.endTime);

            Branch branch = branchRepo.findById(dto.branchId);
            if (branch == null) {
                System.out.println("Error: Branch not found for shift.");
                return null;
            }

            // Load required roles
            Map<Role, Integer> requiredRoles = findRequiredRoles(date, type, dto.branchId);

            // Create the shift
            Shift shift = new Shift(date, type, startTime, endTime, requiredRoles, branch);

            // Load assignments
            loadAssignments(shift, date, type, dto.branchId, empRepo);

            return shift;
        } catch (Exception e) {
            System.out.println("Error converting DTO to Shift: " + e.getMessage());
            return null;
        }
    }

    // Returns the required roles for a shift
    private Map<Role, Integer> findRequiredRoles(LocalDate date, ShiftType type,
                                                 String branchId) throws SQLException {
        Map<Role, Integer> requiredRoles = new HashMap<>();
        String sql = """
                SELECT role_name, count FROM shift_required_roles
                WHERE date = ? AND type = ? AND branch_id = ?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, date.toString());
            stmt.setString(2, type.name());
            stmt.setString(3, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    requiredRoles.put(
                            new Role(rs.getString("role_name")),
                            rs.getInt("count")
                    );
                }
            }
        }
        return requiredRoles;
    }

    // Loads all assignments for a shift
    private void loadAssignments(Shift shift, LocalDate date, ShiftType type,
                                 String branchId, EmployeeRepository empRepo) throws SQLException {
        String sql = """
                SELECT employee_id, role_name FROM shift_assignments
                WHERE date = ? AND type = ? AND branch_id = ?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, date.toString());
            stmt.setString(2, type.name());
            stmt.setString(3, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ShiftAssignmentDTO dto = mapAssignmentRowToDTO(rs);
                    try {
                        Employee emp = empRepo.findById(dto.employeeId, new BranchRepository());
                        Role role = new Role(dto.roleName);
                        if (emp != null) {
                            shift.assignEmployee(role, emp);
                        }
                    } catch (Exception e) {
                        System.out.println("Error loading assignment: " + e.getMessage());
                    }
                }
            }
        }
    }
}