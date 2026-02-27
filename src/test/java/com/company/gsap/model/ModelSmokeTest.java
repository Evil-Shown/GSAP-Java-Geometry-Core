package com.company.gsap.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 1 Step 1 Smoke Tests.
 * All 5 pass = model layer is solid. Move to ShapeLoader (Step 2).
 */
class ModelSmokeTest {

    @Test
    void point_distanceAndCloseTo() {
        Point a = new Point(0, 0);
        Point b = new Point(3, 4);

        assertEquals(5.0, a.distanceTo(b), 1e-9, "3-4-5 triangle");
        assertTrue(a.isCloseTo(new Point(0.000001, 0), 1e-4));
        assertFalse(a.isCloseTo(b, 1e-4));

        System.out.println("✓ Point: " + a + " distance to " + b + " = 5.0");
    }

    @Test
    void lineEdge_lengthAndValidate() {
        LineEdge e = new LineEdge("L1", new Point(0, 0), new Point(100, 0));

        assertEquals(100.0, e.length(), 1e-9);
        assertDoesNotThrow(e::validate);

        System.out.println("✓ LineEdge: " + e);
    }

    @Test
    void lineEdge_zeroLength_failsValidation() {
        LineEdge bad = new LineEdge("L_ZERO", new Point(5, 5), new Point(5, 5));

        assertThrows(IllegalStateException.class, bad::validate);
        System.out.println("✓ Zero-length LineEdge correctly rejected");
    }

    @Test
    void arcEdge_quarterCircle() {
        // Quarter circle: center(0,0), r=50, 0 -> PI/2 counter-clockwise
        ArcEdge arc = new ArcEdge("A1",
                new Point(0, 0), 50, 0, Math.PI / 2, false);

        // Arc length = r * theta = 50 * PI/2 ≈ 78.54
        assertEquals(50 * Math.PI / 2, arc.length(), 1e-6);

        // Start point should be (50, 0)
        assertEquals(50.0, arc.getStart().x, 1e-6);
        assertEquals(0.0, arc.getStart().y, 1e-6);

        // End point should be (0, 50)
        assertEquals(0.0, arc.getEnd().x, 1e-6);
        assertEquals(50.0, arc.getEnd().y, 1e-6);

        assertDoesNotThrow(arc::validate);
        System.out.println("✓ ArcEdge: " + arc);
    }

    @Test
    void shape_rectangle_perimeterAndBbox() {
        Shape shape = new Shape("TestRect", "1.0", 5.0);
        shape.addEdge(new LineEdge("L1", new Point(0, 0), new Point(100, 0)));
        shape.addEdge(new LineEdge("L2", new Point(100, 0), new Point(100, 80)));
        shape.addEdge(new LineEdge("L3", new Point(100, 80), new Point(0, 80)));
        shape.addEdge(new LineEdge("L4", new Point(0, 80), new Point(0, 0)));

        // 100 + 80 + 100 + 80 = 360
        assertEquals(360.0, shape.getPerimeter(), 1e-9);
        assertEquals(4, shape.getEdgeCount());

        double[] bb = shape.getBoundingBox();
        assertEquals(0.0, bb[0], 1e-9, "minX");
        assertEquals(0.0, bb[1], 1e-9, "minY");
        assertEquals(100.0, bb[2], 1e-9, "maxX");
        assertEquals(80.0, bb[3], 1e-9, "maxY");

        assertDoesNotThrow(shape::validateEdges);
        System.out.println("✓ Shape: " + shape);
    }
}
