package employees.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:employees.db";
    private static final String TEST_DB_URL = "jdbc:sqlite::memory:";

    private static DatabaseManager instance = null;
    private static boolean testMode = false;

    private Connection connection;

    private DatabaseManager() throws SQLException {
        String url = testMode ? TEST_DB_URL : DB_URL;
        this.connection = DriverManager.getConnection(url);
        connection.createStatement().execute("PRAGMA foreign_keys = ON");
        createTables();
    }

    public static DatabaseManager getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // Call this before each test to get a fresh in-memory database
    public static void resetForTesting() throws SQLException {
        testMode = true;
        if (instance != null) {
            instance.close();
            instance = null;
        }
        // Reset all service singletons
        resetServiceSingletons();
    }

    // Resets all service singletons so they reconnect to the new DB
    private static void resetServiceSingletons() {
        try {
            java.lang.reflect.Field empField =
                    employees.service.EmployeeService.class.getDeclaredField("instance");
            empField.setAccessible(true);
            empField.set(null, null);

            java.lang.reflect.Field shiftField =
                    employees.service.ShiftService.class.getDeclaredField("instance");
            shiftField.setAccessible(true);
            shiftField.set(null, null);

            java.lang.reflect.Field deliveryField =
                    employees.service.DeliveryService.class.getDeclaredField("instance");
            deliveryField.setAccessible(true);
            deliveryField.set(null, null);
        } catch (Exception e) {
            System.out.println("Warning: Could not reset service singletons: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    private void createTables() throws SQLException {
        Statement stmt = connection.createStatement();

        stmt.execute("""
                CREATE TABLE IF NOT EXISTS branches (
                    branch_id   TEXT PRIMARY KEY,
                    name        TEXT NOT NULL,
                    address     TEXT
                )
                """);

        stmt.execute("""
                CREATE TABLE IF NOT EXISTS employees (
                    id                TEXT PRIMARY KEY,
                    first_name        TEXT NOT NULL,
                    last_name         TEXT NOT NULL,
                    bank_account      TEXT NOT NULL,
                    wage              REAL NOT NULL,
                    job_scope         TEXT NOT NULL,
                    salary_type       TEXT NOT NULL,
                    fixed_day_off     TEXT,
                    start_date        TEXT NOT NULL,
                    branch_id         TEXT NOT NULL,
                    license_type      TEXT,
                    is_active         INTEGER NOT NULL DEFAULT 1,
                    annual_leave_days INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (branch_id) REFERENCES branches(branch_id)
                )
                """);

        stmt.execute("""
                CREATE TABLE IF NOT EXISTS roles_pool (
                    role_name TEXT PRIMARY KEY
                )
                """);

        stmt.execute("""
                CREATE TABLE IF NOT EXISTS employee_roles (
                    employee_id TEXT NOT NULL,
                    role_name   TEXT NOT NULL,
                    PRIMARY KEY (employee_id, role_name),
                    FOREIGN KEY (employee_id) REFERENCES employees(id)
                )
                """);

        stmt.execute("""
                CREATE TABLE IF NOT EXISTS availabilities (
                    employee_id  TEXT NOT NULL,
                    day          TEXT NOT NULL,
                    shift_type   TEXT NOT NULL,
                    submitted_at TEXT NOT NULL,
                    PRIMARY KEY (employee_id, day, shift_type),
                    FOREIGN KEY (employee_id) REFERENCES employees(id)
                )
                """);

        stmt.execute("""
                CREATE TABLE IF NOT EXISTS shifts (
                    date       TEXT NOT NULL,
                    type       TEXT NOT NULL,
                    branch_id  TEXT NOT NULL,
                    start_time TEXT NOT NULL,
                    end_time   TEXT NOT NULL,
                    PRIMARY KEY (date, type, branch_id),
                    FOREIGN KEY (branch_id) REFERENCES branches(branch_id)
                )
                """);

        stmt.execute("""
                CREATE TABLE IF NOT EXISTS shift_required_roles (
                    date      TEXT NOT NULL,
                    type      TEXT NOT NULL,
                    branch_id TEXT NOT NULL,
                    role_name TEXT NOT NULL,
                    count     INTEGER NOT NULL,
                    PRIMARY KEY (date, type, branch_id, role_name),
                    FOREIGN KEY (date, type, branch_id) REFERENCES shifts(date, type, branch_id)
                )
                """);

        stmt.execute("""
                CREATE TABLE IF NOT EXISTS shift_assignments (
                    date        TEXT NOT NULL,
                    type        TEXT NOT NULL,
                    branch_id   TEXT NOT NULL,
                    employee_id TEXT NOT NULL,
                    role_name   TEXT NOT NULL,
                    PRIMARY KEY (date, type, branch_id, employee_id, role_name),
                    FOREIGN KEY (date, type, branch_id) REFERENCES shifts(date, type, branch_id),
                    FOREIGN KEY (employee_id)           REFERENCES employees(id)
                )
                """);

        stmt.execute("""
                CREATE TABLE IF NOT EXISTS pending_shifts (
                    employee_id TEXT NOT NULL,
                    date        TEXT NOT NULL,
                    type        TEXT NOT NULL,
                    branch_id   TEXT NOT NULL,
                    role_name   TEXT NOT NULL,
                    status      TEXT NOT NULL DEFAULT 'PENDING',
                    PRIMARY KEY (employee_id, date, type, branch_id),
                    FOREIGN KEY (employee_id)           REFERENCES employees(id),
                    FOREIGN KEY (date, type, branch_id) REFERENCES shifts(date, type, branch_id)
                )
                """);

        stmt.execute("""
                CREATE TABLE IF NOT EXISTS deliveries (
                    delivery_id TEXT PRIMARY KEY,
                    date        TEXT NOT NULL,
                    shift_type  TEXT NOT NULL,
                    branch_id   TEXT NOT NULL,
                    truck_type  TEXT NOT NULL,
                    driver_id   TEXT,
                    FOREIGN KEY (branch_id) REFERENCES branches(branch_id),
                    FOREIGN KEY (driver_id) REFERENCES employees(id)
                )
                """);

        stmt.close();
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}