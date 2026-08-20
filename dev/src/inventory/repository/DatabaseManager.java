package inventory.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.sqlite.SQLiteConfig;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:dev/inventory.db";

    public static Connection getConnection() throws SQLException {
        SQLiteConfig config = new SQLiteConfig();
        config.setBusyTimeout(15000);
        config.setJournalMode(SQLiteConfig.JournalMode.WAL);
        return DriverManager.getConnection(URL, config.toProperties());
    }

    static {
        java.io.File devDir = new java.io.File("dev");
        if (!devDir.exists()) {
            devDir.mkdirs();
        }
        initializeDatabase();
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // Create Categories table
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (" +
                    "name TEXT PRIMARY KEY, " +
                    "parent_name TEXT " +
                    ")");

            // Create Items table
            stmt.execute("CREATE TABLE IF NOT EXISTS items (" +
                    "id INTEGER PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "cost_price REAL NOT NULL, " +
                    "selling_price REAL NOT NULL, " +
                    "min_quantity INTEGER NOT NULL, " +
                    "manufacturer TEXT, " +
                    "category_name TEXT, " +
                    "FOREIGN KEY(category_name) REFERENCES categories(name)" +
                    ")");

            // Create InventoryItems table
            stmt.execute("CREATE TABLE IF NOT EXISTS inventory_items (" +
                    "item_id INTEGER PRIMARY KEY, " +
                    "shelf_quantity INTEGER NOT NULL, " +
                    "ware_quantity INTEGER NOT NULL, " +
                    "shelf_location TEXT, " +
                    "FOREIGN KEY(item_id) REFERENCES items(id)" +
                    ")");

            try {
                stmt.execute("ALTER TABLE inventory_items ADD COLUMN shelf_location TEXT");
            } catch (SQLException e) {
                // Ignore if column already exists
            }

            // Create Discounts table
            stmt.execute("CREATE TABLE IF NOT EXISTS discounts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "percentage REAL NOT NULL, " +
                    "start_date TEXT NOT NULL, " +
                    "end_date TEXT NOT NULL, " +
                    "item_id INTEGER, " +
                    "category_name TEXT, " +
                    "FOREIGN KEY(item_id) REFERENCES items(id), " +
                    "FOREIGN KEY(category_name) REFERENCES categories(name)" +
                    ")");

            // Create DamageReports table
            stmt.execute("CREATE TABLE IF NOT EXISTS damage_reports (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "item_id INTEGER NOT NULL, " +
                    "quantity INTEGER NOT NULL, " +
                    "reason TEXT NOT NULL, " +
                    "report_date TEXT NOT NULL, " +
                    "FOREIGN KEY(item_id) REFERENCES items(id)" +
                    ")");

            // Create LowStockAlerts table
            stmt.execute("CREATE TABLE IF NOT EXISTS low_stock_alerts (" +
                    "alert_id INTEGER PRIMARY KEY, " +
                    "item_id INTEGER NOT NULL, " +
                    "quantity_deficit INTEGER NOT NULL, " +
                    "alert_date TEXT NOT NULL, " +
                    "processed INTEGER NOT NULL, " +
                    "FOREIGN KEY(item_id) REFERENCES items(id)" +
                    ")");

            // Create Orders table
            stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                    "id INTEGER PRIMARY KEY, " +
                    "supplier_name TEXT NOT NULL, " +
                    "total_price REAL NOT NULL, " +
                    "order_date TEXT NOT NULL, " +
                    "status TEXT NOT NULL, " +
                    "order_type TEXT NOT NULL DEFAULT 'MANUAL'" +
                    ")");

            // Create OrderItems table
            stmt.execute("CREATE TABLE IF NOT EXISTS order_items (" +
                    "order_id INTEGER NOT NULL, " +
                    "item_id INTEGER NOT NULL, " +
                    "quantity INTEGER NOT NULL, " +
                    "unit_price REAL NOT NULL, " +
                    "total_line_price REAL NOT NULL, " +
                    "PRIMARY KEY (order_id, item_id), " +
                    "FOREIGN KEY(order_id) REFERENCES orders(id), " +
                    "FOREIGN KEY(item_id) REFERENCES items(id)" +
                    ")");

            // Migration: add order_type column to existing orders table
            try {
                stmt.execute("ALTER TABLE orders ADD COLUMN order_type TEXT NOT NULL DEFAULT 'MANUAL'");
            } catch (SQLException ignored) {
                // Column already exists
            }


        } catch (SQLException e) {
            System.out.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
