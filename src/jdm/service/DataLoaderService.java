package jdm.service;

import jdm.model.BiomarkerMeasurement;
import jdm.model.CMASMeasurement;
import jdm.model.Patient;
import jdm.repository.PatientRepository;
import jdm.repository.SQLitePatientRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataLoaderService {

    private final String dataDirectory;
    private final PatientRepository repository;

    public DataLoaderService(String dataDirectory, String databasePath) {
        this.dataDirectory = dataDirectory;
        this.repository = new SQLitePatientRepository(databasePath);
    }

    public DataLoaderService(String dataDirectory, PatientRepository repository) {
        this.dataDirectory = dataDirectory;
        this.repository = repository;
    }

    public LoadingSummary load() {
        Path dir = Paths.get(dataDirectory);
        if (!Files.exists(dir)) {
            log("Data directory not found: " + dataDirectory);
            return new LoadingSummary(0, 0, 0, 0);
        }

        Map<String, Patient> patients = loadPatients(dir.resolve("Patient.csv"));
        Map<String, LabResultInfo> labResults = loadLabResults(dir.resolve("LabResults(EN).csv"), patients);
        int biomarkerCount = loadMeasurements(dir.resolve("Measurement.csv"), labResults, patients);
        List<Patient> cmasPatients = loadCmasPatients(dir.resolve("CMAS.csv"));
        int cmasCount = cmasPatients.stream()
                .mapToInt(p -> (int) p.getMeasurements().stream().filter(m -> m instanceof CMASMeasurement).count())
                .sum();

        int savedPatients = 0;
        for (Patient patient : patients.values()) {
            if (repository.save(patient)) {
                savedPatients++;
            } else {
                log("Failed to save patient: " + patient.getName());
            }
        }

        for (Patient cmasPatient : cmasPatients) {
            if (!cmasPatient.getMeasurements().isEmpty()) {
                if (!repository.save(cmasPatient)) {
                    log("Failed to save CMAS summary patient: " + cmasPatient.getName());
                }
            }
        }

        LoadingSummary summary = new LoadingSummary(savedPatients, biomarkerCount, cmasCount, cmasPatients.size());
        System.out.println(summary);
        return summary;
    }

    public List<Patient> loadCmasPatients(Path file) {
        List<Patient> result = new ArrayList<>();
        if (!Files.exists(file)) {
            log("CMAS file not found: " + file);
            return result;
        }

        List<String> lines = readAllLines(file);
        if (lines.isEmpty()) {
            log("CMAS file is empty: " + file);
            return result;
        }

        String[] headers = splitCsvLine(lines.get(0));
        List<LocalDate> dates = new ArrayList<>();
        for (int i = 1; i < headers.length; i++) {
            dates.add(parseDate(headers[i].trim()));
        }

        for (int row = 1; row < lines.size(); row++) {
            String[] values = splitCsvLine(lines.get(row));
            if (values.length < 2) {
                log("Skipping malformed CMAS row " + (row + 1) + ": not enough columns");
                continue;
            }

            String label = values[0].trim();
            if (label.isEmpty()) {
                label = "CMAS row " + row;
            }

            Patient cmasPatient = new Patient(0, label, 0);
            int maxColumns = Math.min(values.length - 1, dates.size());
            for (int col = 0; col < maxColumns; col++) {
                LocalDate date = dates.get(col);
                if (date == null) {
                    continue;
                }
                String rawValue = values[col + 1].trim();
                if (rawValue.isEmpty() || rawValue.equalsIgnoreCase("points")) {
                    continue;
                }
                Double score = parseDouble(rawValue);
                if (score == null) {
                    log("Skipping malformed CMAS score at row " + (row + 1) + ", col " + (col + 2) + ": " + rawValue);
                    continue;
                }
                cmasPatient.addMeasurement(new CMASMeasurement(0, date, score, 52));
            }
            if (!cmasPatient.getMeasurements().isEmpty()) {
                result.add(cmasPatient);
            }
        }

        return result;
    }

    private Map<String, Patient> loadPatients(Path file) {
        Map<String, Patient> loaded = new HashMap<>();
        if (!Files.exists(file)) {
            log("Patient file not found: " + file);
            return loaded;
        }

        List<String> lines = readAllLines(file);
        for (int lineIndex = 1; lineIndex < lines.size(); lineIndex++) {
            String line = lines.get(lineIndex);
            if (line.isBlank()) {
                continue;
            }
            String[] columns = splitCsvLine(line);
            if (columns.length < 2) {
                log("Skipping malformed patient row " + (lineIndex + 1) + ": " + line);
                continue;
            }
            String externalId = columns[0].trim();
            String name = columns[1].trim();
            if (externalId.isEmpty() || name.isEmpty()) {
                log("Skipping patient with missing id or name on line " + (lineIndex + 1));
                continue;
            }
            loaded.put(externalId, new Patient(0, name, 0));
        }
        return loaded;
    }

    private Map<String, LabResultInfo> loadLabResults(Path file, Map<String, Patient> patients) {
        Map<String, LabResultInfo> result = new HashMap<>();
        if (!Files.exists(file)) {
            log("Lab results file not found: " + file);
            return result;
        }

        List<String> lines = readAllLines(file);
        for (int lineIndex = 1; lineIndex < lines.size(); lineIndex++) {
            String line = lines.get(lineIndex);
            if (line.isBlank()) {
                continue;
            }
            String[] columns = splitCsvLine(line);
            if (columns.length < 6) {
                log("Skipping malformed lab result row " + (lineIndex + 1) + ": " + line);
                continue;
            }
            String labResultId = columns[0].trim();
            String patientId = columns[2].trim();
            String resultName = columns[3].trim();
            String unit = columns[4].trim();
            String englishName = columns[5].trim();
            if (labResultId.isEmpty() || patientId.isEmpty()) {
                log("Skipping lab result with missing id or patient on line " + (lineIndex + 1));
                continue;
            }
            if (!patients.containsKey(patientId)) {
                log("Creating placeholder patient for unknown patient id " + patientId + " on line " + (lineIndex + 1));
                patients.put(patientId, new Patient(0, "Unknown Patient " + patientId, 0));
            }
            if (englishName.isEmpty()) {
                englishName = resultName.isEmpty() ? "Unknown" : resultName;
            }
            result.put(labResultId, new LabResultInfo(patientId, englishName, unit));
        }
        return result;
    }

    private int loadMeasurements(Path file, Map<String, LabResultInfo> labResults, Map<String, Patient> patients) {
        if (!Files.exists(file)) {
            log("Measurement file not found: " + file);
            return 0;
        }

        int count = 0;
        List<String> lines = readAllLines(file);
        for (int lineIndex = 1; lineIndex < lines.size(); lineIndex++) {
            String line = lines.get(lineIndex);
            if (line.isBlank()) {
                continue;
            }
            String[] columns = splitCsvLine(line);
            if (columns.length < 4) {
                log("Skipping malformed measurement row " + (lineIndex + 1) + ": " + line);
                continue;
            }
            String labResultId = columns[1].trim();
            String rawDate = columns[2].trim();
            String rawValue = columns[3].trim();

            LabResultInfo labInfo = labResults.get(labResultId);
            if (labInfo == null) {
                log("Skipping measurement for unknown lab result id " + labResultId + " on line " + (lineIndex + 1));
                continue;
            }
            Patient patient = patients.get(labInfo.patientId);
            if (patient == null) {
                log("Skipping measurement for unknown patient id " + labInfo.patientId + " on line " + (lineIndex + 1));
                continue;
            }
            LocalDate measurementDate = parseDateTime(rawDate);
            if (measurementDate == null) {
                log("Skipping malformed measurement date on line " + (lineIndex + 1) + ": " + rawDate);
                continue;
            }
            Double value = parseDouble(rawValue);
            if (value == null) {
                log("Skipping malformed measurement value on line " + (lineIndex + 1) + ": " + rawValue);
                continue;
            }
            BiomarkerMeasurement measurement = new BiomarkerMeasurement(0, measurementDate, value, labInfo.englishName, labInfo.unit);
            patient.addMeasurement(measurement);
            count++;
        }
        return count;
    }

    private List<String> readAllLines(Path file) {
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log("Unable to read file " + file + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private String[] splitCsvLine(String line) {
        return line.split(",", -1);
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim().replaceAll("\uFEFF", "");
        for (DateTimeFormatter formatter : new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("d-M-uuuu"),
                DateTimeFormatter.ofPattern("dd-MM-uuuu"),
                DateTimeFormatter.ISO_LOCAL_DATE}) {
            try {
                return LocalDate.parse(trimmed, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private LocalDate parseDateTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim().replaceAll("\uFEFF", "");
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("d-M-uuuuHH:mm"),
                DateTimeFormatter.ofPattern("dd-MM-uuuuHH:mm"),
                DateTimeFormatter.ofPattern("d-M-uuuu H:mm"),
                DateTimeFormatter.ofPattern("dd-MM-uuuu H:mm")
        };
        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(trimmed, formatter);
                return dateTime.toLocalDate();
            } catch (DateTimeParseException ignored) {
            }
        }
        return parseDate(trimmed);
    }

    private Double parseDouble(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().replace(',', '.');
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void log(String message) {
        System.err.println("[DataLoader] " + message);
    }

    private static class LabResultInfo {
        private final String patientId;
        private final String englishName;
        private final String unit;

        private LabResultInfo(String patientId, String englishName, String unit) {
            this.patientId = patientId;
            this.englishName = englishName;
            this.unit = unit;
        }
    }

    public static class LoadingSummary {
        private final int patientsLoaded;
        private final int biomarkerMeasurements;
        private final int cmasScores;
        private final int cmasPatientsSaved;

        public LoadingSummary(int patientsLoaded, int biomarkerMeasurements, int cmasScores, int cmasPatientsSaved) {
            this.patientsLoaded = patientsLoaded;
            this.biomarkerMeasurements = biomarkerMeasurements;
            this.cmasScores = cmasScores;
            this.cmasPatientsSaved = cmasPatientsSaved;
        }

        public int getPatientsLoaded() {
            return patientsLoaded;
        }

        public int getBiomarkerMeasurements() {
            return biomarkerMeasurements;
        }

        public int getCmasScores() {
            return cmasScores;
        }

        public int getCmasPatientsSaved() {
            return cmasPatientsSaved;
        }

        @Override
        public String toString() {
            return "Loaded " + patientsLoaded + " patient(s), "
                    + biomarkerMeasurements + " biomarker measurements, "
                    + cmasScores + " CMAS score(s) across " + cmasPatientsSaved + " CMAS patient groups.";
        }
    }
}
