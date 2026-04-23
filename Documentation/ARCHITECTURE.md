# GSAP Geometry Core v2.0 - Architecture Overview

**Version:** 2.0.0  
**Status:** Production Ready ✅  
**Last Updated:** March 3, 2026

```
╔════════════════════════════════════════════════════════════════════════════╗
║                         SHAPE EDITOR (JavaScript)                          ║
║                          ExportService.js v2.0                             ║
╚════════════════════════════════╦═══════════════════════════════════════════╝
                                 │
                                 ▼
                    ┌────────────────────────┐
                    │   Parametric JSON      │
                    │   (v2.0 Format)        │
                    │                        │
                    │ • parametricEdges      │
                    │ • pointExpressions     │
                    │ • parameters           │
                    │ • completeness         │
                    └────────────┬───────────┘
                                 │
                                 ▼
╔════════════════════════════════════════════════════════════════════════════╗
║                    GSAP GEOMETRY CORE (Java 17+)                           ║
╠════════════════════════════════════════════════════════════════════════════╣
║                                                                            ║
║  ┌──────────────────────────────────────────────────────────────────┐    ║
║  │ 1. ShapeLoader                                                    │    ║
║  │    • loadDTO() reads JSON                                         │    ║
║  │    • Parses into ShapeDTO                                         │    ║
║  │    • Detects format version                                       │    ║
║  └───────────────────────────┬──────────────────────────────────────┘    ║
║                              │                                             ║
║                              ▼                                             ║
║  ┌──────────────────────────────────────────────────────────────────┐    ║
║  │ 2. Format Detection                                               │    ║
║  │    if (parametricEdges exists) → v2.0                            │    ║
║  │    else → v1.0 legacy                                            │    ║
║  └──────────────┬──────────────────────────────┬────────────────────┘    ║
║                 │                               │                          ║
║      ┌──────────┴────────┐         ┌──────────┴────────┐                 ║
║      │    v1.0 Path      │         │    v2.0 Path      │                 ║
║      │  (Legacy)         │         │  (Parametric)     │                 ║
║      └──────────┬────────┘         └──────────┬────────┘                 ║
║                 │                              │                           ║
║                 ▼                              ▼                           ║
║  ┌──────────────────────┐      ┌──────────────────────────────────────┐  ║
║  │ CodeGenerator        │      │ ParametricCodeGenerator              │  ║
║  │ (v1.0)               │      │ (v2.0)                               │  ║
║  │                      │      │ • Evaluates expressions              │  ║
║  │ • Hardcoded coords   │      │ • Orders dependencies                │  ║
║  │ • 1 output file      │      │ • Generates parametric code          │  ║
║  └──────────┬───────────┘      └──────────┬───────────────────────────┘  ║
║             │                              │                               ║
║             │                              ├──────────────────┐            ║
║             │                              │                  │            ║
║             │                              ▼                  ▼            ║
║             │               ┌──────────────────┐  ┌──────────────────┐   ║
║             │               │ ShapeTransformer │  │ ShapePreview     │   ║
║             │               │ Generator        │  │ Generator        │   ║
║             │               └────────┬─────────┘  └────────┬─────────┘   ║
║             │                        │                     │              ║
║             ▼                        ▼                     ▼              ║
║  ┌──────────────────┐    ┌────────────────────┐ ┌──────────────────┐   ║
║  │ 1 File Output    │    │ 2 Files Output                          │   ║
║  │                  │    │                                          │   ║
║  │ ShapeTransformer │    │ ShapeTransformer   │ │ ShapePreview     │   ║
║  │ _XXX.java        │    │ _XXX.java          │ │ _XXX.java        │   ║
║  │                  │    │                     │ │                  │   ║
║  │ (hardcoded)      │    │ (parametric)       │ │ (metadata)       │   ║
║  └──────────────────┘    └────────────────────┘ └──────────────────┘   ║
║                                                                            ║
╚════════════════════════════════════════════════════════════════════════════╝
                 │                        │                   │
                 └────────────┬───────────┴───────────────────┘
                              │
                              ▼
        ╔═════════════════════════════════════════════════╗
        ║              OUTPUT DIRECTORY                    ║
        ║          shapes/output/                          ║
        ╠═════════════════════════════════════════════════╣
        ║                                                  ║
        ║  • ShapeTransformer_XXX.java                    ║
        ║    └─→ For manufacturing/CNC                    ║
        ║                                                  ║
        ║  • ShapePreview_XXX.java (v2.0 only)           ║
        ║    └─→ For visualization/UI                     ║
        ║                                                  ║
        ╚═════════════════════════════════════════════════╝
                 │                              │
     ┌───────────┴──────────┐      ┌──────────┴───────────┐
     ▼                      ▼      ▼                      ▼
┌─────────────┐    ┌──────────────────┐    ┌──────────────────┐
│ CNC Machine │    │ Manufacturing    │    │ UI / Frontend    │
│             │    │ Execution System │    │ Preview System   │
│             │    │                  │    │                  │
│ Uses:       │    │ Uses:            │    │ Uses:            │
│ Edges       │◄───│ ShapeTransformer │    │ ShapePreview     │
│             │    │ .resize()        │    │ .getParameters() │
│             │    │                  │    │ .getPreviewPts() │
└─────────────┘    └──────────────────┘    └──────────────────┘
```

---

## Component Responsibilities

### Shape Editor (JavaScript)
- Creates/edits shapes visually
- Auto-assigns parametric expressions
- Exports v2.0 JSON with full parametric data

### ShapeLoader (Java)
- Reads JSON files
- Parses into DTOs
- Detects v1.0 vs v2.0 format
- Routes to appropriate generator

### ParametricCodeGenerator
- Reads parametricEdges and pointExpressions
- Evaluates string expressions
- Orders points by dependency
- Generates ShapeTransformer with parametric code

### ShapePreviewGenerator (NEW)
- Extracts parameters with metadata
- Collects shape metadata
- Creates point calculator
- Generates ShapePreview class

### ShapeTransformer (Output)
- Extends base ShapeTransformer class
- Implements resize() method
- Uses Param and ParamList
- Returns List<Edge> for manufacturing

### ShapePreview (Output - NEW)
- Standalone class
- Provides parameter inspection
- Exposes metadata
- Calculates preview points
- No manufacturing dependencies

---

## Data Flow Summary

```
JSON → Loader → Format Detection
                     │
         ┌───────────┴───────────┐
         │                       │
      v1.0 Path              v2.0 Path
         │                       │
    CodeGen                 ParamCodeGen + PreviewGen
         │                       │              │
    1 File                  2 Files         2 Files
         │                       │              │
 ShapeTransformer      ShapeTransformer  ShapePreview
  (hardcoded)           (parametric)    (metadata)
         │                       │              │
         └───────────┬───────────┴──────────────┘
                     │
              Manufacturing / UI
```

---

## File Count by Version

| Version | Input Format | Output Files | Total Lines |
|---------|--------------|--------------|-------------|
| v1.0    | edges array  | 1            | ~150        |
| v2.0    | parametric   | 2            | ~400        |

---

## Key Innovation

**Before (v1.0):**
- 1 file with hardcoded coordinates
- No preview capability
- No parameter inspection

**After (v2.0):**
- 2 specialized files
- Manufacturing code is fully parametric
- Preview code provides metadata & inspection
- Clear separation of concerns

---

**Architecture:** Dual-output parametric geometry engine  
**Version:** 2.0.0  
**Status:** ✅ Production ready  
**Backward Compatible:** Yes (v1.0 still supported)  
**Last Updated:** March 3, 2026
