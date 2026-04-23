# Dual Output Feature — ShapeTransformer + ShapePreview

**GSAP Geometry Core v2.0**  
**Feature:** Dual File Generation  
**Status:** Production Ready  
**Last Updated:** March 3, 2026

---

## Overview

Starting with v2.0, the GSAP Geometry Core generates **TWO specialized Java files** for each parametric shape, separating manufacturing concerns from visualization concerns.

### The Two Files

| File | Purpose | Use Case | Dependencies |
|------|---------|----------|--------------|
| **ShapeTransformer_XXX** | Manufacturing | CNC execution, offset generation | Manufacturing framework |
| **ShapePreview_XXX** | Visualization | UI preview, parameter inspection | Standalone (no dependencies) |

---

## Why Dual Output?

### Problem with Single File Approach (v1.0)

In v1.0, a single file contained everything:
- Manufacturing logic
- Geometry calculations
- Metadata
- Preview information

**Issues:**
- ❌ UI components had to import manufacturing framework
- ❌ Preview features were tightly coupled to production code
- ❌ No way to inspect parameters without creating manufacturing objects
- ❌ Single responsibility principle violated

### Solution: Separation of Concerns

v2.0 splits functionality into two specialized files:

```
┌─────────────────────────────────────────────────────────────┐
│                   Parametric Shape (v2.0)                   │
└──────────────────────┬──────────────────────────────────────┘
                       │
          ┌────────────┴────────────┐
          ▼                         ▼
┌──────────────────────┐  ┌──────────────────────┐
│ ShapeTransformer_XXX │  │ ShapePreview_XXX     │
├──────────────────────┤  ├──────────────────────┤
│ • resize()           │  │ • getParameters()    │
│ • Edge generation    │  │ • getMetadata()      │
│ • Offset calculation │  │ • calculatePreview() │
│ • Manufacturing code │  │ • No dependencies    │
└──────────────────────┘  └──────────────────────┘
          │                         │
          ▼                         ▼
   Manufacturing              Visualization
   System (CNC)               System (UI)
```

---

## File 1: ShapeTransformer_XXX.java

### Purpose

Executes shape transformations for manufacturing, including resizing and offset generation.

### Key Features

- **Parametric resize()** — Dynamically calculates edges based on parameters
- **Expression evaluation** — Converts string expressions to Java code
- **Edge generation** — Creates LineEdge and ArcEdge objects
- **Manufacturing integration** — Extends base ShapeTransformer class

### Generated Code Structure

```java
package com.example.geometry.generated;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.ArrayList;

public class ShapeTransformer_ExampleShape extends ShapeTransformer {

    @Override
    public List<Edge> resize(Param param, ParamList paramList) {
        // ═══ Point calculations (parametric) ═══
        Point2D p0 = new Point2D.Double(
            param.getTrimLeft(), 
            param.getTrimBottom()
        );
        Point2D p1 = new Point2D.Double(
            p0.getX(), 
            p0.getY() - paramList.get("H")
        );
        Point2D p2 = new Point2D.Double(
            p0.getX() + paramList.get("L"), 
            p1.getY()
        );
        
        // ═══ Arc center points ═══
        Point2D center0 = new Point2D.Double(
            p0.getX() + paramList.get("L") - paramList.get("R1"),
            p0.getY() - paramList.get("R1")
        );
        
        // ═══ Build edges ═══
        List<Edge> edges = new EdgeBuilder()
            .startPoint(p0)
            .straightEdge(p1)
            .straightEdge(p2)
            .arcEdge(center0, paramList.get("R1"), 0.0, 1.5708, false)
            .build();
        
        return edges;
    }
}
```

### Key Components

#### 1. Point Declarations

Converts parametric expressions to Java code:

**From JSON:**
```json
"p1": { "x": "p0.x", "y": "p0.y - H" }
```

**To Java:**
```java
Point2D p1 = new Point2D.Double(p0.getX(), p0.getY() - paramList.get("H"));
```

#### 2. Parameter Access

- `param.getTrimLeft()` — Trim reference points
- `param.getTrimBottom()` — Trim reference points
- `paramList.get("L")` — User-defined parameters

#### 3. Edge Construction

Uses fluent EdgeBuilder API:
- `.startPoint(p0)` — Set starting point
- `.straightEdge(p1)` — Add line edge
- `.arcEdge(center, radius, start, end, clockwise)` — Add arc edge

