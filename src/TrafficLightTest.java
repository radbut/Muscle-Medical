import jdm.model.*;
import java.time.LocalDate;

public class TrafficLightTest {

    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        // ── Test 1: RED because CMAS score is critically low ─────────────────
        Patient p1 = new Patient(1, "Test RED - low CMAS", 10);
        p1.addMeasurement(new CMASMeasurement(1, LocalDate.now(), 15, 52)); // 15 < 20 = RED
        p1.calculateStatus();
        if (p1.getStatus() == TrafficLight.RED) {
            System.out.println("PASS — Test 1: Low CMAS correctly gives RED");
            passed++;
        } else {
            System.out.println("FAIL — Test 1: Expected RED, got " + p1.getStatus());
            failed++;
        }

        // ── Test 2: RED because CXCL10 is critically high ────────────────────
        Patient p2 = new Patient(2, "Test RED - high CXCL10", 10);
        p2.addMeasurement(new CMASMeasurement(2, LocalDate.now(), 45, 52)); // CMAS fine
        p2.addMeasurement(new BiomarkerMeasurement(3, LocalDate.now(), 450, "CXCL10", "pg/mL")); // 450 > 400 = RED
        p2.calculateStatus();
        if (p2.getStatus() == TrafficLight.RED) {
            System.out.println("PASS — Test 2: High CXCL10 correctly gives RED");
            passed++;
        } else {
            System.out.println("FAIL — Test 2: Expected RED, got " + p2.getStatus());
            failed++;
        }

        // ── Test 3: RED because Galectin-9 is critically high ─────────────────
        Patient p3 = new Patient(3, "Test RED - high Gal9", 10);
        p3.addMeasurement(new CMASMeasurement(4, LocalDate.now(), 45, 52)); // CMAS fine
        p3.addMeasurement(new BiomarkerMeasurement(5, LocalDate.now(), 8000, "Galectin-9", "pg/mL")); // 8000 > 7000 = RED
        p3.calculateStatus();
        if (p3.getStatus() == TrafficLight.RED) {
            System.out.println("PASS — Test 3: High Galectin-9 correctly gives RED");
            passed++;
        } else {
            System.out.println("FAIL — Test 3: Expected RED, got " + p3.getStatus());
            failed++;
        }

        // ── Test 4: YELLOW because CMAS is in the middle range ───────────────
        Patient p4 = new Patient(4, "Test YELLOW - mid CMAS", 10);
        p4.addMeasurement(new CMASMeasurement(6, LocalDate.now(), 30, 52)); // 20 <= 30 < 40 = YELLOW
        p4.calculateStatus();
        if (p4.getStatus() == TrafficLight.YELLOW) {
            System.out.println("PASS — Test 4: Mid CMAS correctly gives YELLOW");
            passed++;
        } else {
            System.out.println("FAIL — Test 4: Expected YELLOW, got " + p4.getStatus());
            failed++;
        }

        // ── Test 5: YELLOW because CXCL10 is in monitoring range ─────────────
        Patient p5 = new Patient(5, "Test YELLOW - mid CXCL10", 10);
        p5.addMeasurement(new CMASMeasurement(7, LocalDate.now(), 45, 52)); // CMAS fine
        p5.addMeasurement(new BiomarkerMeasurement(8, LocalDate.now(), 300, "CXCL10", "pg/mL")); // 200 < 300 < 400 = YELLOW
        p5.calculateStatus();
        if (p5.getStatus() == TrafficLight.YELLOW) {
            System.out.println("PASS — Test 5: Mid CXCL10 correctly gives YELLOW");
            passed++;
        } else {
            System.out.println("FAIL — Test 5: Expected YELLOW, got " + p5.getStatus());
            failed++;
        }

        // ── Test 6: GREEN — everything within safe range ──────────────────────
        Patient p6 = new Patient(6, "Test GREEN - all fine", 10);
        p6.addMeasurement(new CMASMeasurement(9, LocalDate.now(), 48, 52));  // >= 40 = fine
        p6.addMeasurement(new BiomarkerMeasurement(10, LocalDate.now(), 190, "CXCL10", "pg/mL")); // < 200 = fine
        p6.addMeasurement(new BiomarkerMeasurement(11, LocalDate.now(), 5000, "Galectin-9", "pg/mL")); // < 5500 = fine
        p6.calculateStatus();
        if (p6.getStatus() == TrafficLight.GREEN) {
            System.out.println("PASS — Test 6: All safe values correctly gives GREEN");
            passed++;
        } else {
            System.out.println("FAIL — Test 6: Expected GREEN, got " + p6.getStatus());
            failed++;
        }

        // ── Test 7: RED — no measurements at all ─────────────────────────────
        Patient p7 = new Patient(7, "Test RED - no data", 10);
        p7.calculateStatus();
        if (p7.getStatus() == TrafficLight.RED) {
            System.out.println("PASS — Test 7: No measurements correctly gives RED");
            passed++;
        } else {
            System.out.println("FAIL — Test 7: Expected RED, got " + p7.getStatus());
            failed++;
        }

        // ── Summary ───────────────────────────────────────────────────────────
        System.out.println("\n────────────────────────────────");
        System.out.println("Results: " + passed + " passed, " + failed + " failed.");
        if (failed == 0) {
            System.out.println("All tests passed. Your traffic light logic is correct.");
        } else {
            System.out.println("Some tests failed. Check the logic in Patient.java.");
        }
    }
}