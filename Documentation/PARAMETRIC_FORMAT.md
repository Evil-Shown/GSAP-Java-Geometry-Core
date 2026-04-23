# Parametric Format Specification v2.0

**GSAP Geometry Core — Parametric JSON Format**  
**Version:** 2.0.0  
**Status:** Production Ready  
**Last Updated:** March 3, 2026

---

## Overview

The v2.0 parametric format enables fully parametric shape definitions where all coordinates are expressed as formulas rather than hardcoded values. This allows shapes to be dynamically resized while maintaining geometric relationships.

### Format Versions

| Version | Format | Output Files | Use Case |
|---------|--------|--------------|----------|
| v1.0 | Legacy hardcoded coordinates | 1 file (ShapeTransformer) | Simple static shapes |
| v2.0 | Parametric expressions | 2 files (ShapeTransformer + ShapePreview) | Dynamic resizable shapes |

---

## JSON Structure

### Complete Example

```json
{
  "name": "ParametricRectangle",
  "version": "2.0",
  "unit": "mm",
  "thickness": 5.0,
  "parameters": [
    {
      "name": "L",
      "type": "LINEAR",
      "defaultValue": 20000.0,
      "description": "Overall width"
    },
    {
      "name": "H",
      "type": "LINEAR",
      "defaultValue": 40000.0,
      "description": "Overall height"
    },
    {
      "name": "R1",
      "type": "RADIUS",
      "defaultValue": 10000.0,
      "description": "Corner radius"
    }
  ],
  "parametricEdges": [
    {
      "type": "line",
      "startPoint": "p0",
      "endPoint": "p1"
    },
    {
      "type": "line",
      "startPoint": "p1",
      "endPoint": "p2"
    },
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
  ],
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
    }
  },
  "parametricCompleteness": {
    "fullyParametric": true,
    "literalPoints": [],
    "unmatchedArcs": []
  }
}
```

---

## Field Specifications

### Root Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | String | Yes | Shape identifier (becomes class name) |
| `version` | String | Yes | Must be "2.0" for parametric format |
| `unit` | String | Yes | Measurement unit (typically "mm") |
| `thickness` | Number | Yes | Material thickness |
| `parameters` | Array | Yes | Shape parameters (dimensions) |
| `parametricEdges` | Array | Yes | Edge definitions using point references |
| `pointExpressions` | Object | Yes | Point coordinate expressions |
| `parametricCompleteness` | Object | No | Validation metadata |

---

## Parameters

Shape parameters define the resizable dimensions.

### Parameter Object

```json
{
  "name": "L",
  "type": "LINEAR",
  "defaultValue": 20000.0,
  "description": "Overall width"
}
```

| Field | Type | Values | Description |
|-------|------|--------|-------------|
| `name` | String | Any valid identifier | Parameter name (used in expressions) |
| `type` | String | `LINEAR`, `RADIUS`, `ANGLE` | Parameter type |
| `defaultValue` | Number | Any positive number | Default value in specified units |
| `description` | String | Any text | Human-readable description |

### Parameter Types

- **LINEAR**: Length/distance measurements (e.g., width, height)
- **RADIUS**: Arc radii
- **ANGLE**: Angular measurements (radians)

---

## Parametric Edges

Edges define the shape boundary using point references instead of hardcoded coordinates.

### Line Edge

```json
{
  "type": "line",
  "startPoint": "p0",
  "endPoint": "p1"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `type` | String | Always "line" |
| `startPoint` | String | Point reference (e.g., "p0") |
| `endPoint` | String | Point reference (e.g., "p1") |

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

| Field | Type | Description |
|-------|------|-------------|
| `type` | String | Always "arc" |
| `startPoint` | String | Point reference |
| `endPoint` | String | Point reference |
| `radiusParam` | String | Parameter name for radius |
| `largeArc` | Boolean | True for arc > 180° |
| `sweep` | Boolean | True for clockwise, false for counter-clockwise |
| `centerExpression` | Object | Arc center coordinate expressions |

---

## Point Expressions

Point expressions define coordinates as formulas that can reference parameters and other points.

### Expression Syntax

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
    "y": "p0.y - H + R1"
  }
}
```

### Special Keywords

| Keyword | Description | Example |
|---------|-------------|---------|
| `trimLeft` | Left trim reference point | Base X coordinate |
| `trimBottom` | Bottom trim reference point | Base Y coordinate |
| `trimRight` | Right trim reference point | Alternative base X |
| `trimTop` | Top trim reference point | Alternative base Y |

