package employees.presentation;

import employees.domain.*;
import employees.service.EmployeeService;
import employees.service.ShiftService;
import employees.service.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ManagerUI {

    private final Scanner scanner;
    private final EmployeeService empService;
    private final ShiftService shiftService;
    private Branch currentBranch;

    public ManagerUI(Scanner scanner) {
        this.scanner = scanner;
        this.empService = EmployeeService.getInstance();
        this.shiftService = ShiftService.getInstance();
    }

    // Shows a menu of roles from the pool and returns the selected one
    private Role selectRoleFromMenu() {
        List<Role> pool = empService.getCompanyRolesPool();
        if (pool.isEmpty()) {
            System.out.println("No roles defined in the company pool.");
            return null;
        }
        System.out.println("Select a role:");
        for (int i = 0; i < pool.size(); i++) {
            System.out.println((i + 1) + ". " + pool.get(i).getRoleName());
        }
        System.out.print("Your choice: ");
        try {
            int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (index < 0 || index >= pool.size()) {
                System.out.println("Invalid choice.");
                return null;
            }
            return pool.get(index);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return null;
        }
    }

    // Shows a menu of roles from a specific list and returns the selected role name
    private String selectRoleFromList(List<Role> roles) {
        if (roles.isEmpty()) {
            System.out.println("No roles available.");
            return null;
        }
        System.out.println("Select a role:");
        for (int i = 0; i < roles.size(); i++) {
            System.out.println((i + 1) + ". " + roles.get(i).getRoleName());
        }
        System.out.print("Your choice: ");
        String input = scanner.nextLine().trim();
        try {
            int index = Integer.parseInt(input) - 1;
            if (index < 0 || index >= roles.size()) {
                System.out.println("Invalid choice.");
                return null;
            }
            return roles.get(index).getRoleName();
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return null;
        }
    }

    // Entry point
    public void start() {
        System.out.println("\nWelcome, HR Manager!");
        if (!selectBranch()) return;
        showMenu();
    }

    // Main menu loop
    private void showMenu() {
        while (true) {
            System.out.println("\nManager Menu");
            System.out.println("Employee Management:");
            System.out.println("1.  Add new employee");
            System.out.println("2.  Update employee details");
            System.out.println("3.  Deactivate employee");
            System.out.println("4.  View all employees");
            System.out.println("5.  View employee details");
            System.out.println("6.  Assign role to employee");
            System.out.println("7.  Remove role from employee");
            System.out.println("Shift Management:");
            System.out.println("8.  Create new shift");
            System.out.println("9.  View shifts by date");
            System.out.println("10. Assign employee to shift");
            System.out.println("11. Remove employee from shift");
            System.out.println("12. Send override request to employee");
            System.out.println("13. View available & qualified employees for shift");
            System.out.println("14. Validate shift (check Shift Manager assigned)");
            System.out.println("15. View all shifts for an employee");
            System.out.println("16. View shifts for current week");
            System.out.println("Role Pool:");
            System.out.println("17. Add new role to company pool");
            System.out.println("18. View company role pool");
            System.out.println("19. Remove role from the role pool");
            System.out.println("20. Set availability submission deadline");
            System.out.println("21. Switch branch");
            System.out.println("0.  Back to main menu");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":  addEmployee();                        break;
                    case "2":  updateEmployeeDetails();              break;
                    case "3":  deactivateEmployee();                 break;
                    case "4":  viewAllEmployees();                   break;
                    case "5":  viewEmployeeDetails();                break;
                    case "6":  assignRoleToEmployee();               break;
                    case "7":  removeRoleFromEmployee();             break;
                    case "8":  createShift();                        break;
                    case "9":  viewShiftsByDate();                   break;
                    case "10": assignEmployeeToShift();              break;
                    case "11": removeEmployeeFromShift();            break;
                    case "12": sendOverrideRequest();                break;
                    case "13": viewAvailableAndQualifiedEmployees(); break;
                    case "14": validateShift();                      break;
                    case "15": viewShiftsForEmployee();              break;
                    case "16": viewCurrentWeekShifts();              break;
                    case "17": addRoleToPool();                      break;
                    case "18": viewRolePool();                       break;
                    case "19": removeRoleFromPool();                 break;
                    case "20": setSubmissionDeadline();              break;
                    case "21":  selectBranch();                      break;
                    case "0":
                        System.out.println("Returning to main menu...");
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }


    //Option 1

    private void addEmployee() throws Exception {
        System.out.println("\nAdd New Employee");

        System.out.print("ID: ");
        String id = scanner.nextLine().trim();

        System.out.print("First name: ");
        String firstName = scanner.nextLine().trim();

        System.out.print("Last name: ");
        String lastName = scanner.nextLine().trim();

        System.out.print("Bank account: ");
        String bankAccount = scanner.nextLine().trim();

        System.out.print("Wage: ");
        double wage = readDouble();

        JobScope jobScope = selectJobScope();
        if (jobScope == null) return;

        SalaryType salaryType = selectSalaryType();
        if (salaryType == null) return;

        System.out.println("Fixed day off:");
        DayOfWeek fixedDayOff = selectDayOptional();

        System.out.print("Start date (YYYY-MM-DD): ");
        LocalDate startDate = readDate();
        if (startDate == null) return;

        System.out.println("License type:");
        TruckType licenseType = selectLicenseType();

        empService.addEmployee(id, firstName, lastName, bankAccount,
                wage, jobScope, salaryType, fixedDayOff, startDate, currentBranch.getBranchId());

        if (licenseType != null) {
            empService.getEmployeeById(id).setLicenseType(licenseType);
        }

        System.out.println("Employee added successfully.");
    }


    //Option 2

    private void updateEmployeeDetails() throws Exception {
        System.out.println("\nUpdate Employee Details");

        System.out.print("Enter employee ID: ");
        String id = scanner.nextLine().trim();

        Employee emp = empService.getEmployeeById(id);
        if (emp == null) {
            System.out.println("Error: Employee not found.");
            return;
        }
        if (!currentBranch.equals(emp.getBranch())) {
            System.out.println("Error: Employee does not belong to this branch.");
            return;
        }

        System.out.println("Current: " + emp.getFullName() +
                " | Wage: " + emp.getWage() +
                " | Scope: " + emp.getJobScope());

        System.out.print("New first name [" + emp.getFirstName() + "]: ");
        String firstName = scanner.nextLine().trim();
        if (firstName.isEmpty()) firstName = emp.getFirstName();

        System.out.print("New last name [" + emp.getLastName() + "]: ");
        String lastName = scanner.nextLine().trim();
        if (lastName.isEmpty()) lastName = emp.getLastName();

        System.out.print("New bank account [" + emp.getBankAccount() + "]: ");
        String bankAccount = scanner.nextLine().trim();
        if (bankAccount.isEmpty()) bankAccount = emp.getBankAccount();

        System.out.print("New wage [" + emp.getWage() + "]: ");
        String wageStr = scanner.nextLine().trim();
        double wage = wageStr.isEmpty() ? emp.getWage() : Double.parseDouble(wageStr);

        System.out.println("New job scope [current: " + emp.getJobScope() + "]:");
        JobScope jobScope = selectJobScopeOptional(emp.getJobScope());

        System.out.println("New salary type [current: " + emp.getSalaryType() + "]:");
        SalaryType salaryType = selectSalaryTypeOptional(emp.getSalaryType());

        System.out.println("New fixed day off [current: " + emp.getFixedDayOff() + "]:");
        DayOfWeek fixedDayOff = selectDayOptional();

        empService.updateEmployeeDetails(id, firstName, lastName, bankAccount,
                wage, jobScope, salaryType, fixedDayOff);
        System.out.println("Update license type [current: " + emp.getLicenseType() + "]:");
        TruckType licenseType = selectLicenseTypeOptional(emp.getLicenseType());
        emp.setLicenseType(licenseType);

        System.out.println("Employee details updated successfully.");
    }


    //Option 3

    private void deactivateEmployee() throws Exception {
        System.out.println("\nDeactivate Employee");

        System.out.print("Enter employee ID: ");
        String id = scanner.nextLine().trim();

        Employee emp = empService.getEmployeeById(id);
        if (emp == null) {
            System.out.println("Error: Employee not found.");
            return;
        }
        if (!currentBranch.equals(emp.getBranch())) {
            System.out.println("Error: Employee does not belong to this branch.");
            return;
        }

        System.out.print("Are you sure you want to deactivate " +
                emp.getFullName() + "? (yes/no): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            System.out.println("Cancelled.");
            return;
        }

        empService.deactivateEmployee(id);
        shiftService.handleEmployeeDeactivation(id);

        System.out.println(emp.getFullName() + " has been deactivated and removed from future shifts.");
    }


    //Option 4

    private void viewAllEmployees() {
        System.out.println("\nAll Employees");

        List<Employee> employees = empService.getEmployeesByBranch(currentBranch.getBranchId());
        if (employees.isEmpty()) {
            System.out.println("No employees in the system.");
            return;
        }

        for (Employee emp : employees) {
            System.out.println(emp + " | Status: " + (emp.isActive() ? "Active" : "Inactive"));
        }
    }


    //Option 5

    private void viewEmployeeDetails() {
        System.out.println("\nEmployee Details");

        System.out.print("Enter employee ID: ");
        String id = scanner.nextLine().trim();

        Employee emp = empService.getEmployeeById(id);
        if (emp == null) {
            System.out.println("Error: Employee not found.");
            return;
        }

        if (!currentBranch.equals(emp.getBranch())) {
            System.out.println("Error: Employee does not belong to this branch.");
            return;
        }

        System.out.println("ID:            " + emp.getId());
        System.out.println("Name:          " + emp.getFullName());
        System.out.println("Bank Account:  " + emp.getBankAccount());
        System.out.println("Wage:          " + emp.getWage());
        System.out.println("Job Scope:     " + emp.getJobScope());
        System.out.println("Salary Type:   " + emp.getSalaryType());
        System.out.println("Fixed Day Off: " + (emp.getFixedDayOff() != null ? emp.getFixedDayOff() : "None"));
        System.out.println("Start Date:    " + emp.getStartDate());
        System.out.println("Annual Leave:  " + emp.getAnnualLeaveDays() + " days");
        System.out.println("Status:        " + (emp.isActive() ? "Active" : "Inactive"));
        System.out.println("License Type:  " +
                (emp.getLicenseType() != null ? emp.getLicenseType() : "None"));

        List<Role> roles = emp.getRoles();
        System.out.print("Roles:         ");
        if (roles.isEmpty()) {
            System.out.println("None");
        } else {
            for (int i = 0; i < roles.size(); i++) {
                System.out.print(roles.get(i).getRoleName());
                if (i < roles.size() - 1) System.out.print(", ");
            }
            System.out.println();
        }

        List<AvailabilitySubmission> avail = emp.getAvailabilities();
        if (avail.isEmpty()) {
            System.out.println("Availability:  Available for all shifts (no constraints).");
        } else {
            System.out.println("Availability:");
            for (AvailabilitySubmission sub : avail) {
                System.out.println("  " + sub.getDay() + ": " + sub.getShiftType());
            }
        }

        List<Shift> shifts = emp.getAssignedShifts();
        if (shifts.isEmpty()) {
            System.out.println("Assigned Shifts: None");
        } else {
            System.out.println("Assigned Shifts:");
            for (Shift s : shifts) {
                System.out.println("  " + s);
            }
        }
    }


    //Option 6

    private void assignRoleToEmployee() throws Exception {
        System.out.println("\nAssign Role to Employee");

        System.out.print("Enter employee ID: ");
        String id = scanner.nextLine().trim();
        Employee emp = empService.getEmployeeById(id);
        if (emp == null) {
            System.out.println("Error: Employee not found.");
            return;
        }
        if (!currentBranch.equals(emp.getBranch())) {
            System.out.println("Error: Employee does not belong to this branch.");
            return;
        }
        Role role = selectRoleFromMenu();
        if (role == null) return;
        empService.addRoleToEmployee(id, role.getRoleName());
        System.out.println("Role '" + role.getRoleName() + "' assigned successfully.");
    }


    //Option 7

    private void removeRoleFromEmployee() throws Exception {
        System.out.println("\nRemove Role from Employee");

        System.out.print("Enter employee ID: ");
        String id = scanner.nextLine().trim();

        Employee emp = empService.getEmployeeById(id);
        if (emp == null) {
            System.out.println("Error: Employee not found.");
            return;
        }
        if (!currentBranch.equals(emp.getBranch())) {
            System.out.println("Error: Employee does not belong to this branch.");
            return;
        }

        List<Role> roles = emp.getRoles();
        if (roles.isEmpty()) {
            System.out.println("This employee has no roles.");
            return;
        }

        System.out.println("Current roles:");
        for (int i = 0; i < roles.size(); i++) {
            System.out.println((i + 1) + ". " + roles.get(i).getRoleName());
        }
        System.out.print("Choose a role number to remove: ");
        try {
            int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (index < 0 || index >= roles.size()) {
                System.out.println("Invalid choice.");
                return;
            }
            empService.removeRoleFromEmployee(id, roles.get(index).getRoleName());
            System.out.println("Role removed successfully.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }


    // Option 8

    private void createShift() throws Exception {
        System.out.println("\nCreate New Shift");

        System.out.print("Enter date (YYYY-MM-DD): ");
        LocalDate date = readDate();
        if (date == null) return;

        ShiftType type = selectShiftType();
        if (type == null) return;

        shiftService.addShift(date, type,currentBranch);
        System.out.println("Shift created: " + type + " on " + date);
    }


    //  Option 9

    private void viewShiftsByDate() throws Exception {
        System.out.println("\nView Shifts by Date");

        System.out.print("Enter date (YYYY-MM-DD): ");
        LocalDate date = readDate();
        if (date == null) return;

        List<Shift> shifts = shiftService.getShiftsByDateAndBranch(date, currentBranch);
        if (shifts.isEmpty()) {
            System.out.println("No shifts on " + date);
            return;
        }

        for (Shift shift : shifts) {
            System.out.println("\n" + shift);
            List<ShiftAssignment> assigned = shift.getAssignedEmployees();
            if (assigned.isEmpty()) {
                System.out.println("  No employees assigned yet.");
            } else {
                for (ShiftAssignment a : assigned) {
                    System.out.println("  " + a.getRole().getRoleName() + ": " +
                            a.getEmployee().getFullName() + " (" + a.getEmployee().getId() + ")");
                }
            }
            System.out.println("  Shift Manager: " + (shift.hasShiftManager() ? "Assigned" : "NOT ASSIGNED"));
            System.out.println("  Fully staffed: " + (shift.isFullyStaffed() ? "Yes" : "No"));
        }
    }


    //  Option 10

    private void assignEmployeeToShift() throws Exception {
        System.out.println("\nAssign Employee to Shift");

        System.out.print("Enter date (YYYY-MM-DD): ");
        LocalDate date = readDate();
        if (date == null) return;

        ShiftType type = selectShiftType();
        if (type == null) return;

        System.out.print("Enter employee ID: ");
        String employeeId = scanner.nextLine().trim();

        Employee emp = empService.getEmployeeById(employeeId);
        if (emp == null) {
            System.out.println("Error: Employee not found.");
            return;
        }
        if (!currentBranch.equals(emp.getBranch())) {
            System.out.println("Error: Employee does not belong to this branch.");
            return;
        }
        Role role = selectRoleFromMenu();
        if (role == null) return;
        shiftService.assignEmployeeToShift(date, type, employeeId, role.getRoleName());

        System.out.println("Employee assigned successfully.");
    }


    //  Option 11

    private void removeEmployeeFromShift() throws Exception {
        System.out.println("\nRemove Employee from Shift");

        System.out.print("Enter date (YYYY-MM-DD): ");
        LocalDate date = readDate();
        if (date == null) return;

        ShiftType type = selectShiftType();
        if (type == null) return;

        System.out.print("Enter employee ID: ");
        String employeeId = scanner.nextLine().trim();

        Shift shift = shiftService.getShift(date, type);
        Employee emp = empService.getEmployeeById(employeeId);
        if (emp == null) {
            System.out.println("Error: Employee not found.");
            return;
        }
        if (!currentBranch.equals(emp.getBranch())) {
            System.out.println("Error: Employee does not belong to this branch.");
            return;
        }
        List<Role> assignedRoles = new ArrayList<>();
        for (ShiftAssignment a : shift.getAssignedEmployees()) {
            if (a.getEmployee().equals(emp)) {
                assignedRoles.add(a.getRole());
            }
        }
        if (assignedRoles.isEmpty()) {
            System.out.println("Error: Employee is not assigned to this shift.");
            return;
        }
        String roleName = selectRoleFromList(assignedRoles);
        if (roleName == null) return;
        shiftService.removeEmployeeFromShift(date, type, employeeId, roleName);
        System.out.println("Employee removed from shift successfully.");
    }


    //  Option 12

    private void sendOverrideRequest() throws Exception {
        System.out.println("\nSend Override Assignment Request");
        System.out.println("(Use this when the employee is not available on this day/shift)");

        System.out.print("Enter date (YYYY-MM-DD): ");
        LocalDate date = readDate();
        if (date == null) return;

        ShiftType type = selectShiftType();
        if (type == null) return;

        System.out.print("Enter employee ID: ");
        String employeeId = scanner.nextLine().trim();

        Employee emp = empService.getEmployeeById(employeeId);
        if (emp == null) {
            System.out.println("Error: Employee not found.");
            return;
        }
        if (!currentBranch.equals(emp.getBranch())) {
            System.out.println("Error: Employee does not belong to this branch.");
            return;
        }
        Role role = selectRoleFromMenu();
        if (role == null) return;
        shiftService.requestOverrideAssignment(date, type, employeeId, role.getRoleName());
        System.out.println("Override request sent. The employee must approve before being assigned.");
    }


    //  Option 13

    private void viewAvailableAndQualifiedEmployees() {
        System.out.println("\nAvailable & Qualified Employees for Shift");
        System.out.print("Enter date (YYYY-MM-DD): ");
        LocalDate date = readDate();
        if (date == null) return;
        ShiftType type = selectShiftType();
        if (type == null) return;
        Role role = selectRoleFromMenu();
        if (role == null) return;
        DayOfWeek day = date.getDayOfWeek();
        List<Employee> employees = empService.getAvailableAndQualifiedEmployees(day, type, role,currentBranch);
        if (employees.isEmpty()) {
            System.out.println("No available and qualified employees found.");
            return;
        }
        System.out.println("Available & qualified for " + role.getRoleName() + " on " + date + " (" + type + "):");
        for (Employee emp : employees) {
            System.out.println("  - " + emp.getFullName() + " (" + emp.getId() + ")");
        }
    }


    //  Option 14

    private void validateShift() throws Exception {
        System.out.println("\nValidate Shift");

        System.out.print("Enter date (YYYY-MM-DD): ");
        LocalDate date = readDate();
        if (date == null) return;

        ShiftType type = selectShiftType();
        if (type == null) return;

        Shift shift = shiftService.getShift(date, type);
        if (!currentBranch.equals(shift.getBranch())) {
            System.out.println("Error: Shift does not belong to this branch.");
            return;
        }
        shiftService.validateShift(date, type,currentBranch);
        System.out.println("Shift is valid — Shift Manager is assigned.");
    }


    //  Option 15

    private void viewShiftsForEmployee() {
        System.out.println("\nShifts for Employee");

        System.out.print("Enter employee ID: ");
        String id = scanner.nextLine().trim();

        Employee emp = empService.getEmployeeById(id);
        if (emp == null) {
            System.out.println("Error: Employee not found.");
            return;
        }

        if (!currentBranch.equals(emp.getBranch())) {
            System.out.println("Error: Employee does not belong to this branch.");
            return;
        }
        List<Shift> shifts = shiftService.getShiftsForEmployee(id);
        if (shifts.isEmpty()) {
            System.out.println(emp.getFullName() + " is not assigned to any shifts.");
            return;
        }

        System.out.println("Shifts for " + emp.getFullName() + ":");
        for (Shift s : shifts) {
            System.out.println("  " + s);
        }
    }


    //  Option 16

    private void viewCurrentWeekShifts() {
        System.out.println("\nCurrent Week Shifts");

        LocalDate today = LocalDate.now();
        LocalDate sunday = today.minusDays(today.getDayOfWeek().getValue() % 7);

        System.out.println("Week: " + sunday + " to " + sunday.plusDays(6));

        boolean found = false;
        for (int i = 0; i < 7; i++) {
            LocalDate date = sunday.plusDays(i);
            List<Shift> shifts = shiftService.getShiftsByDateAndBranch(date, currentBranch);
            if (!shifts.isEmpty()) {
                found = true;
                for (Shift shift : shifts) {
                    System.out.println("\n" + shift);
                    System.out.println("  Shift Manager: " +
                            (shift.hasShiftManager() ? "Assigned" : "NOT ASSIGNED"));
                    System.out.println("  Fully staffed: " +
                            (shift.isFullyStaffed() ? "Yes" : "No"));
                }
            }
        }

        if (!found) {
            System.out.println("No shifts scheduled for this week.");
        }
    }


    //  Option 17

    private void addRoleToPool() throws Exception {
        System.out.println("\nAdd New Role to Company Pool");

        System.out.print("Enter new role name: ");
        String roleName = scanner.nextLine().trim();

        empService.addRoleToPool(roleName);
        System.out.println("Role '" + roleName + "' added to the company role pool.");
    }


    //  Option 18

    private void viewRolePool() {
        System.out.println("\nCompany Role Pool");

        List<Role> pool = empService.getCompanyRolesPool();
        if (pool.isEmpty()) {
            System.out.println("No roles defined.");
            return;
        }

        for (int i = 0; i < pool.size(); i++) {
            System.out.println((i + 1) + ". " + pool.get(i).getRoleName());
        }
    }

    // Option 19
    private void removeRoleFromPool() throws Exception {
        System.out.println("\nRemove Role from Company Pool");
        Role role = selectRoleFromMenu();
        if (role == null) return;
        empService.removeRoleFromPool(role.getRoleName());
        System.out.println("Role '" + role.getRoleName() + "' removed from company pool.");
    }

    //option 20
    private void setSubmissionDeadline() throws Exception {
        System.out.println("\nSet Availability Submission Deadline");
        System.out.println("Current deadline: " +
                empService.getDeadlineDay() + " at " + empService.getDeadlineHour() + ":00");

        DayOfWeek day = selectDayOptional();
        if (day == null) return;

        System.out.print("Enter hour (0-23): ");
        int hour = (int) readDouble();

        empService.setSubmissionDeadline(day, hour);
        System.out.println("Deadline updated: " + day + " at " + hour + ":00");
    }
    //option 21

    // Shows all branches and lets the manager select one.
    // Updates currentBranch and returns true if a valid branch was selected.
    private boolean selectBranch() {
        List<Branch> branches = empService.getAllBranches();
        if (branches.isEmpty()) {
            System.out.println("No branches defined in the system.");
            return false;
        }
        System.out.println("Select your branch:");
        for (int i = 0; i < branches.size(); i++) {
            System.out.println((i + 1) + ". " + branches.get(i).getName());
        }
        System.out.print("Your choice: ");
        try {
            int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (index < 0 || index >= branches.size()) {
                System.out.println("Invalid choice.");
                return false;
            }
            currentBranch = branches.get(index);
            System.out.println("Selected branch: " + currentBranch.getName());
            return true;

        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return false;
        }
    }
    //Input Helpers

    private LocalDate readDate() {
        try {
            return LocalDate.parse(scanner.nextLine().trim());
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            return null;
        }
    }

    private double readDouble() {
        try {
            return Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return 0;
        }
    }

    private ShiftType selectShiftType() {
        System.out.println("Select shift type:");
        System.out.println("1. Morning (06:00-14:00)");
        System.out.println("2. Evening (14:00-22:00)");
        System.out.print("Your choice: ");
        switch (scanner.nextLine().trim()) {
            case "1": return ShiftType.MORNING;
            case "2": return ShiftType.EVENING;
            default:  System.out.println("Invalid shift type."); return null;
        }
    }

    private JobScope selectJobScope() {
        System.out.println("Select job scope:");
        System.out.println("1. Full time");
        System.out.println("2. Part time");
        System.out.print("Your choice: ");
        switch (scanner.nextLine().trim()) {
            case "1": return JobScope.FULL_TIME;
            case "2": return JobScope.PART_TIME;
            default:  System.out.println("Invalid job scope."); return null;
        }
    }

    private JobScope selectJobScopeOptional(JobScope current) {
        System.out.println("1. Full time  2. Part time  (Enter to keep current)");
        System.out.print("Your choice: ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return current;
        switch (input) {
            case "1": return JobScope.FULL_TIME;
            case "2": return JobScope.PART_TIME;
            default:  return current;
        }
    }

    private SalaryType selectSalaryType() {
        System.out.println("Select salary type:");
        System.out.println("1. Hourly");
        System.out.println("2. Global");
        System.out.print("Your choice: ");
        switch (scanner.nextLine().trim()) {
            case "1": return SalaryType.HOURLY;
            case "2": return SalaryType.GLOBAL;
            default:  System.out.println("Invalid salary type."); return null;
        }
    }

    private SalaryType selectSalaryTypeOptional(SalaryType current) {
        System.out.println("1. Hourly  2. Global  (Enter to keep current)");
        System.out.print("Your choice: ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return current;
        switch (input) {
            case "1": return SalaryType.HOURLY;
            case "2": return SalaryType.GLOBAL;
            default:  return current;
        }
    }

    private DayOfWeek selectDayOptional() {
        System.out.println("1.Sunday 2.Monday 3.Tuesday 4.Wednesday 5.Thursday 6.Friday 7.Saturday 0.None");
        System.out.print("Your choice: ");
        switch (scanner.nextLine().trim()) {
            case "1": return DayOfWeek.SUNDAY;
            case "2": return DayOfWeek.MONDAY;
            case "3": return DayOfWeek.TUESDAY;
            case "4": return DayOfWeek.WEDNESDAY;
            case "5": return DayOfWeek.THURSDAY;
            case "6": return DayOfWeek.FRIDAY;
            case "7": return DayOfWeek.SATURDAY;
            default:  return null;
        }
    }

    private TruckType selectLicenseTypeOptional(TruckType current) {
        System.out.println("1. Type B  2. Type C  3. Type D  0. None  (Enter to keep current)");
        System.out.print("Your choice: ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return current;
        switch (input) {
            case "1": return TruckType.TYPE_B;
            case "2": return TruckType.TYPE_C;
            case "3": return TruckType.TYPE_D;
            case "0": return null;
            default: return current;
        }
    }

    private TruckType selectLicenseType() {
        System.out.println("Select license type (0 if not a driver):");
        System.out.println("1. Type B");
        System.out.println("2. Type C");
        System.out.println("3. Type D");
        System.out.println("0. None");
        System.out.print("Your choice: ");
        switch (scanner.nextLine().trim()) {
            case "1": return TruckType.TYPE_B;
            case "2": return TruckType.TYPE_C;
            case "3": return TruckType.TYPE_D;
            default: return null;
        }
    }
}