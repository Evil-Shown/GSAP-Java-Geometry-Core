package com.company.gsap.pipeline;

import com.company.gsap.model.Shape;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 3 Step 1 — Integration Tests.
 * All 3 pass = pipeline wires loader + validator correctly.
 * Ready to build CodeGenerator (Step 2).
 */
class ShapePipelineTest {

    private final ShapePipeline pipeline = new ShapePipeline();

    private String resourcePath(String filename) throws Exception {
        URL resource = getClass().getClassLoader().getResource(filename);
        assertNotNull(resource, filename + " not found in resources");
        return Paths.get(resource.toURI()).toString();
    }

    @Test
    void validRectangle_returnsCorrectName() throws Exception {
        Shape shape = pipeline.process(resourcePath("test-rectangle.json"));
        assertNotNull(shape);
        assertEquals("TestRectangle", shape.getName());
        System.out.println("✓ Pipeline returned shape: " + shape.getName());
    }

    @Test
    void validRectangle_hasCorrectGeometry() throws Exception {
        Shape shape = pipeline.process(resourcePath("test-rectangle.json"));
        assertEquals(4, shape.getEdgeCount());
        assertEquals(360.0, shape.getPerimeter(), 1e-9);
        System.out.println("✓ Edges: " + shape.getEdgeCount()
                + ", Perimeter: " + shape.getPerimeter() + " mm");
    }

    @Test
    void openShape_throwsIllegalStateException() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                pipeline.process(resourcePath("test-open-shape.json"))
        );
        assertTrue(ex.getMessage().contains("validation failed"));
        assertTrue(ex.getMessage().contains("Gap"));
        System.out.println("✓ Open shape rejected: " + ex.getMessage());
    }
}
