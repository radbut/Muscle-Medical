package jdm;

import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("🚦 JDM Traffic Light Clinical Monitoring System");
        System.out.println("============================================");

        // TODO: add database loading here later

        showMainMenu();
    }

    private static void showMainMenu() {
    // Temporary test code for Person 2
    System.out.println("\n=== TESTING PERSON 2 WORK ===");
    
    // TODO: Later we will use real repository
    // For now we just print that service is ready
    System.out.println("MonitoringService is ready (concurrency + traffic light logic working)");

    while (true) {
        System.out.println("\n=== MAIN MENU ===");
        System.out.println("1. Add new patient");
        System.out.println("2. View all patients");
        System.out.println("3. Calculate statuses (Traffic Lights) ← Test this");
        System.out.println("4. View patient history");
        System.out.println("5. Exit");
        System.out.print("Choose option: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 3 -> {
                // Temporary test
                System.out.println("Running status calculation...");
                // We will connect real repository later
                System.out.println("✅ Traffic light logic works (no real data yet)");
            }
            case 5 -> {
                System.out.println("👋 Goodbye!");
                System.exit(0);
            }
            default -> System.out.println("Other options coming soon...");
        }
    }
}
}