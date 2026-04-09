package jdm.model;

import java.util.ArrayList;
import java.util.List;

public class Patient {
    private int id;
    private String name;
    private int age;
    private TrafficLight status;
    private List<Measurement> measurements = new ArrayList<>();

    public Patient(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public void addMeasurement(Measurement m) {
        measurements.add(m);
    }

    // Returns the most recent biomarker measurement of a given type, or null
    public BiomarkerMeasurement getLatestBiomarker(String type) {
        BiomarkerMeasurement latest = null;
        for (Measurement m : measurements) {
            if (m instanceof BiomarkerMeasurement) {
                BiomarkerMeasurement b = (BiomarkerMeasurement) m;
                if (b.getType().equalsIgnoreCase(type)) {
                    if (latest == null || b.getDate().isAfter(latest.getDate())) {
                        latest = b;
                    }
                }
            }
        }
        return latest;
    }

    // Returns the most recent CMAS score, or null
    public CMASMeasurement getLatestCmas() {
        CMASMeasurement latest = null;
        for (Measurement m : measurements) {
            if (m instanceof CMASMeasurement) {
                CMASMeasurement c = (CMASMeasurement) m;
                if (latest == null || c.getDate().isAfter(latest.getDate())) {
                    latest = c;
                }
            }
        }
        return latest;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public TrafficLight getStatus() { return status; }
    public void setStatus(TrafficLight status) { this.status = status; }

    public List<Measurement> getMeasurements() { return measurements; }
}
