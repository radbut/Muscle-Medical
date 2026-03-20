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

    // TODO: You will add more methods here (getters, addMeasurement, getStatus, etc.)
    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public List<Measurement> getMeasurements() { return measurements; }
}