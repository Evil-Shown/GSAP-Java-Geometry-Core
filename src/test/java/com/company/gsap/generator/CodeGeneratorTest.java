package com.company.gsap.generator;

import com.company.gsap.model.*;
import com.company.gsap.pipeline.ShapePipeline;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 3 Step 2 — CodeGenerator Tests.
 * All 4 pass = generator produces valid ShapeTransformer files.
 */
class CodeGeneratorTest {

    private final CodeGenerator generator = new CodeGenerator();

    private Shape buildRectangle() {
        Shape shape = new Shape("TestRectangle", "1.0", 5.0);
        shape.addEdge(new LineEdge("L1", new Point(0, 0), new Point(100, 0)));
        shape.addEdge(new LineEdge("L2", new Point(100, 0), new Point(100, 80)));
        shape.addEdge(new LineEdge("L3", new Point(100, 80), new Point(0, 80)));
        shape.addEdge(new LineEdge("L4", new Point(0, 80), new Point(0, 0)));
        return shape;
    }

    private String resourcePath(String filename) throws Exception {
        URL resource = getClass().getClassLoader().getResource(filename);
        assertNotNull(resource, filename + " not found in resources");
        return Paths.get(resource.toURI()).toString();
    }

    // ── Test 1: Rectangle generates valid code ──────────────────

    @Test
    void rectangleGeneratesValidCode() {
        GeneratorResult result = generator.generate(buildRectangle());

        assertTrue(result.isSuccess());
        String code = result.getCode();
        assertTrue(code.contains("ShapeTransformer_TestRectangle"),
                "Should contain class name");
        assertTrue(code.contains("Point2D"),
                "Should contain Point2D declarations");
        assertTrue(code.contains("EdgeBuilder"),
                "Should contain EdgeBuilder chain");
        assertTrue(code.contains("straightEdge"),
                "Should contain straightEdge calls for lines");

        System.out.println("✓ Rectangle generates valid code");
        System.out.println("--- Generated output (first 600 chars) ---");
        System.out.println(code.substring(0, Math.min(600, code.length())));
    }

    // ── Test 2: Exactly 4 unique point declarations ─────────────

    @Test
    void rectangleHasExactlyFourPointDeclarations() {
        GeneratorResult result = generator.generate(buildRectangle());
        assertTrue(result.isSuccess());

        String code = result.getCode();

        // Count "Point2D p" declarations (p0, p1, p2, p3)
        long pointCount = code.lines()
                .filter(line -> line.trim().startsWith("Point2D p"))
                .count();

        assertEquals(4, pointCount,
                "Rectangle with 4 edges sharing corners should have exactly 4 unique points");

        // Verify deduplication: p0 should be reused for the closing edge
        assertTrue(code.contains("p0"), "First point p0 should exist");
        assertTrue(code.contains("p3"), "Fourth point p3 should exist");

        System.out.println("✓ Exactly 4 deduplicated point declarations");
    }

    // ── Test 3: Mixed line and arc shape ────────────────────────

    @Test
    void mixedShapeContainsArcContent() {
        Shape shape = new Shape("ArchWindow", "1.0", 6.0);
        // Bottom
        shape.addEdge(new LineEdge("L1", new Point(0, 0), new Point(100, 0)));
        // Right side
        shape.addEdge(new LineEdge("L2", new Point(100, 0), new Point(100, 80)));
        // Top arc
        shape.addEdge(new ArcEdge("A1", new Point(50, 80), 50.0,
                0.0, Math.PI, false));
        // Left side
        shape.addEdge(new LineEdge("L3", new Point(0, 80), new Point(0, 0)));

        GeneratorResult result = generator.generate(shape);
        assertTrue(result.isSuccess());

        String code = result.getCode();
        assertTrue(code.contains("arcEdge"), "Should contain arcEdge call");
        assertTrue(code.contains("center"), "Should contain center point declaration");
        assertTrue(code.contains("ShapeTransformer_ArchWindow"), "Should contain class name");

        System.out.println("✓ Mixed shape with arcs generates correctly");
    }

    // ── Test 4: Full pipeline writes file to disk ───────────────

    @Test
    void processAndGenerateWritesFile(@TempDir Path tempDir) throws Exception {
        ShapePipeline pipeline = new ShapePipeline();
        String jsonPath = resourcePath("test-rectangle.json");

        List<Path> outputFiles = pipeline.processAndGenerate(jsonPath, tempDir.toString());

        assertFalse(outputFiles.isEmpty(), "Generated .java files should exist");
        Path outputFile = outputFiles.get(0);
        assertTrue(Files.exists(outputFile), "Generated .java file should exist on disk");
        assertEquals("ShapeTransformer_TestRectangle.java", outputFile.getFileName().toString());

        String content = Files.readString(outputFile);
        assertTrue(content.contains("class ShapeTransformer_TestRectangle"));
        assertTrue(content.length() > 100, "Generated file should have substantial content");

        System.out.println("✓ Full pipeline wrote file: " + outputFile.getFileName());
        System.out.println("  File size: " + content.length() + " chars");
    }
}
