package employees;

import employees.domain.*;
import employees.repository.DatabaseManager;
import employees.service.EmployeeService;
import employees.service.ShiftService;
import org.junit.Before;
import org.junit.Test;
import java.time.DayOfWeek;
import java.time.LocalDate;
import static org.junit.Assert.*;

public class ServiceTest {

    private EmployeeService empService;
    private ShiftService shiftService;
    private LocalDate tomorrow;

    @Before
    public void setUp() throws Exception {
        DatabaseManager.resetForTesting();

        empService = EmployeeService.getInstance();
        shiftService = ShiftService.getInstance();
        tomorrow = LocalDate.now().plusDays(1);

        // Create test branch
        empService.addBranch("B001", "Test Branch", "Test Address");

        // Open submission window
        empService.setSubmissionDeadline(DayOfWeek.SATURDAY, 23);
    }

    // 14. Full normal assignment flow
    @Test
    public void testFullAssignmentFlow_succeeds() throws Exception {
        empService.addEmployee("111111111", "Yossi", "Cohen", "12-345-67",
                50.0, JobScope.FULL_TIME, SalaryType.HOURLY, null, LocalDate.now(), "B001");
        empService.addRoleToEmployee("111111111", "Shift Manager");
        empService.addEmployeeAvailability("111111111", tomorrow.getDayOfWeek(), ShiftType.MORNING);

        Branch branch = empService.getBranchById("B001");
        shiftService.addShift(tomorrow, ShiftType.MORNING, branch);
        shiftService.assignEmployeeToShift(tomorrow, ShiftType.MORNING, "111111111", "Shift Manager");

        Shift shift = shiftService.getShift(tomorrow, ShiftType.MORNING);
        Employee emp = empService.getEmployeeById("111111111");

        assertTrue(shift.isEmployeeAssigned(emp));
        assertTrue(shift.hasShiftManager());
        assertTrue(emp.getAssignedShifts().contains(shift));
    }

    // 15. Override flow — employee approves
    @Test
    public void testOverrideFlow_approved_employeeAssigned() throws Exception {
        empService.addEmployee("111111111", "Yossi", "Cohen", "12-345-67",
                50.0, JobScope.FULL_TIME, SalaryType.HOURLY, null, LocalDate.now(), "B001");
        empService.addRoleToEmployee("111111111", "Cashier");
        empService.addEmployeeAvailability("111111111", tomorrow.getDayOfWeek(), ShiftType.MORNING);

        Branch branch = empService.getBranchById("B001");
        shiftService.addShift(tomorrow, ShiftType.EVENING, branch);
        shiftService.requestOverrideAssignment(tomorrow, ShiftType.EVENING, "111111111", "Cashier");

        Employee emp = empService.getEmployeeById("111111111");
        Shift shift = shiftService.getShift(tomorrow, ShiftType.EVENING);

        assertTrue(emp.getPendingShifts().contains(shift));

        shiftService.respondToOverrideRequest(tomorrow, ShiftType.EVENING,
                "111111111", ApprovalStatus.APPROVED);

        assertTrue(shift.isEmployeeAssigned(emp));
        assertTrue(emp.getAssignedShifts().contains(shift));
        assertTrue(emp.getPendingShifts().isEmpty());
    }

    // 16. Deactivate employee removes from future shifts
    @Test
    public void testDeactivate_removesFromFutureShifts() throws Exception {
        empService.addEmployee("111111111", "Yossi", "Cohen", "12-345-67",
                50.0, JobScope.FULL_TIME, SalaryType.HOURLY, null, LocalDate.now(), "B001");
        empService.addRoleToEmployee("111111111", "Cashier");
        empService.addEmployeeAvailability("111111111", tomorrow.getDayOfWeek(), ShiftType.MORNING);

        Branch branch = empService.getBranchById("B001");
        shiftService.addShift(tomorrow, ShiftType.MORNING, branch);
        shiftService.assignEmployeeToShift(tomorrow, ShiftType.MORNING, "111111111", "Cashier");

        empService.deactivateEmployee("111111111");
        shiftService.handleEmployeeDeactivation("111111111");

        Shift shift = shiftService.getShift(tomorrow, ShiftType.MORNING);
        Employee emp = empService.getEmployeeById("111111111");

        assertFalse(shift.isEmployeeAssigned(emp));
        assertFalse(emp.isActive());
    }

