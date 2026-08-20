package employees.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Shift {
    //Shift Details
    private LocalDate date;
    private ShiftType type;

    private LocalTime startTime;
    private LocalTime endTime;
    private Branch branch;

    //Maps each Role to the number of employees required for this shift
    private Map<Role, Integer> requiredRoles;

    private List<ShiftAssignment> assignments;

    //Quick Constructor
    //Creates a shift with default hours based on the shift type.
    public Shift(LocalDate date, ShiftType type, List<Role> rolePool, Branch branch) {
        if (date == null) {
            throw new IllegalArgumentException("date cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        this.date = date;
        this.type = type;

        if (type == ShiftType.MORNING) {
            this.startTime = LocalTime.of(6, 0);
            this.endTime = LocalTime.of(14, 0);
        } else {
            this.startTime = LocalTime.of(14, 0);
            this.endTime = LocalTime.of(22, 0);
        }
        if(branch == null) {
            throw new IllegalArgumentException("branch cannot be null");
        }
        this.branch = branch;

        this.requiredRoles = new HashMap<>();
        for (Role r : rolePool) {
            if (r.getRoleName().equalsIgnoreCase("Cashier"))        this.requiredRoles.put(r, 2);
            if (r.getRoleName().equalsIgnoreCase("Storekeeper"))    this.requiredRoles.put(r, 3);
            if (r.getRoleName().equalsIgnoreCase("Shift Manager"))  this.requiredRoles.put(r, 1);
            if (r.getRoleName().equalsIgnoreCase("General Worker")) this.requiredRoles.put(r, 5);
        }

        if (this.requiredRoles.isEmpty()) {
            throw new IllegalArgumentException("Required roles cannot be empty");
        }

        this.assignments = new ArrayList<>();
    }

    //Full Manual Constructor
    //Allows the manager to define exact hours and required roles right at creation.
    public Shift(LocalDate date, ShiftType type, LocalTime startTime, LocalTime endTime,
                 Map<Role, Integer> requiredRoles, Branch branch) {
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
        if (requiredRoles == null || requiredRoles.isEmpty()) {
            throw new IllegalArgumentException("Required roles cannot be empty");
        }
        this.date = date;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;

        this.requiredRoles = requiredRoles;
        if(branch == null) {
            throw new IllegalArgumentException("branch cannot be null");
        }
        this.branch = branch;

        this.assignments = new ArrayList<>();
    }

    //Helper Methods

    //Defines how many employees of a specific role are needed for this shift
    public void setRoleRequirement(Role role, int amount) {
        this.requiredRoles.put(role, amount);
    }

    //Returns the required amount of employees for a specific role (returns 0 if not set)
    public int getRequiredCountFor(Role role) {
        return this.requiredRoles.getOrDefault(role, 0);
    }

    //Returns the number of employees currently assigned to a specific role
    public int getAssignedCountFor(Role role) {
        int count = 0;
        for (ShiftAssignment a : this.assignments) {
            if (a.getRole().equals(role)) count++;
        }
        return count;
    }

    //Checks if a specific role has been fully staffed
    public boolean isRoleFull(Role role) {
        return getAssignedCountFor(role) >= getRequiredCountFor(role);
    }

    //Assigns an employee to a specific role in this shift
    public void assignEmployee(Role role, Employee employee) throws Exception {
        if (role == null || employee == null) {
            throw new IllegalArgumentException("Role and employee cannot be null");
        }
        boolean roleIsShiftManager = role.getRoleName().equalsIgnoreCase("Shift Manager");

        for (ShiftAssignment a : this.assignments) {
            if (a.getEmployee().equals(employee)) {
                if (a.getRole().equals(role)) return;
                boolean existingIsShiftManager = a.getRole().getRoleName().equalsIgnoreCase("Shift Manager");
                if (!existingIsShiftManager && !roleIsShiftManager) {
                    throw new Exception("Error: " + employee.getFullName() +
                            " is already assigned as " + a.getRole().getRoleName() +
                            " in this shift.");
                }
            }
        }
        if (isRoleFull(role)) {
            throw new Exception("Error: Role " + role.getRoleName() + " is already full.");
        }
        this.assignments.add(new ShiftAssignment(this, employee, role));
    }

    //Removes an employee from a specific role in this shift
    public void removeEmployee(Role role, Employee employee) {
        this.assignments.removeIf(a -> a.getRole().equals(role) && a.getEmployee().equals(employee));
    }

    // Checks if a specific employee is assigned to this shift in any role
    public boolean isEmployeeAssigned(Employee employee) {
        if (employee == null) return false;
        for (ShiftAssignment a : this.assignments) {
            if (a.getEmployee().equals(employee)) return true;
        }
        return false;
    }

    //Removes an employee from ALL roles in this shift
    public void removeEmployeeFromAllRoles(Employee employee) {
        this.assignments.removeIf(a -> a.getEmployee().equals(employee));
    }

    //Checks if at least one employee with "Shift Manager" role is assigned to this shift
    public boolean hasShiftManager() {
        for (ShiftAssignment a : this.assignments) {
            if (a.getRole().getRoleName().equalsIgnoreCase("Shift Manager")) return true;
        }
        return false;
    }

    //Checks if all required roles have been fully staffed
    public boolean isFullyStaffed() {
        for (Map.Entry<Role, Integer> entry : this.requiredRoles.entrySet()) {
            if (getAssignedCountFor(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    //Returns a list of all employees assigned to this shift across all roles (no duplicates)
    public List<Employee> getAllAssignedEmployees() {
        List<Employee> allEmployees = new ArrayList<>();
        for (ShiftAssignment a : this.assignments) {
            if (!allEmployees.contains(a.getEmployee())) {
                allEmployees.add(a.getEmployee());
            }
        }
        return allEmployees;
    }

    @Override
    public String toString() {
        return "Shift{" +
                "Date=" + date +
                ", Type=" + type +
                ", Hours=" + startTime + "-" + endTime +
                '}';
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public ShiftType getType() {
        return type;
    }

    public void setType(ShiftType type) {
        this.type = type;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Map<Role, Integer> getRequiredRoles() {
        return requiredRoles;
    }

    public void setRequiredRoles(Map<Role, Integer> requiredRoles) {
        this.requiredRoles = requiredRoles;
    }

    public List<ShiftAssignment> getAssignedEmployees() {
        return assignments;
    }

    public void setAssignedEmployees(List<ShiftAssignment> assignments) {
        this.assignments = assignments;
    }
}
