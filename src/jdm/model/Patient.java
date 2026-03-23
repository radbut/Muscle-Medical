package jdm.model;

import java.util.ArrayList;
import java.util.List;

public class Patient {
    private int id;
    private String name;
    private int age;
    private List<Measurement> measurements = new ArrayList<>();

    public Patient(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public void addMeasurement(Measurement m) {
        measurements.add(m);
    }

    public Measurement getLatest(String type) {
        for (int i = measurements.size() - 1; i >= 0; i--) {
            if (measurements.get(i).getType().equals(type)) {
                return measurements.get(i);
            }
        }
        return null;
    }

    public TrafficLight getStatus() {
        Measurement cmas = getLatest("CMAS");
        Measurement cxcl10 = getLatest("CXCL10");
        Measurement gal9 = getLatest("Galectin-9");

        if (cmas == null) return TrafficLight.RED; // no data = critical for safety

        double cmasScore = cmas.getValue();

        if (cmasScore < 20) return TrafficLight.RED;
        if (cmasScore < 40) return TrafficLight.YELLOW;

        // Check biomarkers if available
        if (cxcl10 != null && cxcl10.getValue() > 400) return TrafficLight.RED;
        if (gal9 != null && gal9.getValue() > 7000) return TrafficLight.RED;

        return TrafficLight.GREEN;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public List<Measurement> getMeasurements() { return measurements; }
}