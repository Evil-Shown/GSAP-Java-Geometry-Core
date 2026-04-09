package com.company.gsap.pipeline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for v2.0 parametric format processing.
 */
class ParametricPipelineTest {

    private final ShapePipeline pipeline = new ShapePipeline();

    @Test
    void testProcessParametricShape(@TempDir Path tempDir) throws IOException {
        // Create test parametric JSON
        String json = """
            {
              "name": "TestParametricShape",
              "version": "2.0",
              "unit": "mm",
              "thickness": 5,
              "parameters": [
                { "name": "L", "type": "LINEAR", "defaultValue": 100.0 },
                { "name": "H", "type": "LINEAR", "defaultValue": 200.0 }
              ],
              "parametricEdges": [
                { "type": "line", "startPoint": "p0", "endPoint": "p1" },
                { "type": "line", "startPoint": "p1", "endPoint": "p2" },
                { "type": "line", "startPoint": "p2", "endPoint": "p3" },
                { "type": "line", "startPoint": "p3", "endPoint": "p0" }
              ],
              "pointExpressions": {
                "p0": { "x": "trimLeft", "y": "trimBottom" },
                "p1": { "x": "p0.x", "y": "p0.y - H" },
                "p2": { "x": "p0.x + L", "y": "p0.y - H" },
                "p3": { "x": "p0.x + L", "y": "p0.y" }
              },
              "parametricCompleteness": {
                "fullyParametric": true,
                "literalPoints": [],
                "unmatchedArcs": []
              },
              "edges": [
                { "type": "line", "start": {"x": 0, "y": 0}, "end": {"x": 0, "y": -200}, "id": "e1" },
                { "type": "line", "start": {"x": 0, "y": -200}, "end": {"x": 100, "y": -200}, "id": "e2" },
                { "type": "line", "start": {"x": 100, "y": -200}, "end": {"x": 100, "y": 0}, "id": "e3" },
                { "type": "line", "start": {"x": 100, "y": 0}, "end": {"x": 0, "y": 0}, "id": "e4" }
              ]
            }
            """;

        Path inputFile = tempDir.resolve("test-shape.json");
        Files.writeString(inputFile, json);

        // Process and generate - should return TWO files now
        List<Path> outputFiles = pipeline.processAndGenerate(
            inputFile.toString(),
            tempDir.toString()
        );

        // Verify TWO output files were created
        assertEquals(2, outputFiles.size(), "Should generate 2 files for v2.0 format");

        // Find the ShapeTransformer and ShapePreview files
        Path transformerFile = outputFiles.stream()
            .filter(f -> f.getFileName().toString().startsWith("ShapeTransformer_"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("ShapeTransformer file not found"));

        Path previewFile = outputFiles.stream()
            .filter(f -> f.getFileName().toString().startsWith("ShapePreview_"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("ShapePreview file not found"));

        // Verify ShapeTransformer
        assertTrue(Files.exists(transformerFile), "ShapeTransformer file should exist");
        assertEquals("ShapeTransformer_TestParametricShape.java", 
                    transformerFile.getFileName().toString());

        String transformerCode = Files.readString(transformerFile);
        assertTrue(transformerCode.contains("package com.company.gsap.generated;"));
        assertTrue(transformerCode.contains("public class ShapeTransformer_TestParametricShape"));
        assertTrue(transformerCode.contains("param.getTrimLeft()"));
        assertTrue(transformerCode.contains("paramList.get(\"L\")"));

        // Verify ShapePreview
        assertTrue(Files.exists(previewFile), "ShapePreview file should exist");
        assertEquals("ShapePreview_TestParametricShape.java", 
                    previewFile.getFileName().toString());

        String previewCode = Files.readString(previewFile);
        assertTrue(previewCode.contains("package com.company.gsap.generated.preview;"));
        assertTrue(previewCode.contains("public class ShapePreview_TestParametricShape"));
        assertTrue(previewCode.contains("getParameters()"));
        assertTrue(previewCode.contains("getMetadata()"));
        assertTrue(previewCode.contains("getPreviewPoints()"));

        System.out.println("✓ Parametric pipeline test passed");
        System.out.println("Generated files:");
        System.out.println("  - " + transformerFile.getFileName());
        System.out.println("  - " + previewFile.getFileName());
    }

    @Test
    void testProcessLegacyShapeStillWorks(@TempDir Path tempDir) throws IOException {
        // Create legacy v1.0 JSON (no parametric fields)
        String json = """
            {
              "name": "TestLegacyShape",
              "version": "1.0",
              "unit": "mm",
              "thickness": 5,
              "edges": [
                { "type": "line", "start": {"x": 0, "y": 0}, "end": {"x": 100, "y": 0}, "id": "e1" },
                { "type": "line", "start": {"x": 100, "y": 0}, "end": {"x": 100, "y": 100}, "id": "e2" },
                { "type": "line", "start": {"x": 100, "y": 100}, "end": {"x": 0, "y": 100}, "id": "e3" },
                { "type": "line", "start": {"x": 0, "y": 100}, "end": {"x": 0, "y": 0}, "id": "e4" }
              ]
            }
            """;

        Path inputFile = tempDir.resolve("test-legacy.json");
        Files.writeString(inputFile, json);

        // Process and generate (should use legacy generator, returns 1 file)
        List<Path> outputFiles = pipeline.processAndGenerate(
            inputFile.toString(),
            tempDir.toString()
        );

        // Verify ONE output file was created for legacy format
        assertEquals(1, outputFiles.size(), "Should generate 1 file for v1.0 format");
        Path outputFile = outputFiles.get(0);
        
        assertTrue(Files.exists(outputFile), "Output file should exist");
        
        String code = Files.readString(outputFile);
        assertTrue(code.contains("package com.company.gsap.generated;"));
        assertTrue(code.contains("public class ShapeTransformer_TestLegacyShape"));

        System.out.println("✓ Legacy pipeline test passed (backward compatibility maintained)");
    }
}
