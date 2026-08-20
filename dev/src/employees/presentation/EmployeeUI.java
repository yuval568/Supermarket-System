package employees.presentation;

import employees.domain.*;
import employees.domain.*;
import employees.service.EmployeeService;
import employees.service.ShiftService;
import employees.service.*;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Scanner;

public class EmployeeUI {

    private final Scanner scanner;
    private final EmployeeService empService;
    private final ShiftService shiftService;
    private Employee currentEmployee;

    public EmployeeUI(Scanner scanner) {
        this.scanner = scanner;
        this.empService = EmployeeService.getInstance();
        this.shiftService = ShiftService.getInstance();
    }

    //Login screen - employee enters their ID
    public void login() {
        System.out.println("\n Employee Login");
        System.out.print("Enter your ID: ");
        String id = scanner.nextLine().trim();

        Employee emp = empService.getEmployeeById(id);
        if (emp == null) {
            System.out.println("Error: Employee not found.");
            return;
        }
        if (!emp.isActive()) {
            System.out.println("Error: Your account is no longer active.");
            return;
        }

        this.currentEmployee = emp;
        System.out.println("Welcome, " + emp.getFullName() + "!");
        showMenu();
    }

    //Main menu for the employee
    private void showMenu() {
        while (true) {
            System.out.println("\n===== Employee Menu =====");
            System.out.println("1. View my assigned shifts");
            System.out.println("2. View my pending shift requests");
            System.out.println("3. Respond to a pending shift request");
            System.out.println("4. Add availability");
            System.out.println("5. Remove availability");
            System.out.println("6. View my availabilities");
            System.out.println("7. Clear all availabilities");
            System.out.println("8. View my details");
            System.out.println("0. Back to main menu");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        viewMyShifts();
                        break;
                    case "2":
                        viewPendingRequests();
                        break;
                    case "3":
                        respondToPendingRequest();
                        break;
                    case "4":
                        addAvailability();
                        break;
                    case "5":
                        removeAvailability();
                        break;
                    case "6":
                        viewMyAvailabilities();
                        break;
                    case "7":
                        clearAllAvailabilities();
                        break;
                    case "8":
                        viewMyDetails();
                        break;
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

    //Shift Views

    //Display assigned shifts
    private void viewMyShifts() {
        System.out.println("\nMy Assigned Shifts");

        List<Shift> shifts = currentEmployee.getAssignedShifts();
        if (shifts.isEmpty()) {
            System.out.println("You are not assigned to any shifts.");
            return;
        }

        System.out.println("Your assigned shifts:");
        for (int i = 0; i < shifts.size(); i++) {
            System.out.println((i + 1) + ". " + shifts.get(i));
        }
    }

    //Display pending shift requests
    private void viewPendingRequests() {
        System.out.println("\nPending Shift Requests");

        List<Shift> pending = currentEmployee.getPendingShifts();
        if (pending.isEmpty()) {
            System.out.println("No pending requests.");
            return;
        }

        System.out.println("Your Pending Shift Requests:");
        for (int i = 0; i < pending.size(); i++) {
            System.out.println((i + 1) + ". " + pending.get(i));
        }
    }

    //Respond to a pending shift request
    private void respondToPendingRequest() {
        List<Shift> pending = currentEmployee.getPendingShifts();
        if (pending.isEmpty()) {
            System.out.println("No pending requests to respond to.");
            return;
        }

        System.out.println("\nRespond to Pending Request");
        for (int i = 0; i < pending.size(); i++) {
            System.out.println((i + 1) + ". " + pending.get(i));
        }

        System.out.print("Choose a request number: ");
        String indexStr = scanner.nextLine().trim();

        int index;
        try {
            index = Integer.parseInt(indexStr) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        if (index < 0 || index >= pending.size()) {
            System.out.println("Invalid request number.");
            return;
        }

        Shift selectedShift = pending.get(index);

        System.out.println("1. Approve");
        System.out.println("2. Reject");
        System.out.print("Your choice: ");
        String responseChoice = scanner.nextLine().trim();

        ApprovalStatus response;
        switch (responseChoice) {
            case "1": response = ApprovalStatus.APPROVED; break;
            case "2": response = ApprovalStatus.REJECTED; break;
            default:
                System.out.println("Invalid choice.");
                return;
        }

        try {

            shiftService.respondToOverrideRequest(
                    selectedShift.getDate(),
                    selectedShift.getType(),
                    currentEmployee.getId(),
                    response
            );

            if (response == ApprovalStatus.APPROVED) {
                System.out.println("Shift approved and assigned successfully!");
            } else {
                System.out.println("Shift request rejected.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Availability Management

    //Helper method to select a day of the week
    private DayOfWeek selectDay() {
        System.out.println("Choose a day:");
        System.out.println("1. Sunday");
        System.out.println("2. Monday");
        System.out.println("3. Tuesday");
        System.out.println("4. Wednesday");
        System.out.println("5. Thursday");
        System.out.println("6. Friday");
        System.out.println("7. Saturday");
        System.out.print("Your choice: ");
        String dayChoice = scanner.nextLine().trim();

        switch (dayChoice) {
            case "1": return DayOfWeek.SUNDAY;
            case "2": return DayOfWeek.MONDAY;
            case "3": return DayOfWeek.TUESDAY;
            case "4": return DayOfWeek.WEDNESDAY;
            case "5": return DayOfWeek.THURSDAY;
            case "6": return DayOfWeek.FRIDAY;
            case "7": return DayOfWeek.SATURDAY;
            default: return null;
        }
    }

    //Helper method to select a shift type
    private ShiftType selectShiftType() {
        System.out.println("Choose shift type:");
        System.out.println("1. Morning (06:00-14:00)");
        System.out.println("2. Evening (14:00-22:00)");
        System.out.print("Your choice: ");
        String shiftChoice = scanner.nextLine().trim();

        switch (shiftChoice) {
            case "1": return ShiftType.MORNING;
            case "2": return ShiftType.EVENING;
            default: return null;
        }
    }

    //Add availability for a specific day and shift type
    private void addAvailability() {
        System.out.println("\nAdd Availability");

        DayOfWeek day = selectDay();
        if (day == null) {
            System.out.println("Invalid day.");
            return;
        }

        ShiftType type = selectShiftType();
        if (type == null) {
            System.out.println("Invalid shift type.");
            return;
        }

        try {
            empService.addEmployeeAvailability(currentEmployee.getId(), day, type);
            System.out.println("Availability added: " + day + " " + type);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Remove a specific availability
    private void removeAvailability() {
        System.out.println("\nRemove Availability");

        DayOfWeek day = selectDay();
        if (day == null) {
            System.out.println("Invalid day.");
            return;
        }

        ShiftType type = selectShiftType();
        if (type == null) {
            System.out.println("Invalid shift type.");
            return;
        }

        try {
            empService.removeEmployeeAvailability(currentEmployee.getId(), day, type);
            System.out.println("Availability removed: " + day + " " + type);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Display current availabilities
    private void viewMyAvailabilities() {
        System.out.println("\nMy Availabilities");

        List<AvailabilitySubmission> availabilities = currentEmployee.getAvailabilities();
        if (availabilities.isEmpty()) {
            System.out.println("No specific availabilities submitted (available for all shifts by default).");
            return;
        }

        for (AvailabilitySubmission sub : availabilities) {
            System.out.println(sub.getDay() + ": " + sub.getShiftType());
        }
    }

    //Clear all availabilities
    private void clearAllAvailabilities() {
        System.out.println("\nClear All Availabilities");
        System.out.print("Are you sure? This will remove ALL your availabilities. (yes/no): ");
        String confirm = scanner.nextLine().trim();

        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("Operation cancelled.");
            return;
        }

        try {
            empService.clearEmployeeAvailabilities(currentEmployee.getId());
            System.out.println("All availabilities cleared. You are now available for all shifts by default.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Personal Details

    //Display personal details including roles
    private void viewMyDetails() {
        System.out.println("\n--- My Details ---");
        System.out.println("ID: " + currentEmployee.getId());
        System.out.println("Name: " + currentEmployee.getFullName());
        System.out.println("Bank Account: " + currentEmployee.getBankAccount());
        System.out.println("Wage: " + currentEmployee.getWage());
        System.out.println("Job Scope: " + currentEmployee.getJobScope());
        System.out.println("Salary Type: " + currentEmployee.getSalaryType());
        System.out.println("Fixed Day Off: " + currentEmployee.getFixedDayOff());
        System.out.println("Start Date: " + currentEmployee.getStartDate());
        System.out.println("Annual Leave Days: " + currentEmployee.getAnnualLeaveDays());
        System.out.println("Status: " + (currentEmployee.isActive() ? "Active" : "Inactive"));
        System.out.println("License Type: " +
                (currentEmployee.getLicenseType() != null ? currentEmployee.getLicenseType() : "None"));

        List<Role> roles = currentEmployee.getRoles();
        if (roles.isEmpty()) {
            System.out.println("Roles: None");
        } else {
            System.out.print("Roles: ");
            for (int i = 0; i < roles.size(); i++) {
                System.out.print(roles.get(i).getRoleName());
                if (i < roles.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }
    }
}