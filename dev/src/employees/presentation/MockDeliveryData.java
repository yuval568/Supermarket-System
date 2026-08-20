package employees.presentation;

import employees.domain.*;
import employees.service.DeliveryService;
import employees.service.EmployeeService;

import java.time.DayOfWeek;
import java.time.LocalDate;

// This class creates fake (mock) deliveries to test the system integration
public class MockDeliveryData {

    // Loads fixed delivery data into the system
    public static void loadMockDeliveries() throws Exception {

        DeliveryService deliveryService = DeliveryService.getInstance();
        EmployeeService empService = EmployeeService.getInstance();

        // Get the branches from the system
        Branch b001 = empService.getBranchById("B001");
        Branch b002 = empService.getBranchById("B002");

        // Find the date for next Sunday (same calculation used in DataUI)
        LocalDate nextSunday = LocalDate.now().with(DayOfWeek.SUNDAY);
        if (!nextSunday.isAfter(LocalDate.now())) {
            nextSunday = nextSunday.plusWeeks(1);
        }

        // Delivery 1: Type B truck to branch B001 on Sunday Morning
        deliveryService.addDelivery("D001", nextSunday, ShiftType.MORNING, b001, TruckType.TYPE_B);

        // Delivery 2: Type C truck to branch B001 on Monday Evening (+1 day)
        deliveryService.addDelivery("D002", nextSunday.plusDays(1), ShiftType.EVENING, b001, TruckType.TYPE_C);

        // Delivery 3: Type D truck to branch B002 on Sunday Morning
        deliveryService.addDelivery("D003", nextSunday, ShiftType.MORNING, b002, TruckType.TYPE_D);

        // Delivery 4: Type B truck to branch B002 on Tuesday Evening (+2 days)
        deliveryService.addDelivery("D004", nextSunday.plusDays(2), ShiftType.EVENING, b002, TruckType.TYPE_B);

        System.out.println("Mock delivery data loaded successfully for integration testing.");
    }
}