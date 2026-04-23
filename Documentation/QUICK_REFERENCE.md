# Quick Reference Guide

**GSAP Geometry Core v2.0**  
**Quick Command & API Reference**  
**Last Updated:** March 3, 2026

---

## 🚀 Quick Start Commands

### Build & Test

```cmd
REM Clean build and run all tests
mvn clean test

REM Compile only
mvn compile

REM Package JAR
mvn package

REM Clean all generated files
mvn clean
```

### Run the Application

```cmd
REM Single pass (process existing files and exit)
mvn exec:java

REM Watch mode (continuous monitoring)
mvn exec:java -Pwatch

REM Custom folders
mvn exec:java -Dexec.args="--input=my/input --output=my/output"

REM Custom folders with watch mode
mvn exec:java -Dexec.args="--input=my/input --output=my/output --watch=true"
```

### Batch Scripts

```cmd
REM Run tests (if run_tests.bat exists)
run_tests.bat

REM Process shapes once
mvn exec:java -Psingle

REM Start watching for new files
mvn exec:java -Pwatch
```

---

## 📂 Folder Structure

```
GSAP Geometry Core/
├── shapes/
│   ├── input/              ← Drop JSON files here
│   │   ├── processed/      ← Successfully processed files
│   │   └── failed/         ← Failed validation files
│   └── output/             ← Generated .java files appear here
├── src/
│   ├── main/java/          ← Production code
│   └── test/java/          ← Test code
├── target/                 ← Build output
└── pom.xml                 ← Maven configuration
```

---

## 📝 JSON Format Quick Reference

### v1.0 (Legacy)

```json
{
  "name": "SimpleShape",
  "version": "1.0",
  "unit": "mm",
  "thickness": 5.0,
  "edges": [
    {
      "type": "line",
      "start": { "x": 0, "y": 0 },
      "end": { "x": 100, "y": 0 }
    }
  ]
}
```

**Output:** 1 file (`ShapeTransformer_SimpleShape.java`)

### v2.0 (Parametric)

```json
{
  "name": "ParametricShape",
  "version": "2.0",
  "unit": "mm",
  "thickness": 5.0,
  "parameters": [
    { "name": "L", "type": "LINEAR", "defaultValue": 1000, "description": "Width" }
  ],
  "parametricEdges": [
    { "type": "line", "startPoint": "p0", "endPoint": "p1" }
  ],
  "pointExpressions": {
    "p0": { "x": "trimLeft", "y": "trimBottom" },
    "p1": { "x": "p0.x + L", "y": "p0.y" }
  }
}
```

**Output:** 2 files
- `ShapeTransformer_ParametricShape.java` (manufacturing)
- `ShapePreview_ParametricShape.java` (visualization)

---

## 🔧 Expression Syntax

### Special Keywords

| Keyword | Description | Example |
|---------|-------------|---------|
| `trimLeft` | Left reference point | `"x": "trimLeft"` |
| `trimBottom` | Bottom reference point | `"y": "trimBottom"` |
| `trimRight` | Right reference point | `"x": "trimRight"` |
| `trimTop` | Top reference point | `"y": "trimTop"` |

### Operators

| Operator | Operation | Example |
|----------|-----------|---------|
| `+` | Addition | `"p0.x + L"` |
| `-` | Subtraction | `"p0.y - H"` |
| `*` | Multiplication | `"L * 0.5"` |
| `/` | Division | `"L / 2"` |

### Point References

| Reference | Description | Example |
|-----------|-------------|---------|
| `pX.x` | X coordinate of point pX | `"p0.x"` |
| `pX.y` | Y coordinate of point pX | `"p0.y"` |

### Parameter References

| Reference | Description | Example |
|-----------|-------------|---------|
| `PARAM_NAME` | Direct parameter reference | `"L"`, `"H"`, `"R1"` |

### Complete Expression Examples

