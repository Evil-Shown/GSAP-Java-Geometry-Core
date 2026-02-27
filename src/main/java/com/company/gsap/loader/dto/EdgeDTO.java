package com.company.gsap.loader.dto;

/**
 * Raw data holder for one edge from JSON.
 * Gson deserializes JSON directly into this.
 *
 * Rules:
 * - NO logic here
 * - NO validation here
 * - Fields named exactly as they appear in JSON
 * - Line fields (start, end) will be null for arcs
 * - Arc fields (center, radius, angles) will be null for lines
 */
public class EdgeDTO {

    public String id;
    public String type; // "line" or "arc"

    // ── Line fields ──────────────────────────
    public PointDTO start;
    public PointDTO end;

    // ── Arc fields ───────────────────────────
    public PointDTO center;
    public Double radius;
    public Double startAngle; // radians
    public Double endAngle;   // radians
    public Boolean clockwise;

    // ── Nested DTO for point coordinates ─────
    public static class PointDTO {
        public double x;
        public double y;
    }
}