### Usage in Manufacturing

```java
// Load transformer
ShapeTransformer transformer = new ShapeTransformer_ExampleShape();

// Set parameters
Param param = new Param(trimLeft, trimBottom);
ParamList paramList = new ParamList();
paramList.put("L", 1000.0);
paramList.put("H", 800.0);
paramList.put("R1", 50.0);

// Generate edges for manufacturing
List<Edge> edges = transformer.resize(param, paramList);

// Use edges for CNC toolpath generation
for (Edge edge : edges) {
    generateToolpath(edge);
}
```

---

## File 2: ShapePreview_XXX.java

### Purpose

Provides visualization and metadata for UI components, completely independent of manufacturing framework.

### Key Features

- **Parameter inspection** — Get all parameters with types and defaults
- **Metadata access** — Shape name, thickness, unit
- **Preview calculation** — Calculate point positions for any parameter values
- **Zero dependencies** — Standalone class, no manufacturing imports

### Generated Code Structure

```java
package com.example.geometry.generated;

import java.awt.geom.Point2D;
import java.util.*;

public class ShapePreview_ExampleShape {

    // ═══ Parameter definitions ═══
    private final Map<String, ParamInfo> parameters;
    
    public ShapePreview_ExampleShape() {
        parameters = new LinkedHashMap<>();
        parameters.put("L", new ParamInfo("L", "LINEAR", 1000.0, "Overall width"));
        parameters.put("H", new ParamInfo("H", "LINEAR", 800.0, "Overall height"));
        parameters.put("R1", new ParamInfo("R1", "RADIUS", 50.0, "Corner radius"));
    }
    
    // ═══ Public API ═══
    
    public Map<String, ParamInfo> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }
    
    public Map<String, String> getMetadata() {
        Map<String, String> meta = new LinkedHashMap<>();
        meta.put("name", "ExampleShape");
        meta.put("unit", "mm");
        meta.put("thickness", "5.0");
        meta.put("version", "2.0");
        return meta;
    }
    
    public Map<String, Point2D> calculatePreviewPoints(Map<String, Double> paramValues) {
        Map<String, Point2D> points = new LinkedHashMap<>();
        
        double trimLeft = paramValues.getOrDefault("trimLeft", 0.0);
        double trimBottom = paramValues.getOrDefault("trimBottom", 0.0);
        double L = paramValues.getOrDefault("L", 1000.0);
        double H = paramValues.getOrDefault("H", 800.0);
        double R1 = paramValues.getOrDefault("R1", 50.0);
        
        // Calculate points
        Point2D p0 = new Point2D.Double(trimLeft, trimBottom);
        points.put("p0", p0);
        
        Point2D p1 = new Point2D.Double(p0.getX(), p0.getY() - H);
        points.put("p1", p1);
        
        Point2D p2 = new Point2D.Double(p0.getX() + L, p1.getY());
        points.put("p2", p2);
        
        return points;
    }
    
    // ═══ Parameter info class ═══
    
    public static class ParamInfo {
        public final String name;
        public final String type;
        public final double defaultValue;
        public final String description;
        
        public ParamInfo(String name, String type, double defaultValue, String description) {
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
            this.description = description;
        }
    }
}
```

### Key Components

#### 1. Parameter Information

Complete metadata for each parameter:
```java
ParamInfo info = preview.getParameters().get("L");
System.out.println("Name: " + info.name);
System.out.println("Type: " + info.type);
System.out.println("Default: " + info.defaultValue);
System.out.println("Description: " + info.description);
```

#### 2. Shape Metadata

Basic shape information:
```java
Map<String, String> meta = preview.getMetadata();
String unit = meta.get("unit");       // "mm"
String thickness = meta.get("thickness"); // "5.0"
```

#### 3. Preview Point Calculator

Calculate all shape points for given parameter values:
```java
Map<String, Double> params = new HashMap<>();
params.put("L", 1200.0);
params.put("H", 900.0);
params.put("R1", 75.0);

Map<String, Point2D> points = preview.calculatePreviewPoints(params);
Point2D p0 = points.get("p0");
Point2D p1 = points.get("p1");
```

### Usage in UI

