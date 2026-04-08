'''package jdm.repository;

import jdm.model.Patient;
import java.util.List;

public interface PatientRepository {
    void save(Patient patient);
    Patient findById(int id);
    List<Patient> findAll();
    void update(Patient patient);
    void delete(int id);
}'''

package jdm.repository;

import jdm.model.Patient;
import java.util.List;

public interface PatientRepository {
    boolean save(Patient patient);
    Patient findById(int id);
    List<Patient> findAll();
    boolean update(Patient patient);
    boolean delete(int id);
}