package com.company.gsap.model;

/**
 * Abstract base for all edge types: Line, Arc, (future: Bezier).
 *
 * Every edge must be able to:
 * 1. Report its start and end point
 * 2. Calculate its own length
 * 3. Validate itself in isolation
 * 4. Produce an offset (parallel-shifted) copy of itself
 */
public abstract class Edge {

    protected final String id;

    protected Edge(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Edge id cannot be null or blank");
        }
        this.id = id;
    }

    // ─── Required contract ─────────────────────────────────────

    /** First point of this edge */
    public abstract Point getStart();

    /** Last point of this edge */
    public abstract Point getEnd();

    /** Arc length or straight-line distance */
    public abstract double length();

    /**
     * Validate this edge in isolation.
     * Throws IllegalStateException with a message if invalid.
     */
    public abstract void validate();

    /**
     * Returns a NEW edge parallel-shifted by 'distance'.
     * Positive = outward (expand), Negative = inward (shrink).
     * Full implementation in Phase 4 — OffsetEngine.
     */
    public abstract Edge offset(double distance);

    // ─── Shared logic ───────────────────────────────────────────

    public String getId() { return id; }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + id + "]";
    }
}
