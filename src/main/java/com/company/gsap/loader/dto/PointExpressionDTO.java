package com.company.gsap.loader.dto;

/**
 * Raw data holder for parametric point expressions.
 * Maps point ID → coordinate expressions.
 *
 * Rules:
 * - NO logic here
 * - NO validation here
 * - Field names must match JSON keys exactly
 */
public class PointExpressionDTO {
    
    public String x; // Expression string (e.g., "trimLeft", "p0.x + L", "p0.x + L - R1")
    public String y; // Expression string (e.g., "trimBottom", "p0.y - H")
}
