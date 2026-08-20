package employees;

import employees.domain.*;
import employees.repository.DatabaseManager;
import employees.service.DeliveryService;
import employees.service.EmployeeService;
import employees.service.ShiftService;
import org.junit.Before;
import org.junit.Test;
import java.time.DayOfWeek;
import java.time.LocalDate;
import static org.junit.Assert.*;

public class IntegrationTest {

    private EmployeeService empService;
    private ShiftService shiftService;
    private DeliveryService deliveryService;
    private LocalDate tomorrow;
    private Branch branch;

    @Before
    public void setUp() throws Exception {
        DatabaseManager.resetForTesting();

        empService = EmployeeService.getInstance();
        shiftService = ShiftService.getInstance();
        deliveryService = DeliveryService.getInstance();
        tomorrow = LocalDate.now().plusDays(1);

        empService.addBranch("B001", "Test Branch", "Test Address");
        branch = empService.getBranchById("B001");

        empService.setSubmissionDeadline(DayOfWeek.SATURDAY, 23);
    }

    //Branch saved to DB and loaded back correctly
    @Test
    public void testBranch_savedAndLoadedFromDB() throws Exception {
        Branch loaded = empService.getBranchById("B001");
        assertNotNull(loaded);
        assertEquals("Test Branch", loaded.getName());
        assertEquals("Test Address", loaded.getAddress());
    }

    //Employee saved to DB with roles
    @Test
    public void testEmployee_savedWithRoles() throws Exception {
        empService.addEmployee("111111111", "Yossi", "Cohen", "12-345-67",
                50.0, JobScope.FULL_TIME, SalaryType.HOURLY, null, LocalDate.now(), "B001");
        empService.addRoleToEmployee("111111111", "Cashier");
        empService.addRoleToEmployee("111111111", "Shift Manager");

        Employee emp = empService.getEmployeeById("111111111");
        assertNotNull(emp);
        assertTrue(emp.isQualifiedFor(new Role("Cashier")));
        assertTrue(emp.isQualifiedFor(new Role("Shift Manager")));
    }

    //Employee saved to DB with availabilities
    @Test
    public void testEmployee_savedWithAvailabilities() throws Exception {
        empService.addEmployee("111111111", "Yossi", "Cohen", "12-345-67",
                50.0, JobScope.FULL_TIME, SalaryType.HOURLY, null, LocalDate.now(), "B001");
        empService.addEmployeeAvailability("111111111", DayOfWeek.SUNDAY, ShiftType.MORNING);
        empService.addEmployeeAvailability("111111111", DayOfWeek.MONDAY, ShiftType.EVENING);

        Employee emp = empService.getEmployeeById("111111111");
        assertTrue(emp.isAvailable(DayOfWeek.SUNDAY, ShiftType.MORNING));
        assertTrue(emp.isAvailable(DayOfWeek.MONDAY, ShiftType.EVENING));
        assertFalse(emp.isAvailable(DayOfWeek.TUESDAY, ShiftType.MORNING));
    }

    //Shift saved to DB and loaded back correctly
    @Test
    public void testShift_savedAndLoadedFromDB() throws Exception {
        shiftService.addShift(tomorrow, ShiftType.MORNING, branch);
        Shift shift = shiftService.getShift(tomorrow, ShiftType.MORNING);
        assertNotNull(shift);
        assertEquals(tomorrow, shift.getDate());
        assertEquals(ShiftType.MORNING, shift.getType());
        assertEquals(branch, shift.getBranch());
    }

    //Assignment saved to DB
    @Test
    public void testAssignment_savedToDB() throws Exception {
        empService.addEmployee("111111111", "Yossi", "Cohen", "12-345-67",
                50.0, JobScope.FULL_TIME, SalaryType.HOURLY, null, LocalDate.now(), "B001");
        empService.addRoleToEmployee("111111111", "Cashier");
        empService.addEmployeeAvailability("111111111", tomorrow.getDayOfWeek(), ShiftType.MORNING);

        shiftService.addShift(tomorrow, ShiftType.MORNING, branch);
        shiftService.assignEmployeeToShift(tomorrow, ShiftType.MORNING, "111111111", "Cashier");

        Shift shift = shiftService.getShift(tomorrow, ShiftType.MORNING);
        Employee emp = empService.getEmployeeById("111111111");
        assertTrue(shift.isEmployeeAssigned(emp));
    }

