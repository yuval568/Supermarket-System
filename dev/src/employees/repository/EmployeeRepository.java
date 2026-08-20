package employees.repository;

import employees.domain.*;
import employees.dto.AvailabilitySubmissionDTO;
import employees.dto.EmployeeDTO;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EmployeeRepository {

    private final Connection connection;

    public EmployeeRepository() throws SQLException {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    // Saves a new employee to the database
    public void insert(Employee emp) throws SQLException {
        String sql = """
                INSERT INTO employees (id, first_name, last_name, bank_account,
                wage, job_scope, salary_type, fixed_day_off, start_date,
                branch_id, license_type, is_active, annual_leave_days)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, emp.getId());
            stmt.setString(2, emp.getFirstName());
            stmt.setString(3, emp.getLastName());
            stmt.setString(4, emp.getBankAccount());
            stmt.setDouble(5, emp.getWage());
            stmt.setString(6, emp.getJobScope().name());
            stmt.setString(7, emp.getSalaryType().name());
            stmt.setString(8, emp.getFixedDayOff() != null ? emp.getFixedDayOff().name() : null);
            stmt.setString(9, emp.getStartDate().toString());
            stmt.setString(10, emp.getBranch().getBranchId());
            stmt.setString(11, emp.getLicenseType() != null ? emp.getLicenseType().name() : null);
            stmt.setInt(12, emp.isActive() ? 1 : 0);
            stmt.setInt(13, emp.getAnnualLeaveDays());
            stmt.executeUpdate();
        }
    }

    // Updates an existing employee in the database
    public void update(Employee emp) throws SQLException {
        String sql = """
                UPDATE employees SET first_name=?, last_name=?, bank_account=?,
                wage=?, job_scope=?, salary_type=?, fixed_day_off=?,
                branch_id=?, license_type=?, is_active=?, annual_leave_days=?
                WHERE id=?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, emp.getFirstName());
            stmt.setString(2, emp.getLastName());
            stmt.setString(3, emp.getBankAccount());
            stmt.setDouble(4, emp.getWage());
            stmt.setString(5, emp.getJobScope().name());
            stmt.setString(6, emp.getSalaryType().name());
            stmt.setString(7, emp.getFixedDayOff() != null ? emp.getFixedDayOff().name() : null);
            stmt.setString(8, emp.getBranch().getBranchId());
            stmt.setString(9, emp.getLicenseType() != null ? emp.getLicenseType().name() : null);
            stmt.setInt(10, emp.isActive() ? 1 : 0);
            stmt.setInt(11, emp.getAnnualLeaveDays());
            stmt.setString(12, emp.getId());
            stmt.executeUpdate();
        }
    }

    // Returns all employees from the database
    public List<Employee> findAll(BranchRepository branchRepo) throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                EmployeeDTO dto = mapRowToDTO(rs);
                Employee emp = dtoToEmployee(dto, branchRepo);
                if (emp != null) employees.add(emp);
            }
        }
        return employees;
    }

    // Returns a specific employee by ID
    public Employee findById(String id, BranchRepository branchRepo) throws SQLException {
        String sql = "SELECT * FROM employees WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    EmployeeDTO dto = mapRowToDTO(rs);
                    return dtoToEmployee(dto, branchRepo);
                }
            }
        }
        return null;
    }

    // Returns all employees for a specific branch
    public List<Employee> findByBranch(String branchId, BranchRepository branchRepo) throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE branch_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    EmployeeDTO dto = mapRowToDTO(rs);
                    Employee emp = dtoToEmployee(dto, branchRepo);
                    if (emp != null) employees.add(emp);
                }
            }
        }
        return employees;
    }

    // Checks if an employee exists
    public boolean exists(String id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM employees WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Role management

    public void insertRole(String employeeId, String roleName) throws SQLException {
        String sql = "INSERT OR IGNORE INTO employee_roles (employee_id, role_name) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employeeId);
            stmt.setString(2, roleName);
            stmt.executeUpdate();
        }
    }

    public void deleteRole(String employeeId, String roleName) throws SQLException {
        String sql = "DELETE FROM employee_roles WHERE employee_id = ? AND role_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employeeId);
            stmt.setString(2, roleName);
            stmt.executeUpdate();
        }
    }

    public List<String> findRoles(String employeeId) throws SQLException {
        List<String> roles = new ArrayList<>();
        String sql = "SELECT role_name FROM employee_roles WHERE employee_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    roles.add(rs.getString("role_name"));
                }
            }
        }
        return roles;
    }

    // Availability management

    public void insertAvailability(String employeeId, DayOfWeek day,
                                   ShiftType shiftType, LocalDateTime submittedAt) throws SQLException {
        String sql = "INSERT OR IGNORE INTO availabilities (employee_id, day, shift_type, submitted_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employeeId);
            stmt.setString(2, day.name());
            stmt.setString(3, shiftType.name());
            stmt.setString(4, submittedAt.toString());
            stmt.executeUpdate();
        }
    }

    public void deleteAvailability(String employeeId, DayOfWeek day, ShiftType shiftType) throws SQLException {
        String sql = "DELETE FROM availabilities WHERE employee_id = ? AND day = ? AND shift_type = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employeeId);
            stmt.setString(2, day.name());
            stmt.setString(3, shiftType.name());
            stmt.executeUpdate();
        }
    }

    public void deleteAllAvailabilities(String employeeId) throws SQLException {
        String sql = "DELETE FROM availabilities WHERE employee_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employeeId);
            stmt.executeUpdate();
        }
    }

    public List<AvailabilitySubmission> findAvailabilities(String employeeId,
                                                           Employee emp) throws SQLException {
        List<AvailabilitySubmission> result = new ArrayList<>();
        String sql = "SELECT * FROM availabilities WHERE employee_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AvailabilitySubmissionDTO dto = mapAvailabilityRowToDTO(rs);
                    try {
                        DayOfWeek day = DayOfWeek.valueOf(dto.day);
                        ShiftType shiftType = ShiftType.valueOf(dto.shiftType);
                        result.add(new AvailabilitySubmission(emp, day, shiftType));
                    } catch (Exception e) {
                        System.out.println("Error loading availability: " + e.getMessage());
                    }
                }
            }
        }
        return result;
    }

    // Roles pool management

    public void insertRoleToPool(String roleName) throws SQLException {
        String sql = "INSERT OR IGNORE INTO roles_pool (role_name) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, roleName);
            stmt.executeUpdate();
        }
    }

    public void deleteRoleFromPool(String roleName) throws SQLException {
        String sql = "DELETE FROM roles_pool WHERE role_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, roleName);
            stmt.executeUpdate();
        }
    }

    public List<String> findAllRolesInPool() throws SQLException {
        List<String> roles = new ArrayList<>();
        String sql = "SELECT role_name FROM roles_pool";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                roles.add(rs.getString("role_name"));
            }
        }
        return roles;
    }

    // Maps a database row to an EmployeeDTO
    private EmployeeDTO mapRowToDTO(ResultSet rs) throws SQLException {
        EmployeeDTO dto = new EmployeeDTO();
        dto.id = rs.getString("id");
        dto.firstName = rs.getString("first_name");
        dto.lastName = rs.getString("last_name");
        dto.bankAccount = rs.getString("bank_account");
        dto.wage = rs.getDouble("wage");
        dto.jobScope = rs.getString("job_scope");
        dto.salaryType = rs.getString("salary_type");
        dto.fixedDayOff = rs.getString("fixed_day_off");
        dto.startDate = rs.getString("start_date");
        dto.branchId = rs.getString("branch_id");
        dto.licenseType = rs.getString("license_type");
        dto.isActive = rs.getInt("is_active") == 1;
        dto.annualLeaveDays = rs.getInt("annual_leave_days");
        return dto;
    }

    // Maps a database row to an AvailabilitySubmissionDTO
    private AvailabilitySubmissionDTO mapAvailabilityRowToDTO(ResultSet rs) throws SQLException {
        AvailabilitySubmissionDTO dto = new AvailabilitySubmissionDTO();
        dto.employeeId = rs.getString("employee_id");
        dto.day = rs.getString("day");
        dto.shiftType = rs.getString("shift_type");
        dto.submittedAt = rs.getString("submitted_at");
        return dto;
    }

    // Converts an EmployeeDTO to an Employee domain object
    private Employee dtoToEmployee(EmployeeDTO dto, BranchRepository branchRepo) throws SQLException {
        try {
            Branch branch = branchRepo.findById(dto.branchId);
            if (branch == null) {
                System.out.println("Error: Branch not found for employee.");
                return null;
            }

            Employee emp = new Employee(
                    dto.id,
                    dto.firstName,
                    dto.lastName,
                    dto.bankAccount,
                    dto.wage,
                    JobScope.valueOf(dto.jobScope),
                    SalaryType.valueOf(dto.salaryType),
                    dto.fixedDayOff != null ? DayOfWeek.valueOf(dto.fixedDayOff) : null,
                    LocalDate.parse(dto.startDate),
                    branch
            );

            emp.setActive(dto.isActive);
            emp.setAnnualLeaveDays(dto.annualLeaveDays);

            if (dto.licenseType != null) {
                emp.setLicenseType(TruckType.valueOf(dto.licenseType));
            }

            // Load roles
            List<String> roleNames = findRoles(dto.id);
            for (String roleName : roleNames) {
                emp.addRole(new Role(roleName));
            }

            // Load availabilities
            List<AvailabilitySubmission> availabilities = findAvailabilities(dto.id, emp);
            emp.setAvailabilities(availabilities);

            return emp;
        } catch (Exception e) {
            System.out.println("Error converting DTO to Employee: " + e.getMessage());
            return null;
        }
    }
}