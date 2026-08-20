package employees.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Employee {
    //Personal Information
    private String id;
    private String firstName;
    private String lastName;
    private String bankAccount;

    //Employment Terms
    private double wage;
    private JobScope jobScope;
    private SalaryType salaryType;
    private int annualLeaveDays;
    private DayOfWeek fixedDayOff;
    private LocalDate startDate;
    private Branch branch;
    private TruckType licenseType; //null if not a driver

    //Relationships & Lists
    private List<Role> roles;

    private List<AvailabilitySubmission> availabilities;

    //A map tracking the approval status of shifts assigned to this employee
    private Map<Shift, ApprovalStatus> shiftStatuses;

    private Map<Shift,Role> pendingRoles;

    //List of shifts the employee has been assigned to (confirmed)
    private List<Shift> assignedShifts;

    //Indicates if the employee is currently active in the system
    private boolean isActive;

    //Default constructor
    public Employee(String id, String firstName, String lastName, String bankAccount,
                    double wage, JobScope jobScope, SalaryType salaryType,
                    DayOfWeek fixedDayOff, LocalDate startDate, Branch branch) throws Exception {


        //Validate ID - must be exactly 9 digits
        if (id == null || id.trim().isEmpty() ) {
            throw new Exception("Error: Employee must have an ID.");
        }

        //Validate names
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new Exception("Error: First name cannot be empty.");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new Exception("Error: Last name cannot be empty.");
        }

        //Validate bank account
        if (bankAccount == null || bankAccount.trim().isEmpty()) {
            throw new Exception("Error: Bank account cannot be empty.");
        }

        //Validate wage - must be non-negative
        if (wage < 0) {
            throw new Exception("Error: Wage cannot be a negative number.");
        }

        //Validate start date - cannot be unreasonably far in the future
        if (startDate != null && startDate.isAfter(LocalDate.now().plusYears(1))) {
            throw new Exception("Error: Start date cannot be more than one year in the future.");
        }

        if (branch == null) {
            throw new Exception("Error: Branch cannot be null.");
        }


        //Personal and financial data
        this.id = id;
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.bankAccount = bankAccount;

        //Employment terms
        this.wage = wage;
        this.jobScope = jobScope;
        this.salaryType = salaryType;
        this.fixedDayOff = fixedDayOff;
        this.startDate = startDate;
        this.branch = branch;
        this.licenseType = null;

        //Collections initialization
        this.roles = new ArrayList<>();
        this.availabilities = new ArrayList<>();
        this.shiftStatuses = new HashMap<>();
        this.assignedShifts = new ArrayList<>();
        this.pendingRoles = new HashMap<>();


        //Default terms (can be changed later via Setters)
        this.isActive = true;
        this.annualLeaveDays = 0;
    }

    //Availability logic

    //Records that an employee is available to work a specific shift type on a specific day
    public void addAvailability(DayOfWeek day, ShiftType type) throws Exception {
        for (AvailabilitySubmission existing : this.availabilities) {
            if (existing.getDay() == day && existing.getShiftType() == type) return;
        }
        this.availabilities.add(new AvailabilitySubmission(this, day, type));
    }

    //Checks if the employee has submitted availability for a specific day and shift type
    //If the employee submitted NO availabilities, they are considered available for ALL shifts
    public boolean isAvailable(DayOfWeek day, ShiftType type) {
        if (day == null || type == null) return false;
        if (this.fixedDayOff != null && this.fixedDayOff == day) return false;
        if (this.availabilities.isEmpty()) return true;
        for (AvailabilitySubmission sub : this.availabilities) {
            if (sub.getDay() == day && sub.getShiftType() == type) return true;
        }
        return false;
    }


    //Removes a specific availability entry for a given day and shift type
    public void removeAvailability(DayOfWeek day, ShiftType type) {
        this.availabilities.removeIf(sub -> sub.getDay() == day && sub.getShiftType() == type);
    }

    //Clears all availability entries for this employee
    public void clearAvailabilities() {
        this.availabilities.clear();
    }


    //Approval flow

    //Retrieves a list of all shifts that are currently pending the employee's approval
    public List<Shift> getPendingShifts() {
        List<Shift> pendingShifts = new ArrayList<>();

        for (Map.Entry<Shift, ApprovalStatus> entry : this.shiftStatuses.entrySet()) {
            if (entry.getValue() == ApprovalStatus.PENDING) {
                pendingShifts.add(entry.getKey());
            }
        }

        return pendingShifts;
    }

    // Creates a PENDING request when HR assigns outside availability.
    public void addPendingShift(Shift shift, Role role) {
        if (shift == null || role == null) return;
        this.shiftStatuses.put(shift, ApprovalStatus.PENDING);
        this.pendingRoles.put(shift, role);
    }

    // Returns the role the manager requested for a specific pending shift.
    public Role getPendingRole(Shift shift) {
        return this.pendingRoles.get(shift);
    }

    //Employee responds to a pending shift request - approve or reject
    public void respondToShift(Shift shift, ApprovalStatus response) throws Exception {
        if (!this.shiftStatuses.containsKey(shift)) {
            throw new Exception("Error: Shift not found in employee's pending list.");
        }
        if (this.shiftStatuses.get(shift) != ApprovalStatus.PENDING) {
            throw new Exception("Error: This shift is not pending approval.");
        }
        if (response == ApprovalStatus.APPROVED) {
            this.shiftStatuses.remove(shift);
            this.assignedShifts.add(shift);
        } else if (response == ApprovalStatus.REJECTED) {
            this.shiftStatuses.put(shift, ApprovalStatus.REJECTED);
        }
        this.pendingRoles.remove(shift);
    }

    //Shift assignment

    //Adds a confirmed shift directly to the employee's assigned list
    //Used when manager assigns an employee who submitted availability (no approval needed)
    public void addAssignedShift(Shift shift) {
        if (!this.assignedShifts.contains(shift)) {
            this.assignedShifts.add(shift);
        }
    }

    //Removes a shift from the employee's assigned shifts list
    public void removeAssignedShift(Shift shift) {
        this.assignedShifts.remove(shift);
    }



    //Role logic

    //Adds a role certification to the employee if they don't already have it
    public void addRole(Role role) {
        if (role == null) {
            return;
        }
        if (!this.roles.contains(role)) {
            this.roles.add(role);
        }
    }

    //Removes a role certification from the employee
    public void removeRole(Role role) {
        this.roles.remove(role);
    }


    //Checks if the employee is certified for a specific role
    public boolean isQualifiedFor(Role role) {
        return this.roles.contains(role);
    }



    //Returns the full name of the employee
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return "Employee{" + "Name='" + firstName + " " + lastName + '\'' + ", ID='" + id + '\'' + '}';
    }

    //Getters and Setters


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public double getWage() {
        return wage;
    }

    public void setWage(double wage) {
        this.wage = wage;
    }

    public JobScope getJobScope() {
        return jobScope;
    }

    public void setJobScope(JobScope jobScope) {
        this.jobScope = jobScope;
    }

    public SalaryType getSalaryType() {
        return salaryType;
    }

    public void setSalaryType(SalaryType salaryType) {
        this.salaryType = salaryType;
    }

    public int getAnnualLeaveDays() {
        return annualLeaveDays;
    }

    public void setAnnualLeaveDays(int annualLeaveDays) {
        this.annualLeaveDays = annualLeaveDays;
    }

    public DayOfWeek getFixedDayOff() {
        return fixedDayOff;
    }

    public void setFixedDayOff(DayOfWeek fixedDayOff) {
        this.fixedDayOff = fixedDayOff;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public List<AvailabilitySubmission> getAvailabilities() {
        return availabilities;
    }

    public void setAvailabilities(List<AvailabilitySubmission> availabilities) {
        this.availabilities = availabilities;
    }

    public Map<Shift, ApprovalStatus> getShiftStatuses() {
        return shiftStatuses;
    }

    public void setShiftStatuses(Map<Shift, ApprovalStatus> shiftStatuses) {
        this.shiftStatuses = shiftStatuses;
    }

    public Map<Shift, Role> getPendingRoles() {
        return pendingRoles;
    }

    public void setPendingRoles(Map<Shift, Role> pendingRoles) {
        this.pendingRoles = pendingRoles;
    }

    public List<Shift> getAssignedShifts() {
        return assignedShifts;
    }

    public void setAssignedShifts(List<Shift> assignedShifts) {
        this.assignedShifts = assignedShifts;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public TruckType getLicenseType() {return licenseType;}
    public void setLicenseType(TruckType licenseType) {this.licenseType = licenseType;}


}