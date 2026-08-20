package employees.service;

import employees.domain.*;
import employees.repository.BranchRepository;
import employees.repository.EmployeeRepository;
import java.sql.SQLException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;


public class EmployeeService {

    private static EmployeeService instance = null;

    private final BranchRepository branchRepo;
    private final EmployeeRepository employeeRepo;

    private List<Employee> allEmployees;
    private List<Role> companyRolesPool;
    private List<Branch> allBranches;

    private DayOfWeek deadlineDay = DayOfWeek.WEDNESDAY;
    private int deadlineHour = 23;

    private EmployeeService() {
        try {
            this.branchRepo = new BranchRepository();
            this.employeeRepo = new EmployeeRepository();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database: " + e.getMessage());
        }
        this.companyRolesPool = new ArrayList<>();
        this.deadlineDay = DayOfWeek.WEDNESDAY;
        this.deadlineHour = 23;
        this.allBranches = new ArrayList<>();
        this.allEmployees = new ArrayList<>();
        loadFromDatabase();
    }

    private void loadFromDatabase() {
        try {
            // Load roles pool from database
            List<String> roleNames = employeeRepo.findAllRolesInPool();
            if (roleNames.isEmpty()) {
                // First time — load defaults and save to DB
                loadDefaultRoles();
            } else {
                // Load from DB
                for (String roleName : roleNames) {
                    companyRolesPool.add(new Role(roleName));
                }
            }

            this.allBranches = branchRepo.findAll();
            this.allEmployees = employeeRepo.findAll(branchRepo);
            for (Employee emp : allEmployees) {
                emp.getBranch().addEmployee(emp);
            }
        } catch (SQLException e) {
            System.out.println("Warning: Could not load data from database: " + e.getMessage());
            loadDefaultRoles();
            this.allBranches = new ArrayList<>();
            this.allEmployees = new ArrayList<>();
        }
    }

    // Load default roles and save them to the database
    private void loadDefaultRoles() {
        String[] defaults = {"Cashier", "Storekeeper", "Shift Manager", "General Worker", "Driver"};
        for (String name : defaults) {
            Role role = new Role(name);
            companyRolesPool.add(role);
            try {
                employeeRepo.insertRoleToPool(name);
            } catch (SQLException e) {
                System.out.println("Warning: Could not save role to database: " + e.getMessage());
            }
        }
    }

    public static EmployeeService getInstance() {
        if (instance == null) {
            instance = new EmployeeService();
        }
        return instance;
    }

    public Employee getEmployeeById(String id) {
        for (Employee emp : allEmployees) {
            if (emp.getId().equals(id)) return emp;
        }
        return null;
    }

    public List<Employee> getAllEmployees() {
        return allEmployees;
    }

    public List<Employee> getActiveEmployees() {
        List<Employee> activeEmployees = new ArrayList<>();
        for (Employee emp : allEmployees) {
            if (emp.isActive()) {
                activeEmployees.add(emp);
            }
        }
        return activeEmployees;
    }

    public void addEmployee(String id, String firstName, String lastName, String bankAccount,
                            double wage, JobScope jobScope, SalaryType salaryType,
                            DayOfWeek fixedDayOff, LocalDate startDate, String branchId) throws Exception {
        if (getEmployeeById(id) != null) {
            throw new Exception("Error: Employee with ID " + id + " already exists.");
        }
        Branch branch = getBranchById(branchId);
        if (branch == null) {
            throw new Exception("Error: Branch '" + branchId + "' not found.");
        }
        Employee emp = new Employee(id, firstName, lastName, bankAccount,
                wage, jobScope, salaryType, fixedDayOff, startDate, branch);
        branch.addEmployee(emp);
        allEmployees.add(emp);
        employeeRepo.insert(emp);
    }

