package com.company.gsap.generator;

/**
 * Result object returned by CodeGenerator.
 *
 * Design rule: same principle as ValidationResult.
 * Generator never throws for expected failures — it returns a result.
 * The caller decides what to do with failure.
 */
public class GeneratorResult {

    private final boolean success;
    private final String code;
    private final String error;

    private GeneratorResult(boolean success, String code, String error) {
        this.success = success;
        this.code = code;
        this.error = error;
    }

    /** Factory: generation succeeded, code contains the full Java file */
    public static GeneratorResult success(String code) {
        return new GeneratorResult(true, code, null);
    }

    /** Factory: generation failed, reason explains why */
    public static GeneratorResult failure(String reason) {
        return new GeneratorResult(false, null, reason);
    }

    public boolean isSuccess() { return success; }

    public String getCode() { return code; }

    public String getError() { return error; }

    @Override
    public String toString() {
        if (success) return "GeneratorResult: SUCCESS (" + code.length() + " chars)";
        return "GeneratorResult: FAILED — " + error;
    }
}
