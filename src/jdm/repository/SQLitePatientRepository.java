package jdm.repository;

import jdm.model.BiomarkerMeasurement;
import jdm.model.CMASMeasurement;
import jdm.model.Patient;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SQLitePatientRepository implements PatientRepository {

    private final String dbUrl; //Rostyk>
    private final boolean useSharedMemory;
    private Connection sharedConnection; //Rostyk/>

    public SQLitePatientRepository(String dbPath) {
        if (":memory:".equals(dbPath)) {  //Rostyk>
            this.dbUrl = "jdbc:sqlite:file:memdb?mode=memory&cache=shared";
            this.useSharedMemory = true;
        } else {
            this.dbUrl = "jdbc:sqlite:" + dbPath;
            this.useSharedMemory = false;
        } //Rostyk/>
        createTables();
    }

    private Connection connect() throws SQLException {
        if (useSharedMemory) { //Rostyk>
            if (sharedConnection == null || sharedConnection.isClosed()) {
                sharedConnection = DriverManager.getConnection(dbUrl);
                sharedConnection.createStatement().execute("PRAGMA foreign_keys = ON;");
            }
            return sharedConnection;
        } //Rostyk/>
        Connection conn = DriverManager.getConnection(dbUrl);
        conn.createStatement().execute("PRAGMA foreign_keys = ON;");
        return conn;
    }

    // Creates all 5 tables if they don't exist yet
    private void createTables() {
        try {
            Connection conn = connect();
            Statement st = conn.createStatement();

            st.execute(
                "CREATE TABLE IF NOT EXISTS patients (" +
                "id   INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "age  INTEGER);"
            );

            st.execute(
                "CREATE TABLE IF NOT EXISTS lab_result_groups (" +
                "id         INTEGER PRIMARY KEY AUTOINCREMENT," +
                "group_name TEXT NOT NULL);"
            );

            st.execute(
                "CREATE TABLE IF NOT EXISTS lab_results (" +
                "id                  INTEGER PRIMARY KEY AUTOINCREMENT," +
                "group_id            INTEGER," +
                "patient_id          INTEGER NOT NULL," +
                "result_name         TEXT NOT NULL," +
                "result_name_english TEXT," +
                "unit                TEXT," +
                "FOREIGN KEY (group_id)   REFERENCES lab_result_groups(id)," +
                "FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE);"
            );

            st.execute(
                "CREATE TABLE IF NOT EXISTS measurements (" +
                "id            INTEGER PRIMARY KEY AUTOINCREMENT," +
                "lab_result_id INTEGER NOT NULL," +
                "datetime      TEXT NOT NULL," +
                "value         REAL NOT NULL," +
                "FOREIGN KEY (lab_result_id) REFERENCES lab_results(id) ON DELETE CASCADE);"
            );

            st.execute(
                "CREATE TABLE IF NOT EXISTS cmas_scores (" +
                "id         INTEGER PRIMARY KEY AUTOINCREMENT," +
                "patient_id INTEGER NOT NULL," +
                "date       TEXT NOT NULL," +
                "score      REAL NOT NULL," +
                "scale      INTEGER NOT NULL DEFAULT 52," +
                "FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE);"
            );

            st.close();
            closeConnection(conn);

        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    // ── save ─────────────────────────────────────────────────────────────────────

    @Override
    public boolean save(Patient patient) {
        try {
            Connection conn = connect();
            conn.setAutoCommit(false);

            // Insert patient row
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO patients (name, age) VALUES (?, ?);",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, patient.getName());
            ps.setInt(2, patient.getAge());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (!keys.next()) {
                conn.rollback();
                closeConnection(conn);
                return false;
            }
            int patientId = keys.getInt(1);
            patient.setId(patientId);
            ps.close();

            // Insert each measurement
            for (jdm.model.Measurement m : patient.getMeasurements()) {
                if (m instanceof BiomarkerMeasurement) {
                    BiomarkerMeasurement bm = (BiomarkerMeasurement) m;
                    int labResultId = findOrCreateLabResult(conn, patientId,
                                                            bm.getType(), bm.getUnit());
                    PreparedStatement mps = conn.prepareStatement(
                        "INSERT INTO measurements (lab_result_id, datetime, value) VALUES (?, ?, ?);",
                        Statement.RETURN_GENERATED_KEYS
                    );
                    mps.setInt(1, labResultId);
                    mps.setString(2, bm.getDate().toString());
                    mps.setDouble(3, bm.getValue());
                    mps.executeUpdate();
                    ResultSet mkeys = mps.getGeneratedKeys();
                    if (mkeys.next()) bm.setId(mkeys.getInt(1));
                    bm.setLabResultId(labResultId);
                    mps.close();

                } else if (m instanceof CMASMeasurement) {
                    CMASMeasurement cm = (CMASMeasurement) m;
                    PreparedStatement cps = conn.prepareStatement(
                        "INSERT INTO cmas_scores (patient_id, date, score, scale) VALUES (?, ?, ?, ?);",
                        Statement.RETURN_GENERATED_KEYS
                    );
                    cps.setInt(1, patientId);
                    cps.setString(2, cm.getDate().toString());
                    cps.setDouble(3, cm.getValue());
                    cps.setInt(4, cm.getScale());
                    cps.executeUpdate();
                    ResultSet ckeys = cps.getGeneratedKeys();
                    if (ckeys.next()) cm.setId(ckeys.getInt(1));
                    cps.close();
                }
            }

            conn.commit();
            closeConnection(conn);
            return true;

        } catch (SQLException e) {
            System.err.println("Error saving patient: " + e.getMessage());
            return false;
        }
    }

    // Looks up a lab_results row for this patient + biomarker name.
    // Creates one if it doesn't exist yet.
    private int findOrCreateLabResult(Connection conn, int patientId,
                                      String name, String unit) throws SQLException {
        PreparedStatement sel = conn.prepareStatement(
            "SELECT id FROM lab_results WHERE patient_id = ? AND result_name_english = ? LIMIT 1;"
        );
        sel.setInt(1, patientId);
        sel.setString(2, name);
        ResultSet rs = sel.executeQuery();
        if (rs.next()) {
            int id = rs.getInt("id");
            sel.close();
            return id;
        }
        sel.close();

        PreparedStatement ins = conn.prepareStatement(
            "INSERT INTO lab_results (patient_id, result_name, result_name_english, unit) VALUES (?, ?, ?, ?);",
            Statement.RETURN_GENERATED_KEYS
        );
        ins.setInt(1, patientId);
        ins.setString(2, name);
        ins.setString(3, name);
        ins.setString(4, unit);
        ins.executeUpdate();
        ResultSet keys = ins.getGeneratedKeys();
        int newId = keys.getInt(1);
        ins.close();
        return newId;
    }

    // ── findById ──────────────────────────────────────────────────────────────────

    @Override
    public Patient findById(int id) {
        try {
            Connection conn = connect();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT id, name, age FROM patients WHERE id = ?;"
            );
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                closeConnection(conn);
                return null;
            }

            Patient patient = new Patient(rs.getInt("id"), rs.getString("name"), rs.getInt("age"));
            ps.close();

            loadMeasurements(conn, patient);

            closeConnection(conn);
            return patient;

        } catch (SQLException e) {
            System.err.println("Error finding patient id=" + id + ": " + e.getMessage());
            return null;
        }
    }

    // ── findAll ───────────────────────────────────────────────────────────────────

    @Override
    public List<Patient> findAll() {
        List<Patient> list = new ArrayList<>();
        try {
            Connection conn = connect();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, name, age FROM patients ORDER BY id;");

            while (rs.next()) {
                Patient p = new Patient(rs.getInt("id"), rs.getString("name"), rs.getInt("age"));
                loadMeasurements(conn, p);
                list.add(p);
            }

            st.close();
            closeConnection(conn);

        } catch (SQLException e) {
            System.err.println("Error loading all patients: " + e.getMessage());
        }
        return list;
    }

    // ── update ────────────────────────────────────────────────────────────────────

    @Override
    public boolean update(Patient patient) {
        try {
            Connection conn = connect();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE patients SET name = ?, age = ? WHERE id = ?;"
            );
            ps.setString(1, patient.getName());
            ps.setInt(2, patient.getAge());
            ps.setInt(3, patient.getId());
            int rows = ps.executeUpdate();
            ps.close();
            closeConnection(conn);
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating patient: " + e.getMessage());
            return false;
        }
    }

    // ── delete ────────────────────────────────────────────────────────────────────

    @Override
    public boolean delete(int id) {
        try {
            Connection conn = connect();
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM patients WHERE id = ?;"
            );
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            ps.close();
            closeConnection(conn);
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting patient id=" + id + ": " + e.getMessage());
            return false;
        }
    }

    // ── Load measurements from DB into a Patient ──────────────────────────────────

    private void loadMeasurements(Connection conn, Patient patient) throws SQLException {
        // Load biomarker measurements
        PreparedStatement ps = conn.prepareStatement(
            "SELECT m.id, m.datetime, m.value, lr.result_name_english, lr.unit, m.lab_result_id " +
            "FROM measurements m " +
            "JOIN lab_results lr ON m.lab_result_id = lr.id " +
            "WHERE lr.patient_id = ? " +
            "ORDER BY m.datetime;"
        );
        ps.setInt(1, patient.getId());
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            BiomarkerMeasurement bm = new BiomarkerMeasurement(
                rs.getInt("id"),
                LocalDate.parse(rs.getString("datetime")),
                rs.getDouble("value"),
                rs.getString("result_name_english"),
                rs.getString("unit")
            );
            bm.setLabResultId(rs.getInt("lab_result_id"));
            patient.addMeasurement(bm);
        }
        ps.close();

        // Load CMAS scores
        PreparedStatement cps = conn.prepareStatement(
            "SELECT id, date, score, scale FROM cmas_scores " +
            "WHERE patient_id = ? ORDER BY date;"
        );
        cps.setInt(1, patient.getId());
        ResultSet crs = cps.executeQuery();

        while (crs.next()) {
            CMASMeasurement cm = new CMASMeasurement(
                crs.getInt("id"),
                LocalDate.parse(crs.getString("date")),
                crs.getDouble("score"),
                crs.getInt("scale")
            );
            patient.addMeasurement(cm);
        }
        cps.close();
    }

    private void closeConnection(Connection conn) { //Rostyk>
        if (useSharedMemory) {
            return;
        }
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    } //Rostyk/>
}
