package com.company.gsap.generator;

import com.company.gsap.loader.ShapeLoader;
import com.company.gsap.loader.dto.ShapeDTO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ShapePreviewGenerator.
 */
class ShapePreviewGeneratorTest {

    private final ShapePreviewGenerator generator = new ShapePreviewGenerator();
    private final ShapeLoader loader = new ShapeLoader();

    @Test
    void testGenerateShapePreview() throws IOException {
        // Load test parametric JSON
        Path testFile = Path.of("src/test/resources/test-parametric-shape.json");
        if (!Files.exists(testFile)) {
            System.out.println("⚠ Test file not found, skipping test");
            return;
        }

        ShapeDTO dto = loader.loadDTO(testFile.toString());

        // Generate ShapePreview
        GeneratorResult result = generator.generate(dto);

        // Verify success
        assertTrue(result.isSuccess(), "ShapePreview generation should succeed");
        assertNotNull(result.getCode(), "Should generate code");

        String code = result.getCode();

        // Verify package and class
        assertTrue(code.contains("package com.company.gsap.generated.preview;"), 
                  "Should be in preview package");
        assertTrue(code.contains("public class ShapePreview_TestParametricShape"), 
                  "Should have correct class name");

        // Verify methods
        assertTrue(code.contains("getParameters()"), "Should have getParameters method");
        assertTrue(code.contains("getMetadata()"), "Should have getMetadata method");
        assertTrue(code.contains("getPreviewPoints()"), "Should have getPreviewPoints method");
        assertTrue(code.contains("calculatePoints("), "Should have calculatePoints method");

        // Verify parameter initialization
        assertTrue(code.contains("parameters.put(\"L\""), "Should initialize L parameter");
        assertTrue(code.contains("parameters.put(\"H\""), "Should initialize H parameter");
        assertTrue(code.contains("parameters.put(\"R1\""), "Should initialize R1 parameter");

        // Verify metadata
        assertTrue(code.contains("metadata.put(\"name\""), "Should have name metadata");
        assertTrue(code.contains("metadata.put(\"thickness\""), "Should have thickness metadata");
        assertTrue(code.contains("metadata.put(\"edgeCount\""), "Should have edgeCount metadata");

        // Verify Parameter inner class
        assertTrue(code.contains("public static class Parameter"), "Should have Parameter class");

        System.out.println("✓ Generated ShapePreview code:");
        System.out.println("═".repeat(80));
        System.out.println(code);
        System.out.println("═".repeat(80));
    }

    @Test
    void testDeriveClassName() {
        assertEquals("ShapePreview_TestShape", 
                    generator.deriveClassName("TestShape"));
        assertEquals("ShapePreview_Test_Shape_123", 
                    generator.deriveClassName("Test Shape 123"));
    }

    @Test
    void testGenerateFailsWithoutParametricData() {
        ShapeDTO dto = new ShapeDTO();
        dto.name = "TestShape";
        dto.version = "1.0";
        dto.thickness = 5.0;

        GeneratorResult result = generator.generate(dto);

        assertFalse(result.isSuccess(), "Should fail without parametric data");
        assertTrue(result.getError().contains("parametricEdges") || 
                  result.getError().contains("v2.0"), 
                  "Error should mention missing parametric data");
    }
}
