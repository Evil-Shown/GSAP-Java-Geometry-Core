package com.company.gsap.pipeline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ensures malformed editor payloads surface as recoverable exceptions (worker catches these).
 */
class ShapePipelineJsonSafetyTest {

    @Test
    void invalidJsonThrows(@TempDir Path out) {
        assertThrows(Exception.class, () ->
                new ShapePipeline().processAndGenerateFromJsonString("{", out.toString()));
    }
}