### Expression Components

| Component | Example | Description |
|-----------|---------|-------------|
| Parameter reference | `L`, `H`, `R1` | Direct parameter value |
| Point reference | `p0.x`, `p0.y` | Coordinate of another point |
| Arithmetic | `+`, `-`, `*`, `/` | Mathematical operations |
| Combined | `p0.x + L - R1` | Complex expressions |

### Dependency Order

Points must be defined in dependency order:
- Points can only reference previously defined points
- `p0` typically uses trim keywords (no dependencies)
- `p1`, `p2`, etc. can reference `p0` and earlier points

**Valid:**
```json
"p0": { "x": "trimLeft", "y": "trimBottom" },
"p1": { "x": "p0.x + L", "y": "p0.y" }
```

**Invalid:**
```json
"p1": { "x": "p0.x + L", "y": "p0.y" },    // Error: p0 not yet defined
"p0": { "x": "trimLeft", "y": "trimBottom" }
```

---

## Parametric Completeness

Validation metadata indicating conversion quality from legacy format.

```json
"parametricCompleteness": {
  "fullyParametric": true,
  "literalPoints": [],
  "unmatchedArcs": []
}
```

| Field | Type | Description |
|-------|------|-------------|
| `fullyParametric` | Boolean | True if all coordinates are parametric |
| `literalPoints` | Array | Point IDs that still have hardcoded coordinates |
| `unmatchedArcs` | Array | Arc IDs that couldn't be parameterized |

---

## Generated Code Output

### v2.0 Output Files

For each v2.0 shape, the system generates **TWO** Java files:

#### 1. ShapeTransformer_XXX.java

**Purpose:** Manufacturing execution  
**Contains:**
- Parametric resize() method
- Point calculations using expressions
- Edge construction with proper offset support

**Example:**
```java
@Override
public List<Edge> resize(Param param, ParamList paramList) {
    // Point declarations with expressions
    Point2D p0 = new Point2D.Double(param.getTrimLeft(), param.getTrimBottom());
    Point2D p1 = new Point2D.Double(p0.getX(), p0.getY() - paramList.get("H"));
    Point2D p2 = new Point2D.Double(p0.getX() + paramList.get("L"), p1.getY());
    
    // Build edges
    List<Edge> edges = new EdgeBuilder()
        .startPoint(p0)
        .straightEdge(p1)
        .straightEdge(p2)
        .build();
    
    return edges;
}
```

#### 2. ShapePreview_XXX.java

**Purpose:** Visualization and metadata  
**Contains:**
- Parameter definitions with types and defaults
- Shape metadata (name, thickness, unit)
- Preview point calculator
- No manufacturing dependencies

**Example:**
```java
public class ShapePreview_Rectangle {
    private final Map<String, ParamInfo> parameters;
    
    public Map<String, ParamInfo> getParameters() { ... }
    public Map<String, String> getMetadata() { ... }
    public Map<String, Point2D> calculatePreviewPoints(Map<String, Double> paramValues) { ... }
}
```

---

## Expression Evaluation

### Evaluation Order

1. **Trim keywords** → Resolved from `Param` object
2. **Parameters** → Resolved from `ParamList`
3. **Point dependencies** → Resolved in dependency order
4. **Arithmetic** → Standard operator precedence

### Example Evaluation

Given:
```json
"parameters": [{ "name": "L", "defaultValue": 1000 }],
"pointExpressions": {
  "p0": { "x": "trimLeft", "y": "trimBottom" },
  "p1": { "x": "p0.x + L", "y": "p0.y - 500" }
}
```

With `trimLeft=0`, `trimBottom=0`, `L=1000`:

1. `p0.x = trimLeft = 0`
2. `p0.y = trimBottom = 0`
3. `p1.x = p0.x + L = 0 + 1000 = 1000`
4. `p1.y = p0.y - 500 = 0 - 500 = -500`

---

## Backward Compatibility

### v1.0 Legacy Format

The system still supports v1.0 format with hardcoded coordinates:

```json
{
  "name": "LegacyShape",
  "version": "1.0",
  "edges": [
    {
      "type": "line",
      "start": { "x": 0, "y": 0 },
      "end": { "x": 100, "y": 0 }
    }
  ]
}
```

**Detection:** System automatically detects format version:
- If `parametricEdges` field exists → v2.0 (parametric)
- If `edges` array with `start`/`end` → v1.0 (legacy)

