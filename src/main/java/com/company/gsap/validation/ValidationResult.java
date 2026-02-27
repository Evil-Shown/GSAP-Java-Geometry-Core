package com.company.gsap.validation;

import java.util.Collections;
import java.util.List;

/**
 * Result object returned by GeometryValidator.
 *
 * Design rule: valid is always derived from errors.
 * If errors is empty → valid = true. No exceptions.
 */
public class ValidationResult {

    private final boolean valid;
    private final List<String> errors;

    public ValidationResult(List<String> errors) {
        this.errors = Collections.unmodifiableList(errors);
        this.valid = errors.isEmpty();
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        if (valid) return "ValidationResult: VALID";
        StringBuilder sb = new StringBuilder("ValidationResult: INVALID\n");
        errors.forEach(e -> sb.append("  \u2717 ").append(e).append("\n"));
        return sb.toString();
    }
}
