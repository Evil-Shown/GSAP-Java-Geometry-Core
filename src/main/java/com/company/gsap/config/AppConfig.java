package com.company.gsap.config;

/**
 * Configuration holder for the standalone tool.
 * Plain Java — no frameworks.
 *
 * Holds input folder, output folder, and watch mode flag.
 * Supports reading from command line arguments.
 */
public class AppConfig {

    private String inputFolder = "shapes/input";
    private String outputFolder = "shapes/output";
    private boolean watchMode = false;

    // ── Factory methods ─────────────────────────────────────────

    /** Returns a config with all default values. */
    public static AppConfig defaults() {
        return new AppConfig();
    }

    /**
     * Reads command line arguments and overrides defaults.
     *
     * Supported arguments:
     *   --input=path/to/folder
     *   --output=path/to/folder
     *   --watch=true
     */
    public static AppConfig fromArgs(String[] args) {
        AppConfig config = defaults();
        for (String arg : args) {
            if (arg.startsWith("--input=")) {
                config.setInputFolder(arg.substring("--input=".length()));
            } else if (arg.startsWith("--output=")) {
                config.setOutputFolder(arg.substring("--output=".length()));
            } else if (arg.startsWith("--watch=")) {
                config.setWatchMode(Boolean.parseBoolean(arg.substring("--watch=".length())));
            }
        }
        return config;
    }

    // ── Getters and setters ─────────────────────────────────────

    public String getInputFolder() { return inputFolder; }
    public void setInputFolder(String inputFolder) { this.inputFolder = inputFolder; }

    public String getOutputFolder() { return outputFolder; }
    public void setOutputFolder(String outputFolder) { this.outputFolder = outputFolder; }

    public boolean isWatchMode() { return watchMode; }
    public void setWatchMode(boolean watchMode) { this.watchMode = watchMode; }

    @Override
    public String toString() {
        return String.format("AppConfig[input=%s, output=%s, watch=%s]",
                inputFolder, outputFolder, watchMode);
    }
}
