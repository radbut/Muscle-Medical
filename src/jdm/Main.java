package jdm;

import jdm.model.Patient;
import jdm.model.TrafficLight;
import jdm.model.BiomarkerMeasurement;
import jdm.model.CMASMeasurement;
import jdm.repository.PatientRepository;
import jdm.repository.SQLitePatientRepository;
import jdm.service.DataLoaderService;
import jdm.service.ReportService;
import jdm.service.MonitoringService;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println(" JDM Traffic Light Clinical Monitoring System");
        System.out.println("============================================");

        // Load CSV data into the database on startup
        DataLoaderService loader = new DataLoaderService("data", "jdm.db");
        loader.load();

        PatientRepository patientRepository = new SQLitePatientRepository("jdm.db");
        MonitoringService monitoringService = new MonitoringService();
        ReportService reportService = new ReportService(monitoringService);

        showMainMenu(patientRepository, reportService, monitoringService);
    }

    private static void showMainMenu(PatientRepository patientRepository, ReportService reportService, MonitoringService monitoringService) {
        boolean running = true;

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

            String choice = scanner.nextLine().toUpperCase();

            switch (choice) {
                case "A" -> { // All patients with their status
                    List<Patient> patients = patientRepository.findAll();
                    for (Patient p : patients) {
                        p.setStatus(monitoringService.calculateStatus(p));
                        reportService.generatePatientReport(p);
                    }
                }
                case "P" -> {  // Patients with red status
                    List<Patient> patients = patientRepository.findAll();
                    boolean found = false;
                    for (Patient p : patients) {
                        p.setStatus(monitoringService.calculateStatus(p));
                        if (p.getStatus() == TrafficLight.RED) {
                            reportService.generatePatientReport(p);
                            found = true;
                        }
                    }
                    if (!found) {
                        System.out.println("No patients with RED status found.");
                    }
                }
                case "S" -> {  // Search for specific patient
                    System.out.println("Enter patient ID: ");
                    try {
                        int id = Integer.parseInt(scanner.nextLine());

                        Patient patient = patientRepository.findById(id);

                        if (patient != null) {
                            patient.setStatus(monitoringService.calculateStatus(patient));
                            reportService.generatePatientReport(patient);
                        } else {
                            System.out.println("Patient not found.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID format.");
                    }
                }
                case "F" -> {  // Full summary report
                    List<Patient> patients = patientRepository.findAll();
                    for (Patient p : patients) {
                        p.setStatus(monitoringService.calculateStatus(p));
                    }
                    reportService.generateSummaryReport(patients);
                }
                case "N" -> {  // Add new patient
                    System.out.println("Enter patient name: ");
                    String name = scanner.nextLine();
                    System.out.println("Enter patient age: ");
                    try {
                        int age = Integer.parseInt(scanner.nextLine());

                        Patient newPatient = new Patient(0, name, age);
                        if (patientRepository.save(newPatient)) {
                            System.out.println("Patient added successfully with ID: " + newPatient.getId());
                        } else {
                            System.out.println("Failed to add patient.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid age format.");
                    }
                }
                case "R" -> {  // Add new lab or CMAS result for existing patient
                    System.out.println("Enter patient ID: ");
                    try {
                        int id = Integer.parseInt(scanner.nextLine());

                        Patient patient = patientRepository.findById(id);

                        if (patient != null) {
                            System.out.println("Enter measurement type (e.g., CXCL10, CMAS, Galectin-9): ");
                            String type = scanner.nextLine();
                            System.out.println("Enter value: ");
                            double value = Double.parseDouble(scanner.nextLine());
                            System.out.println("Enter date (YYYY-MM-DD): ");
                            String dateStr = scanner.nextLine();
                            LocalDate date = LocalDate.parse(dateStr);

                            if ("CMAS".equalsIgnoreCase(type)) {
                                System.out.println("Enter scale (usually 52): ");
                                int scale = Integer.parseInt(scanner.nextLine());
                                CMASMeasurement cmas = new CMASMeasurement(0, date, value, scale);
                                if (patientRepository.addMeasurement(id, cmas)) {
                                    System.out.println("CMAS measurement added successfully.");
                                } else {
                                    System.out.println("Failed to add CMAS measurement.");
                                }
                            } else {
                                System.out.println("Enter unit (e.g., pg/mL): ");
                                String unit = scanner.nextLine();
                                BiomarkerMeasurement biomarker = new BiomarkerMeasurement(0, date, value, type, unit);
                                if (patientRepository.addMeasurement(id, biomarker)) {
                                    System.out.println("Biomarker measurement added successfully.");
                                } else {
                                    System.out.println("Failed to add biomarker measurement.");
                                }
                            }
                        } else {
                            System.out.println("Patient not found.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input format.");
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
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