package jdm;

import jdm.model.BiomarkerMeasurement;
import jdm.model.CMASMeasurement;
import jdm.model.Patient;
import jdm.repository.PatientRepository;
import jdm.repository.SQLitePatientRepository;
import jdm.service.DataLoaderService;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static PatientRepository repo = new SQLitePatientRepository("jdm.db");

    public static void main(String[] args) {
        System.out.println("JDM Traffic Light Clinical Monitoring System");
        System.out.println("============================================");

        // Load CSV data into the database on startup
        DataLoaderService loader = new DataLoaderService("data", "jdm.db");
        loader.load();

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
            scanner.nextLine();

            switch (choice) {
                case 1 -> System.out.println("-> Add patient feature coming (you will code this)");
                case 2 -> viewAllPatients();
                case 3 -> System.out.println("-> Calculate traffic lights coming");
                case 4 -> System.out.println("-> View history coming");
                case 5 -> {
                    System.out.println("Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid option!");
            }
        }
    }

    private static void viewAllPatients() {
        List<Patient> patients = repo.findAll();

        if (patients.isEmpty()) {
            System.out.println("No patients found.");
            return;
        }

        System.out.println("\n========================================");
        for (Patient p : patients) {
            System.out.println("Name   : " + p.getName());
            System.out.println("ID     : " + p.getId());
            System.out.println("Measurements: " + p.getMeasurements().size());

            BiomarkerMeasurement cxcl10 = p.getLatestBiomarker("CXCL10");
            if (cxcl10 != null) {
                System.out.println("Latest CXCL10    : " + cxcl10.getValue()
                        + " " + cxcl10.getUnit() + " on " + cxcl10.getDate());
            }

            BiomarkerMeasurement gal9 = p.getLatestBiomarker("Galectin-9");
            if (gal9 != null) {
                System.out.println("Latest Galectin-9: " + gal9.getValue()
                        + " " + gal9.getUnit() + " on " + gal9.getDate());
            }

            CMASMeasurement cmas = p.getLatestCmas();
            if (cmas != null) {
                System.out.println("Latest CMAS      : " + cmas.getValue()
                        + " / " + cmas.getScale() + " on " + cmas.getDate());
            }
            System.out.println("----------------------------------------");
        }
    }
}
