package com.company.gsap.loader.dto;

import java.util.List;

/**
 * Raw data holder for parametric completeness metadata.
 * Reports which points/arcs are fully parametric vs. literal.
 *
 * Rules:
 * - NO logic here
 * - NO validation here
 * - Field names must match JSON keys exactly
 */
public class ParametricCompletenessDTO {
    
    public Boolean fullyParametric;
    public List<String> literalPoints;   // Point IDs with hardcoded values
    public List<String> unmatchedArcs;   // Arc IDs without matching radius parameters
}
