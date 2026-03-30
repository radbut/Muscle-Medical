package jdm.service;

import jdm.model.BiomarkerMeasurement;
import jdm.model.CMASMeasurement;
import jdm.model.Patient;
import jdm.repository.PatientRepository;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

// Reads the CSV files from the data/ folder and loads them into the database.
// Call load() once when the app starts.
public class DataLoaderService {

    private String dataDir;
    private String dbUrl;

    // Maps CSV UUIDs to the integer IDs assigned by the database
    private HashMap<String, Integer> groupIdMap     = new HashMap<>();
    private HashMap<String, Integer> labResultIdMap = new HashMap<>();

    private int patientIntId = -1;

    public DataLoaderService(String dataDir, String dbPath) {
        if (dataDir.endsWith(File.separator)) {
            this.dataDir = dataDir;
        } else {
            this.dataDir = dataDir + File.separator;
        }
        this.dbUrl = "jdbc:sqlite:" + dbPath;
    }

    // Runs all loading steps in order
    public void load() {
        System.out.println("Loading CSV data from: " + dataDir);
        try {
            Connection conn = DriverManager.getConnection(dbUrl);
            conn.createStatement().execute("PRAGMA foreign_keys = ON;");
            conn.setAutoCommit(false);

            loadPatients(conn);
            loadLabResultGroups(conn);
            loadLabResults(conn);
            loadMeasurements(conn);
            loadCmasScores(conn);

            conn.commit();
            conn.close();
            System.out.println("CSV import finished.");

        } catch (Exception e) {
            System.err.println("Import failed: " + e.getMessage());
        }
    }

    // ── Step 1: patients ─────────────────────────────────────────────────────────