```java
// Load preview (no manufacturing dependencies needed)
ShapePreview_ExampleShape preview = new ShapePreview_ExampleShape();

// Display parameter form
for (ParamInfo param : preview.getParameters().values()) {
    addSlider(param.name, param.defaultValue, param.description);
}

// User adjusts parameters
Map<String, Double> userValues = getUserInput();

// Calculate preview
Map<String, Point2D> points = preview.calculatePreviewPoints(userValues);

// Render preview
for (Point2D point : points.values()) {
    drawPoint(point.getX(), point.getY());
}
```

---

## Comparison: v1.0 vs v2.0

### Legacy v1.0 (Single File)

```
ShapeTransformer_XXX.java
├─ Manufacturing code
├─ Hardcoded coordinates
├─ No parameter inspection
└─ Tightly coupled to manufacturing framework

Output: 1 file
Use case: Static shapes only
```

### Modern v2.0 (Dual Output)

```
ShapeTransformer_XXX.java        ShapePreview_XXX.java
├─ Manufacturing code            ├─ Parameter definitions
├─ Parametric expressions        ├─ Metadata access
├─ Dynamic resizing              ├─ Preview calculator
└─ Manufacturing framework       └─ Zero dependencies

Output: 2 files
Use case: Dynamic parametric shapes
```

---

## When to Use Each File

### Use ShapeTransformer When...

✅ Generating CNC toolpaths  
✅ Calculating offsets for cutting  
✅ Manufacturing execution  
✅ Production environment  
✅ Need validated geometry with edges

### Use ShapePreview When...

✅ Building UI forms for parameter input  
✅ Rendering shape previews in browser/app  
✅ Inspecting available parameters  
✅ Development/debugging  
✅ Don't want manufacturing dependencies

---

## File Naming Convention

Both files share a common prefix based on the shape name:

**Shape name:** "RectangleWithCutout"

**Generated files:**
- `ShapeTransformer_RectangleWithCutout.java`
- `ShapePreview_RectangleWithCutout.java`

**Rules:**
- Spaces and special characters → underscores
- CamelCase preserved
- Consistent prefix identifies related files

---

## Dual Output Pipeline

### Generation Process

```
Parametric JSON (v2.0)
        │
        ▼
   ShapeLoader
        │
        ▼
 Format Detection
        │
        ▼
   v2.0 Detected
        │
        ├────────────────┬────────────────┐
        ▼                ▼                ▼
 ParametricCodeGen  ShapePreviewGen   (parallel)
        │                │
        ▼                ▼
ShapeTransformer    ShapePreview
   _XXX.java          _XXX.java
```

### Output Directory Structure

```
shapes/output/
├── ShapeTransformer_101.java
├── ShapePreview_101.java
├── ShapeTransformer_Rectangle.java
├── ShapePreview_Rectangle.java
└── ...
```

---

## Benefits

### 1. Separation of Concerns

Each file has a single, clear purpose:
- **ShapeTransformer** → Manufacturing execution
- **ShapePreview** → Visualization and inspection

### 2. Reduced Dependencies

UI code doesn't need manufacturing framework:
```java
// Before (v1.0): UI needs manufacturing imports
import com.manufacturing.Edge;
import com.manufacturing.ShapeTransformer;
import com.manufacturing.Param;

// After (v2.0): UI is standalone
import com.example.geometry.generated.ShapePreview_XXX;
```

### 3. Independent Evolution

Files can evolve independently:
- Manufacturing features don't affect preview
- UI improvements don't risk production code

### 4. Better Testing

Test each concern separately:
- Test manufacturing with production data
- Test preview with UI scenarios
- No cross-contamination

---

## Implementation Details

### Generator Classes

| Generator | Input | Output | Purpose |
|-----------|-------|--------|---------|
| `ParametricCodeGenerator` | ShapeDTO | ShapeTransformer | Manufacturing code |
| `ShapePreviewGenerator` | ShapeDTO | ShapePreview | Visualization code |

### Template System

Each generator uses a dedicated template:
- `ShapeTemplate` → Manufacturing file structure
- `ShapePreviewTemplate` → Preview file structure

### Generation Trigger

Both files generated automatically when:
1. v2.0 JSON detected (has `parametricEdges`)
2. Validation passes
3. Output folder is writable

---

## Backward Compatibility

### v1.0 Legacy Format

Still supported, generates only ShapeTransformer:

