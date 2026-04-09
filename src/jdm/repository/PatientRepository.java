package jdm.repository;

import jdm.model.Patient;
import jdm.model.Measurement;
import java.util.List;

public interface PatientRepository {

    // Save a new patient to the database
    boolean save(Patient patient);

    // Find a patient by id. Returns null if not found.
    Patient findById(int id);

    // Return all patients
    List<Patient> findAll();

    // Update a patient's name
    boolean update(Patient patient);

    // Delete a patient and all their data
    boolean delete(int id);

    // Add a new measurement to an existing patient
    boolean addMeasurement(int patientId, Measurement measurement);
}
