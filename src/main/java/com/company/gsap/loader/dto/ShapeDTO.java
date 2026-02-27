package com.company.gsap.loader.dto;

import java.util.List;

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
    public List<EdgeDTO> edges;
}
