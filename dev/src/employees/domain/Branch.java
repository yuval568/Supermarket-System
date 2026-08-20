package employees.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// A branch is a store location. Each branch has its own employees and shifts.
public class Branch {
    private String branchId;
    private String name;
    private String address;

    // The employees that work in this branch
    private List<Employee> employees;

    // The shifts that belong to this branch
    private List<Shift> shifts;

    // Creates a new branch with an ID, name and address
    public Branch(String branchId, String name, String address) throws Exception {
        if (branchId == null || branchId.trim().isEmpty()) {
            throw new Exception("Error: Branch ID cannot be empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new Exception("Error: Branch name cannot be empty.");
        }

        this.branchId = branchId.trim();
        this.name = name.trim();
        this.address = address != null ? address.trim() : "";
        this.employees = new ArrayList<>();
        this.shifts = new ArrayList<>();
    }

    // Adds an employee to this branch (no duplicates)
    public void addEmployee(Employee employee) {
        if (employee != null && !this.employees.contains(employee)) {
            this.employees.add(employee);
        }
    }

    // Removes an employee from this branch
    public void removeEmployee(Employee employee) {
        this.employees.remove(employee);
    }

    // Adds a shift to this branch (no duplicates)
    public void addShift(Shift shift) {
        if (shift != null && !this.shifts.contains(shift)) {
            this.shifts.add(shift);
        }
    }

    // Removes a shift from this branch
    public void removeShift(Shift shift) {
        this.shifts.remove(shift);
    }

    @Override
    public String toString() {
        return "Branch{" + "ID='" + branchId + "', Name='" + name + "'}";
    }

    // Getters and Setters

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public List<Shift> getShifts() {
        return shifts;
    }

    public void setShifts(List<Shift> shifts) {
        this.shifts = shifts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Branch branch = (Branch) o;
        return branchId.equals(branch.getBranchId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(branchId);
    }
}