package jdm.model;

import java.time.LocalDate;

public class CMASMeasurement extends Measurement {

    private int scale;  // maximum possible score, usually 52

    public CMASMeasurement(int id, LocalDate date, double value, int scale) {
        super(id, date, value);
        this.scale = scale;
    }

    @Override
    public String getType() {
        return "CMAS";
    }

    public int getScale() { return scale; }
    public void setScale(int scale) { this.scale = scale; }
}
