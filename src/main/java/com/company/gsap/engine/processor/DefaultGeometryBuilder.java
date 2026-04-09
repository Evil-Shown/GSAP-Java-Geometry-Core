package com.company.gsap.engine.processor;

import com.company.gsap.engine.core.GeometryBuilder;
import com.company.gsap.engine.model.Geometry;
import com.company.gsap.engine.model.ResolvedShape;

public class DefaultGeometryBuilder implements GeometryBuilder {
    @Override
    public Geometry build(ResolvedShape shape) {
        return new Geometry(shape.edges());
    }
}
