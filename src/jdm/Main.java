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
        while (true) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. Add new patient");
            System.out.println("2. View all patients");
            System.out.println("3. Calculate statuses (Traffic Lights)");
            System.out.println("4. View patient history");
            System.out.println("5. Exit");
            System.out.print("Choose option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // clear newline

            switch (choice) {
                case 1 -> System.out.println("→ Add patient feature coming (you will code this)");
                case 2 -> System.out.println("→ View patients feature coming");
                case 3 -> System.out.println("→ Calculate traffic lights coming");
                case 4 -> System.out.println("→ View history coming");
                case 5 -> {
                    System.out.println("👋 Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid option!");
            }
        }
    }
}