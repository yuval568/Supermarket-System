package employees.presentation;

import employees.domain.*;
import employees.service.EmployeeService;
import employees.service.ShiftService;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class DataUI {

    public static void loadSampleData() throws Exception {

        EmployeeService empService = EmployeeService.getInstance();
        ShiftService shiftService = ShiftService.getInstance();

        // ==================== Create Branches ====================

        empService.addBranch("B001", "Tel Aviv Branch", "Rothschild 1, Tel Aviv");
        empService.addBranch("B002", "Jerusalem Branch", "Jaffa 10, Jerusalem");

        // ==================== Create Employees ====================

        // Branch B001
        empService.addEmployee("111111111", "Yossi", "Cohen", "12-345-67", 45.0,
                JobScope.FULL_TIME, SalaryType.HOURLY, DayOfWeek.SATURDAY, LocalDate.of(2024, 1, 15), "B001");

        empService.addEmployee("222222222", "Dana", "Levi", "23-456-78", 55.0,
                JobScope.FULL_TIME, SalaryType.GLOBAL, DayOfWeek.FRIDAY, LocalDate.of(2023, 6, 1), "B001");

        empService.addEmployee("333333333", "Avi", "Mizrahi", "34-567-89", 40.0,
                JobScope.PART_TIME, SalaryType.HOURLY, DayOfWeek.SATURDAY, LocalDate.of(2025, 3, 10), "B001");

        empService.addEmployee("444444444", "Noa", "Ben David", "45-678-90", 50.0,
                JobScope.FULL_TIME, SalaryType.GLOBAL, DayOfWeek.FRIDAY, LocalDate.of(2024, 9, 1), "B001");

        empService.addEmployee("555555555", "Eli", "Peretz", "56-789-01", 42.0,
                JobScope.PART_TIME, SalaryType.HOURLY, DayOfWeek.SATURDAY, LocalDate.of(2025, 1, 20), "B001");

        // Branch B002
        empService.addEmployee("666666666", "Shira", "Alon", "67-890-12", 48.0,
                JobScope.FULL_TIME, SalaryType.HOURLY, DayOfWeek.FRIDAY, LocalDate.of(2024, 4, 1), "B002");

        empService.addEmployee("777777777", "Omer", "Katz", "78-901-23", 52.0,
                JobScope.FULL_TIME, SalaryType.GLOBAL, DayOfWeek.SATURDAY, LocalDate.of(2023, 11, 15), "B002");

        empService.addEmployee("888888888", "Tal", "Shamir", "89-012-34", 38.0,
                JobScope.PART_TIME, SalaryType.HOURLY, DayOfWeek.FRIDAY, LocalDate.of(2025, 2, 1), "B002");

        empService.addEmployee("999999999", "Lior", "Hadad", "90-123-45", 60.0,
                JobScope.FULL_TIME, SalaryType.GLOBAL, DayOfWeek.SATURDAY, LocalDate.of(2022, 8, 1), "B002");

        empService.addEmployee("123456789", "Noy", "Rubin", "01-234-56", 44.0,
                JobScope.PART_TIME, SalaryType.HOURLY, DayOfWeek.FRIDAY, LocalDate.of(2024, 12, 1), "B002");

        // ==================== Assign Roles ====================

        empService.addRoleToEmployee("111111111", "Cashier");
        empService.addRoleToEmployee("111111111", "Shift Manager");

        empService.addRoleToEmployee("222222222", "Storekeeper");
        empService.addRoleToEmployee("222222222", "Shift Manager");

        empService.addRoleToEmployee("333333333", "Cashier");
        empService.addRoleToEmployee("333333333", "General Worker");
        empService.addRoleToEmployee("333333333", "Driver");

        empService.addRoleToEmployee("444444444", "Storekeeper");
        empService.addRoleToEmployee("444444444", "General Worker");
        empService.addRoleToEmployee("444444444", "Driver");

        empService.addRoleToEmployee("555555555", "General Worker");
        empService.addRoleToEmployee("555555555", "Cashier");

        empService.addRoleToEmployee("666666666", "Cashier");
        empService.addRoleToEmployee("666666666", "Storekeeper");

        empService.addRoleToEmployee("777777777", "Shift Manager");
        empService.addRoleToEmployee("777777777", "General Worker");
        empService.addRoleToEmployee("777777777", "Driver");

        empService.addRoleToEmployee("888888888", "General Worker");
        empService.addRoleToEmployee("888888888", "Driver");

        empService.addRoleToEmployee("999999999", "Shift Manager");
        empService.addRoleToEmployee("999999999", "Storekeeper");

        empService.addRoleToEmployee("123456789", "Cashier");
        empService.addRoleToEmployee("123456789", "General Worker");

        // ==================== Assign License Types ====================

        empService.setEmployeeLicense("333333333", TruckType.TYPE_B);
        empService.setEmployeeLicense("444444444", TruckType.TYPE_C);
        empService.setEmployeeLicense("777777777", TruckType.TYPE_D);
        empService.setEmployeeLicense("888888888", TruckType.TYPE_B);

        // ==================== Set Availabilities ====================

        empService.setSubmissionDeadline(DayOfWeek.SATURDAY, 23);

        empService.addEmployeeAvailability("111111111", DayOfWeek.SUNDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("111111111", DayOfWeek.MONDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("111111111", DayOfWeek.TUESDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("111111111", DayOfWeek.WEDNESDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("111111111", DayOfWeek.THURSDAY, ShiftType.MORNING);

        empService.addEmployeeAvailability("222222222", DayOfWeek.SUNDAY, ShiftType.EVENING);
        empService.addEmployeeAvailability("222222222", DayOfWeek.MONDAY, ShiftType.EVENING);
        empService.addEmployeeAvailability("222222222", DayOfWeek.TUESDAY, ShiftType.EVENING);
        empService.addEmployeeAvailability("222222222", DayOfWeek.WEDNESDAY, ShiftType.EVENING);

        empService.addEmployeeAvailability("333333333", DayOfWeek.SUNDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("333333333", DayOfWeek.SUNDAY, ShiftType.EVENING);
        empService.addEmployeeAvailability("333333333", DayOfWeek.TUESDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("333333333", DayOfWeek.TUESDAY, ShiftType.EVENING);

        empService.addEmployeeAvailability("444444444", DayOfWeek.MONDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("444444444", DayOfWeek.MONDAY, ShiftType.EVENING);
        empService.addEmployeeAvailability("444444444", DayOfWeek.TUESDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("444444444", DayOfWeek.TUESDAY, ShiftType.EVENING);
        empService.addEmployeeAvailability("444444444", DayOfWeek.WEDNESDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("444444444", DayOfWeek.WEDNESDAY, ShiftType.EVENING);
        empService.addEmployeeAvailability("444444444", DayOfWeek.THURSDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("444444444", DayOfWeek.THURSDAY, ShiftType.EVENING);

        // Eli - no availabilities (available for all shifts by default)

        empService.addEmployeeAvailability("666666666", DayOfWeek.SUNDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("666666666", DayOfWeek.MONDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("666666666", DayOfWeek.TUESDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("666666666", DayOfWeek.WEDNESDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("666666666", DayOfWeek.THURSDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("666666666", DayOfWeek.SUNDAY, ShiftType.EVENING);
        empService.addEmployeeAvailability("666666666", DayOfWeek.MONDAY, ShiftType.EVENING);

        empService.addEmployeeAvailability("777777777", DayOfWeek.SUNDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("777777777", DayOfWeek.SUNDAY, ShiftType.EVENING);
        empService.addEmployeeAvailability("777777777", DayOfWeek.MONDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("777777777", DayOfWeek.MONDAY, ShiftType.EVENING);
        empService.addEmployeeAvailability("777777777", DayOfWeek.TUESDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("777777777", DayOfWeek.TUESDAY, ShiftType.EVENING);
        empService.addEmployeeAvailability("777777777", DayOfWeek.WEDNESDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("777777777", DayOfWeek.WEDNESDAY, ShiftType.EVENING);

        empService.addEmployeeAvailability("888888888", DayOfWeek.TUESDAY, ShiftType.EVENING);
        empService.addEmployeeAvailability("888888888", DayOfWeek.WEDNESDAY, ShiftType.EVENING);
        empService.addEmployeeAvailability("888888888", DayOfWeek.THURSDAY, ShiftType.EVENING);

        empService.addEmployeeAvailability("999999999", DayOfWeek.SUNDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("999999999", DayOfWeek.SUNDAY, ShiftType.EVENING);
        empService.addEmployeeAvailability("999999999", DayOfWeek.MONDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("999999999", DayOfWeek.MONDAY, ShiftType.EVENING);
        empService.addEmployeeAvailability("999999999", DayOfWeek.TUESDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("999999999", DayOfWeek.TUESDAY, ShiftType.EVENING);
        empService.addEmployeeAvailability("999999999", DayOfWeek.WEDNESDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("999999999", DayOfWeek.WEDNESDAY, ShiftType.EVENING);
        empService.addEmployeeAvailability("999999999", DayOfWeek.THURSDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("999999999", DayOfWeek.THURSDAY, ShiftType.EVENING);

        empService.addEmployeeAvailability("123456789", DayOfWeek.SUNDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("123456789", DayOfWeek.WEDNESDAY, ShiftType.MORNING);

        empService.setSubmissionDeadline(DayOfWeek.WEDNESDAY, 23);

        // ==================== Create Shifts ====================

        LocalDate nextSunday = LocalDate.now().with(DayOfWeek.SUNDAY);
        if (!nextSunday.isAfter(LocalDate.now())) {
            nextSunday = nextSunday.plusWeeks(1);
        }

        Branch b001 = empService.getBranchById("B001");
        Branch b002 = empService.getBranchById("B002");

        for (int i = 0; i < 7; i++) {
            shiftService.addShift(nextSunday.plusDays(i), ShiftType.MORNING, b001);
            shiftService.addShift(nextSunday.plusDays(i), ShiftType.EVENING, b001);
            shiftService.addShift(nextSunday.plusDays(i), ShiftType.MORNING, b002);
            shiftService.addShift(nextSunday.plusDays(i), ShiftType.EVENING, b002);
        }

        // Load mock deliveries for integration testing
        MockDeliveryData.loadMockDeliveries();
    }
}