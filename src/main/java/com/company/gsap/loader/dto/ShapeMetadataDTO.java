package com.company.gsap.loader.dto;

/**
 * Raw data holder for shape metadata (class name, package, etc.).
 * Field names must match JSON keys exactly.
 */
public class ShapeMetadataDTO {
    public String className;       // e.g., "ShapeTransformer_101"
    public String shapeNumber;     // e.g., "101"
    public String packageName;     // e.g., "com.core.shape.transformer.impl"
    public String trimBottomService; // e.g., "E1"
    public String trimLeftService;   // e.g., "E7"
}
