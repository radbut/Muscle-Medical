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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getValue() {
        return value;
    }

    public abstract String getType();
}