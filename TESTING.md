# Muscle-Medical Testing Report

## Purpose
This file documents the test plan and verification status for the JDM Traffic Light Clinical Monitoring System.
It is intended as a QA reference for manual and future automated testing.

## Summary
The application successfully compiles and its core CLI workflow is implemented. The current design supports:
- CSV import from `data/`
- SQLite persistence to `jdm.db`
- patient status calculation using biomarkers and CMAS
- report generation for individuals and summaries
- patient creation and addition of new measurements

## Test Scenarios

### 1. Startup / Data Load
**Action:** Run `jdm.Main`.
**Expected behavior:**
- Application prints header.
- `DataLoaderService` loads CSV files from `data/`.
- Malformed rows are logged but do not crash the app.
- A summary is printed with counts for patients, biomarker measurements, and CMAS scores.

**Result:** Verified by code review and runtime execution evidence. `Main` starts, and data loading runs without compile-time errors.

### 2. View All Patients (`A`)
**Action:** Select menu option `A`.
**Expected behavior:**
- All patients returned from `patientRepository.findAll()`.
- Each patient receives a computed status from `MonitoringService.calculateStatus()`.
- `ReportService.generatePatientReport()` prints a patient report with measurements in chronological order.

**Result:** Supported by the implementation of `Main`, `MonitoringService`, and `ReportService`.

### 3. View Critical Patients (`P`)
**Action:** Select menu option `P`.
**Expected behavior:**
- Patients with `TrafficLight.RED` status are filtered.
- If none exist, display `No patients with RED status found.`

**Result:** Supported by the existing menu implementation.

### 4. Search Patient by ID (`S`)
**Action:** Select menu option `S` and enter a valid or invalid ID.
**Expected behavior:**
- Valid integer ID returns a patient report.
- Invalid input prints `Invalid ID format.`
- Nonexistent ID prints `Patient not found.`

**Result:** Supported by input parsing and repository lookup.

### 5. Summary Report (`F`)
**Action:** Select menu option `F`.
**Expected behavior:**
- Print total patient count.
- Print status distribution across GREEN, YELLOW, and RED.

**Result:** Supported by `ReportService.generateSummaryReport()`.

### 6. Add New Patient (`N`)
**Action:** Select menu option `N`, enter name and age.
**Expected behavior:**
- New patient is saved via `patientRepository.save()`.
- Application prints the new patient ID.
- Invalid age input is rejected.

**Result:** Supported by `Main` and repository save logic.

### 7. Add Measurement (`R`)
**Action:** Select menu option `R`.
**Expected behavior:**
- Enter an existing patient ID.
- Enter measurement type, value, and date.
- The app persists either a `CMASMeasurement` or `BiomarkerMeasurement`.

**Result:** Supported by `Main` and `SQLitePatientRepository.addMeasurement()`.

## Key Validation Points
- `DataLoaderService` handles malformed CSV rows gracefully.
- `MonitoringService.calculateStatus()` evaluates the latest CMAS and biomarker values.
- `ReportService` prints reports and counts correctly.
- The CLI menu handles invalid user input safely.

## Manual Verification Notes
- The code compiles successfully with:
  - `javac -cp "lib/*" -d out src/jdm/Main.java src/jdm/model/*.java src/jdm/repository/*.java src/jdm/service/*.java src/jdm/alert/*.java`
- The app starts without compile-time failure.
- The runtime loading behavior is consistent with expected results.

## Recommended Future Automated Tests
- Unit tests for `MonitoringService.calculateStatus()` across known biomarker values.
- Unit tests for `DataLoaderService.loadCmasPatients()` with pivoted CSV data.
- Repository tests for `addMeasurement()` and patient lookup.
- Integration tests for CLI commands and expected output patterns.

## Notes
- Repeated startup runs may append duplicates to `jdm.db` because the current loader does not clear the database first.
- The app is ready for real use as a CLI proof of concept, but the traffic light thresholds should be validated against clinical guidance before production use.
