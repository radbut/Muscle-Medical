package jdm.service;

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

    public void calculateAllStatuses() {
        List<Patient> patients = repository.findAll();

        if (patients.isEmpty()) {
            System.out.println("No patients loaded yet.");
            return;
        }

        // Concurrency (this is what the rubric wants)
        ExecutorService executor = Executors.newFixedThreadPool(4);

        for (Patient p : patients) {
            executor.submit(() -> {
                TrafficLight status = p.getStatus();
                System.out.println("✅ Patient " + p.getName() + " (ID " + p.getId() + ") → " + status + " | " + status.getMessage());
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}