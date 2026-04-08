package jdm.service;

public class ReportService {
    
}package jdm.service;

import jdm.model.Patient;
import java.util.List;

public class ReportService {

    public void generatePatientReport(Patient patient) {
        System.out.println("----- Patient Report -----");
        System.out.println("ID: " + patient.getId());
        System.out.println("Name: " + patient.getName());
        System.out.println("Age: " + patient.getAge());
        System.out.println("Status: " + patient.getStatus());
        System.out.println("--------------------------");
    }

    public void generateSummaryReport(List<Patient> patients) {
        System.out.println("===== Summary Report =====");

        for (Patient p : patients) {
            System.out.println(
                p.getId() + " - " +
                p.getName() + " - " +
                p.getStatus()
            );
        }

        System.out.println("==========================");
    }
}
