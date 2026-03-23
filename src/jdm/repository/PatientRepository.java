package jdm.repository;

import jdm.model.Patient;
import java.util.List;

public interface PatientRepository {
    void save(Patient patient);
    Patient findById(int id);
    List<Patient> findAll();
    void update(Patient patient);
    void delete(int id);
}