```json
"pointExpressions": {
  "p0": { 
    "x": "trimLeft", 
    "y": "trimBottom" 
  },
  "p1": { 
    "x": "p0.x", 
    "y": "p0.y - H" 
  },
  "p2": { 
    "x": "p0.x + L", 
    "y": "p0.y - H" 
  },
  "p3": { 
    "x": "p0.x + L - R1", 
    "y": "p0.y" 
  },
  "p4": { 
    "x": "p0.x + L / 2", 
    "y": "p0.y - H * 0.75" 
  }
}
```

---

## 📊 Parameter Types

| Type | Description | Use For | Example |
|------|-------------|---------|---------|
| `LINEAR` | Length/distance | Width, height, offset | `1000.0` mm |
| `RADIUS` | Circular radius | Arc radii, corner rounds | `50.0` mm |
| `ANGLE` | Angular measurement | Rotation angles | `1.5708` radians (90°) |

### Parameter Definition Template

```json
{
  "name": "PARAM_NAME",
  "type": "LINEAR | RADIUS | ANGLE",
  "defaultValue": 0.0,
  "description": "Human readable description"
}
```

---

## 🏗️ Edge Types

### Line Edge

```json
{
  "type": "line",
  "startPoint": "p0",
  "endPoint": "p1"
}
```

### Arc Edge

```json
{
  "type": "arc",
  "startPoint": "p2",
  "endPoint": "p3",
  "radiusParam": "R1",
  "largeArc": false,
  "sweep": false,
  "centerExpression": {
    "x": "p0.x + L - R1",
    "y": "p0.y - R1"
  }
}
```

**Arc Properties:**
- `largeArc`: `true` for arc > 180°, `false` otherwise
- `sweep`: `true` for clockwise, `false` for counter-clockwise
- `centerExpression`: Parametric expression for arc center

---

## 🧪 Testing Commands

### Run All Tests

```cmd
mvn test
```

### Run Specific Test Class

```cmd
mvn test -Dtest=CodeGeneratorTest
mvn test -Dtest=ParametricCodeGeneratorTest
mvn test -Dtest=ShapeLoaderTest
```

### Run Specific Test Method

```cmd
mvn test -Dtest=CodeGeneratorTest#testGenerateSimpleRectangle
```

### Test with Verbose Output

```cmd
mvn test -X
```

---

## 🔍 Validation Rules

### Shape Requirements

- ✅ Minimum 3 edges
- ✅ All edges must connect (within epsilon tolerance)
- ✅ Shape must be closed (last edge connects to first)
- ✅ No zero-length edges

### v2.0 Parametric Requirements

- ✅ At least one parameter defined
- ✅ All point references must exist in `pointExpressions`
- ✅ Points must be defined before use (dependency order)
- ✅ Expressions must reference valid parameters

### Common Validation Errors

| Error | Cause | Fix |
|-------|-------|-----|
| "Shape must have at least 3 edges" | Too few edges | Add more edges |
| "Shape is not closed" | Last edge doesn't connect to first | Fix edge coordinates |
| "Point pX not defined" | Point used before definition | Reorder point expressions |
| "Parameter Y not found" | Expression references undefined parameter | Add parameter or fix typo |

---

## 📦 Generated Code Structure

### ShapeTransformer (Manufacturing)

```java
public class ShapeTransformer_XXX extends ShapeTransformer {
    @Override
    public List<Edge> resize(Param param, ParamList paramList) {
        // Point calculations
        Point2D p0 = new Point2D.Double(...);
        
        // Edge construction
        List<Edge> edges = new EdgeBuilder()
            .startPoint(p0)
            .straightEdge(p1)
            .build();
        
        return edges;
    }
}
```

### ShapePreview (Visualization)

```java
public class ShapePreview_XXX {
    public Map<String, ParamInfo> getParameters() { ... }
    public Map<String, String> getMetadata() { ... }
    public Map<String, Point2D> calculatePreviewPoints(...) { ... }
}
```

---

## 🐛 Troubleshooting

### Build Issues

