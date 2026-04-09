package com.company.gsap.integration;

import com.company.gsap.pipeline.ShapePipeline;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test for v2.0 parametric format.
 * Verifies the complete flow from JSON to dual Java file output.
 */
class EndToEndIntegrationTest {

    private final ShapePipeline pipeline = new ShapePipeline();

    @Test
    void completeV2Flow_generatesBothFiles(@TempDir Path tempDir) throws IOException {
        // Create a complete v2.0 JSON with all features
        String v2Json = """
            {
              "name": "IntegrationTestShape",
              "version": "2.0",
              "unit": "mm",
              "thickness": 5.0,
              "parameters": [
                {
                  "name": "L",
                  "type": "LINEAR",
                  "defaultValue": 20000.0,
                  "description": "Overall width"
                },
                {
                  "name": "H",
                  "type": "LINEAR",
                  "defaultValue": 40000.0,
                  "description": "Overall height"
                },
                {
                  "name": "R1",
                  "type": "RADIUS",
                  "defaultValue": 10000.0,
                  "description": "Corner radius"
                }
              ],
              "parametricEdges": [
                {
                  "type": "line",
                  "startPoint": "p0",
                  "endPoint": "p1"
                },
                {
                  "type": "line",
                  "startPoint": "p1",
                  "endPoint": "p2"
                },
                {
                  "type": "line",
                  "startPoint": "p2",
                  "endPoint": "p3"
                },
                {
                  "type": "arc",
                  "startPoint": "p3",
                  "endPoint": "p4",
                  "radiusParam": "R1",
                  "largeArc": false,
                  "sweep": false,
                  "centerExpression": {
                    "x": "p0.x + L - R1",
                    "y": "p0.y - R1"
                  }
                },
                {
                  "type": "line",
                  "startPoint": "p4",
                  "endPoint": "p0"
                }
              ],
              "pointExpressions": {
                "p0": {
                  "x": "trimLeft",
                  "y": "trimBottom"
                },
                "p1": {
                  "x": "p0.x",
                  "y": "p0.y - H"
                },
                "p2": {
                  "x": "p0.x + L",
                  "y": "p0.y - H"
                },
                "p3": {
                  "x": "p0.x + L",
                  "y": "p0.y - R1"
                },
                "p4": {
                  "x": "p0.x + L - R1",
                  "y": "p0.y"
                }
              },
              "parametricCompleteness": {
                "fullyParametric": true,
                "literalPoints": [],
                "unmatchedArcs": []
              },
              "edges": [
                {
                  "type": "line",
                  "start": {"x": 0, "y": 5000},
                  "end": {"x": 0, "y": -35000},
                  "id": "edge_1"
                },
                {
                  "type": "line",
                  "start": {"x": 0, "y": -35000},
                  "end": {"x": 20000, "y": -35000},
                  "id": "edge_2"
                },
                {
                  "type": "line",
                  "start": {"x": 20000, "y": -35000},
                  "end": {"x": 20000, "y": -5000},
                  "id": "edge_3"
                },
                {
                  "type": "arc",
                  "center": {"x": 10000, "y": -5000},
                  "radius": 10000.0,
                  "startAngle": 0.0,
                  "endAngle": 1.5708,
                  "clockwise": false,
                  "id": "edge_4"
                },
                {
                  "type": "line",
                  "start": {"x": 10000, "y": 5000},
                  "end": {"x": 0, "y": 5000},
                  "id": "edge_5"
                }
              ]
            }
            """;

        // Write JSON file
        Path inputFile = tempDir.resolve("integration-test.json");
        Files.writeString(inputFile, v2Json);

        // Process through pipeline
        List<Path> outputFiles = pipeline.processAndGenerate(
            inputFile.toString(),
            tempDir.toString()
        );

        // Verify: Should generate TWO files
        assertNotNull(outputFiles, "Output files should not be null");
        assertEquals(2, outputFiles.size(), "Should generate exactly 2 files for v2.0");

        // Find each file type
        Path transformerFile = outputFiles.stream()
            .filter(f -> f.getFileName().toString().startsWith("ShapeTransformer_"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("ShapeTransformer file not found"));

        Path previewFile = outputFiles.stream()
            .filter(f -> f.getFileName().toString().startsWith("ShapePreview_"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("ShapePreview file not found"));

        // Verify file names
        assertEquals("ShapeTransformer_IntegrationTestShape.java", 
                    transformerFile.getFileName().toString());
        assertEquals("ShapePreview_IntegrationTestShape.java", 
                    previewFile.getFileName().toString());

        // Verify both files exist
        assertTrue(Files.exists(transformerFile), "ShapeTransformer should exist");
        assertTrue(Files.exists(previewFile), "ShapePreview should exist");

        // Read and verify ShapeTransformer content
        String transformerCode = Files.readString(transformerFile);
        assertAll("ShapeTransformer content checks",
            () -> assertTrue(transformerCode.contains("package com.company.gsap.generated;"), 
                           "Should have correct package"),
            () -> assertTrue(transformerCode.contains("public class ShapeTransformer_IntegrationTestShape"), 
                           "Should have correct class name"),
            () -> assertTrue(transformerCode.contains("param.getTrimLeft()"), 
                           "Should use trim service"),
            () -> assertTrue(transformerCode.contains("param.getTrimBottom()"), 
                           "Should use trim service"),
            () -> assertTrue(transformerCode.contains("paramList.get(\"L\")"), 
                           "Should reference L parameter"),
            () -> assertTrue(transformerCode.contains("paramList.get(\"H\")"), 
                           "Should reference H parameter"),
            () -> assertTrue(transformerCode.contains("paramList.get(\"R1\")"), 
                           "Should reference R1 parameter"),
            () -> assertTrue(transformerCode.contains("p0.getX()"), 
                           "Should reference point coordinates"),
            () -> assertTrue(transformerCode.contains("p0.getY()"), 
                           "Should reference point coordinates"),
            () -> assertTrue(transformerCode.contains(".straightEdge("), 
                           "Should have straightEdge calls"),
            () -> assertTrue(transformerCode.contains(".arcEdge("), 
                           "Should have arcEdge calls"),
            () -> assertTrue(transformerCode.contains("Point2D p0 ="), 
                           "Should declare points"),
            () -> assertTrue(transformerCode.contains("Point2D center"), 
                           "Should declare arc centers")
        );

        // Read and verify ShapePreview content
        String previewCode = Files.readString(previewFile);
        assertAll("ShapePreview content checks",
            () -> assertTrue(previewCode.contains("package com.company.gsap.generated.preview;"), 
                           "Should have preview package"),
            () -> assertTrue(previewCode.contains("public class ShapePreview_IntegrationTestShape"), 
                           "Should have correct class name"),
            () -> assertTrue(previewCode.contains("public Map<String, Parameter> getParameters()"), 
                           "Should have getParameters method"),
            () -> assertTrue(previewCode.contains("public Map<String, String> getMetadata()"), 
                           "Should have getMetadata method"),
            () -> assertTrue(previewCode.contains("public Map<String, Point2D> getPreviewPoints()"), 
                           "Should have getPreviewPoints method"),
            () -> assertTrue(previewCode.contains("public Map<String, Point2D> calculatePoints("), 
                           "Should have calculatePoints method"),
            () -> assertTrue(previewCode.contains("parameters.put(\"L\""), 
                           "Should initialize L parameter"),
            () -> assertTrue(previewCode.contains("parameters.put(\"H\""), 
                           "Should initialize H parameter"),
            () -> assertTrue(previewCode.contains("parameters.put(\"R1\""), 
                           "Should initialize R1 parameter"),
            () -> assertTrue(previewCode.contains("metadata.put(\"name\""), 
                           "Should have name metadata"),
            () -> assertTrue(previewCode.contains("metadata.put(\"thickness\""), 
                           "Should have thickness metadata"),
            () -> assertTrue(previewCode.contains("public static class Parameter"), 
                           "Should have Parameter inner class")
        );

        // Verify ShapeTransformer is parametric (no hardcoded coordinates)
        assertFalse(transformerCode.matches(".*new Point2D\\.Double\\(\\d+\\.\\d+, \\d+\\.\\d+\\).*"),
                   "ShapeTransformer should not contain hardcoded coordinate literals");

        System.out.println("✓ End-to-end integration test PASSED");
        System.out.println("  Generated files:");
        System.out.println("    - " + transformerFile.getFileName());
        System.out.println("    - " + previewFile.getFileName());
        System.out.println("  ShapeTransformer: " + transformerCode.length() + " chars");
        System.out.println("  ShapePreview: " + previewCode.length() + " chars");
    }

    @Test
    void v1Legacy_stillWorks(@TempDir Path tempDir) throws IOException {
        // Create a v1.0 JSON (no parametric fields)
        String v1Json = """
            {
              "name": "LegacyShape",
              "version": "1.0",
              "unit": "mm",
              "thickness": 5.0,
              "edges": [
                {
                  "type": "line",
                  "start": {"x": 0, "y": 0},
                  "end": {"x": 100, "y": 0},
                  "id": "e1"
                },
                {
                  "type": "line",
                  "start": {"x": 100, "y": 0},
                  "end": {"x": 100, "y": 100},
                  "id": "e2"
                },
                {
                  "type": "line",
                  "start": {"x": 100, "y": 100},
                  "end": {"x": 0, "y": 100},
                  "id": "e3"
                },
                {
                  "type": "line",
                  "start": {"x": 0, "y": 100},
                  "end": {"x": 0, "y": 0},
                  "id": "e4"
                }
              ]
            }
            """;

        Path inputFile = tempDir.resolve("legacy-test.json");
        Files.writeString(inputFile, v1Json);

        // Process through pipeline
        List<Path> outputFiles = pipeline.processAndGenerate(
            inputFile.toString(),
            tempDir.toString()
        );

        // Verify: Should generate ONE file for v1.0
        assertNotNull(outputFiles);
        assertEquals(1, outputFiles.size(), "Should generate 1 file for v1.0");

        Path outputFile = outputFiles.get(0);
        assertEquals("ShapeTransformer_LegacyShape.java", 
                    outputFile.getFileName().toString());

        String code = Files.readString(outputFile);
        assertTrue(code.contains("package com.company.gsap.generated;"));
        assertTrue(code.contains("public class ShapeTransformer_LegacyShape"));

        System.out.println("✓ Backward compatibility test PASSED");
        System.out.println("  v1.0 still generates 1 file: " + outputFile.getFileName());
    }
}
