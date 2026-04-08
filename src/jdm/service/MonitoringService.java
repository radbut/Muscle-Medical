package jdm.service;

import jdm.alert.Alert;
import jdm.alert.CriticalAlert;
import jdm.alert.MonitoringAlert;
import jdm.alert.StableAlert;
import jdm.model.Patient;
import jdm.model.TrafficLight;
import jdm.repository.PatientRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MonitoringService {

    private final PatientRepository repository;

    public MonitoringService(PatientRepository repository) {
        this.repository = repository;
    }

    // Calculates status for every patient in the database using multiple threads
    public void calculateAllStatuses() {
        List<Patient> patients = repository.findAll();

        if (patients.isEmpty()) {
            System.out.println("No patients found in the database.");
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(4);

        for (Patient p : patients) {
            executor.submit(() -> {
                p.calculateStatus();
                repository.update(p);
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Status calculation was interrupted.");
        }

        System.out.println("Status calculated for " + patients.size() + " patient(s).");
    }

    // Returns the right alert object for a given status
    public Alert getAlertForStatus(TrafficLight status) {
        if (status == null) return new StableAlert();
        return switch (status) {
            case GREEN  -> new StableAlert();
            case YELLOW -> new MonitoringAlert();
            case RED    -> new CriticalAlert();
        };
    }

    // Returns the right alert for a patient directly
    public Alert getAlertForPatient(Patient patient) {
        if (patient == null || patient.getStatus() == null) return new StableAlert();
        return getAlertForStatus(patient.getStatus());
    }
}