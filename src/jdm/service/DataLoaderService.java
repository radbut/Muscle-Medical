package jdm.service;

import java.io.*;
import java.util.*;

public class DataLoaderService {
    public static void dataLoader(String[] args) {
        String filePath = "../../data/LabResult";
        List<Patient> patientData = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            br.readLine(); // skip header

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                String Name = values[0];
                String PatientID = values[0];

                patientData.add(new Patient(PatientID, Name));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // test
        for (Patient s : patientData) {
            System.out.println(s);
        }
    }

}