| Issue | Solution |
|-------|----------|
| "Cannot find symbol" | Run `mvn clean compile` |
| "Tests fail" | Check test resources exist |
| "Out of memory" | Increase Maven memory: `set MAVEN_OPTS=-Xmx1024m` |

### Runtime Issues

| Issue | Solution |
|-------|----------|
| "File not found" | Check input folder path |
| "Invalid JSON" | Validate JSON syntax |
| "Shape validation failed" | Check validation error messages |
| "No output files" | Check output folder permissions |

### Common Fixes

```cmd
REM Reset everything
mvn clean

REM Update dependencies
mvn dependency:resolve

REM Refresh IDE
REM In IntelliJ: File → Invalidate Caches → Restart

REM Check Java version
java -version
REM Should be Java 17 or higher
```

---

## 📚 API Quick Reference

### ShapeLoader

```java
ShapeLoader loader = new ShapeLoader();

// Load v1.0 or v2.0
Shape shape = loader.load("path/to/shape.json");

// Load DTO directly (v2.0)
ShapeDTO dto = loader.loadDTO("path/to/shape.json");
```

### GeometryValidator

```java
GeometryValidator validator = new GeometryValidator();
ValidationResult result = validator.validate(shape);

if (result.isValid()) {
    // Shape is valid
} else {
    for (String error : result.getErrors()) {
        System.err.println(error);
    }
}
```

### CodeGenerator (v1.0)

```java
CodeGenerator generator = new CodeGenerator();
GeneratorResult result = generator.generate(shape);

if (result.isSuccess()) {
    String javaCode = result.getCode();
    // Write to file
}
```

### ParametricCodeGenerator (v2.0)

```java
ParametricCodeGenerator generator = new ParametricCodeGenerator();
GeneratorResult result = generator.generate(shapeDTO);

if (result.isSuccess()) {
    String transformerCode = result.getCode();
    // Write ShapeTransformer file
}
```

### ShapePreviewGenerator (v2.0)

```java
ShapePreviewGenerator generator = new ShapePreviewGenerator();
GeneratorResult result = generator.generate(shapeDTO);

if (result.isSuccess()) {
    String previewCode = result.getCode();
    // Write ShapePreview file
}
```

### ShapePipeline (End-to-End)

```java
ShapePipeline pipeline = new ShapePipeline();

// Process single file
Shape shape = pipeline.process("input/shape.json");

// Generate code (auto-detects v1.0 vs v2.0)
List<GeneratorResult> results = pipeline.generateCode(
    shapeDTO, 
    "output"
);
```

---

## 🎯 Common Tasks

### Task: Add a New Shape

1. Create JSON file (v1.0 or v2.0 format)
2. Drop into `shapes/input/`
3. Run `mvn exec:java` or start watch mode
4. Find generated files in `shapes/output/`

### Task: Convert v1.0 to v2.0

1. Open v1.0 JSON
2. Add `parameters` array
3. Replace `edges` with `parametricEdges` and `pointExpressions`
4. Change version to "2.0"
5. Save and regenerate

### Task: Debug Shape Issues

1. Check `shapes/input/failed/` for error files
2. Read error message in console output
3. Fix JSON according to validation rules
4. Move back to `shapes/input/` and reprocess

### Task: Modify Generated Code

**Don't!** Instead:
1. Modify source JSON
2. Regenerate files
3. Generated code should never be manually edited

---

## 🔗 Configuration

### Maven Profiles

| Profile | Activation | Use Case |
|---------|------------|----------|
| `single` | Default | One-time processing |
| `watch` | `-Pwatch` | Continuous monitoring |

### Command Line Arguments

| Argument | Default | Description |
|----------|---------|-------------|
| `--input=path` | `shapes/input` | Input folder path |
| `--output=path` | `shapes/output` | Output folder path |
| `--watch=true/false` | `false` | Enable watch mode |

### Example Configurations

```cmd
REM Default (single pass, default folders)
mvn exec:java

REM Watch mode, default folders
mvn exec:java -Pwatch

REM Custom folders, single pass
mvn exec:java -Dexec.args="--input=custom/in --output=custom/out"

REM Everything custom
mvn exec:java -Dexec.args="--input=custom/in --output=custom/out --watch=true"
```

