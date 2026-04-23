package com.company.gsap.engine.processor;

import com.company.gsap.engine.core.GeometryBuilder;
import com.company.gsap.engine.core.ShapeDefinition;
import com.company.gsap.engine.model.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultShapeProcessor {
    private final GeometryBuilder geometryBuilder;
    private final TransformationService transformationService;
    private final MeasurementService measurementService;
    private final SvgRenderer svgRenderer;

    public DefaultShapeProcessor(
            GeometryBuilder geometryBuilder,
            TransformationService transformationService,
            MeasurementService measurementService,
            SvgRenderer svgRenderer
    ) {
        this.geometryBuilder = geometryBuilder;
        this.transformationService = transformationService;
        this.measurementService = measurementService;
        this.svgRenderer = svgRenderer;
    }

    public ShapeResult process(ShapeDefinition definition, ShapeInput input) {
        Map<String, String> mergedMetadata = new LinkedHashMap<>(definition.metadata());
        if (input.metadata() != null) {
            mergedMetadata.putAll(input.metadata());
        }

        List<Transformation> allTransformations = new ArrayList<>(definition.transformations());
        if (input.transformations() != null) {
            allTransformations.addAll(input.transformations());
        }

        List<EdgeDefinition> transformed = transformationService.apply(definition.edges(), allTransformations);
        transformed = EdgeServiceOutlineExpander.apply(transformed, input.edgeServiceAmountsByEdgeId());
        ResolvedShape resolvedShape = new ResolvedShape(definition.id(), definition.parameters(), transformed, mergedMetadata);
        Geometry geometry = geometryBuilder.build(resolvedShape);
        ShapeMetrics metrics = measurementService.measure(geometry.edges());
        String svg = svgRenderer.render(geometry.edges(), metrics);
        return new ShapeResult(geometry, svg, mergedMetadata, metrics);
    }
}
