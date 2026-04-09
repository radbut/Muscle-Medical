package jdm.service;

import jdm.alert.Alert;
import jdm.model.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportService {

    private final MonitoringService monitoringService;

    public ReportService(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    public void generatePatientReport(Patient patient) {
        if (patient == null) {
            System.out.println("Patient not found.");
            return;
        }

        System.out.println("=== Patient Report ===");
        System.out.println("ID: " + patient.getId());
        System.out.println("Name: " + patient.getName());
        System.out.println("Age: " + patient.getAge());

        TrafficLight status = monitoringService.calculateStatus(patient);
        System.out.println("Current Status: " + status);
        Alert alert = monitoringService.getAlertForPatient(patient);
        System.out.println("Alert: " + alert.getMessage() + " (" + alert.getColor() + ")");

        System.out.println("\n=== Measurement History (Chronological) ===");
        List<Measurement> measurements = patient.getMeasurements();
        measurements.sort((m1, m2) -> m1.getDate().compareTo(m2.getDate()));

        for (Measurement measurement : measurements) {
            String unit = "";
            if (measurement instanceof BiomarkerMeasurement) {
                unit = ((BiomarkerMeasurement) measurement).getUnit();
            }
            System.out.println(measurement.getDate() + " - " + measurement.getType() + ": " + measurement.getValue() + " " + unit);
        }

        System.out.println("=== End of Report ===");
    }

    public void generateSummaryReport(List<Patient> patients) {
        if (patients == null || patients.isEmpty()) {
            System.out.println("No patients to report on.");
            return;
        }

        System.out.println("=== Summary Report ===");
        System.out.println("Total Patients: " + patients.size());

        Map<TrafficLight, Long> statusCounts = patients.stream()
                .collect(Collectors.groupingBy(monitoringService::calculateStatus, Collectors.counting()));

        System.out.println("Status Distribution:");
        System.out.println("Green (Stable): " + statusCounts.getOrDefault(TrafficLight.GREEN, 0L));
        System.out.println("Yellow (Monitoring): " + statusCounts.getOrDefault(TrafficLight.YELLOW, 0L));
        System.out.println("Red (Critical): " + statusCounts.getOrDefault(TrafficLight.RED, 0L));

        System.out.println("=== End of Summary ===");
    }
}