    public void updateEmployeeDetails(String id, String firstName, String lastName,
                                      String bankAccount, double wage, JobScope jobScope,
                                      SalaryType salaryType, DayOfWeek fixedDayOff) throws Exception {
        Employee emp = getEmployeeById(id);
        if (emp == null) {
            throw new Exception("Error: Employee with ID " + id + " not found.");
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new Exception("Error: First name cannot be empty.");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new Exception("Error: Last name cannot be empty.");
        }
        if (bankAccount == null || bankAccount.trim().isEmpty()) {
            throw new Exception("Error: Bank account cannot be empty.");
        }
        if (wage < 0) {
            throw new Exception("Error: Wage cannot be a negative number.");
        }
        emp.setFirstName(firstName.trim());
        emp.setLastName(lastName.trim());
        emp.setBankAccount(bankAccount);
        emp.setWage(wage);
        emp.setJobScope(jobScope);
        emp.setSalaryType(salaryType);
        emp.setFixedDayOff(fixedDayOff);
        employeeRepo.update(emp);
    }

    public void updateEmployeeWage(String id, double newWage) throws Exception {
        Employee emp = getEmployeeById(id);
        if (emp == null) {
            throw new Exception("Error: Employee with ID " + id + " not found.");
        }
        if (newWage < 0) {
            throw new Exception("Error: Wage cannot be a negative number.");
        }
        emp.setWage(newWage);
        employeeRepo.update(emp);
    }

    public void deactivateEmployee(String id) throws Exception {
        Employee emp = getEmployeeById(id);
        if (emp == null) {
            throw new Exception("Error: Employee with ID " + id + " not found.");
        }
        emp.setActive(false);
        employeeRepo.update(emp);
    }

    public List<Role> getCompanyRolesPool() {
        return companyRolesPool;
    }

    public void addRoleToPool(String roleName) throws Exception {
        Role newRole = new Role(roleName);
        if (companyRolesPool.contains(newRole)) {
            throw new Exception("Error: Role '" + roleName + "' already exists.");
        }
        companyRolesPool.add(newRole);
        try {
            employeeRepo.insertRoleToPool(roleName);
        } catch (SQLException e) {
            throw new Exception("Error saving role to database: " + e.getMessage());
        }
    }

    public void addRoleToEmployee(String employeeId, String roleName) throws Exception {
        Employee emp = getEmployeeById(employeeId);
        if (emp == null) {
            throw new Exception("Error: Employee with ID " + employeeId + " not found.");
        }
        Role role = getRoleFromPool(roleName);
        emp.addRole(role);
        employeeRepo.insertRole(employeeId, role.getRoleName());
    }

    public void removeRoleFromEmployee(String employeeId, String roleName) throws Exception {
        Employee emp = getEmployeeById(employeeId);
        if (emp == null) {
            throw new Exception("Error: Employee with ID " + employeeId + " not found.");
        }
        Role role = getRoleFromPool(roleName);
        emp.removeRole(role);
        employeeRepo.deleteRole(employeeId, role.getRoleName());
    }

    public void removeRoleFromPool(String roleName) throws Exception {
        Role role = getRoleFromPool(roleName);
        companyRolesPool.remove(role);
        try {
            employeeRepo.deleteRoleFromPool(roleName);
        } catch (SQLException e) {
            throw new Exception("Error removing role from database: " + e.getMessage());
        }
    }

    public void addEmployeeAvailability(String employeeId, DayOfWeek day, ShiftType type) throws Exception {
        validateSubmissionWindow();
        Employee emp = getEmployeeById(employeeId);
        if (emp == null) {
            throw new Exception("Error: Employee with ID " + employeeId + " not found.");
        }
        emp.addAvailability(day, type);
        AvailabilitySubmission sub = emp.getAvailabilities().get(emp.getAvailabilities().size() - 1);
        employeeRepo.insertAvailability(employeeId, day, type, sub.getSubmittedAt());
    }

    public void removeEmployeeAvailability(String employeeId, DayOfWeek day, ShiftType type) throws Exception {
        validateSubmissionWindow();
        Employee emp = getEmployeeById(employeeId);
        if (emp == null) {
            throw new Exception("Error: Employee with ID " + employeeId + " not found.");
        }
        emp.removeAvailability(day, type);
        employeeRepo.deleteAvailability(employeeId, day, type);
    }

