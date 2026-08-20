package employees.domain;

// Represents the employee who is responsible for managing a specific shift.
// The employee must hold the "Shift Manager" role certification.
// This employee can also be assigned to another regular role in the same shift
public class ShiftManager {
    private Shift shift;
    private Employee employee;

    // Creates a new ShiftManager link - checks that the employee is certified
    public ShiftManager(Shift shift, Employee employee) throws Exception {
        if (shift == null) {
            throw new Exception("Error: Shift cannot be null.");
        }
        if (employee == null) {
            throw new Exception("Error: Employee cannot be null.");
        }

        Role shiftManagerRole = new Role("Shift Manager");
        if (!employee.isQualifiedFor(shiftManagerRole)) {
            throw new Exception("Error: " + employee.getFullName() +
                    " is not certified as Shift Manager.");
        }

        this.shift = shift;
        this.employee = employee;
    }

    @Override
    public String toString() {
        return "ShiftManager{" +
                "Employee=" + employee.getFullName() +
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
}