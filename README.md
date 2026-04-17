## Run the application

**Requirements:** 
- Java 17 or later
- SQLite JDBC driver in lib/
- Project file structure:
- src/ for Java sources
- data/ with Patient.csv, LabResults(EN).csv, Measurement.csv, CMAS.csv
- lib/ containing JDBC jar

**Run-file:**
### Run the "run.bat" file - If you are using Windows
### Run the "run.sh" file - If you are using Mac/Linux

## OR

**Terminal:**
### Windows

#### javac -cp "lib/*" -d out src/jdm/Main.java src/jdm/model/*.java src/jdm/repository/*.java src/jdm/service/*.java src/jdm/alert/*.java
#### java -cp "out;lib/*" jdm.Main
### Mac/Linux

#### find src/jdm -name "*.java" | xargs javac -cp "lib/*" -d out
#### java -cp "out:lib/*" jdm.Main

---


# Muscle-Medical

# JDM Traffic Light Clinical Monitoring System

A CLI-based Java application that functions as an Electronic Patient Dossier (EPD) and clinical decision-support tool for **Juvenile Dermatomyositis (JDM)**. Designed for clinicians to monitor patients using biomarker data and CMAS scores.

## 📋 Project Overview

Juvenile Dermatomyositis (JDM) is a rare autoimmune disease in children causing muscle weakness. This system monitors disease progression using:
- **CMAS scores** (Childhood Myositis Assessment Scale)
- **Biomarkers**: CXCL10, Galectin-9 (from the cytokines lab group)

The application automatically assigns each patient a **Traffic Light status** based on their latest measurements:

| Status | Colour | Meaning |
|--------|--------|---------|
| 🟢 Green | Stable | No immediate action needed |
| 🟡 Yellow | Monitoring Required | Follow-up recommended |
| 🔴 Red | Critical | Urgent clinical attention needed |



## 📂 Dataset Structure

The dataset consists of **5 relational CSV files** for a single patient (Patient X):

| File | Description |
|------|-------------|
| `Patient.csv` | Patient ID and name |
| `LabResultGroup.csv` | Groups of lab tests (e.g. Cytokines, Hematology) |
| `LabResult.csv` | Individual lab test definitions with units (Dutch names) |
| `LabResults(EN).csv` | Same as LabResult.csv but includes English translations |
| `Measurement.csv` | Actual timestamped measurement values, linked to LabResult |
| `CMAS.csv` | CMAS scores in a wide (pivot) format — dates as columns |

**Join chain:** `Patient → LabResult (via PatientID) → LabResultGroup (via GroupID) → Measurement (via LabResultID)`




---

## 🗃️ Database

The app uses a local **SQLite** database (`jdm.db`) created automatically on first run. No database server installation required — the file is stored in the project root and is listed in `.gitignore`.


## 📐 Clinical Thresholds (Traffic Light Logic)

> ⚠️ These thresholds must be confirmed by the team and ideally validated against clinical literature before implementation. The ranges below are based on the observed data values in the provided dataset and are a starting point only.

| Biomarker / Score | 🟢 Green (Stable) | 🟡 Yellow (Monitor) | 🔴 Red (Critical) |
|---|---|---|---|
| CMAS Score (>10 scale) | ≥ 40 | 20–39 | < 20 |
| CXCL10 (pg/mL) | < 200 | 200–400 | > 400 |
| Galectin-9 (pg/mL) | < 5500 | 5500–7000 | > 7000 |

> **Note:** TNFR2 is not present in the provided dataset. Only CXCL10 and Galectin-9 are available as cytokine biomarkers.
