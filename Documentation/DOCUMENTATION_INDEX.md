# GSAP Geometry Core v2.0 — Documentation Index

**Last updated:** May 4, 2026  
**Parametric / dual-output format:** v2.0.0 (see [PARAMETRIC_FORMAT.md](PARAMETRIC_FORMAT.md))

The repository root [README.md](../README.md) is the primary entry point. This folder holds the technical specifications below.

---

## Available documentation

| Document | Description | Time to read |
|----------|-------------|--------------|
| [README.md](../README.md) | Project overview, build, worker runtime, scope | 5 min |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Pipeline architecture, data flow, components | 15 min |
| [PARAMETRIC_FORMAT.md](PARAMETRIC_FORMAT.md) | v2.0 JSON format specification with examples | 10 min |
| [DUAL_OUTPUT.md](DUAL_OUTPUT.md) | ShapeTransformer + ShapePreview generation | 10 min |
| [QUICK_REFERENCE.md](QUICK_REFERENCE.md) | Commands, expressions, API cheatsheet | 5 min |
| [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md) | This navigation file | 2 min |

---

## How to use this documentation

### I want to…

| Need | Go to |
|------|-------|
| Get started (build, worker) | [README.md — Build and Test](../README.md#build-and-test) |
| Understand the v2.0 JSON format | [PARAMETRIC_FORMAT.md](PARAMETRIC_FORMAT.md) |
| Find a quick command or API | [QUICK_REFERENCE.md](QUICK_REFERENCE.md) |
| Understand the generator pipeline | [ARCHITECTURE.md](ARCHITECTURE.md) |
| Learn about dual output | [DUAL_OUTPUT.md](DUAL_OUTPUT.md) |
| See API examples | [QUICK_REFERENCE.md — API quick reference](QUICK_REFERENCE.md#api-quick-reference) |
| Write a new shape JSON | [PARAMETRIC_FORMAT.md — Common patterns](PARAMETRIC_FORMAT.md#common-patterns) |
| Debug a shape issue | [QUICK_REFERENCE.md — Troubleshooting](QUICK_REFERENCE.md#troubleshooting) |

---

## Documentation by category

### Getting started

**[README.md](../README.md)**
- Project overview, scope, and repository layout
- Build, test, and packaging (`mvn clean test`, fat JAR)
- **Worker runtime** (`mvn exec:java` → `WorkerApplication`, Redis + MySQL)
- Core components (model, loader, validation, pipeline, generators, worker)
- JSON format examples (v1.0 and v2.0) and usage snippets
- Roadmap and troubleshooting

### Quick lookup

**[QUICK_REFERENCE.md](QUICK_REFERENCE.md)**
- Maven commands (build, test, worker entrypoint)
- JSON format templates (v1.0 and v2.0)
- Expression syntax table (keywords, operators, point references)
- Parameter type reference
- Edge type reference (line, arc)
- All validation rules
- API quick reference (ShapeLoader, Validator, Generators, Pipeline)
- Common task recipes
- Troubleshooting table

### Technical specification

**[PARAMETRIC_FORMAT.md](PARAMETRIC_FORMAT.md)**
- v1.0 vs v2.0 format comparison
- Complete JSON schema with all fields
- Parameters: types (LINEAR, RADIUS, ANGLE), fields
- Parametric edges: line and arc definitions
- Point expressions: syntax, keywords, operators
- Parametric completeness metadata
- Generated code output examples
- Expression evaluation order
- Backward compatibility rules
- Validation rules
- Common patterns (rectangle, rounded corner, symmetric)
- Migration guide (v1.0 → v2.0)

**[DUAL_OUTPUT.md](DUAL_OUTPUT.md)**
- Why dual output was introduced
- ShapeTransformer_XXX.java structure and purpose
- ShapePreview_XXX.java structure and purpose
- v1.0 vs v2.0 output comparison
- When to use each file
- File naming conventions
- Dual output pipeline diagram
- Manufacturing integration example
- UI integration example
- Benefits of separation of concerns

**[ARCHITECTURE.md](ARCHITECTURE.md)**
- Full ASCII architecture diagram
- Component responsibilities
  - Shape Editor (JavaScript)
  - ShapeLoader (Java)
  - ParametricCodeGenerator
  - ShapePreviewGenerator
  - ShapeTransformer (output)
  - ShapePreview (output)
- Data flow summary diagram
- File count by version (v1.0 vs v2.0)
- Key innovation before/after

---

## Recommended reading order

### New developer
1. [README.md](../README.md) — project purpose and how to build (5 min)
2. [PARAMETRIC_FORMAT.md](PARAMETRIC_FORMAT.md) — JSON contract (10 min)
3. [DUAL_OUTPUT.md](DUAL_OUTPUT.md) — generated artifacts (10 min)
4. [QUICK_REFERENCE.md](QUICK_REFERENCE.md) — commands and APIs (5 min)
5. `mvn clean test` — current suite is **42** tests (see build output)

### Architect / tech lead
1. [ARCHITECTURE.md](ARCHITECTURE.md)
2. [DUAL_OUTPUT.md](DUAL_OUTPUT.md)
3. [PARAMETRIC_FORMAT.md](PARAMETRIC_FORMAT.md)

### Integration developer
1. [DUAL_OUTPUT.md](DUAL_OUTPUT.md)
2. [QUICK_REFERENCE.md — API quick reference](QUICK_REFERENCE.md#api-quick-reference)
3. [PARAMETRIC_FORMAT.md — Generated code output](PARAMETRIC_FORMAT.md#generated-code-output)

---

## Finding specific information

### Commands and shell
→ [QUICK_REFERENCE.md — Quick start commands](QUICK_REFERENCE.md#quick-start-commands)

### JSON format
→ [PARAMETRIC_FORMAT.md — Field specifications](PARAMETRIC_FORMAT.md#field-specifications)

### Expression syntax
→ [QUICK_REFERENCE.md — Expression syntax](QUICK_REFERENCE.md#expression-syntax)  
→ [PARAMETRIC_FORMAT.md — Point expressions](PARAMETRIC_FORMAT.md#point-expressions)

### Generated Java code
→ [DUAL_OUTPUT.md — ShapeTransformer](DUAL_OUTPUT.md#file-1-shapetransformerxxxjava)  
→ [DUAL_OUTPUT.md — ShapePreview](DUAL_OUTPUT.md#file-2-shapepreviewxxxjava)

### Architecture
→ [ARCHITECTURE.md — Component responsibilities](ARCHITECTURE.md#component-responsibilities)  
→ [ARCHITECTURE.md — Data flow summary](ARCHITECTURE.md#data-flow-summary)

### Validation rules
→ [QUICK_REFERENCE.md — Validation rules](QUICK_REFERENCE.md#validation-rules)  
→ [PARAMETRIC_FORMAT.md — Validation rules](PARAMETRIC_FORMAT.md#validation-rules)

### Troubleshooting
→ [QUICK_REFERENCE.md — Troubleshooting](QUICK_REFERENCE.md#troubleshooting)  
→ [README.md — Troubleshooting](../README.md#troubleshooting)

---

## Build verification

```cmd
java -version
mvn clean test
```

Expected: `Tests run: 42`, `BUILD SUCCESS`, `Failures: 0`.

To run the **production worker** (requires Redis, MySQL, and env vars — see [README.md](../README.md)):

```cmd
mvn -q exec:java
```

---

## Cross-reference map

| Topic | Primary doc | Secondary doc |
|-------|-------------|---------------|
| Project overview | README | ARCHITECTURE |
| JSON format | PARAMETRIC_FORMAT | QUICK_REFERENCE |
| Code generation | DUAL_OUTPUT | ARCHITECTURE |
| Commands | QUICK_REFERENCE | README |
| Expressions | PARAMETRIC_FORMAT | QUICK_REFERENCE |
| Validation | QUICK_REFERENCE | PARAMETRIC_FORMAT |
| Architecture | ARCHITECTURE | README |
| Integration | DUAL_OUTPUT | QUICK_REFERENCE |

**Start here:** [README.md](../README.md) · **Format:** [PARAMETRIC_FORMAT.md](PARAMETRIC_FORMAT.md) · **Commands:** [QUICK_REFERENCE.md](QUICK_REFERENCE.md)
