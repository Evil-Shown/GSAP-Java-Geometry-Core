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
     * Throws IOException if file cannot be read.
     * Throws IllegalArgumentException if JSON is structurally invalid.
     */
    public Shape load(String filePath) throws IOException {
        String json = Files.readString(Path.of(filePath));
        ShapeDTO dto = GSON.fromJson(json, ShapeDTO.class);
        return mapToShape(dto);
    }

    private Shape mapToShape(ShapeDTO dto) {
        Shape shape = new Shape(dto.name, dto.version, dto.thickness);
        for (EdgeDTO e : dto.edges) {
            shape.addEdge(mapToEdge(e));
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