    private void loadPatients(Connection conn) throws Exception {
        ArrayList<String[]> rows = readCsv(dataDir + "Patient.csv");

        for (String[] row : rows) {
            if (row.length < 2) continue;
            String name = row[1].trim();

            // Check if already in database
            PreparedStatement check = conn.prepareStatement(
                "SELECT id FROM patients WHERE name = ? LIMIT 1;"
            );
            check.setString(1, name);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                patientIntId = rs.getInt("id");
                System.out.println("Patient already exists: " + name);
                check.close();
                continue;
            }
            check.close();

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO patients (name) VALUES (?);",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, name);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                patientIntId = keys.getInt(1);
                System.out.println("Inserted patient: " + name + " (id=" + patientIntId + ")");
            }
            ps.close();
        }
    }

    // ── Step 2: lab result groups ────────────────────────────────────────────────

    private void loadLabResultGroups(Connection conn) throws Exception {
        ArrayList<String[]> rows = readCsv(dataDir + "LabResultGroup.csv");
        int count = 0;

        for (String[] row : rows) {
            if (row.length < 2) continue;
            String uuid      = row[0].trim();
            String groupName = row[1].trim();

            PreparedStatement check = conn.prepareStatement(
                "SELECT id FROM lab_result_groups WHERE group_name = ? LIMIT 1;"
            );
            check.setString(1, groupName);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                groupIdMap.put(uuid, rs.getInt("id"));
                check.close();
                continue;
            }
            check.close();

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO lab_result_groups (group_name) VALUES (?);",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, groupName);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                groupIdMap.put(uuid, keys.getInt(1));
                count++;
            }
            ps.close();
        }
        System.out.println("Lab result groups inserted: " + count);
    }

    // ── Step 3: lab results ──────────────────────────────────────────────────────

    private void loadLabResults(Connection conn) throws Exception {
        // Try the English file first
        String filePath = dataDir + "LabResults_EN_.csv";
        if (!new File(filePath).exists()) {
            filePath = dataDir + "LabResults(EN).csv";
        }

        ArrayList<String[]> rows = readCsv(filePath);
        // Columns: LabResultID, LabResultGroupID, (internal id), ResultName, Unit, ResultName_English
        int count = 0;

        for (String[] row : rows) {
            if (row.length < 4) continue;
            String labResultUuid = row[0].trim();
            String groupUuid     = row[1].trim();
            String resultName    = row[3].trim();
            String unit          = row.length > 4 ? row[4].trim() : "";
            String resultNameEn  = row.length > 5 ? row[5].trim() : resultName;
            if (resultNameEn.isEmpty()) resultNameEn = resultName;

            Integer groupIntId = groupIdMap.get(groupUuid);

            PreparedStatement check = conn.prepareStatement(
                "SELECT id FROM lab_results WHERE result_name = ? AND patient_id = ? LIMIT 1;"
            );
            check.setString(1, resultName);
            check.setInt(2, patientIntId);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                labResultIdMap.put(labResultUuid, rs.getInt("id"));
                check.close();
                continue;
            }
            check.close();

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO lab_results (group_id, patient_id, result_name, result_name_english, unit) VALUES (?, ?, ?, ?, ?);",
                Statement.RETURN_GENERATED_KEYS
            );
            if (groupIntId != null) ps.setInt(1, groupIntId);
            else                    ps.setNull(1, Types.INTEGER);
            ps.setInt(2, patientIntId);
            ps.setString(3, resultName);
            ps.setString(4, resultNameEn);
            ps.setString(5, unit);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                labResultIdMap.put(labResultUuid, keys.getInt(1));
                count++;
            }
            ps.close();
        }
        System.out.println("Lab results inserted: " + count);
    }

    // ── Step 4: measurements ─────────────────────────────────────────────────────

    private void loadMeasurements(Connection conn) throws Exception {
        ArrayList<String[]> rows = readCsv(dataDir + "Measurement.csv");
        // Columns: MeasurementID, LabResultID, DateTime, Value
        int inserted = 0;
        int skipped  = 0;

        for (String[] row : rows) {
            if (row.length < 4) continue;
            String labResultUuid = row[1].trim();
            String rawDateTime   = row[2].trim().replace("\"", "").trim();
            String rawValue      = row[3].trim();

            // Skip rows where the value is text (e.g. "neg", "pos")
            double value;
            try {
                value = Double.parseDouble(rawValue.replace(",", "."));
            } catch (NumberFormatException e) {
                skipped++;
                continue;
            }

            // Parse just the date part from "23-05-202409:46" or "2024-05-23"
            String isoDate = parseDate(rawDateTime);
            if (isoDate == null) {
                skipped++;
                continue;
            }

            Integer labResultIntId = labResultIdMap.get(labResultUuid);
            if (labResultIntId == null) {
                skipped++;
                continue;
            }

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO measurements (lab_result_id, datetime, value) VALUES (?, ?, ?);"
            );
            ps.setInt(1, labResultIntId);
            ps.setString(2, isoDate);
            ps.setDouble(3, value);
            ps.executeUpdate();
            ps.close();
            inserted++;
        }
        System.out.println("Measurements inserted: " + inserted + "  skipped: " + skipped);
    }

    // Converts "23-05-202409:46" or "2024-05-23" to "YYYY-MM-DD"
    private String parseDate(String raw) {
        if (raw == null || raw.isEmpty()) return null;

        // Take just the first 10 characters (the date part)
        String datePart = raw.length() >= 10 ? raw.substring(0, 10) : raw;

        // Already YYYY-MM-DD
        if (datePart.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return datePart;
        }

        // DD-MM-YYYY → YYYY-MM-DD
        if (datePart.matches("\\d{2}-\\d{2}-\\d{4}")) {
            String[] parts = datePart.split("-");
            return parts[2] + "-" + parts[1] + "-" + parts[0];
        }

        return null;
    }

    // ── Step 5: CMAS scores ───────────────────────────────────────────────────────

    private void loadCmasScores(Connection conn) throws Exception {
        ArrayList<String[]> rows = readCsv(dataDir + "CMAS.csv");
        if (rows.size() < 2) return;

        // Row 0 = date headers (first cell is the row label column, skip it)
        String[] dateHeaders = rows.get(0);
        int inserted = 0;

        // Row 1 onwards = score rows (one row per scale type)
        for (int r = 1; r < rows.size(); r++) {
            String[] row = rows.get(r);
            if (row.length == 0) continue;

            String label = row[0].trim();
            int scale;
            if (label.contains(">") || label.contains("10")) {
                scale = 52;
            } else {
                scale = 9;
            }

            for (int col = 1; col < row.length; col++) {
                if (col >= dateHeaders.length) break;

                String rawScore = row[col].trim();
                if (rawScore.isEmpty() || rawScore.equalsIgnoreCase("points")) continue;

                double score;
                try {
                    score = Double.parseDouble(rawScore);
                } catch (NumberFormatException e) {
                    continue;
                }

                String isoDate = parseCmasDate(dateHeaders[col].trim());
                if (isoDate == null) continue;

                PreparedStatement ps = conn.prepareStatement(
                    "INSERT OR IGNORE INTO cmas_scores (patient_id, date, score, scale) VALUES (?, ?, ?, ?);"
                );
                ps.setInt(1, patientIntId);
                ps.setString(2, isoDate);
                ps.setDouble(3, score);
                ps.setInt(4, scale);
                ps.executeUpdate();
                ps.close();
                inserted++;
            }
        }
        System.out.println("CMAS scores inserted: " + inserted);
    }

    // Converts "2023-10-07" or "15-3-2022" to "YYYY-MM-DD"
    private String parseCmasDate(String raw) {
        if (raw == null || raw.isEmpty()) return null;

        // Already YYYY-MM-DD
        if (raw.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return raw;
        }

        // D-M-YYYY or DD-MM-YYYY (single or double digit day/month)
        if (raw.matches("\\d{1,2}-\\d{1,2}-\\d{4}")) {
            String[] parts = raw.split("-");
            String day   = parts[0].length() == 1 ? "0" + parts[0] : parts[0];
            String month = parts[1].length() == 1 ? "0" + parts[1] : parts[1];
            return parts[2] + "-" + month + "-" + day;
        }

        return null;
    }

    // ── CSV reader ────────────────────────────────────────────────────────────────

    private ArrayList<String[]> readCsv(String filePath) throws IOException {
        ArrayList<String[]> result = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("File not found: " + filePath);
            return result;
        }

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(file), "UTF-8")
        );

        boolean isHeader = true;
        String line;
        String pending = null;

        while ((line = reader.readLine()) != null) {
            if (pending != null) {
                pending = pending + "\n" + line;
                if (countQuotes(pending) % 2 == 0) {
                    if (!isHeader) result.add(splitLine(pending));
                    else isHeader = false;
                    pending = null;
                }
                continue;
            }

            if (isHeader) { isHeader = false; continue; }

            if (countQuotes(line) % 2 != 0) {
                pending = line;
                continue;
            }

            result.add(splitLine(line));
        }

        reader.close();
        return result;
    }

    private int countQuotes(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '"') count++;
        }
        return count;
    }

    private String[] splitLine(String line) {
        ArrayList<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
}
