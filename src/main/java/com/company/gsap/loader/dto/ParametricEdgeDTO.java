package com.company.gsap.loader.dto;

/**
 * Raw data holder for parametric edge definitions (v2.0 format).
 * These edges reference point IDs and parameter names instead of literal coordinates.
 *
 * Rules:
 * - NO logic here
 * - NO validation here
 * - Field names must match JSON keys exactly
 */
public class ParametricEdgeDTO {

    public String type; // "line" or "arc"
    
    // ── Common fields ────────────────────────
    public String startPoint; // Point ID (e.g., "p0", "p1")
    public String endPoint;   // Point ID (e.g., "p1", "p2")
    
    // ── Arc-specific fields ──────────────────
    public String radiusParam;        // Parameter name (e.g., "R1")
    public Boolean largeArc;          // SVG arc flag
    public Boolean sweep;             // SVG arc sweep direction
    public CenterExpressionDTO centerExpression; // Computed center
    
    // ── Nested DTO for center expressions ────
    public static class CenterExpressionDTO {
        public String x; // e.g., "p0.x + L - R1"
        public String y; // e.g., "p0.y - R1"
    }
}
