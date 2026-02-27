package com.company.gsap.runner;

import com.company.gsap.config.AppConfig;
import com.company.gsap.pipeline.ShapePipeline;

/**
 * Command-line entry point for the GSAP shape processing tool.
 *
 * Usage:
 *   java StandaloneRunner                                    → single pass, default folders
 *   java StandaloneRunner --watch=true                       → continuous watch mode
 *   java StandaloneRunner --input=my/in --output=my/out      → custom folders
 *   java StandaloneRunner --input=my/in --output=my/out --watch=true
 *
 * Workflow:
 *   1. Drop JSON files from the browser editor into the input folder
 *   2. Tool picks them up, validates, generates ShapeTransformer .java files
 *   3. Find generated files in the output folder
 */
public class StandaloneRunner {

    public static void main(String[] args) {
        AppConfig config = AppConfig.fromArgs(args);
        ShapePipeline pipeline = new ShapePipeline();
        FolderWatcher watcher = new FolderWatcher(config, pipeline);

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║      GSAP Geometry Core — Runner        ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("  Input:  " + config.getInputFolder());
        System.out.println("  Output: " + config.getOutputFolder());
        System.out.println();

        try {
            if (config.isWatchMode()) {
                System.out.println("  Mode: WATCH — drop JSON files into " + config.getInputFolder());
                System.out.println();

                // Clean shutdown on Ctrl+C
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("\n  Shutting down...");
                    watcher.stop();
                }));

                watcher.start();
            } else {
                System.out.println("  Mode: SINGLE PASS — processing existing files and exiting");
                System.out.println();

                watcher.processOnce();

                System.out.println();
                System.out.println("  Done. Exiting.");
            }
        } catch (Exception e) {
            System.err.println("  FATAL ERROR: " + e.getMessage());
            System.exit(1);
        }
    }
}