**Output:**
- v1.0 generates only `ShapeTransformer_XXX.java` (1 file)
- v2.0 generates both `ShapeTransformer_XXX.java` and `ShapePreview_XXX.java` (2 files)

---

## Validation Rules

### Required Fields

- ✅ `version` must be "2.0"
- ✅ `parameters` array must not be empty
- ✅ `parametricEdges` array must not be empty
- ✅ `pointExpressions` must define all referenced points

### Point Expression Validation

- ✅ All point references must be defined before use
- ✅ Expressions must use valid operators (+, -, *, /)
- ✅ Parameter names must match declared parameters
- ✅ No circular dependencies

### Edge Validation

- ✅ Minimum 3 edges required
- ✅ Edges must form a closed shape
- ✅ Arc edges must specify valid radius parameter
- ✅ All point references must exist in `pointExpressions`

---

## Best Practices

### Naming Conventions

- **Points:** Use sequential numbering (p0, p1, p2, ...)
- **Parameters:** Use descriptive uppercase (L, H, R1, ANGLE1)
- **Descriptions:** Be specific ("Top left corner radius" not "Radius")

### Expression Guidelines

1. **Keep expressions simple** — Complex math makes debugging harder
2. **Use intermediate points** — Break complex calculations into steps
3. **Document non-obvious relationships** — Add comments in description fields
4. **Maintain dependency order** — Define points sequentially

### Parameter Organization

```json
"parameters": [
  // Primary dimensions first
  { "name": "L", "type": "LINEAR", ... },
  { "name": "H", "type": "LINEAR", ... },
  
  // Secondary features
  { "name": "R1", "type": "RADIUS", ... },
  { "name": "R2", "type": "RADIUS", ... }
]
```

---

## Common Patterns

### Rectangle with Rounded Corner

```json
"parameters": [
  { "name": "W", "type": "LINEAR", "defaultValue": 1000 },
  { "name": "H", "type": "LINEAR", "defaultValue": 800 },
  { "name": "R", "type": "RADIUS", "defaultValue": 50 }
],
"pointExpressions": {
  "p0": { "x": "trimLeft", "y": "trimBottom" },
  "p1": { "x": "p0.x", "y": "p0.y + H - R" },
  "p2": { "x": "p0.x + R", "y": "p0.y + H" },
  "p3": { "x": "p0.x + W", "y": "p0.y + H" },
  "p4": { "x": "p0.x + W", "y": "p0.y" }
}
```

### Symmetric Shape

```json
"pointExpressions": {
  "p0": { "x": "trimLeft + W / 2", "y": "trimBottom" },
  "p1": { "x": "p0.x - W / 2", "y": "p0.y + H" },
  "p2": { "x": "p0.x + W / 2", "y": "p0.y + H" }
}
```

---

## Troubleshooting

### Common Errors

**"Point pX not defined"**
- Cause: Point referenced before definition
- Fix: Reorder point expressions

**"Parameter Y not found"**
- Cause: Expression references undefined parameter
- Fix: Add parameter to `parameters` array or fix typo

**"Circular dependency detected"**
- Cause: Point A depends on B, B depends on A
- Fix: Break circular reference using intermediate point

**"Shape is not closed"**
- Cause: Last edge doesn't connect to first point
- Fix: Ensure edge chain forms closed loop

---

## Migration from v1.0 to v2.0

### Manual Conversion Steps

1. **Identify parameters:** Find dimensions that should be resizable
2. **Create parameters array:** Define each parameter with type and default
3. **Choose anchor point:** Typically bottom-left as `p0`
4. **Express coordinates:** Write formulas for each point
5. **Update edges:** Change from hardcoded start/end to point references
6. **Test:** Validate with different parameter values

### Example Migration

**Before (v1.0):**
```json
{
  "edges": [
    { "type": "line", "start": {"x": 0, "y": 0}, "end": {"x": 1000, "y": 0} }
  ]
}
```

**After (v2.0):**
```json
{
  "parameters": [
    { "name": "L", "type": "LINEAR", "defaultValue": 1000 }
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

---

## See Also

- [ARCHITECTURE.md](ARCHITECTURE.md) — System architecture overview
- [DUAL_OUTPUT.md](DUAL_OUTPUT.md) — Dual file generation details
- [COMPLETE_GUIDE.md](COMPLETE_GUIDE.md) — Usage examples
- [README.md](README.md) — Project overview

---

**Format Version:** 2.0.0  
**Status:** Production Ready ✅  
**Last Updated:** March 3, 2026
