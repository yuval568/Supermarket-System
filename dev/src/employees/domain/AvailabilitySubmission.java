package employees.domain;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

// Represents a single availability entry submitted by an employee.
public class AvailabilitySubmission {
    private Employee employee;
    private DayOfWeek day;
    private ShiftType shiftType;

    // The date and time when this availability was submitted
    private LocalDateTime submittedAt;

    // Creates a new availability submission and records the current time
    public AvailabilitySubmission(Employee employee, DayOfWeek day, ShiftType shiftType) throws Exception {
        if (employee == null) {
            throw new Exception("Error: Employee cannot be null.");
        }
        if (day == null) {
            throw new Exception("Error: Day cannot be null.");
        }
        if (shiftType == null) {
            throw new Exception("Error: Shift type cannot be null.");
        }
        this.employee = employee;
        this.day = day;
        this.shiftType = shiftType;
        this.submittedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "AvailabilitySubmission{" +
                "Employee=" + employee.getFullName() +
                ", Day=" + day +
                ", ShiftType=" + shiftType +
                ", SubmittedAt=" + submittedAt +
                '}';
    }

    // Getters and Setters

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(ShiftType shiftType) {
        this.shiftType = shiftType;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}