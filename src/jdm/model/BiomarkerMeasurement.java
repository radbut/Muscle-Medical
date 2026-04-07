package jdm.model;

import java.time.LocalDate;

public class BiomarkerMeasurement extends Measurement {

    private String type;  // e.g. "CXCL10" or "Galectin-9"
    private String unit;  // e.g. "pg/mL"
    private int labResultId;

    public BiomarkerMeasurement(int id, LocalDate date, double value, String type, String unit) {
        super(id, date, value);
        this.type = type;
        this.unit = unit;
    }

    @Override
    public String getType() {
        return type;
    }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public int getLabResultId() { return labResultId; }
    public void setLabResultId(int labResultId) { this.labResultId = labResultId; }
}
