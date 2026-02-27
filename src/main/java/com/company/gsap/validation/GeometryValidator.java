package com.company.gsap.validation;

import com.company.gsap.model.Edge;
import com.company.gsap.model.Shape;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates that a Shape is geometrically sound before
 * it is passed to the OffsetEngine or Optimizer.
 *
 * Checks performed:
 * 1. Minimum edge count (>= 3)
 * 2. All edges validate individually (no zero-length)
 * 3. Edges connect end-to-start within epsilon tolerance
 * 4. Shape is closed (last edge end == first edge start)
 */
public class GeometryValidator {

    private static final double EPSILON = 1e-6;

    /**
     * Runs all checks on the shape.
     * Always collects ALL errors — never stops at the first.
     */
    public ValidationResult validate(Shape shape) {
        List<String> errors = new ArrayList<>();

        // Check 1: minimum edge count
        if (shape.getEdgeCount() < 3) {
            errors.add("Shape must have at least 3 edges, found: "
                    + shape.getEdgeCount());
            // No point checking connectivity with fewer than 3 edges
            return new ValidationResult(errors);
        }

        // Check 2: individual edge validation (zero-length etc.)
        checkIndividualEdges(shape, errors);

        // Check 3 + 4: connectivity between edges and closure
        checkConnectivity(shape, errors);

        return new ValidationResult(errors);
    }

    // ── Private helpers ──────────────────────────────────────────

    private void checkIndividualEdges(Shape shape, List<String> errors) {
        for (Edge edge : shape.getEdges()) {
            try {
                edge.validate();
            } catch (IllegalStateException ex) {
                errors.add("Edge [" + edge.getId() + "]: " + ex.getMessage());
            }
        }
    }

    private void checkConnectivity(Shape shape, List<String> errors) {
        List<Edge> edges = shape.getEdges();
        int count = edges.size();

        for (int i = 0; i < count; i++) {
            Edge current = edges.get(i);
            Edge next = edges.get((i + 1) % count); // wraps around to 0

            if (!current.getEnd().isCloseTo(next.getStart(), EPSILON)) {
                errors.add(String.format(
                        "Gap between edge [%s] end %s and edge [%s] start %s",
                        current.getId(), current.getEnd(),
                        next.getId(), next.getStart()
                ));
            }
        }
        // Note: the loop already checks last→first due to (i+1) % count
        // So closure is validated automatically — no separate check needed
    }
}
