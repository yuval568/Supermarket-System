package employees;

import employees.domain.*;
import employees.repository.DatabaseManager;
import employees.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import java.time.LocalDate;
import static org.junit.Assert.*;

public class ShiftTest {

    private Shift shift;
    private Employee manager;
    private Employee cashier;
    private Branch branch;

    @Before
    public void setUp() throws Exception {
        DatabaseManager.resetForTesting();
        EmployeeService empService = EmployeeService.getInstance();
        empService.addBranch("B001", "Test Branch", "Test Address");
        branch = empService.getBranchById("B001");

        shift = new Shift(LocalDate.now().plusDays(1), ShiftType.MORNING,
                empService.getCompanyRolesPool(), branch);

        manager = new Employee("111111111", "Yossi", "Cohen", "12-345-67",
                50.0, JobScope.FULL_TIME, SalaryType.HOURLY, null, LocalDate.now(), branch);
        cashier = new Employee("222222222", "Dana", "Levi", "23-456-78",
                55.0, JobScope.FULL_TIME, SalaryType.GLOBAL, null, LocalDate.now(), branch);

        manager.addRole(new Role("Shift Manager"));
        manager.addRole(new Role("Cashier"));
        cashier.addRole(new Role("Cashier"));
        cashier.addRole(new Role("Storekeeper"));
    }

    // 9. Shift Manager + regular role allowed for same employee
    @Test
    public void testAssign_shiftManagerPlusRegularRole_allowed() throws Exception {
        shift.assignEmployee(new Role("Shift Manager"), manager);
        shift.assignEmployee(new Role("Cashier"), manager);

        assertTrue(shift.isEmployeeAssigned(manager));
        assertEquals(1, shift.getAllAssignedEmployees().size());
        assertTrue(shift.hasShiftManager());
    }

    // 10. Two regular roles for same employee — blocked
    @Test(expected = Exception.class)
    public void testAssign_twoRegularRoles_throwsException() throws Exception {
        shift.assignEmployee(new Role("Cashier"), cashier);
        shift.assignEmployee(new Role("Storekeeper"), cashier);
    }

    // 11. Role capacity enforced
    @Test(expected = Exception.class)
    public void testAssign_roleAtCapacity_throwsException() throws Exception {
        Employee emp1 = new Employee("333333333", "A", "B", "11-111-11",
                40.0, JobScope.PART_TIME, SalaryType.HOURLY, null, LocalDate.now(), branch);
        Employee emp2 = new Employee("444444444", "C", "D", "22-222-22",
                40.0, JobScope.PART_TIME, SalaryType.HOURLY, null, LocalDate.now(), branch);
        emp1.addRole(new Role("Cashier"));
        emp2.addRole(new Role("Cashier"));

        shift.assignEmployee(new Role("Cashier"), emp1);
        shift.assignEmployee(new Role("Cashier"), emp2);

        // Now full — should be blocked
        shift.assignEmployee(new Role("Cashier"), cashier);
    }

    // 12. hasShiftManager — false before assignment
    @Test
    public void testHasShiftManager_falseBeforeAssignment() {
        assertFalse(shift.hasShiftManager());
    }

    // 12b. hasShiftManager — true after assignment
    @Test
    public void testHasShiftManager_trueAfterAssignment() throws Exception {
        shift.assignEmployee(new Role("Shift Manager"), manager);
        assertTrue(shift.hasShiftManager());
    }

    // 13. removeEmployeeFromAllRoles cleans up completely
    @Test
    public void testRemoveFromAllRoles_cleansUpCompletely() throws Exception {
        shift.assignEmployee(new Role("Shift Manager"), manager);
        shift.assignEmployee(new Role("Cashier"), manager);
        shift.removeEmployeeFromAllRoles(manager);

        assertFalse(shift.isEmployeeAssigned(manager));
        assertFalse(shift.hasShiftManager());
    }
}