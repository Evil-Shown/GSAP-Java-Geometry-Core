package com.company.gsap.generator;

import com.company.gsap.loader.ShapeLoader;
import com.company.gsap.loader.dto.ShapeDTO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ParametricCodeGenerator (v2.0 format).
 */
class ParametricCodeGeneratorTest {

    private final ParametricCodeGenerator generator = new ParametricCodeGenerator();
    private final ShapeLoader loader = new ShapeLoader();

    @Test
    void testGenerateFromParametricJSON() throws IOException {
        // Load test parametric JSON
        Path testFile = Path.of("src/test/resources/test-parametric-shape.json");
        if (!Files.exists(testFile)) {
            System.out.println("⚠ Test file not found, skipping test");
            return;
        }

        ShapeDTO dto = loader.loadDTO(testFile.toString());

        // Verify DTO has parametric data
        assertNotNull(dto.parametricEdges, "Should have parametricEdges");
        assertNotNull(dto.pointExpressions, "Should have pointExpressions");
        assertNotNull(dto.parameters, "Should have parameters");
        assertEquals(5, dto.parametricEdges.size(), "Should have 5 parametric edges");
        assertEquals(5, dto.pointExpressions.size(), "Should have 5 point expressions");

        // Generate code
        GeneratorResult result = generator.generate(dto);

        // Verify success
        assertTrue(result.isSuccess(), "Generation should succeed");
        assertNotNull(result.getCode(), "Should generate code");

        String code = result.getCode();

        // Verify generated code contains expected elements
        assertTrue(code.contains("package com.company.gsap.generated;"), 
                  "Should have package declaration");
        assertTrue(code.contains("public class ShapeTransformer_TestParametricShape"), 
                  "Should have correct class name");
        assertTrue(code.contains("param.getTrimLeft()"), 
                  "Should use trim service for p0.x");
        assertTrue(code.contains("param.getTrimBottom()"), 
                  "Should use trim service for p0.y");
        assertTrue(code.contains("paramList.get(\"L\")"), 
                  "Should reference parameter L");
        assertTrue(code.contains("paramList.get(\"H\")"), 
                  "Should reference parameter H");
        assertTrue(code.contains("paramList.get(\"R1\")"), 
                  "Should reference parameter R1");
        assertTrue(code.contains("p0.getX()"), 
                  "Should reference other points");
        assertTrue(code.contains("p0.getY()"), 
                  "Should reference other points");
        assertTrue(code.contains(".straightEdge("), 
                  "Should have straightEdge calls");
        assertTrue(code.contains(".arcEdge("), 
                  "Should have arcEdge calls");

        // Verify point order (p0 should be declared before p1, etc.)
        int p0Index = code.indexOf("Point2D p0 =");
        int p1Index = code.indexOf("Point2D p1 =");
        int p2Index = code.indexOf("Point2D p2 =");
        assertTrue(p0Index > 0, "Should declare p0");
        assertTrue(p1Index > p0Index, "p1 should be declared after p0");
        assertTrue(p2Index > p1Index, "p2 should be declared after p1");

        System.out.println("✓ Generated parametric code:");
        System.out.println("═".repeat(80));
        System.out.println(code);
        System.out.println("═".repeat(80));
    }

    @Test
    void testDeriveClassName() {
        assertEquals("ShapeTransformer_TestShape", 
                    generator.deriveClassName("TestShape"));
        assertEquals("ShapeTransformer_Test_Shape_123", 
                    generator.deriveClassName("Test Shape 123"));
        assertEquals("ShapeTransformer_Shape__101", 
                    generator.deriveClassName("Shape #101"));
    }

    @Test
    void testGenerateFailsWithoutParametricEdges() {
        ShapeDTO dto = new ShapeDTO();
        dto.name = "TestShape";
        dto.version = "1.0";
        dto.thickness = 5.0;

        GeneratorResult result = generator.generate(dto);

        assertFalse(result.isSuccess(), "Should fail without parametricEdges");
        assertTrue(result.getError().contains("parametricEdges"), 
                  "Error should mention parametricEdges");
    }

    @Test
    void testGenerateFailsWithoutPointExpressions() {
        ShapeDTO dto = new ShapeDTO();
        dto.name = "TestShape";
        dto.version = "2.0";
        dto.thickness = 5.0;
        dto.parametricEdges = java.util.List.of(); // Empty but not null

        GeneratorResult result = generator.generate(dto);

        assertFalse(result.isSuccess(), "Should fail with empty parametricEdges");
    }
}
