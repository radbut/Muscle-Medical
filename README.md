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

## 🗂️ Project Structure

```
muscle-medical/
├── src/
│   └── jdm/
│       ├── Main.java
│       ├── model/
│       │   ├── Patient.java
│       │   ├── Measurement.java          (abstract)
│       │   ├── BiomarkerMeasurement.java
│       │   ├── CMASMeasurement.java
│       │   └── TrafficLight.java
│       ├── repository/
│       │   ├── PatientRepository.java    (interface)
│       │   └── SQLitePatientRepository.java
│       ├── service/
│       │   ├── DataLoaderService.java
│       │   ├── MonitoringService.java
│       │   └── ReportService.java
│       └── alert/
│           ├── Alert.java                (abstract)
│           ├── StableAlert.java
│           ├── MonitoringAlert.java
│           └── CriticalAlert.java
├── lib/
│   └── sqlite-jdbc-3.51.3.0.jar
├── data/
│   ├── Patient.csv
│   ├── LabResult.csv
│   ├── LabResults(EN).csv
│   ├── LabResultGroup.csv
│   ├── Measurement.csv
│   └── CMAS.csv
├── .gitignore
└── README.md
```

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

> ⚠️ **CMAS.csv is a pivot table** — dates are column headers, not rows. The DataLoader must transpose this when parsing.

## ⚙️ Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| Java (Temurin JDK) | 17 | [adoptium.net](https://adoptium.net/temurin/releases/?version=17) |
| SQLite JDBC Driver | 3.51.3.0 | [github.com/xerial/sqlite-jdbc](https://github.com/xerial/sqlite-jdbc/releases/latest) |
| Git | Latest | [git-scm.com](https://git-scm.com/) |

> **Mac users (M1/M2/M3/M4):** Download the `aarch64` JDK package.  
> **Mac users (Intel):** Download the `x64` JDK package.  
> **Windows users:** Download the `.msi` installer from the same Adoptium page.

---

## 🚀 Setup & Installation

### 1. Clone the repository
```bash
git clone https://github.com/radbut/Muscle-Medical.git
cd muscle-medical
```

### 2. Add the SQLite driver
- Download `sqlite-jdbc-3.51.3.0.jar` from the link above.
- Place it in the `lib/` folder.

### 3. Place the dataset files
- Copy all 6 CSV files into the `data/` folder.

### 4. Open in your IDE

**VS Code:**
1. Install the [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack).
2. Open the project folder: `File → Open Folder`.
3. VS Code will auto-detect the Java project.
4. Add the JAR to your classpath — create `.vscode/settings.json` with:
```json
{
  "java.project.referencedLibraries": ["lib/**/*.jar"]
}
```

**IntelliJ IDEA (Community):**
1. `File → Open` → select the project folder.
2. Right-click `lib/` → **Add as Library** → OK.

### 5. Run the application

**VS Code:** Open `Main.java` → click **▶ Run** above `main()`.  
**IntelliJ:** Right-click `Main.java` → **Run 'Main.main()'**.  
**Terminal:**
```bash
# Mac/Linux
javac -cp lib/sqlite-jdbc-3.51.3.0.jar -d out src/jdm/**/*.java src/jdm/*.java
java -cp out:lib/sqlite-jdbc-3.51.3.0.jar jdm.Main

# Windows
javac -cp lib\sqlite-jdbc-3.51.3.0.jar -d out src\jdm\**\*.java src\jdm\*.java
java -cp out;lib\sqlite-jdbc-3.51.3.0.jar jdm.Main
```

---

## 🗃️ Database

The app uses a local **SQLite** database (`jdm.db`) created automatically on first run. No database server installation required — the file is stored in the project root and is listed in `.gitignore`.

## 🧪 Running Tests

Tests are located in `src/test/`. To run:
```bash
javac -cp lib/sqlite-jdbc-3.51.3.0.jar -d out src/test/**/*.java
java -cp out:lib/sqlite-jdbc-3.51.3.0.jar org.junit.runner.JUnitCore [TestClassName]
```

## 📌 Git Workflow

1. **Never push directly to `main`.**
2. Create a branch for your feature:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. Commit regularly with clear messages:
   ```bash
   git commit -m "Add Patient model with getStatus() method"
   ```
4. Push your branch and open a **Pull Request** on GitHub.
5. At least one teammate must review before merging.

### Branch naming convention
- `feature/model-and-repository`
- `feature/patient-trafficlight-monitoring`
- `feature/dataloader-alerts`
- `feature/reports-and-menu`
- `fix/cmas-parsing-bug`

## 📐 Clinical Thresholds (Traffic Light Logic)

> ⚠️ These thresholds must be confirmed by the team and ideally validated against clinical literature before implementation. The ranges below are based on the observed data values in the provided dataset and are a starting point only.

| Biomarker / Score | 🟢 Green (Stable) | 🟡 Yellow (Monitor) | 🔴 Red (Critical) |
|---|---|---|---|
| CMAS Score (>10 scale) | ≥ 40 | 20–39 | < 20 |
| CXCL10 (pg/mL) | < 200 | 200–400 | > 400 |
| Galectin-9 (pg/mL) | < 5500 | 5500–7000 | > 7000 |

> **Note:** TNFR2 is not present in the provided dataset. Only CXCL10 and Galectin-9 are available as cytokine biomarkers.
