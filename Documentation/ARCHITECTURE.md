# GSAP Geometry Core v2.0 — Architecture overview

**Parametric format:** v2.0.0  
**Last updated:** May 4, 2026

This document describes the **shape JSON → loader → validation → Java generators** path (`com.company.gsap.loader`, `validation`, `generator`, `pipeline`). The same `ShapePipeline` is invoked from the **Redis/MySQL worker** (`com.company.gsap.worker`) in integrated deployments. The repository also includes a separate **geometry engine** module under `com.company.gsap.engine` (resolution, SVG, measurements) used by tests and downstream tooling.

```
  CLIENT / UPSTREAM (e.g. shape editor, API, or test fixtures)
  Shape JSON: v1.0 literal edges  OR  v2.0 parametricEdges + pointExpressions + …
                                    │
                                    ▼
╔════════════════════════════════════════════════════════════════════════════╗
║  ShapePipeline                                            (Java 17)      ║
║  Used from tests, custom code, or WorkerApplication → same orchestration  ║
╠════════════════════════════════════════════════════════════════════════════╣
║                                                                            ║
║   ┌────────────────────────────────────────────────────────────────────┐   ║
║   │ 1. ShapeLoader                                                     │   ║
║   │    load(path) / loadDTO(path) → domain Shape + ShapeDTO            │   ║
║   └───────────────────────────────────┬──────────────────────────────┘   ║
║                                       │                                   ║
║                                       ▼                                   ║
║   ┌────────────────────────────────────────────────────────────────────┐   ║
║   │ 2. GeometryValidator                                               │   ║
║   │    Closed polygon, edge gaps, min edge count, degenerate checks    │   ║
║   └───────────────────────────────────┬──────────────────────────────┘   ║
║                                       │                                   ║
║                        ┌──────────────┴──────────────┐                    ║
║                        │ ShapeDTO.parametricEdges    │                    ║
║                        │ present & non-empty?        │                    ║
║            ┌───────────┴───────────┐    ┌───────────┴───────────┐          ║
║            │ NO  → v1.0 legacy    │    │ YES → v2.0 parametric│          ║
║            └───────────┬──────────┘    └───────────┬──────────┘          ║
║                        │                          │                      ║
║                        ▼                          ▼                      ║
║            ┌─────────────────────┐   ┌─────────────────────────────┐   ║
║            │ CodeGenerator         │   │ ParametricCodeGenerator     │   ║
║            │ 1 output file         │   │ + ShapePreviewGenerator     │   ║
║            │ ShapeTransformer_*    │   │ 2 output files              │   ║
║            │ (coordinates literal  │   │ transformer + preview       │   ║
║            │  in generated Java)   │   │                             │   ║
║            └───────────┬───────────┘   └──────────────┬──────────────┘   ║
║                        │                            │                    ║
║                        └────────────┬───────────────┘                    ║
║                                     │                                    ║
║                                     ▼                                    ║
║            Write under OUTPUT_DIR (env; default ./shapes/output/)        ║
║            • com.company.gsap.generated.ShapeTransformer_<Name>          ║
║            • com.company.gsap.generated.preview.ShapePreview_<Name> (v2) ║
║                                                                            ║
╚════════════════════════════════════════════════════════════════════════════╝
        │                              │                      │
        │ List<Edge> / CNC             │ host linkage         │ inspection
        ▼                              ▼                      ▼
 ┌──────────────┐           ┌──────────────────┐    ┌────────────────────────┐
 │ CAM /        │           │ Manufacturing    │    │ ShapePreview_*         │
 │ fabrication  │◄──────────│ stack uses       │    │ getParameters()        │
 │              │           │ ShapeTransformer │    │ getPreviewPoints()     │
 │              │           │ .resize(Param,   │    │ calculatePoints(…)     │
 └──────────────┘           │  ParamList)      │    └────────────────────────┘
                             └──────────────────┘

  Integrated deployment:  Redis job → WorkerApplication → ShapePipeline → OUTPUT_DIR
```

---

## Component Responsibilities

### Client / shape editor (upstream)

- Produces shape JSON (v1.0 or v2.0) consumed by the loader.
- v2.0 adds parametric edges, point expressions, and parameters for dual-file generation.

### ShapeLoader (Java)

- Reads JSON from disk or paths supplied by the caller.
- Parses into `ShapeDTO` and domain `Shape`.
- Supports both legacy and parametric payloads.

### GeometryValidator (Java)

- Validates topology and numeric consistency before code generation.
- Invalid shapes do not reach the generators (pipeline fails fast after load).

### ParametricCodeGenerator

- Reads `parametricEdges` and `pointExpressions`.
- Evaluates string expressions and orders point dependencies.
- Emits `com.company.gsap.generated.ShapeTransformer_*` with parametric `resize()`.

### ShapePreviewGenerator

- Builds parameter and metadata maps for UI-oriented output.
- Emits `com.company.gsap.generated.preview.ShapePreview_*` with preview calculators.

### CodeGenerator (v1.0)

- Legacy path when parametric data is absent.
- Emits a single `ShapeTransformer_*` with literal coordinates.

### ShapeTransformer (generated)

- Extends the host `ShapeTransformer` base class.
- Implements `resize(Param, ParamList)` and returns `List<Edge>` for manufacturing.

### ShapePreview (generated)

- Standalone preview class under `com.company.gsap.generated.preview`.
- Exposes parameters, metadata, `getPreviewPoints()`, and `calculatePoints(...)`.
- No dependency on manufacturing edge construction for UI-only use.

### WorkerApplication (optional runtime)

- Consumes jobs from Redis and runs the same `ShapePipeline` with JDBC-backed state.
- Writes generated sources to `OUTPUT_DIR`.

---

## Data Flow Summary

```
JSON → Loader → Validator → branch on parametricEdges?
                                    │
                    ┌───────────────┴───────────────┐
                    │                               │
                 absent                         non-empty
                    │                               │
               CodeGenerator          ParametricCodeGenerator
               (1 file)             + ShapePreviewGenerator
                    │                      (2 files)
                    └───────────────┬───────────────┘
                                    │
                          OUTPUT_DIR (generated .java)
                                    │
                          manufacturing / UI consumers
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

**Summary:** Dual-output parametric geometry pipeline (v2.0) with v1.0 legacy support.  
**Backward compatible:** yes — v1.0 continues to emit a single `ShapeTransformer` class.
