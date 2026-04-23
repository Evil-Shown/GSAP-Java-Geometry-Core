# GSAP Geometry Core

Java-based geometry and processing engine for parametric 2D shape manufacturing workflows.  
It validates incoming shape definitions, resolves parametric data, and generates Java outputs used by downstream preview and execution systems.

## What This Project Does

`GSAP Geometry Core` acts as the processing backend between editor-created shape JSON and manufacturing-ready generated artifacts.

Primary responsibilities:

- Parse and normalize shape JSON (legacy and parametric formats)
- Validate geometric correctness with tolerance-aware rules
- Execute shape-processing pipeline logic
- Generate dual Java output classes per shape (`ShapeTransformer` + `ShapePreview`)
- Run as a Redis + MySQL worker in integrated environments

## High-Level Architecture

```text
Shape JSON (from editor / DB)
          |
          v
      ShapeLoader
          |
          v
   Domain Model (Point, Edge, Shape, ...)
          |
          v
    GeometryValidator
          |
          v
   Shape Pipeline + Generators
          |
          v
  Generated Java output artifacts
```

For deeper architecture details, see `ARCHITECTURE.md`.

## Key Features

### Geometry domain model

- Immutable primitives and edge types (`LineEdge`, `ArcEdge`)
- Epsilon-aware comparisons for numerical stability
- Utility methods for perimeter and geometry calculations

### Multi-format loading

- Supports legacy `v1.0` shape definitions
- Supports `v2.0` parametric definitions with expression-based points
- DTO-based parsing boundary to keep domain model safe and strict

### Validation engine

- Enforces minimum topology requirements
- Checks closure and edge connectivity
- Flags invalid or degenerate geometry
- Returns aggregated validation errors instead of failing fast on first issue

### Parametric dual-output generation

- Produces two Java outputs for each processed shape:
  - `ShapeTransformer` for execution logic
  - `ShapePreview` for visualization metadata

See `PARAMETRIC_FORMAT.md` and `DUAL_OUTPUT.md` for full contracts.

## Repository Structure

```text
GSAP Geometry Core/
|- src/main/java/com/company/gsap/
|  |- model/                # Domain types
|  |- loader/               # JSON parsing + DTOs
|  |- validation/           # Geometry validation rules
|  |- pipeline/             # Shape processing orchestration
|  |- generator/            # Java output generation
|  `- worker/               # Redis/MySQL worker runtime
|- src/test/                # Unit and integration-style tests
|- shapes/                  # Input/output working directories
|- pom.xml                  # Maven build configuration
`- README.md
```

## Prerequisites

- Java 17+
- Maven 3.6+
- MySQL (for worker/database integration mode)
- Redis (for queue consumption mode)

## Build and Test

```bash
mvn clean test
```

Useful commands:

- `mvn compile` - compile source only
- `mvn test` - run tests
- `mvn -q exec:java` - run worker entrypoint (`WorkerApplication`)
- `mvn -q -DskipTests package` - build fat jar via shade plugin

Generated jar:

- `target/gsap-geometry-worker.jar`

Run jar directly:

```bash
java -jar target/gsap-geometry-worker.jar
```

## Running Worker Mode

Main class: `com.company.gsap.worker.WorkerApplication`

The worker:

- Reads jobs from Redis list key
- Fetches and updates shape state in MySQL
- Runs pipeline/generation
- Writes output to configured output directory

### Environment Variables

The worker reads these env vars:

- `JDBC_URL` (optional if `MYSQL_*` provided)
- `MYSQL_HOST` (default `localhost`)
- `MYSQL_PORT` (default `3306`)
- `MYSQL_DATABASE` (default `gsap_editor`)
- `MYSQL_USER` (default `root`)
- `MYSQL_PASSWORD`
- `REDIS_HOST` (default `127.0.0.1`)
- `REDIS_PORT` (default `6379`)
- `REDIS_PASSWORD` (optional)
- `SHAPE_JOB_LIST_KEY` (default `gsap:shape-processing:jobs`)
- `OUTPUT_DIR` (default `shapes/output`)

