package com.company.gsap.runner;

import com.company.gsap.config.AppConfig;
import com.company.gsap.pipeline.ShapePipeline;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 3 Step 3 — FolderWatcher Tests.
 * All 4 pass = automatic folder processing works end to end.
 */
class FolderWatcherTest {

    private String resourcePath(String filename) throws Exception {
        URL resource = getClass().getClassLoader().getResource(filename);
        assertNotNull(resource, filename + " not found in resources");
        return Paths.get(resource.toURI()).toString();
    }

    private AppConfig configFor(Path tempDir) {
        AppConfig config = AppConfig.defaults();
        config.setInputFolder(tempDir.resolve("input").toString());
        config.setOutputFolder(tempDir.resolve("output").toString());
        return config;
    }

    private List<Path> listFiles(Path dir, String glob) throws IOException {
        List<Path> results = new ArrayList<>();
        if (!Files.exists(dir)) return results;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, glob)) {
            for (Path entry : stream) {
                results.add(entry);
            }
        }
        return results;
    }

    // ── Test 1: Empty folder completes without error ────────────

    @Test
    void emptyFolder_completesWithoutError(@TempDir Path tempDir) throws Exception {
        AppConfig config = configFor(tempDir);
        FolderWatcher watcher = new FolderWatcher(config, new ShapePipeline());

        assertDoesNotThrow(() -> watcher.processOnce());

        assertTrue(Files.exists(Path.of(config.getOutputFolder())),
                "Output folder should be created even when empty");
        assertTrue(Files.exists(Path.of(config.getInputFolder(), "processed")),
                "Processed subfolder should be created");

        System.out.println("✓ Empty folder processes without error");
    }

    // ── Test 2: Valid JSON generates correct .java file ─────────

    @Test
    void validJson_generatesJavaFile(@TempDir Path tempDir) throws Exception {
        AppConfig config = configFor(tempDir);
        Path inputDir = Path.of(config.getInputFolder());
        Files.createDirectories(inputDir);

        // Copy test file into input folder
        Files.copy(
                Path.of(resourcePath("test-rectangle.json")),
                inputDir.resolve("test-rectangle.json"));

        FolderWatcher watcher = new FolderWatcher(config, new ShapePipeline());
        watcher.processOnce();

        List<Path> javaFiles = listFiles(Path.of(config.getOutputFolder()), "*.java");
        assertEquals(1, javaFiles.size(), "Should generate exactly one .java file");
        assertEquals("ShapeTransformer_TestRectangle.java",
                javaFiles.get(0).getFileName().toString());

        System.out.println("✓ Generated: " + javaFiles.get(0).getFileName());
    }

    // ── Test 3: Mixed valid/invalid — only valid generates ──────

    @Test
    void mixedFiles_onlyValidGenerates(@TempDir Path tempDir) throws Exception {
        AppConfig config = configFor(tempDir);
        Path inputDir = Path.of(config.getInputFolder());
        Files.createDirectories(inputDir);

        // Copy both test files
        Files.copy(
                Path.of(resourcePath("test-rectangle.json")),
                inputDir.resolve("test-rectangle.json"));
        Files.copy(
                Path.of(resourcePath("test-open-shape.json")),
                inputDir.resolve("test-open-shape.json"));

        FolderWatcher watcher = new FolderWatcher(config, new ShapePipeline());
        assertDoesNotThrow(() -> watcher.processOnce(),
                "Invalid file should not crash the batch");

        List<Path> javaFiles = listFiles(Path.of(config.getOutputFolder()), "*.java");
        assertEquals(1, javaFiles.size(),
                "Only the valid shape should generate a .java file");
        assertEquals("ShapeTransformer_TestRectangle.java",
                javaFiles.get(0).getFileName().toString());

        // Failed file should be moved to failed/ subfolder, not left in input/
        assertFalse(Files.exists(inputDir.resolve("test-open-shape.json")),
                "Failed JSON should not remain in input folder");
        assertTrue(Files.exists(inputDir.resolve("failed").resolve("test-open-shape.json")),
                "Failed JSON should be moved to failed subfolder");

        System.out.println("✓ Mixed batch: 1 generated, 1 moved to failed/ (no crash)");
    }

    // ── Test 4: Processed file moves to processed subfolder ─────

    @Test
    void processedFile_movedToSubfolder(@TempDir Path tempDir) throws Exception {
        AppConfig config = configFor(tempDir);
        Path inputDir = Path.of(config.getInputFolder());
        Files.createDirectories(inputDir);

        Files.copy(
                Path.of(resourcePath("test-rectangle.json")),
                inputDir.resolve("test-rectangle.json"));

        FolderWatcher watcher = new FolderWatcher(config, new ShapePipeline());
        watcher.processOnce();

        // Original should be gone from input root
        assertFalse(Files.exists(inputDir.resolve("test-rectangle.json")),
                "JSON should no longer be in input folder root");

        // Should exist in processed subfolder
        assertTrue(Files.exists(inputDir.resolve("processed").resolve("test-rectangle.json")),
                "JSON should be moved to processed subfolder");

        System.out.println("✓ File moved to processed/ after generation");
    }
}
