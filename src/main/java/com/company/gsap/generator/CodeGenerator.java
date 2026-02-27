package com.company.gsap.generator;

import com.company.gsap.model.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Takes a validated Shape and produces a complete ShapeTransformer Java file.
 *
 * This is the core deliverable of the entire project.
 * One public method: generate(Shape) → GeneratorResult.
 */
public class CodeGenerator {

    private static final double EPSILON = 1e-6;
    private static final String INDENT = "        "; // 8 spaces (inside method body)

    private final ShapeTemplate template = new ShapeTemplate();

    /**
     * Generates a ShapeTransformer Java file from a validated Shape.
     *
     * @param shape a Shape that has already passed GeometryValidator
     * @return GeneratorResult containing the Java source or an error
     */
    public GeneratorResult generate(Shape shape) {
        try {
            String className = deriveClassName(shape.getName());
            String body = buildMethodBody(shape);
            String fullFile = template.buildFile(shape.getName(), className, body);
            return GeneratorResult.success(fullFile);
        } catch (Exception e) {
            return GeneratorResult.failure(e.getMessage());
        }
    }

    /**
     * Derives the generated class name from the shape name.
     * Replaces spaces and special characters with underscores,
     * prefixes with "ShapeTransformer_".
     */
    public String deriveClassName(String shapeName) {
        String sanitized = shapeName.replaceAll("[^a-zA-Z0-9_]", "_");
        return "ShapeTransformer_" + sanitized;
    }

    // ── Private helpers ─────────────────────────────────────────

    private String buildMethodBody(Shape shape) {
        StringBuilder body = new StringBuilder();
        List<Edge> edges = shape.getEdges();

        // Track declared points: coordinate key → variable name
        // Using LinkedHashMap to preserve declaration order
        Map<String, String> declaredPoints = new LinkedHashMap<>();
        int pointCounter = 0;

        // Map each edge to its start/end variable names
        String[] startVars = new String[edges.size()];
        String[] endVars = new String[edges.size()];

        // ── Pass 1: Collect all points, deduplicate, assign variable names ──
        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);

            // Start point
            String startKey = coordKey(edge.getStart());
            if (!declaredPoints.containsKey(startKey)) {
                String varName = "p" + pointCounter++;
                declaredPoints.put(startKey, varName);
            }
            startVars[i] = declaredPoints.get(startKey);

            // End point
            String endKey = coordKey(edge.getEnd());
            if (!declaredPoints.containsKey(endKey)) {
                String varName = "p" + pointCounter++;
                declaredPoints.put(endKey, varName);
            }
            endVars[i] = declaredPoints.get(endKey);
        }

        // ── Write point declarations ──
        body.append(INDENT).append("// Point declarations\n");
        for (Map.Entry<String, String> entry : declaredPoints.entrySet()) {
            String[] coords = entry.getKey().split(",");
            body.append(INDENT)
                .append("Point2D ").append(entry.getValue())
                .append(" = new Point2D.Double(")
                .append(coords[0]).append(", ").append(coords[1])
                .append(");\n");
        }

        // ── Write arc center declarations (if any) ──
        Map<String, String> centerVars = new LinkedHashMap<>();
        int centerCounter = 0;
        for (Edge edge : edges) {
            if (edge instanceof ArcEdge arc) {
                String centerKey = coordKey(arc.getCenter());
                if (!centerVars.containsKey(centerKey)) {
                    String varName = "center" + centerCounter++;
                    centerVars.put(centerKey, varName);
                }
            }
        }
        if (!centerVars.isEmpty()) {
            body.append("\n").append(INDENT).append("// Arc center points\n");
            for (Map.Entry<String, String> entry : centerVars.entrySet()) {
                String[] coords = entry.getKey().split(",");
                body.append(INDENT)
                    .append("Point2D ").append(entry.getValue())
                    .append(" = new Point2D.Double(")
                    .append(coords[0]).append(", ").append(coords[1])
                    .append(");\n");
            }
        }

        // ── Write EdgeBuilder chain ──
        body.append("\n").append(INDENT).append("// Build edges\n");
        body.append(INDENT).append("List<Edge> edges = new EdgeBuilder()\n");
        body.append(INDENT).append("        .startPoint(").append(startVars[0]).append(")\n");

        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            if (edge instanceof LineEdge) {
                body.append(INDENT).append("        .straightEdge(")
                    .append(endVars[i]).append(")");
            } else if (edge instanceof ArcEdge arc) {
                String centerKey = coordKey(arc.getCenter());
                String centerVar = centerVars.get(centerKey);
                body.append(INDENT).append("        .arcEdge(")
                    .append(centerVar).append(", ")
                    .append(formatDouble(arc.getRadius())).append(", ")
                    .append(formatDouble(arc.getStartAngle())).append(", ")
                    .append(formatDouble(arc.getEndAngle())).append(", ")
                    .append(arc.isClockwise()).append(")");
            }

            // Comment with edge ID
            body.append("  // ").append(edge.getId());

            if (i < edges.size() - 1) {
                body.append("\n");
            } else {
                body.append("\n");
                body.append(INDENT).append("        .build();\n");
            }
        }

        return body.toString();
    }

    /**
     * Creates a coordinate key for point deduplication.
     * Uses rounded values to handle floating-point epsilon.
     */
    private String coordKey(Point p) {
        // Round to 4 decimal places for deduplication
        double rx = Math.round(p.x * 10000.0) / 10000.0;
        double ry = Math.round(p.y * 10000.0) / 10000.0;
        return formatDouble(rx) + "," + formatDouble(ry);
    }

    private String formatDouble(double val) {
        // Remove unnecessary trailing zeros but keep at least one decimal
        if (val == Math.floor(val) && !Double.isInfinite(val)) {
            return String.format("%.1f", val);
        }
        return String.format("%.4f", val);
    }
}
