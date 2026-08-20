package employees.service;

import employees.domain.*;
import employees.repository.BranchRepository;
import employees.repository.EmployeeRepository;
import employees.repository.ShiftRepository;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ShiftService {

    private static ShiftService instance = null;

    private final ShiftRepository shiftRepo;

    private List<Shift> allShifts;

    private ShiftService() {
        try {
            this.shiftRepo = new ShiftRepository();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database: " + e.getMessage());
        }
        this.allShifts = new ArrayList<>();
    }

    public static ShiftService getInstance() {
        if (instance == null) {
            instance = new ShiftService();
        }
        return instance;
    }

    // Loads all shifts from the database into memory
    // Called after EmployeeService is initialized so branches and employees exist
    public void loadFromDatabase() {
        try {
            BranchRepository branchRepo = new BranchRepository();
            EmployeeRepository empRepo = new EmployeeRepository();
            this.allShifts = shiftRepo.findAll(branchRepo, empRepo);

            // Connect each shift to its branch
            for (Shift shift : allShifts) {
                if (shift.getBranch() != null) {
                    shift.getBranch().addShift(shift);
                }
            }

            // Connect assigned shifts to employees
            for (Shift shift : allShifts) {
                for (Employee emp : shift.getAllAssignedEmployees()) {
                    emp.addAssignedShift(shift);
                }
            }
        } catch (SQLException e) {
            System.out.println("Warning: Could not load shifts from database: " + e.getMessage());
            this.allShifts = new ArrayList<>();
        }
    }

    // Add a new shift to the system and save it to the database
    public void addShift(LocalDate date, ShiftType type, Branch branch) throws Exception {
        if (date == null || type == null)
            throw new Exception("Error: Date and type cannot be null.");
        if (branch == null)
            throw new Exception("Error: Branch cannot be null.");
        if (date.isBefore(LocalDate.now()))
            throw new Exception("Error: Cannot create a shift in the past.");

        for (Shift s : allShifts) {
            if (s.getDate().equals(date) && s.getType() == type && branch.equals(s.getBranch()))
                throw new Exception("Error: A " + type + " shift on " + date + " already exists.");
        }

        Shift newShift = new Shift(date, type, EmployeeService.getInstance().getCompanyRolesPool(), branch);
        branch.addShift(newShift);
        allShifts.add(newShift);

        // Save to database
        shiftRepo.insert(newShift);
    }

    public List<Shift> getShiftsByBranch(String branchId) {
        List<Shift> result = new ArrayList<>();
        for (Shift s : allShifts) {
            if (s.getBranch() != null && s.getBranch().getBranchId().equals(branchId)) {
                result.add(s);
            }
        }
        return result;
    }

    public List<Shift> getAllShifts() {
        return allShifts;
    }

    public Shift findShift(LocalDate date, ShiftType type) {
        for (Shift s : allShifts) {
            if (s.getDate().equals(date) && s.getType() == type) {
                return s;
            }
        }
        return null;
    }

    public Shift getShift(LocalDate date, ShiftType type) throws Exception {
        Shift shift = findShift(date, type);
        if (shift == null) {
            throw new Exception("Error: Shift not found.");
        }
        return shift;
    }

    // Assign an employee to a shift and save to database
    public void assignEmployeeToShift(LocalDate date, ShiftType type,
                                      String employeeId, String roleName) throws Exception {

        Shift targetShift = getShift(date, type);

        Employee emp = EmployeeService.getInstance().getEmployeeById(employeeId);
        if (emp == null)
            throw new Exception("Error: Employee not found.");

        if (!emp.isActive())
            throw new Exception("Error: " + emp.getFirstName() + " is no longer active.");

        Role requiredRole = EmployeeService.getInstance().getRoleFromPool(roleName);
        if (!emp.isQualifiedFor(requiredRole))
            throw new Exception("Error: " + emp.getFirstName() +
                    " is not certified as " + roleName + ".");

        DayOfWeek shiftDay = date.getDayOfWeek();
        if (!emp.isAvailable(shiftDay, type))
            throw new Exception("Error: " + emp.getFirstName() +
                    " is not available for this shift. " +
                    "To assign outside their availability, use the override request flow.");

        targetShift.assignEmployee(requiredRole, emp);
        emp.addAssignedShift(targetShift);

        // Save assignment to database
        shiftRepo.insertAssignment(date, type, targetShift.getBranch().getBranchId(),
                employeeId, requiredRole.getRoleName());
    }

    // Remove an employee from a shift and update the database
    public void removeEmployeeFromShift(LocalDate date, ShiftType type,
                                        String employeeId, String roleName) throws Exception {

        Shift targetShift = getShift(date, type);

        Employee emp = EmployeeService.getInstance().getEmployeeById(employeeId);
        if (emp == null)
            throw new Exception("Error: Employee not found.");

        Role targetRole = EmployeeService.getInstance().getRoleFromPool(roleName);

        targetShift.removeEmployee(targetRole, emp);
        emp.removeAssignedShift(targetShift);

        // Remove from database
        shiftRepo.deleteAssignment(date, type, targetShift.getBranch().getBranchId(),
                employeeId, targetRole.getRoleName());
    }

    // Remove a deactivated employee from all future shifts and update the database
    public void handleEmployeeDeactivation(String employeeId) throws Exception {
        Employee emp = EmployeeService.getInstance().getEmployeeById(employeeId);
        if (emp == null)
            throw new Exception("Error: Employee not found.");

        LocalDate today = LocalDate.now();
        for (Shift shift : allShifts) {
            if (!shift.getDate().isBefore(today)) {
                shift.removeEmployeeFromAllRoles(emp);
                emp.removeAssignedShift(shift);
            }
        }

        emp.getShiftStatuses().clear();
        emp.getPendingRoles().clear();

        // Remove all assignments and pending requests from database
        shiftRepo.deleteAllAssignmentsForEmployee(employeeId);
        shiftRepo.deleteAllPendingForEmployee(employeeId);
    }

    // Validates a shift - checks Shift Manager and delivery requirements
    public void validateShift(LocalDate date, ShiftType type, Branch branch) throws Exception {

        Shift shift = getShiftsByDateAndBranch(date, branch).stream()
                .filter(s -> s.getType() == type)
                .findFirst()
                .orElse(null);

        if (shift == null)
            throw new Exception("Error: Shift not found.");

        if (!shift.hasShiftManager())
            throw new Exception("Validation Error: No Shift Manager assigned!");

        boolean hasDelivery = DeliveryService.getInstance()
                .hasDeliveryForShift(date, type, branch);

        if (hasDelivery) {
            Role storekeeperRole = EmployeeService.getInstance().getRoleFromPool("Storekeeper");
            Role driverRole = EmployeeService.getInstance().getRoleFromPool("Driver");

            if (shift.getAssignedCountFor(storekeeperRole) < 1)
                throw new Exception("Validation Error: Delivery scheduled but no Storekeeper assigned!");

            if (shift.getAssignedCountFor(driverRole) < 1)
                throw new Exception("Validation Error: Delivery scheduled but no Driver assigned!");
        }
    }

    public List<Shift> getShiftsByDate(LocalDate date) {
        List<Shift> result = new ArrayList<>();
        for (Shift s : allShifts) {
            if (s.getDate().equals(date)) {
                result.add(s);
            }
        }
        return result;
    }

    public List<Shift> getShiftsForEmployee(String employeeId) {
        Employee emp = EmployeeService.getInstance().getEmployeeById(employeeId);
        if (emp == null) return new ArrayList<>();
        return emp.getAssignedShifts();
    }

    // Send an override request and save to database
    public void requestOverrideAssignment(LocalDate date, ShiftType type,
                                          String employeeId, String roleName) throws Exception {

        Shift targetShift = getShift(date, type);

        Employee emp = EmployeeService.getInstance().getEmployeeById(employeeId);
        if (emp == null)
            throw new Exception("Error: Employee not found.");
        if (!emp.isActive())
            throw new Exception("Error: " + emp.getFullName() + " is not active.");

        Role requiredRole = EmployeeService.getInstance().getRoleFromPool(roleName);
        if (!emp.isQualifiedFor(requiredRole))
            throw new Exception("Error: " + emp.getFullName() + " is not certified as " + roleName + ".");

        if (targetShift.isRoleFull(requiredRole))
            throw new Exception("Error: The role '" + roleName + "' is already full.");

        emp.addPendingShift(targetShift, requiredRole);

        // Save pending request to database
        shiftRepo.insertPendingShift(employeeId, date, type,
                targetShift.getBranch().getBranchId(), requiredRole.getRoleName());
    }

    // Respond to an override request and update the database
    public void respondToOverrideRequest(LocalDate date, ShiftType type,
                                         String employeeId,
                                         ApprovalStatus response) throws Exception {

        Shift targetShift = getShift(date, type);

        Employee emp = EmployeeService.getInstance().getEmployeeById(employeeId);
        if (emp == null)
            throw new Exception("Error: Employee not found.");

        Role requiredRole = emp.getPendingRole(targetShift);
        if (requiredRole == null)
            throw new Exception("Error: No pending role found for this shift.");

        emp.respondToShift(targetShift, response);

        // Update status in database
        shiftRepo.updatePendingShiftStatus(employeeId, date, type,
                targetShift.getBranch().getBranchId(), response.name());

        if (response == ApprovalStatus.APPROVED) {
            targetShift.assignEmployee(requiredRole, emp);
            emp.addAssignedShift(targetShift);

            // Save assignment to database
            shiftRepo.insertAssignment(date, type, targetShift.getBranch().getBranchId(),
                    employeeId, requiredRole.getRoleName());
        }
    }

    public List<Shift> getShiftsByDateAndBranch(LocalDate date, Branch branch) {
        List<Shift> result = new ArrayList<>();
        for (Shift s : allShifts) {
            if (s.getDate().equals(date) && branch.equals(s.getBranch())) {
                result.add(s);
            }
        }
        return result;
    }

    // Add delivery requirements and update the database
    public void addDeliveryRequirementsToShift(LocalDate date, ShiftType type,
                                               Branch branch) throws Exception {
        Shift shift = getShiftsByDateAndBranch(date, branch).stream()
                .filter(s -> s.getType() == type)
                .findFirst()
                .orElse(null);

        if (shift == null)
            throw new Exception("Error: Shift not found for this branch.");

        Role driverRole = EmployeeService.getInstance().getRoleFromPool("Driver");
        shift.setRoleRequirement(driverRole, shift.getRequiredCountFor(driverRole) + 1);

        Role storekeeperRole = EmployeeService.getInstance().getRoleFromPool("Storekeeper");
        if (shift.getAssignedCountFor(storekeeperRole) < 1) {
            shift.setRoleRequirement(storekeeperRole, 1);
        }

        // Update required roles in database
        shiftRepo.insertRequiredRoles(shift);
    }
}