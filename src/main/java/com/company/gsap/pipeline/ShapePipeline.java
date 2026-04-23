package com.company.gsap.pipeline;

import com.company.gsap.generator.CodeGenerator;
import com.company.gsap.generator.GeneratorResult;
import com.company.gsap.generator.ParametricCodeGenerator;
import com.company.gsap.generator.ShapePreviewGenerator;
import com.company.gsap.loader.ShapeLoader;
import com.company.gsap.loader.dto.ShapeDTO;
import com.company.gsap.model.Shape;
import com.company.gsap.validation.GeometryValidator;
import com.company.gsap.validation.ValidationResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrates the full shape processing sequence:
 *   Load JSON → Validate geometry → Generate code → Write files
 *
 * Supports both v1.0 (legacy) and v2.0 (parametric) formats:
 *   - v1.0: Generates ShapeTransformer only
 *   - v2.0: Generates ShapeTransformer + ShapePreview
 */
public class ShapePipeline {

    private final ShapeLoader loader = new ShapeLoader();
    private final GeometryValidator validator = new GeometryValidator();
    private final CodeGenerator legacyGenerator = new CodeGenerator();
    private final ParametricCodeGenerator parametricGenerator = new ParametricCodeGenerator();
    private final ShapePreviewGenerator previewGenerator = new ShapePreviewGenerator();

    /**
     * Runs load + validate. Returns a validated Shape.
     *
     * @param jsonFilePath path to the shape JSON file on disk
     * @return a validated Shape object ready for code generation
     * @throws IOException if the file cannot be read or parsed
     * @throws IllegalStateException if the shape fails geometry validation
     */
    public Shape process(String jsonFilePath) throws IOException {
        // 1. Load JSON into domain model
        Shape shape = loader.load(jsonFilePath);

        // 2. Validate geometry
        ValidationResult result = validator.validate(shape);

        // 3. Gate: reject invalid shapes before they reach the generator
        if (!result.isValid()) {
            String allErrors = result.getErrors().stream()
                    .collect(Collectors.joining("\n  - ", "\n  - ", ""));
            throw new IllegalStateException(
                    "Geometry validation failed for [" + shape.getName() + "]:" + allErrors);
        }

        return shape;
    }

    /**
     * Full pipeline: load → validate → generate code → write .java file(s).
     *
     * @param jsonFilePath path to the input JSON file
     * @param outputDir    directory where the generated .java files are written
     * @return list of Paths to the generated .java files
     * @throws IOException if reading input or writing output fails
     * @throws IllegalStateException if validation or generation fails
     */
    /**
     * Runs the same pipeline as {@link #processAndGenerate(String, String)} using in-memory JSON.
     * Writes a temporary file so existing loader/generator code paths stay unchanged.
     */
    public List<Path> processAndGenerateFromJsonString(String json, String outputDir) throws IOException {
        Path tmp = Files.createTempFile("shape-", ".json");
        try {
            Files.writeString(tmp, json, StandardCharsets.UTF_8);
            return processAndGenerate(tmp.toString(), outputDir);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    public List<Path> processAndGenerate(String jsonFilePath, String outputDir) throws IOException {
        List<Path> generatedFiles = new ArrayList<>();
        
        // Load DTO to detect format
        ShapeDTO dto = loader.loadDTO(jsonFilePath);
        
        // Detect v2.0 parametric format
        boolean isParametric = dto.parametricEdges != null && !dto.parametricEdges.isEmpty();
        
        Path outDir = Path.of(outputDir);
        Files.createDirectories(outDir);
        
        if (isParametric) {
            System.out.println("→ Using v2.0 Parametric Generators");
            
            // Generate ShapeTransformer
            GeneratorResult transformerResult = parametricGenerator.generate(dto);
            if (!transformerResult.isSuccess()) {
                throw new IllegalStateException(
                        "ShapeTransformer generation failed: " + transformerResult.getError());
            }
            String transformerClassName = parametricGenerator.deriveClassName(dto.name);
            Path transformerFile = outDir.resolve(transformerClassName + ".java");
            Files.writeString(transformerFile, transformerResult.getCode());
            generatedFiles.add(transformerFile);
            System.out.println("  ✓ Generated: " + transformerFile.getFileName());
            
            // Generate ShapePreview
            GeneratorResult previewResult = previewGenerator.generate(dto);
            if (!previewResult.isSuccess()) {
                throw new IllegalStateException(
                        "ShapePreview generation failed: " + previewResult.getError());
            }
            String previewClassName = previewGenerator.deriveClassName(dto.name);
            Path previewFile = outDir.resolve(previewClassName + ".java");
            Files.writeString(previewFile, previewResult.getCode());
            generatedFiles.add(previewFile);
            System.out.println("  ✓ Generated: " + previewFile.getFileName());
            
            // Also validate if legacy edges are present
            if (dto.edges != null && !dto.edges.isEmpty()) {
                Shape shape = loader.load(jsonFilePath);
                ValidationResult result = validator.validate(shape);
                if (!result.isValid()) {
                    String allErrors = result.getErrors().stream()
                            .collect(Collectors.joining("\n  - ", "\n  - ", ""));
                    System.out.println("⚠ Geometry validation warnings: " + allErrors);
                }
            }
            
        } else {
            System.out.println("→ Using CodeGenerator (v1.0 legacy format)");
            
            // Load + validate
            Shape shape = process(jsonFilePath);

            // Generate code
            GeneratorResult genResult = legacyGenerator.generate(shape);
            if (!genResult.isSuccess()) {
                throw new IllegalStateException(
                        "Code generation failed: " + genResult.getError());
            }
            
            String className = legacyGenerator.deriveClassName(shape.getName());
            Path outputFile = outDir.resolve(className + ".java");
            Files.writeString(outputFile, genResult.getCode());
            generatedFiles.add(outputFile);
            System.out.println("  ✓ Generated: " + outputFile.getFileName());
        }

        return generatedFiles;
    }
}

