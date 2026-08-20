package employees.domain;

// Represents a single assignment of an employee to a specific role in a specific shift.
// For example: "Yossi is assigned as Cashier in Sunday Morning shift"
public class ShiftAssignment {
    private Shift shift;
    private Employee employee;
    private Role role;

    // Creates a new assignment - connects an employee to a role in a shift
    public ShiftAssignment(Shift shift, Employee employee, Role role) throws Exception {
        if (shift == null) {
            throw new Exception("Error: Shift cannot be null.");
        }
        if (employee == null) {
            throw new Exception("Error: Employee cannot be null.");
        }
        if (role == null) {
            throw new Exception("Error: Role cannot be null.");
        }
        this.shift = shift;
        this.employee = employee;
        this.role = role;
    }

    @Override
    public String toString() {
        return "ShiftAssignment{" +
                "Employee=" + employee.getFullName() +
                ", Role=" + role.getRoleName() +
                ", Shift=" + shift +
                '}';
    }

    // Getters and Setters

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}