Example (PowerShell):

```powershell
$env:MYSQL_HOST="localhost"
$env:MYSQL_PORT="3306"
$env:MYSQL_DATABASE="gsap_editor"
$env:MYSQL_USER="root"
$env:MYSQL_PASSWORD="your_password"
$env:REDIS_HOST="127.0.0.1"
$env:REDIS_PORT="6379"
$env:SHAPE_JOB_LIST_KEY="gsap:shape-processing:jobs"
$env:OUTPUT_DIR="shapes/output"
mvn -q exec:java
```

## Input and Output Directories

Typical local flow:

- Place shape payloads in `shapes/input/` (if running file-based pipeline tooling)
- Generated Java outputs are written to `shapes/output/`
- Processed/failed input routing depends on pipeline mode and configuration

## Example Usage in Code

```java
ShapeLoader loader = new ShapeLoader();
Shape shape = loader.load("path/to/shape.json");

GeometryValidator validator = new GeometryValidator();
ValidationResult result = validator.validate(shape);

if (result.isValid()) {
    System.out.println("Shape valid: " + shape.getName());
} else {
    result.getErrors().forEach(System.err::println);
}
```

---

## 🤝 Contributing

This is a personal/Research project. Not currently accepting contributions, but feel free to fork and experiment.

---

## 📄 License

MIT License - See LICENSE file for details

---

## 🔧 Troubleshooting

### IntelliJ shows 244 errors

**Fix:** Open the project by selecting `pom.xml` directly (not the folder):
1. File → Open
2. Select `pom.xml`
3. Click "Open as Project"
4. Wait for Maven indexing to complete

### Tests fail with "InvalidPathException"

**Fix:** Already fixed in code. Using `Paths.get(resource.toURI())` instead of `URL.getPath()` for Windows compatibility.

### Build warnings about encoding

**Fix:** Already fixed. `pom.xml` includes `<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>`

---

## 🚀 How to Build and Run

### In IntelliJ IDEA

1. **Import the project as a Maven project** (File → Open → select the project root).
2. **Open the right-side Maven panel** (View → Tool Windows → Maven).
3. **To build and test:**
   - Expand `Lifecycle` and double-click `clean`, then `install`.
4. **To run the app (single pass):**
   - Expand `Plugins` → `exec` → double-click `exec:java`.
5. **To run in watch mode:**
   - At the top of the Maven panel, type `watch` in the `Profiles` field, then double-click `exec:java` under `Plugins`.

### From the Command Line

- **Build and test:**
  ```cmd
  mvn clean install
  ```
- **Run (single pass):**
  ```cmd
  mvn exec:java
  ```
- **Run in watch mode:**
  ```cmd
  mvn exec:java -Pwatch
  ```

### Where to put your JSON files
- Drop exported JSON files into `shapes/input/`.
- Generated `.java` files will appear in `shapes/output/`.
- Processed JSONs move to `shapes/input/processed/`.
- Failed JSONs move to `shapes/input/failed/`.

---

- `DOCUMENTATION_INDEX.md` - entry point for all docs
- `ARCHITECTURE.md` - architecture and processing flow
- `PARAMETRIC_FORMAT.md` - v2.0 JSON schema and conventions
- `DUAL_OUTPUT.md` - generated output structure and intent
- `QUICK_REFERENCE.md` - frequently used commands and quick ops

## Troubleshooting

- **IDE import issues**: open the project as a Maven project via `pom.xml`
- **MySQL connection errors**: verify `MYSQL_*`/`JDBC_URL` values and DB availability
- **Redis connection errors**: verify `REDIS_HOST`/`REDIS_PORT` and server state
- **No generated files**: verify `OUTPUT_DIR` exists or can be created, then check worker logs
- **Test path issues on Windows**: ensure paths are URI-safe and do not rely on raw URL path parsing

## Roadmap

**Project:** GSAP Geometry Core  
**Started:** February 2026  
**Author:** Damitha Samarakoon

## License

MIT