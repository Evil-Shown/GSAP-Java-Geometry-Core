package com.company.gsap.loader;

import com.company.gsap.loader.dto.EdgeDTO;
import com.company.gsap.loader.dto.ShapeDTO;
import com.company.gsap.model.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads a shape JSON file from disk and returns a Shape domain object.
 *
 * Supports both formats:
 *   - v1.0: Legacy format with edges array (for backward compatibility)
 *   - v2.0: Parametric format with parametricEdges + pointExpressions
 *
 * Flow:
 *   JSON file
 *   → Gson parses into ShapeDTO + EdgeDTOs
 *   → mapToShape() builds Shape
 *   → mapToEdge() builds LineEdge or ArcEdge per edge
 *   → real Shape returned, ready for validation
 */
public class ShapeLoader {

    private static final Gson GSON = new Gson();

    /**
     * Reads a JSON file and returns a built Shape object.
     * Automatically detects v1.0 vs v2.0 format.
     * 
     * Throws IOException if file cannot be read.
     * Throws IllegalArgumentException if JSON is structurally invalid.
     */
    public Shape load(String filePath) throws IOException {
        String json = Files.readString(Path.of(filePath));
        ShapeDTO dto = GSON.fromJson(json, ShapeDTO.class);
        return mapToShape(dto);
    }
    
    /**
     * Loads a ShapeDTO without converting to domain model.
     * Useful for parametric code generation that works directly with DTOs.
     */
    public ShapeDTO loadDTO(String filePath) throws IOException {
        String json = Files.readString(Path.of(filePath));
        return GSON.fromJson(json, ShapeDTO.class);
    }

    private Shape mapToShape(ShapeDTO dto) {
        Shape shape = new Shape(dto.name, dto.version, dto.thickness);
        
        // Detect format version
        boolean isParametric = dto.parametricEdges != null && !dto.parametricEdges.isEmpty();
        boolean isLegacy = dto.edges != null && !dto.edges.isEmpty();
        
        if (isParametric) {
            System.out.println("✓ Detected v2.0 parametric format with " + 
                             dto.parametricEdges.size() + " parametric edges");
            // For now, we still need legacy edges for Shape domain model
            // In future, Shape could be enhanced to hold parametric data
            if (!isLegacy) {
                System.out.println("⚠ Warning: v2.0 format without legacy edges. " +
                                 "Shape domain model requires edges array.");
            }
        } else if (isLegacy) {
            System.out.println("✓ Detected v1.0 legacy format with " + 
                             dto.edges.size() + " edges");
        } else {
            throw new IllegalArgumentException("No edges or parametricEdges found in JSON");
        }
        
        // Load legacy edges (required for current Shape model)
        if (isLegacy) {
            for (EdgeDTO e : dto.edges) {
                shape.addEdge(mapToEdge(e));
            }
        }
        
        return shape;
    }

    private Edge mapToEdge(EdgeDTO e) {
        if (e.type == null || e.type.isBlank()) {
            throw new IllegalArgumentException("Edge [" + e.id + "] has no type");
        }

        switch (e.type.toLowerCase()) {
            case "line":
                if (e.start == null || e.end == null) {
                    throw new IllegalArgumentException(
                            "LineEdge [" + e.id + "] missing start or end point");
                }
                return new LineEdge(e.id, toPoint(e.start), toPoint(e.end));

            case "arc":
                if (e.center == null || e.radius == null ||
                        e.startAngle == null || e.endAngle == null || e.clockwise == null) {
                    throw new IllegalArgumentException(
                            "ArcEdge [" + e.id + "] missing required arc fields");
                }
                return new ArcEdge(e.id, toPoint(e.center), e.radius,
                        e.startAngle, e.endAngle, e.clockwise);

            default:
                throw new IllegalArgumentException("Unknown edge type: " + e.type);
        }
    }

    private Point toPoint(EdgeDTO.PointDTO p) {
        return new Point(p.x, p.y);
    }
}

