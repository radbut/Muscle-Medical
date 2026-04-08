package jdm;

import java.util.Scanner;
import java.util.List;
// import java.io.*;  // used for reading CSV files
import jdm.repository;
// import jdm.model.TrafficLight;
import jdm.model.Patient;
import jdm.model.Measurement;
// import jdm.service.MonitoringService;
import jdm.service.ReportService;
import jdm.util.ArrayList;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("🚦 JDM Traffic Light Clinical Monitoring System");
        System.out.println("============================================");

        PatientRepository patientRepository = new PatientRepository();
        ReportService reportService = new ReportService();

        showMainMenu(patientRepository, reportService);
    }

    private static void showMainMenu(PatientRepository patientRepository, ReportService reportService) {
        boolean running = true; // otherwise the program stops after making the first choice

        while (running) {
            System.out.println("\nPlease find the menu below:");

            System.out.println("A: See All patients with their status");
            System.out.println("P: See all Patients with red status");
            System.out.println("S: Search for Specific patient");
            System.out.println("F: See Full summary report");
            System.out.println("N: Add New patient");
            System.out.println("R: Add new lab or CMAS Result for existing patient");
            System.out.println("X: EXIT program");
            System.out.println("Enter your option: ");

            String choice = scanner.nextLine().toUpperCase();  // Convert to uppercase for easier comparison

            switch (choice) {
                case "A" -> {  // All patients
                    List<Patient> patients = patientRepository.findAll();  // getAllPatients
                    for (Patient p : patients) {
                        reportService.generatePatientReport(p);
                    }
                }
                case "P" -> {  // Patients with red status  // TODO: add traffic light status
                    List<Patient> patients = patientRepository.findAll();
                    reportService.generateSummaryReport(patients);
                }
                case "S" -> {  // Search for specific patient
                    System.out.println("Enter patient ID: ");
                    String id = scanner.nextLine();

                    Patient patient = patientRepository.findById(id);

                    if (patient != null) {
                        reportService.generatePatientReport(patient);
                    } else {
                        System.out.println("Patient not found.");
                    }
                }
                case "F" -> {  // Full summary report
                    List<Patient> patients = patientRepository.findAll();
                    reportService.generateSummaryReport(patients);
                }
                case "N" -> {  // Add new patient
                    System.out.println("Enter patient name: ");
                    String name = scanner.nextLine();
                    System.out.println("Enter patient age: ");
                    int age = scanner.nextInt();
                    scanner.nextLine();  // consume newline

                    Patient newPatient = new Patient(name, age);
                    patientRepository.save(newPatient);
                    System.out.println("Patient added successfully.");
                }
                case "R" -> {  // Add new lab or CMAS result for existing patient
                    System.out.println("Enter patient ID: ");
                    String id = scanner.nextLine();

                    Patient patient = patientRepository.findById(id);

                    if (patient != null) {
                        System.out.println("Enter new lab/CMAS result: ");
                        String result = scanner.nextLine();
                        patientRepository.cmas(patient, result);
                        System.out.println("Result added successfully.");
                    } else {
                        System.out.println("Patient not found.");
                    }
                }
            case "X" -> {  // Exiting
                System.out.println("Exiting...");
                running = false;
            }
            default -> System.out.println("Invalid option. Please try again.");
        }
        }
    }
}