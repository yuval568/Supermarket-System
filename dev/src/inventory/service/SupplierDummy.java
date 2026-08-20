package inventory.service;

import inventory.domain.Order;
import inventory.domain.OrderItem;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SupplierDummy {

    private static final List<String> SUPPLIERS = Arrays.asList(
            "HADADI", "LESHEM", "TAKACH", "BARGILOVSKI"
    );


    private static final Map<String, List<DayOfWeek>> SUPPLIER_DELIVERY_DAYS = new HashMap<>();
    static {

        SUPPLIER_DELIVERY_DAYS.put("HADADI", Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY));
        SUPPLIER_DELIVERY_DAYS.put("LESHEM", Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));
        SUPPLIER_DELIVERY_DAYS.put("TAKACH", Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));
        SUPPLIER_DELIVERY_DAYS.put("BARGILOVSKI", Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY));
    }

    private static final Random random = new Random();

    public static void loadData() {
        System.out.println(">>> [SupplierDummy] Loading all supplier data...");
    }

    public static List<String> getSuppliers(int itemId) {
        Random seededRandom = new Random(itemId);
        int numSuppliers = seededRandom.nextInt(SUPPLIERS.size()) + 1; // 1 to 4 suppliers
        List<String> shuffled = new java.util.ArrayList<>(SUPPLIERS);
        java.util.Collections.shuffle(shuffled, seededRandom);
        return shuffled.subList(0, numSuppliers);
    }

    public static List<DayOfWeek> getDeliveryDays(String supplierName) {
        return SUPPLIER_DELIVERY_DAYS.getOrDefault(supplierName, Arrays.asList(DayOfWeek.SUNDAY));
    }

    public static String getBestDeal(int itemId, int quantity) {
        int index = random.nextInt(SUPPLIERS.size());
        return SUPPLIERS.get(index);
    }

    public static double calculateFinalPrice(double baseUnitPrice, int quantity, String supplierName) {
        double discountPercentage = 0.0;
        
        if (quantity >= 500) {
            discountPercentage = 0.20; 
        } else if (quantity >= 200) {
            discountPercentage = 0.15; 
        } else if (quantity >= 100) {
            discountPercentage = 0.10; 
        } else if (quantity >= 50) {
            discountPercentage = 0.05; 
        }

        double finalUnitPrice = baseUnitPrice * (1.0 - discountPercentage);
        return finalUnitPrice * quantity;
    }

}