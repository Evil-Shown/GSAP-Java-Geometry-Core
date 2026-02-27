package com.company.gsap.runner;

import com.company.gsap.config.AppConfig;
import com.company.gsap.pipeline.ShapePipeline;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Watches an input folder for JSON files and processes them automatically.
 *
 * Two modes:
 *   processOnce() — scan once, process everything found, return.
 *   start()       — continuous loop: scan → process → sleep → repeat.
 *
 * Per-file error handling: one bad file never crashes the whole batch.
 */
public class FolderWatcher {

    private static final long POLL_INTERVAL_MS = 2000;

    private final AppConfig config;
    private final ShapePipeline pipeline;
    private volatile boolean running = false;

    public FolderWatcher(AppConfig config, ShapePipeline pipeline) {
        this.config = config;
        this.pipeline = pipeline;
    }

    /**
     * Scan once, process all JSON files found, return.
     * Used when watchMode is false.
     */
    public void processOnce() throws IOException {
        ensureDirectories();
        List<Path> jsonFiles = scanForJsonFiles();

        if (jsonFiles.isEmpty()) {
            System.out.println("  No JSON files found in: " + config.getInputFolder());
            return;
        }

        System.out.println("  Found " + jsonFiles.size() + " JSON file(s)");
        for (Path jsonFile : jsonFiles) {
            processFile(jsonFile);
        }
    }

    /**
     * Continuous watch loop: scan → process → sleep → repeat.
     * Blocks until stop() is called.
     */
    public void start() throws IOException {
        ensureDirectories();
        running = true;
        System.out.println("  Watching: " + Path.of(config.getInputFolder()).toAbsolutePath());
        System.out.println("  Press Ctrl+C to stop.\n");

        while (running) {
            List<Path> jsonFiles = scanForJsonFiles();
            for (Path jsonFile : jsonFiles) {
                processFile(jsonFile);
            }
            try {
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("  Watcher stopped.");
    }

    /** Signals the watch loop to exit cleanly. */
    public void stop() {
        running = false;
    }

    // ── Private helpers ─────────────────────────────────────────

    private void ensureDirectories() throws IOException {
        Files.createDirectories(Path.of(config.getInputFolder()));
        Files.createDirectories(Path.of(config.getOutputFolder()));
        Files.createDirectories(Path.of(config.getInputFolder(), "processed"));
        Files.createDirectories(Path.of(config.getInputFolder(), "failed"));
    }

    private List<Path> scanForJsonFiles() throws IOException {
        List<Path> results = new ArrayList<>();
        Path inputDir = Path.of(config.getInputFolder());

        if (!Files.exists(inputDir)) return results;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDir, "*.json")) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    results.add(entry);
                }
            }
        }
        return results;
    }

    private void processFile(Path jsonFile) {
        String fileName = jsonFile.getFileName().toString();
        try {
            System.out.println("  Processing: " + fileName);
            Path outputFile = pipeline.processAndGenerate(
                    jsonFile.toString(), config.getOutputFolder());

            // Move to processed subfolder
            Path processedDir = Path.of(config.getInputFolder(), "processed");
            Path destination = processedDir.resolve(fileName);
            Files.move(jsonFile, destination);

            System.out.println("  ✓ Generated: " + outputFile.getFileName());
            System.out.println("    Moved input to: processed/" + fileName);

        } catch (Exception e) {
            System.err.println("  ✗ Failed: " + fileName);
            System.err.println("    Reason: " + e.getMessage());
            // Move to failed subfolder so it doesn't retry every scan
            try {
                Path failedDir = Path.of(config.getInputFolder(), "failed");
                Files.move(jsonFile, failedDir.resolve(fileName));
                System.err.println("    Moved to: failed/" + fileName);
            } catch (IOException moveEx) {
                System.err.println("    Could not move failed file: " + moveEx.getMessage());
            }
        }
    }
}