    //validateShift fails without Shift Manager
    @Test(expected = Exception.class)
    public void testValidateShift_noShiftManager_throwsException() throws Exception {
        shiftService.addShift(tomorrow, ShiftType.MORNING, branch);
        shiftService.validateShift(tomorrow, ShiftType.MORNING, branch);
    }

    //validateShift passes with Shift Manager
    @Test
    public void testValidateShift_withShiftManager_passes() throws Exception {
        empService.addEmployee("111111111", "Yossi", "Cohen", "12-345-67",
                50.0, JobScope.FULL_TIME, SalaryType.HOURLY, null, LocalDate.now(), "B001");
        empService.addRoleToEmployee("111111111", "Shift Manager");
        empService.addEmployeeAvailability("111111111", tomorrow.getDayOfWeek(), ShiftType.MORNING);

        shiftService.addShift(tomorrow, ShiftType.MORNING, branch);
        shiftService.assignEmployeeToShift(tomorrow, ShiftType.MORNING, "111111111", "Shift Manager");
        shiftService.validateShift(tomorrow, ShiftType.MORNING, branch);
    }

    //validateShift fails with delivery but no storekeeper
    @Test(expected = Exception.class)
    public void testValidateShift_deliveryNoStorekeeper_throwsException() throws Exception {
        empService.addEmployee("111111111", "Yossi", "Cohen", "12-345-67",
                50.0, JobScope.FULL_TIME, SalaryType.HOURLY, null, LocalDate.now(), "B001");
        empService.addRoleToEmployee("111111111", "Shift Manager");
        empService.addEmployeeAvailability("111111111", tomorrow.getDayOfWeek(), ShiftType.MORNING);

        shiftService.addShift(tomorrow, ShiftType.MORNING, branch);
        shiftService.assignEmployeeToShift(tomorrow, ShiftType.MORNING, "111111111", "Shift Manager");

        deliveryService.addDelivery("D001", tomorrow, ShiftType.MORNING, branch, TruckType.TYPE_B);

        // No storekeeper or driver assigned — should fail
        shiftService.validateShift(tomorrow, ShiftType.MORNING, branch);
    }

    //validateShift fails with delivery but no driver
    @Test(expected = Exception.class)
    public void testValidateShift_deliveryNoDriver_throwsException() throws Exception {
        empService.addEmployee("111111111", "Yossi", "Cohen", "12-345-67",
                50.0, JobScope.FULL_TIME, SalaryType.HOURLY, null, LocalDate.now(), "B001");
        empService.addRoleToEmployee("111111111", "Shift Manager");
        empService.addRoleToEmployee("111111111", "Storekeeper");
        empService.addEmployeeAvailability("111111111", tomorrow.getDayOfWeek(), ShiftType.MORNING);

        shiftService.addShift(tomorrow, ShiftType.MORNING, branch);
        shiftService.assignEmployeeToShift(tomorrow, ShiftType.MORNING, "111111111", "Shift Manager");
        shiftService.assignEmployeeToShift(tomorrow, ShiftType.MORNING, "111111111", "Storekeeper");

        deliveryService.addDelivery("D001", tomorrow, ShiftType.MORNING, branch, TruckType.TYPE_B);

        // No driver assigned — should fail
        shiftService.validateShift(tomorrow, ShiftType.MORNING, branch);
    }

    //Deactivate employee — removed from DB assignments
    @Test
    public void testDeactivate_removedFromDBAssignments() throws Exception {
        empService.addEmployee("111111111", "Yossi", "Cohen", "12-345-67",
                50.0, JobScope.FULL_TIME, SalaryType.HOURLY, null, LocalDate.now(), "B001");
        empService.addRoleToEmployee("111111111", "Cashier");
        empService.addEmployeeAvailability("111111111", tomorrow.getDayOfWeek(), ShiftType.MORNING);

        shiftService.addShift(tomorrow, ShiftType.MORNING, branch);
        shiftService.assignEmployeeToShift(tomorrow, ShiftType.MORNING, "111111111", "Cashier");

        empService.deactivateEmployee("111111111");
        shiftService.handleEmployeeDeactivation("111111111");

        Shift shift = shiftService.getShift(tomorrow, ShiftType.MORNING);
        Employee emp = empService.getEmployeeById("111111111");

        assertFalse(shift.isEmployeeAssigned(emp));
        assertFalse(emp.isActive());
    }

