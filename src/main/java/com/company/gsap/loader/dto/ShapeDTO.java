package com.company.gsap.loader.dto;

import java.util.List;
import java.util.Map;

/**
 * Raw data holder for the entire shape JSON file.
 * Gson deserializes the whole file into this single object.
 *
 * Rules:
 * - NO logic here
 * - NO validation here
 * - Field names must match JSON keys exactly
 */
public class ShapeDTO {

    public String name;
    public String version;
    public String unit; // "mm" expected
    public double thickness;
    
    // ── Legacy format (v1.0) ─────────────────────────
    public List<EdgeDTO> edges;
    
    // ── Parametric format (v2.0) ─────────────────────
    public List<ParametricEdgeDTO> parametricEdges;
    public Map<String, PointExpressionDTO> pointExpressions;
    public ParametricCompletenessDTO parametricCompleteness;
    public List<ParameterDTO> parameters; // Shape parameters (dimensions, radii, etc.)

    // ── Shape metadata (v2.0) ────────────────────────
    public ShapeMetadataDTO shapeMetadata;

    // ── Resize2 data (v2.0) ──────────────────────────
    public Resize2DTO resize2;

    // ── Area formula (v2.0) ──────────────────────────
    public String areaFormula; // e.g., "H * L - R1 * R1 * (1.0 - Math.PI / 4.0)"
    
    // ── Nested DTO for parameter definitions ─────────
    public static class ParameterDTO {
        public String name;           // e.g., "L", "H", "R1"
        public String type;           // e.g., "LINEAR", "RADIUS"
        public Double defaultValue;
        public String description;
    }
}
