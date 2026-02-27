package com.company.gsap.validation;

import com.company.gsap.loader.ShapeLoader;
import com.company.gsap.model.*;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 1 Step 3 Smoke Tests.
 * All 6 pass = GeometryValidator is solid.
 * Phase 1 complete. Move to Three.js Editor (Phase 2).
 */
class GeometryValidatorTest {

    private final GeometryValidator validator = new GeometryValidator();
    private final ShapeLoader loader = new ShapeLoader();

    private String resourcePath(String filename) throws Exception {
        URL resource = getClass().getClassLoader().getResource(filename);
        assertNotNull(resource, filename + " not found in resources");
        return Paths.get(resource.toURI()).toString();
    }

    @Test
    void validRectangle_isValid() throws Exception {
        Shape shape = loader.load(resourcePath("test-rectangle.json"));
        ValidationResult result = validator.validate(shape);
        assertTrue(result.isValid());
        System.out.println("✓ Valid rectangle passed: " + result);
    }

    @Test
    void validRectangle_hasNoErrors() throws Exception {
        Shape shape = loader.load(resourcePath("test-rectangle.json"));
        ValidationResult result = validator.validate(shape);
        assertTrue(result.getErrors().isEmpty());
        System.out.println("✓ No errors on valid shape");
    }

    @Test
    void openShape_isInvalid() throws Exception {
        Shape shape = loader.load(resourcePath("test-open-shape.json"));
        ValidationResult result = validator.validate(shape);
        assertFalse(result.isValid());
        System.out.println("✓ Open shape correctly rejected: " + result);
    }

    @Test
    void openShape_hasGapError() throws Exception {
        Shape shape = loader.load(resourcePath("test-open-shape.json"));
        ValidationResult result = validator.validate(shape);
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("Gap"));
        System.out.println("✓ Gap error reported: " + result.getErrors().get(0));
    }

    @Test
    void twoEdgeShape_isInvalid() {
        Shape shape = new Shape("TwoEdges", "1.0", 5.0);
        shape.addEdge(new LineEdge("L1", new Point(0, 0), new Point(100, 0)));
        shape.addEdge(new LineEdge("L2", new Point(100, 0), new Point(0, 0)));

        ValidationResult result = validator.validate(shape);
        assertFalse(result.isValid());
        System.out.println("✓ Two-edge shape rejected: " + result);
    }

    @Test
    void zeroLengthEdge_isInvalid() {
        Shape shape = new Shape("ZeroEdge", "1.0", 5.0);
        shape.addEdge(new LineEdge("L1", new Point(0, 0), new Point(100, 0)));
        shape.addEdge(new LineEdge("L2", new Point(100, 0), new Point(100, 80)));
        shape.addEdge(new LineEdge("L3", new Point(100, 80), new Point(100, 80))); // zero length
        shape.addEdge(new LineEdge("L4", new Point(100, 80), new Point(0, 0)));

        ValidationResult result = validator.validate(shape);
        assertFalse(result.isValid());
        System.out.println("✓ Zero-length edge rejected: " + result);
    }
}
