package com.company.gsap.loader;

import com.company.gsap.model.LineEdge;
import com.company.gsap.model.Shape;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 1 Step 2 Smoke Tests.
 * All 5 pass = ShapeLoader is solid. Move to GeometryValidator (Step 3).
 */
class ShapeLoaderTest {

    private final ShapeLoader loader = new ShapeLoader();

    private String testFilePath() throws URISyntaxException {
        URL resource = getClass().getClassLoader()
                .getResource("test-rectangle.json");
        assertNotNull(resource, "test-rectangle.json not found in resources");
        return Paths.get(resource.toURI()).toString();
    }

    @Test
    void loadsShapeName() throws Exception {
        Shape shape = loader.load(testFilePath());
        assertEquals("TestRectangle", shape.getName());
        System.out.println("✓ Name: " + shape.getName());
    }

    @Test
    void loadsCorrectEdgeCount() throws Exception {
        Shape shape = loader.load(testFilePath());
        assertEquals(4, shape.getEdgeCount());
        System.out.println("✓ Edge count: " + shape.getEdgeCount());
    }

    @Test
    void calculatesCorrectPerimeter() throws Exception {
        Shape shape = loader.load(testFilePath());
        assertEquals(360.0, shape.getPerimeter(), 1e-9);
        System.out.println("✓ Perimeter: " + shape.getPerimeter() + "mm");
    }

    @Test
    void allEdgesAreLineEdges() throws Exception {
        Shape shape = loader.load(testFilePath());
        shape.getEdges().forEach(e ->
                assertInstanceOf(LineEdge.class, e,
                        "Expected LineEdge but got: " + e.getClass().getSimpleName())
        );
        System.out.println("✓ All edges are LineEdge instances");
    }

    @Test
    void throwsOnMissingFile() {
        assertThrows(IOException.class, () ->
                loader.load("nonexistent/path/shape.json")
        );
        System.out.println("✓ IOException thrown for missing file");
    }
}