    public void clearEmployeeAvailabilities(String employeeId) throws Exception {
        validateSubmissionWindow();
        Employee emp = getEmployeeById(employeeId);
        if (emp == null) {
            throw new Exception("Error: Employee with ID " + employeeId + " not found.");
        }
        emp.clearAvailabilities();
        employeeRepo.deleteAllAvailabilities(employeeId);
    }

    private void validateSubmissionWindow() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek today = now.getDayOfWeek();
        int hour = now.getHour();
        int todayValue = today.getValue();
        int deadlineValue = deadlineDay.getValue();
        boolean isClosed = false;
        if (todayValue > deadlineValue) {
            isClosed = true;
        } else if (todayValue == deadlineValue && hour >= deadlineHour) {
            isClosed = true;
        }
        if (isClosed) {
            throw new Exception("Error: The weekly deadline (" +
                    deadlineDay + " " + deadlineHour + ":00) has passed. Submission is closed.");
        }
    }

    public void setSubmissionDeadline(DayOfWeek day, int hour) throws Exception {
        if (day == null)
            throw new Exception("Error: Deadline day cannot be null.");
        if (hour < 0 || hour > 23)
            throw new Exception("Error: Hour must be between 0 and 23.");
        this.deadlineDay = day;
        this.deadlineHour = hour;
    }

    public DayOfWeek getDeadlineDay() { return deadlineDay; }
    public int getDeadlineHour() { return deadlineHour; }

    public List<Employee> getAvailableAndQualifiedEmployees(DayOfWeek day, ShiftType shiftType,
                                                            Role role, Branch branch) {
        List<Employee> result = new ArrayList<>();
        for (Employee emp : allEmployees) {
            if (emp.isActive() && emp.isAvailable(day, shiftType) &&
                    emp.isQualifiedFor(role) && emp.getBranch().equals(branch)) {
                result.add(emp);
            }
        }
        return result;
    }

    public Role getRoleFromPool(String roleName) throws Exception {
        for (Role r : companyRolesPool) {
            if (r.getRoleName().equalsIgnoreCase(roleName)) {
                return r;
            }
        }
        throw new Exception("Error: Role '" + roleName + "' does not exist.");
    }

    public List<Branch> getAllBranches() {
        return allBranches;
    }

    public Branch getBranchById(String branchId) {
        for (Branch b : allBranches) {
            if (b.getBranchId().equals(branchId)) {
                return b;
            }
        }
        return null;
    }

    public void addBranch(String branchId, String name, String address) throws Exception {
        if (getBranchById(branchId) != null) {
            throw new Exception("Error: Branch with ID " + branchId + " already exists.");
        }
        Branch branch = new Branch(branchId, name, address);
        allBranches.add(branch);
        branchRepo.insert(branch);
    }

    public void assignEmployeeToBranch(String employeeId, String branchId) throws Exception {
        Employee emp = getEmployeeById(employeeId);
        if (emp == null) {
            throw new Exception("Error: Employee not found.");
        }
        Branch branch = getBranchById(branchId);
        if (branch == null) {
            throw new Exception("Error: Branch not found.");
        }
        Branch oldBranch = emp.getBranch();
        if (oldBranch != null) {
            oldBranch.removeEmployee(emp);
        }
        emp.setBranch(branch);
        branch.addEmployee(emp);
        employeeRepo.update(emp);
    }

    public List<Employee> getEmployeesByBranch(String branchId) {
        Branch branch = getBranchById(branchId);
        if (branch == null) {
            return new ArrayList<>();
        }
        return branch.getEmployees();
    }

    public void setEmployeeLicense(String employeeId, TruckType licenseType) throws Exception {
        Employee emp = getEmployeeById(employeeId);
        if (emp == null) {
            throw new Exception("Error: Employee with ID " + employeeId + " not found.");
        }
        emp.setLicenseType(licenseType);
        employeeRepo.update(emp);
    }
}