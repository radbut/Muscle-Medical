package test.jdm;

import jdm.model.BiomarkerMeasurement;
import jdm.model.CMASMeasurement;
import jdm.model.Patient;
import jdm.repository.PatientRepository;
import jdm.repository.SQLitePatientRepository;

import java.time.LocalDate;
import java.util.List;

// Manual tests for the repository.
//
// How to compile and run (from project root):
//   javac -cp lib/sqlite-jdbc-3.51.3.0.jar -d out src/jdm/model/*.java src/jdm/repository/*.java src/test/jdm/RepositoryManualTest.java
//   java  -cp out:lib/sqlite-jdbc-3.51.3.0.jar test.jdm.RepositoryManualTest
public class RepositoryManualTest {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        System.out.println("Running repository tests...\n");

        // Use in-memory database so tests leave no files behind
        PatientRepository repo = new SQLitePatientRepository(":memory:");

        testSaveAndFindById(repo);
        testFindAll(repo);
        testDelete(repo);

        System.out.println("\n--- Results: " + passed + " passed, " + failed + " failed ---");
    }

    // Test 1: save a patient then load them back by id
    static void testSaveAndFindById(PatientRepository repo) {
        System.out.println("Test 1: save() and findById()");

        Patient patient = new Patient(0, "Test Patient", 12);
        patient.addMeasurement(new BiomarkerMeasurement(0, LocalDate.of(2024, 1, 10), 350.0, "CXCL10", "pg/mL"));
        patient.addMeasurement(new CMASMeasurement(0, LocalDate.of(2024, 1, 10), 35.0, 52));

        boolean saved = repo.save(patient);
        check("save() returns true", saved);
        check("patient id is set after save", patient.getId() > 0);

        Patient loaded = repo.findById(patient.getId());
        check("findById() finds the patient", loaded != null);

        if (loaded != null) {
            check("name matches", loaded.getName().equals("Test Patient"));
            check("age matches", loaded.getAge() == 12);
            check("2 measurements loaded", loaded.getMeasurements().size() == 2);

            CMASMeasurement cmas = loaded.getLatestCmas();
            check("CMAS score is 35.0", cmas != null && cmas.getValue() == 35.0);
            check("CMAS scale is 52", cmas != null && cmas.getScale() == 52);

            BiomarkerMeasurement bm = loaded.getLatestBiomarker("CXCL10");
            check("CXCL10 value is 350.0", bm != null && bm.getValue() == 350.0);
            check("CXCL10 unit is pg/mL", bm != null && bm.getUnit().equals("pg/mL"));
        }
    }

    // Test 2: save two patients and check findAll returns both
    static void testFindAll(PatientRepository repo) {
        System.out.println("\nTest 2: findAll()");

        Patient second = new Patient(0, "Second Patient", 10);
        second.addMeasurement(new CMASMeasurement(0, LocalDate.of(2024, 6, 1), 45.0, 52));
        repo.save(second);

        List<Patient> all = repo.findAll();
        check("findAll() returns at least 2 patients", all.size() >= 2);

        boolean foundFirst  = false;
        boolean foundSecond = false;
        for (Patient p : all) {
            if (p.getName().equals("Test Patient"))   foundFirst  = true;
            if (p.getName().equals("Second Patient")) foundSecond = true;
        }
        check("Test Patient is in findAll()",   foundFirst);
        check("Second Patient is in findAll()", foundSecond);
    }

    // Test 3: delete a patient and confirm they are gone
    static void testDelete(PatientRepository repo) {
        System.out.println("\nTest 3: delete()");

        Patient toDelete = new Patient(0, "Delete Me", 8);
        toDelete.addMeasurement(new BiomarkerMeasurement(0, LocalDate.of(2024, 1, 1), 100.0, "CXCL10", "pg/mL"));
        repo.save(toDelete);

        int id = toDelete.getId();
        check("patient to delete has a valid id", id > 0);

        boolean deleted = repo.delete(id);
        check("delete() returns true", deleted);

        Patient result = repo.findById(id);
        check("findById() returns null after delete", result == null);

        boolean secondDelete = repo.delete(id);
        check("deleting again returns false", !secondDelete);
    }

    static void check(String description, boolean condition) {
        if (condition) {
            System.out.println("  PASS: " + description);
            passed++;
        } else {
            System.out.println("  FAIL: " + description);
            failed++;
        }
    }
}