    // 17. Inactive employee cannot be assigned
    @Test(expected = Exception.class)
    public void testAssign_inactiveEmployee_throwsException() throws Exception {
        empService.addEmployee("111111111", "Yossi", "Cohen", "12-345-67",
                50.0, JobScope.FULL_TIME, SalaryType.HOURLY, null, LocalDate.now(), "B001");
        empService.addRoleToEmployee("111111111", "Cashier");
        empService.addEmployeeAvailability("111111111", tomorrow.getDayOfWeek(), ShiftType.MORNING);
        empService.deactivateEmployee("111111111");

        Branch branch = empService.getBranchById("B001");
        shiftService.addShift(tomorrow, ShiftType.MORNING, branch);
        shiftService.assignEmployeeToShift(tomorrow, ShiftType.MORNING, "111111111", "Cashier");
    }

    // 18. Deadline — employee cannot submit after deadline
    @Test(expected = Exception.class)
    public void testDeadline_afterDeadline_submissionBlocked() throws Exception {
        empService.setSubmissionDeadline(DayOfWeek.MONDAY, 0);
        empService.addEmployee("111111111", "Yossi", "Cohen", "12-345-67",
                50.0, JobScope.FULL_TIME, SalaryType.HOURLY, null, LocalDate.now(), "B001");
        empService.addEmployeeAvailability("111111111", DayOfWeek.MONDAY, ShiftType.MORNING);
    }

    // 19. Deadline — manager can still assign after deadline
    @Test
    public void testDeadline_afterDeadline_managerCanStillAssign() throws Exception {
        empService.setSubmissionDeadline(DayOfWeek.MONDAY, 0);
        empService.addEmployee("111111111", "Yossi", "Cohen", "12-345-67",
                50.0, JobScope.FULL_TIME, SalaryType.HOURLY, null, LocalDate.now(), "B001");
        empService.addRoleToEmployee("111111111", "Cashier");

        Branch branch = empService.getBranchById("B001");
        shiftService.addShift(tomorrow, ShiftType.MORNING, branch);
        shiftService.assignEmployeeToShift(tomorrow, ShiftType.MORNING, "111111111", "Cashier");

        assertTrue(shiftService.getShift(tomorrow, ShiftType.MORNING)
                .isEmployeeAssigned(empService.getEmployeeById("111111111")));
    }

    // 20. Employee can work both morning and evening same day
    @Test
    public void testEmployee_canWorkBothShiftsSameDay() throws Exception {
        empService.addEmployee("111111111", "Yossi", "Cohen", "12-345-67",
                50.0, JobScope.FULL_TIME, SalaryType.HOURLY, null, LocalDate.now(), "B001");
        empService.addRoleToEmployee("111111111", "Cashier");
        empService.addEmployeeAvailability("111111111", tomorrow.getDayOfWeek(), ShiftType.MORNING);
        empService.addEmployeeAvailability("111111111", tomorrow.getDayOfWeek(), ShiftType.EVENING);

        Branch branch = empService.getBranchById("B001");
        shiftService.addShift(tomorrow, ShiftType.MORNING, branch);
        shiftService.addShift(tomorrow, ShiftType.EVENING, branch);

        shiftService.assignEmployeeToShift(tomorrow, ShiftType.MORNING, "111111111", "Cashier");
        shiftService.assignEmployeeToShift(tomorrow, ShiftType.EVENING, "111111111", "Cashier");

        assertEquals(2, empService.getEmployeeById("111111111").getAssignedShifts().size());
    }
}