package inventory.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class SystemLogger {
    private static final List<String> logs = new ArrayList<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static synchronized void log(String message) {
        String formatted = String.format("[%s] %s", LocalDateTime.now().format(formatter), message);
        logs.add(formatted);
    }

    public static synchronized List<String> getLogs() {
        return new ArrayList<>(logs);
    }
}