    //Role pool saved to DB
    @Test
    public void testRolePool_savedToDB() throws Exception {
        empService.addRoleToPool("TestRole");
        assertTrue(empService.getCompanyRolesPool().stream()
                .anyMatch(r -> r.getRoleName().equals("TestRole")));
    }

    //Duplicate branch ID throws exception
    @Test(expected = Exception.class)
    public void testAddBranch_duplicateId_throwsException() throws Exception {
        empService.addBranch("B001", "Another Branch", "Another Address");
    }

    //assignDriver succeeds when driver is assigned to shift
    @Test
    public void testAssignDriver_driverInShift_succeeds() throws Exception {
        empService.addEmployee("111111111", "Yossi", "Cohen", "12-345-67",
                50.0, JobScope.FULL_TIME, SalaryType.HOURLY, null, LocalDate.now(), "B001");
        empService.addRoleToEmployee("111111111", "Driver");
        empService.getEmployeeById("111111111").setLicenseType(TruckType.TYPE_B);
        empService.addEmployeeAvailability("111111111", tomorrow.getDayOfWeek(), ShiftType.MORNING);

        shiftService.addShift(tomorrow, ShiftType.MORNING, branch);

        deliveryService.addDelivery("D001", tomorrow, ShiftType.MORNING, branch, TruckType.TYPE_B);

        shiftService.assignEmployeeToShift(tomorrow, ShiftType.MORNING, "111111111", "Driver");

        deliveryService.assignDriver("D001", "111111111");

        Delivery delivery = deliveryService.getDeliveryById("D001");
        assertNotNull(delivery.getDriver());
        assertEquals("111111111", delivery.getDriver().getId());
    }

    //assignDriver fails when driver is not assigned to shift
    @Test(expected = Exception.class)
    public void testAssignDriver_driverNotInShift_throwsException() throws Exception {
        empService.addEmployee("111111111", "Yossi", "Cohen", "12-345-67",
                50.0, JobScope.FULL_TIME, SalaryType.HOURLY, null, LocalDate.now(), "B001");
        empService.addRoleToEmployee("111111111", "Driver");
        empService.getEmployeeById("111111111").setLicenseType(TruckType.TYPE_B);

        shiftService.addShift(tomorrow, ShiftType.MORNING, branch);
        deliveryService.addDelivery("D001", tomorrow, ShiftType.MORNING, branch, TruckType.TYPE_B);

        // Driver not assigned to shift — should fail
        deliveryService.assignDriver("D001", "111111111");
    }

    // assignDriver fails when license type does not match truck type
    @Test(expected = Exception.class)
    public void testAssignDriver_wrongLicense_throwsException() throws Exception {
        empService.addEmployee("111111111", "Yossi", "Cohen", "12-345-67",
                50.0, JobScope.FULL_TIME, SalaryType.HOURLY, null, LocalDate.now(), "B001");
        empService.addRoleToEmployee("111111111", "Driver");
        // TYPE_B license but TYPE_C truck
        empService.getEmployeeById("111111111").setLicenseType(TruckType.TYPE_B);
        empService.addEmployeeAvailability("111111111", tomorrow.getDayOfWeek(), ShiftType.MORNING);

        shiftService.addShift(tomorrow, ShiftType.MORNING, branch);
        shiftService.assignEmployeeToShift(tomorrow, ShiftType.MORNING, "111111111", "Driver");

        deliveryService.addDelivery("D001", tomorrow, ShiftType.MORNING, branch, TruckType.TYPE_C);

        // Wrong license — should fail
        deliveryService.assignDriver("D001", "111111111");
    }
}