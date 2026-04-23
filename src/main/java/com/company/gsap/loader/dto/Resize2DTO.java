package com.company.gsap.loader.dto;

import java.util.List;
import java.util.Map;

/**
 * Raw data holder for resize2 generation data.
 * Contains all expressions needed to generate the resize2 method
 * (dimension lines, cutting edges, coating-removal edges).
 *
 * Field names must match JSON keys exactly.
 */
public class Resize2DTO {

    /** Extra point expressions needed for dimension lines (beyond the shape outline) */
    public Map<String, PointExpressionDTO> extraPointExpressions;

    /** Dimension line definitions */
    public List<DimensionLineDTO> dimensionLines;

    /** Service edge names used in cutting (e.g., ["E1", "E3", "E4", "E5", "E7"]) */
    public List<String> serviceEdges;

    /** Cutting edge point expressions (keyed by variable name, e.g., "cP0") */
    public Map<String, PointExpressionDTO> cuttingPointExpressions;

    /** Cutting edge sequence */
    public EdgeSequenceDTO cuttingEdgeSequence;

    /** Coating-removal service edge names (usually same as serviceEdges) */
    public List<String> coatingEdges;

    /** Coating-removal point expressions (keyed by variable name, e.g., "crP0") */
    public Map<String, PointExpressionDTO> coatingPointExpressions;

    /** Coating-removal edge sequence */
    public EdgeSequenceDTO coatingEdgeSequence;

    // ── Nested DTOs ──────────────────────────────────

    public static class DimensionLineDTO {
        /** If set, reference an existing point variable */
        public String startPoint;
        public String endPoint;
        /** If startPoint is null, use inline expressions */
        public String startX;
        public String startY;
        public String endX;
        public String endY;
        /** Dimension label constant (e.g., "L", "H", "R") */
        public String label;
    }

    public static class EdgeSequenceDTO {
        public String startPoint;
        public List<EdgeSequenceItemDTO> edges;
    }

    public static class EdgeSequenceItemDTO {
        public String type;         // "straight" or "arc"
        public String endPoint;     // Point variable name
        public String radiusExpr;   // For arcs: Java expression (e.g., "R1 + s4")
        public Boolean largeArcFlag;
        public Boolean sweepFlag;
    }
}
