package com.company.gsap.engine.core;

import com.company.gsap.engine.model.Geometry;
import com.company.gsap.engine.model.ResolvedShape;

public interface GeometryBuilder {
    Geometry build(ResolvedShape shape);
}
