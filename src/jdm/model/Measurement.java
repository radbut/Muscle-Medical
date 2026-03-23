package jdm.model;

import java.time.LocalDate;

public abstract class Measurement {
    protected int id;
    protected LocalDate date;
    protected double value;

    public Measurement(int id, LocalDate date, double value) {
        this.id = id;
        this.date = date;
        this.value = value;
    }

    // These two methods were missing
    public double getValue() {
        return value;
    }

    public abstract String getType();
}