---

## 📈 Performance Tips

### Batch Processing

Process multiple files in watch mode for efficiency:
1. Start watch mode once
2. Drop multiple files
3. All processed automatically

### Large Shapes

For shapes with 100+ edges:
- Expect generation time: < 1 second
- Memory usage: < 100 MB
- No special configuration needed

---

## 🆘 Getting Help

### Quick Checks

```cmd
REM 1. Verify Java version
java -version

REM 2. Verify Maven installation
mvn -version

REM 3. Check project structure
dir shapes\input
dir shapes\output

REM 4. Run tests
mvn clean test
```

### Documentation

- **Format details:** [PARAMETRIC_FORMAT.md](PARAMETRIC_FORMAT.md)
- **Dual output:** [DUAL_OUTPUT.md](DUAL_OUTPUT.md)
- **Complete guide:** [COMPLETE_GUIDE.md](COMPLETE_GUIDE.md)
- **Architecture:** [ARCHITECTURE.md](ARCHITECTURE.md)
- **Main docs:** [README.md](README.md)

---

## 🎓 Examples

### Minimal v2.0 Shape

```json
{
  "name": "MinimalRectangle",
  "version": "2.0",
  "unit": "mm",
  "thickness": 5,
  "parameters": [
    { "name": "W", "type": "LINEAR", "defaultValue": 100 },
    { "name": "H", "type": "LINEAR", "defaultValue": 80 }
  ],
  "parametricEdges": [
    { "type": "line", "startPoint": "p0", "endPoint": "p1" },
    { "type": "line", "startPoint": "p1", "endPoint": "p2" },
    { "type": "line", "startPoint": "p2", "endPoint": "p3" },
    { "type": "line", "startPoint": "p3", "endPoint": "p0" }
  ],
  "pointExpressions": {
    "p0": { "x": "trimLeft", "y": "trimBottom" },
    "p1": { "x": "p0.x", "y": "p0.y + H" },
    "p2": { "x": "p0.x + W", "y": "p0.y + H" },
    "p3": { "x": "p0.x + W", "y": "p0.y" }
  }
}
```

### Rectangle with Rounded Corner

```json
{
  "name": "RoundedRectangle",
  "version": "2.0",
  "unit": "mm",
  "thickness": 5,
  "parameters": [
    { "name": "W", "type": "LINEAR", "defaultValue": 1000 },
    { "name": "H", "type": "LINEAR", "defaultValue": 800 },
    { "name": "R", "type": "RADIUS", "defaultValue": 50 }
  ],
  "parametricEdges": [
    { "type": "line", "startPoint": "p0", "endPoint": "p1" },
    { "type": "line", "startPoint": "p1", "endPoint": "p2" },
    { "type": "line", "startPoint": "p2", "endPoint": "p3" },
    {
      "type": "arc",
      "startPoint": "p3",
      "endPoint": "p4",
      "radiusParam": "R",
      "largeArc": false,
      "sweep": false,
      "centerExpression": {
        "x": "p0.x + W - R",
        "y": "p0.y + R"
      }
    },
    { "type": "line", "startPoint": "p4", "endPoint": "p0" }
  ],
  "pointExpressions": {
    "p0": { "x": "trimLeft", "y": "trimBottom" },
    "p1": { "x": "p0.x", "y": "p0.y + H" },
    "p2": { "x": "p0.x + W", "y": "p0.y + H" },
    "p3": { "x": "p0.x + W", "y": "p0.y + R" },
    "p4": { "x": "p0.x + W - R", "y": "p0.y" }
  }
}
```

---

**Quick Reference Guide v2.0**  
**Status:** Production Ready ✅  
**Last Updated:** March 3, 2026

For comprehensive documentation, see [README.md](README.md) and [COMPLETE_GUIDE.md](COMPLETE_GUIDE.md).
