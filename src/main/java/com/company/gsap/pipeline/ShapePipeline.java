package com.company.gsap.pipeline;

import com.company.gsap.generator.CodeGenerator;
import com.company.gsap.generator.GeneratorResult;
import com.company.gsap.loader.ShapeLoader;
import com.company.gsap.model.Shape;
import com.company.gsap.validation.GeometryValidator;
import com.company.gsap.validation.ValidationResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * Orchestrates the full shape processing sequence:
 *   Load JSON → Validate geometry → Generate code → Write file
 */
public class ShapePipeline {

    private final ShapeLoader loader = new ShapeLoader();
    private final GeometryValidator validator = new GeometryValidator();
    private final CodeGenerator generator = new CodeGenerator();

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
     * Full pipeline: load → validate → generate code → write .java file.
     *
     * @param jsonFilePath path to the input JSON file
     * @param outputDir    directory where the generated .java file is written
     * @return the Path to the generated .java file
     * @throws IOException if reading input or writing output fails
     * @throws IllegalStateException if validation or generation fails
     */
    public Path processAndGenerate(String jsonFilePath, String outputDir) throws IOException {
        // Load + validate
        Shape shape = process(jsonFilePath);

        // Generate code
        GeneratorResult genResult = generator.generate(shape);
        if (!genResult.isSuccess()) {
            throw new IllegalStateException(
                    "Code generation failed for [" + shape.getName() + "]: " + genResult.getError());
        }

        // Write file
        String className = generator.deriveClassName(shape.getName());
        Path outDir = Path.of(outputDir);
        Files.createDirectories(outDir);

        Path outputFile = outDir.resolve(className + ".java");
        Files.writeString(outputFile, genResult.getCode());

        return outputFile;
    }
}
