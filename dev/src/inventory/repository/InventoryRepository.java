package inventory.repository;

import inventory.domain.*;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryRepository {

    private void setPrivateField(Object obj, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPrivateIntField(Object obj, String fieldName, int value) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setInt(obj, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =====================================================================
    // Category
    // =====================================================================

    public void saveCategory(Category category) {
        String sql = "INSERT OR IGNORE INTO categories (name, parent_name) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getParent() != null ? category.getParent().getName() : null);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Category findCategoryByName(String name) {
        if (name == null) return null;
        String sql = "SELECT * FROM categories WHERE name = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String parentName = rs.getString("parent_name");
                Category parent = (parentName != null) ? findCategoryByName(parentName) : null;
                return new Category(rs.getString("name"), parent);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Category> findAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT name FROM categories";
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                categories.add(findCategoryByName(rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    // =====================================================================
    // Item
    // =====================================================================

    public void saveItem(Item item) {
        String sql = "INSERT OR REPLACE INTO items (id, name, cost_price, selling_price, min_quantity, manufacturer, category_name) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, item.getId());
            pstmt.setString(2, item.getName());
            pstmt.setDouble(3, item.getCostPrice());
            pstmt.setDouble(4, item.getSellingPrice());
            pstmt.setInt(5, item.getMinQuantity());
            pstmt.setString(6, item.getManufacturer());
            pstmt.setString(7, item.getCategory() != null ? item.getCategory().getName() : null);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateItem(Item item) {
        saveItem(item); // INSERT OR REPLACE acts as upsert
    }

    public Item findById(int id) {
        String sql = "SELECT * FROM items WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Category category = findCategoryByName(rs.getString("category_name"));
                Item item = new Item(rs.getString("name"), rs.getDouble("cost_price"), rs.getDouble("selling_price"), rs.getString("manufacturer"), category);
                item.setMinQuantity(rs.getInt("min_quantity"));
                setPrivateIntField(item, "id", rs.getInt("id"));
                return item;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Item> findAllItems() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT id FROM items";
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                items.add(findById(rs.getInt("id")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }


    public void saveInventoryItem(InventoryItem iItem) {
        String sql = "INSERT OR REPLACE INTO inventory_items (item_id, shelf_quantity, ware_quantity, shelf_location) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, iItem.getItem().getId());
            pstmt.setInt(2, iItem.getShelfQuantity());
            pstmt.setInt(3, iItem.getWareQuantity());
            if (iItem.getShelfLocation() != null) {
                pstmt.setString(4, iItem.getLocation());
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateInventoryItem(InventoryItem iItem) {
        saveInventoryItem(iItem);
    }

    public InventoryItem findInventoryItemByItemId(int itemId) {
        String sql = "SELECT * FROM inventory_items WHERE item_id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Item item = findById(itemId);
                InventoryItem iItem = new InventoryItem(item, rs.getInt("shelf_quantity"), rs.getInt("ware_quantity"));
                String locStr = rs.getString("shelf_location");
                if (locStr != null && locStr.startsWith("Aisle:")) {
                    try {
                        String[] parts = locStr.split(", ");
                        String aisle = parts[0].split(": ")[1];
                        int row = Integer.parseInt(parts[1].split(": ")[1]);
                        int shelf = Integer.parseInt(parts[2].split(": ")[1]);
                        iItem.setShelfLocation(new ShelfLocation(aisle, row, shelf));
                    } catch (Exception e) {
                        // Keep location null on parse failure
                    }
                }
                return iItem;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<InventoryItem> findAllInventoryItems() {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT item_id FROM inventory_items";
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                items.add(findInventoryItemByItemId(rs.getInt("item_id")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // =====================================================================
    // Discount
    // =====================================================================

    public void saveDiscount(Discount discount) {
        String sql = "INSERT INTO discounts (name, percentage, start_date, end_date, item_id, category_name) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, discount.getName());
            pstmt.setDouble(2, discount.getPercentage());
            pstmt.setString(3, discount.getStartDate().toString());
            pstmt.setString(4, discount.getEndDate().toString());
            
            if (discount.getItem() != null) {
                pstmt.setInt(5, discount.getItem().getId());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            
            if (discount.getCategory() != null) {
                pstmt.setString(6, discount.getCategory().getName());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
            }
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Discount> findAllDiscounts() {
        List<Discount> discounts = new ArrayList<>();
        String sql = "SELECT * FROM discounts";
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                LocalDate start;
                LocalDate end;
                try {
                    start = LocalDate.parse(rs.getString("start_date"));
                } catch (Exception e) {
                    start = Instant.ofEpochSecond(rs.getLong("start_date")).atZone(ZoneId.systemDefault()).toLocalDate();
                }
                try {
                    end = LocalDate.parse(rs.getString("end_date"));
                } catch (Exception e) {
                    end = Instant.ofEpochSecond(rs.getLong("end_date")).atZone(ZoneId.systemDefault()).toLocalDate();
                }
                
                Item item = null;
                if (rs.getObject("item_id") != null) {
                    item = findById(rs.getInt("item_id"));
                }
                
                Category category = null;
                if (rs.getString("category_name") != null) {
                    category = findCategoryByName(rs.getString("category_name"));
                }
                
                Discount d = new Discount(rs.getString("name"), rs.getDouble("percentage"), start, end, item, category);
                setPrivateIntField(d, "id", rs.getInt("id"));
                discounts.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return discounts;
    }

    // =====================================================================
    // DamageReport
    // =====================================================================

    public void saveDamageReport(DamageReport report) {
        String sql = "INSERT INTO damage_reports (item_id, quantity, reason, report_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, report.getItemId());
            pstmt.setInt(2, report.getQuantity());
            pstmt.setString(3, report.getReason().name());
            pstmt.setString(4, report.getReportDate().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<DamageReport> findAllDamageReports() {
        List<DamageReport> reports = new ArrayList<>();
        String sql = "SELECT * FROM damage_reports";
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Item item = findById(rs.getInt("item_id"));
                DamageReason reason = DamageReason.valueOf(rs.getString("reason"));
                DamageReport d = new DamageReport(item, rs.getInt("quantity"), reason);
                setPrivateIntField(d, "id", rs.getInt("id"));
                setPrivateField(d, "reportDate", LocalDate.parse(rs.getString("report_date")));
                reports.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }

    public void saveLowStockAlert(LowStockAlert alert) {
        String sql = "INSERT OR REPLACE INTO low_stock_alerts (alert_id, item_id, quantity_deficit, alert_date, processed) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, alert.getAlertId());
            pstmt.setInt(2, alert.getInventoryItem().getItem().getId());
            pstmt.setInt(3, alert.getQuantityDeficit());
            pstmt.setString(4, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(alert.getAlertDate()));
            pstmt.setInt(5, alert.isProcessed() ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<LowStockAlert> findAllLowStockAlerts() {
        List<LowStockAlert> alerts = new ArrayList<>();
        String sql = "SELECT * FROM low_stock_alerts";
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                InventoryItem iItem = findInventoryItemByItemId(rs.getInt("item_id"));
                LowStockAlert alert = new LowStockAlert(iItem, rs.getInt("quantity_deficit"));
                setPrivateIntField(alert, "alertId", rs.getInt("alert_id"));
                String dateStr = rs.getString("alert_date");
                java.util.Date parsedDate = new java.util.Date();
                try {
                    parsedDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr);
                } catch (Exception e) {
                    try {
                        parsedDate = new java.util.Date(Long.parseLong(dateStr));
                    } catch (Exception ex) {
                        // fallback to current date if parsing fails
                    }
                }
                setPrivateField(alert, "alertDate", parsedDate);
                setPrivateField(alert, "processed", rs.getInt("processed") == 1);
                alerts.add(alert);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alerts;
    }

    public List<LowStockAlert> findUnprocessedAlerts() {
        List<LowStockAlert> result = new ArrayList<>();
        List<LowStockAlert> all = findAllLowStockAlerts();
        for (LowStockAlert alert : all) {
            if (!alert.isProcessed()) result.add(alert);
        }
        return result;
    }

    public boolean hasActiveAlertForItem(int itemId) {
        String sql = "SELECT COUNT(*) FROM low_stock_alerts WHERE item_id = ? AND processed = 0";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateAlertProcessed(int alertId) {
        String sql = "UPDATE low_stock_alerts SET processed = 1 WHERE alert_id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, alertId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =====================================================================
    // Order
    // =====================================================================

    public void saveOrder(Order order) {
        String sqlOrder = "INSERT OR REPLACE INTO orders (id, supplier_name, total_price, order_date, status, order_type) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlItems = "INSERT OR REPLACE INTO order_items (order_id, item_id, quantity, unit_price, total_line_price) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sqlOrder)) {
                pstmt.setInt(1, order.getId());
                pstmt.setString(2, order.getSupplierName());
                pstmt.setDouble(3, order.getTotalPrice());
                pstmt.setString(4, java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                pstmt.setString(5, order.getStatus().name());
                pstmt.setString(6, order.getOrderType().name());
                pstmt.executeUpdate();
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sqlItems)) {
                for (OrderItem oi : order.getOrderItems()) {
                    pstmt.setInt(1, order.getId());
                    pstmt.setInt(2, oi.getItemId());
                    pstmt.setInt(3, oi.getQuantity());
                    pstmt.setDouble(4, oi.getUnitPrice());
                    pstmt.setDouble(5, oi.getTotalLinePrice());
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Order> findAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sqlOrder = "SELECT * FROM orders";
        String sqlItems = "SELECT * FROM order_items WHERE order_id = ?";
        int maxId = 0;

        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rsOrder = stmt.executeQuery(sqlOrder);
            while (rsOrder.next()) {
                String orderTypeStr = rsOrder.getString("order_type");
                OrderType orderType;
                try {
                    orderType = OrderType.valueOf(orderTypeStr);
                } catch (IllegalArgumentException | NullPointerException e) {
                    orderType = OrderType.MANUAL;
                }
                
                Order order = new Order(rsOrder.getString("supplier_name"), orderType);
                int currentId = rsOrder.getInt("id");
                setPrivateIntField(order, "id", currentId);
                if (currentId > maxId) {
                    maxId = currentId;
                }
                order.setStatus(OrderStatus.valueOf(rsOrder.getString("status")));
                
                try (PreparedStatement pstmt = conn.prepareStatement(sqlItems)) {
                    pstmt.setInt(1, order.getId());
                    ResultSet rsItems = pstmt.executeQuery();
                    while (rsItems.next()) {
                        order.addItem(rsItems.getInt("item_id"), rsItems.getInt("quantity"), rsItems.getDouble("unit_price"));
                    }
                }
                orders.add(order);
            }
            if (maxId > 0) {
                Order.updateIdCounter(maxId);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load orders: " + e.getMessage());
        }
        return orders;
    }

    public void updateOrderStatus(int orderId, OrderStatus status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