**Input:** v1.0 JSON (hardcoded edges)  
**Output:** `ShapeTransformer_XXX.java` (1 file)

**No preview file generated** because:
- No parameters to inspect
- No expressions to evaluate
- Hardcoded coordinates only

### Migration Path

To get dual output, convert shape to v2.0:
1. Add `parameters` array
2. Add `parametricEdges` array
3. Add `pointExpressions` object
4. Set `version` to "2.0"

Next generation will produce both files.

---

## Best Practices

### For Manufacturing Code (ShapeTransformer)

- ✅ Trust the generated code
- ✅ Don't manually edit
- ✅ Regenerate when JSON changes
- ✅ Use in production with confidence

### For Preview Code (ShapePreview)

- ✅ Use for UI parameter forms
- ✅ Use for preview rendering
- ✅ Cache instances for performance
- ✅ Don't use for actual manufacturing

### For Both

- ✅ Version control the JSON source
- ✅ Treat generated files as build artifacts
- ✅ Include in .gitignore if desired
- ✅ Regenerate on deployment

---

## Examples

### Example 1: Parameter Inspection

```java
ShapePreview_Rectangle preview = new ShapePreview_Rectangle();

// Get all parameters
for (ParamInfo param : preview.getParameters().values()) {
    System.out.println(param.name + ": " + param.description);
    System.out.println("  Type: " + param.type);
    System.out.println("  Default: " + param.defaultValue);
}

// Output:
// L: Overall width
//   Type: LINEAR
//   Default: 1000.0
// H: Overall height
//   Type: LINEAR
//   Default: 800.0
```

### Example 2: Preview Rendering

```java
ShapePreview_Rectangle preview = new ShapePreview_Rectangle();

// User adjusts sliders
Map<String, Double> params = new HashMap<>();
params.put("L", 1200.0);
params.put("H", 900.0);

// Calculate preview
Map<String, Point2D> points = preview.calculatePreviewPoints(params);

// Render
graphics.setColor(Color.BLUE);
Point2D prev = null;
for (Point2D point : points.values()) {
    if (prev != null) {
        graphics.drawLine(
            (int)prev.getX(), (int)prev.getY(),
            (int)point.getX(), (int)point.getY()
        );
    }
    prev = point;
}
```

### Example 3: Manufacturing Execution

```java
ShapeTransformer_Rectangle transformer = new ShapeTransformer_Rectangle();

// Production parameters
Param param = new Param(0.0, 0.0);
ParamList paramList = new ParamList();
paramList.put("L", 1000.0);
paramList.put("H", 800.0);

// Generate manufacturing edges
List<Edge> edges = transformer.resize(param, paramList);

// Send to CNC
for (Edge edge : edges) {
    if (edge instanceof LineEdge) {
        generateLinearToolpath((LineEdge)edge);
    } else if (edge instanceof ArcEdge) {
        generateArcToolpath((ArcEdge)edge);
    }
}
```

---

## Troubleshooting

### "Only ShapeTransformer generated, no ShapePreview"

**Cause:** JSON is v1.0 format  
**Fix:** Convert to v2.0 parametric format

### "Preview points are wrong"

**Cause:** Wrong parameter values passed  
**Fix:** Ensure all required parameters are in the map

### "Can't find ShapePreview class"

**Cause:** Not in classpath or not generated  
**Fix:** Verify v2.0 JSON was processed and files exist

---

## Summary

| Aspect | ShapeTransformer | ShapePreview |
|--------|------------------|--------------|
| **Purpose** | Manufacturing | Visualization |
| **Dependencies** | Manufacturing framework | None |
| **Generated for** | v1.0 and v2.0 | v2.0 only |
| **Contains** | Edge generation, resize() | Parameters, metadata, preview |
| **Used by** | CNC system | UI/frontend |
| **Updates when** | Shape changes | Shape changes |

---

## See Also

- [PARAMETRIC_FORMAT.md](PARAMETRIC_FORMAT.md) — v2.0 JSON format details
- [ARCHITECTURE.md](ARCHITECTURE.md) — System architecture
- [COMPLETE_GUIDE.md](COMPLETE_GUIDE.md) — Usage examples
- [README.md](README.md) — Project overview

---

**Feature:** Dual Output (ShapeTransformer + ShapePreview)  
**Status:** Production Ready ✅  
**Version:** 2.0.0  
**Last Updated:** March 3, 2026
