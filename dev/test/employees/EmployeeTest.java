package employees;

import employees.domain.*;
import employees.repository.DatabaseManager;
import employees.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import java.time.DayOfWeek;
import java.time.LocalDate;
import static org.junit.Assert.*;

public class EmployeeTest {

    private Employee employee;
    private Branch branch;

    @Before
    public void setUp() throws Exception {
        DatabaseManager.resetForTesting();
        EmployeeService empService = EmployeeService.getInstance();
        empService.addBranch("B001", "Test Branch", "Test Address");
        branch = empService.getBranchById("B001");

        employee = new Employee("123456789", "Yossi", "Cohen", "12-345-67",
                50.0, JobScope.FULL_TIME, SalaryType.HOURLY,
                DayOfWeek.SATURDAY, LocalDate.of(2024, 1, 1), branch);
    }

    // 1. Constructor validation — null ID throws
    @Test(expected = Exception.class)
    public void testConstructor_nullId_throwsException() throws Exception {
        new Employee(null, "Yossi", "Cohen", "12-345-67",
                50.0, JobScope.FULL_TIME, SalaryType.HOURLY, null, LocalDate.now(), branch);
    }

    // 2. Constructor validation — negative wage throws
    @Test(expected = Exception.class)
    public void testConstructor_negativeWage_throwsException() throws Exception {
        new Employee("111111111", "Yossi", "Cohen", "12-345-67",
                -10.0, JobScope.FULL_TIME, SalaryType.HOURLY, null, LocalDate.now(), branch);
    }

    // 3. fixedDayOff always blocks availability
    @Test
    public void testIsAvailable_fixedDayOff_alwaysBlocked() {
        assertTrue(employee.getAvailabilities().isEmpty());
        assertFalse(employee.isAvailable(DayOfWeek.SATURDAY, ShiftType.MORNING));
        assertFalse(employee.isAvailable(DayOfWeek.SATURDAY, ShiftType.EVENING));
    }

    // 4. Empty availability map = available for all shifts
    @Test
    public void testIsAvailable_emptyMap_availableForAll() {
        assertTrue(employee.isAvailable(DayOfWeek.MONDAY, ShiftType.MORNING));
        assertTrue(employee.isAvailable(DayOfWeek.WEDNESDAY, ShiftType.EVENING));
    }

    // 5. Availability added correctly
    @Test
    public void testAvailability_addAndCheck() throws Exception {
        employee.addAvailability(DayOfWeek.MONDAY, ShiftType.MORNING);
        assertTrue(employee.isAvailable(DayOfWeek.MONDAY, ShiftType.MORNING));
        assertFalse(employee.isAvailable(DayOfWeek.MONDAY, ShiftType.EVENING));
        assertFalse(employee.isAvailable(DayOfWeek.TUESDAY, ShiftType.MORNING));
    }

    // 6. Role assignment and qualification check
    @Test
    public void testRole_addAndCheck_caseInsensitive() {
        employee.addRole(new Role("Cashier"));
        assertTrue(employee.isQualifiedFor(new Role("Cashier")));
        assertTrue(employee.isQualifiedFor(new Role("cashier")));
        assertFalse(employee.isQualifiedFor(new Role("Storekeeper")));
    }

    // 7. Pending shift flow — approve moves to assignedShifts
    @Test
    public void testPendingFlow_approved_movesToAssigned() throws Exception {
        EmployeeService empService = EmployeeService.getInstance();
        Shift shift = new Shift(LocalDate.now().plusDays(1), ShiftType.MORNING,
                empService.getCompanyRolesPool(), branch);
        employee.addPendingShift(shift, new Role("Cashier"));

        assertTrue(employee.getPendingShifts().contains(shift));
        assertEquals(new Role("Cashier"), employee.getPendingRole(shift));

        employee.respondToShift(shift, ApprovalStatus.APPROVED);

        assertFalse(employee.getPendingShifts().contains(shift));
        assertTrue(employee.getAssignedShifts().contains(shift));
        assertNull(employee.getPendingRole(shift));
    }

    // 8. Pending shift flow — reject keeps employee unassigned
    @Test
    public void testPendingFlow_rejected_notAssigned() throws Exception {
        EmployeeService empService = EmployeeService.getInstance();
        Shift shift = new Shift(LocalDate.now().plusDays(1), ShiftType.MORNING,
                empService.getCompanyRolesPool(), branch);
        employee.addPendingShift(shift, new Role("Cashier"));
        employee.respondToShift(shift, ApprovalStatus.REJECTED);

        assertFalse(employee.getAssignedShifts().contains(shift));
        assertTrue(employee.getPendingShifts().isEmpty());
    }
}