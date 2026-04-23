package com.company.gsap.engine.core;

import com.company.gsap.engine.factory.DefaultShapeFactory;
import com.company.gsap.engine.factory.ShapeFactory;
import com.company.gsap.engine.model.ShapeInput;
import com.company.gsap.engine.model.ShapeMetrics;
import com.company.gsap.engine.model.ShapeResult;
import com.company.gsap.engine.parser.JsonShapeDefinitionParser;
import com.company.gsap.engine.processor.*;
import com.company.gsap.engine.validator.ShapeDefinitionValidator;

public class GeometryEngine implements ShapeProcessor {
    private final ShapeFactory shapeFactory;
    private final DefaultShapeProcessor shapeProcessor;
    private final JsonShapeDefinitionParser jsonShapeDefinitionParser;

    public GeometryEngine() {
        ShapeDefinitionValidator validator = new ShapeDefinitionValidator();
        this.jsonShapeDefinitionParser = new JsonShapeDefinitionParser(validator);
        this.shapeFactory = new DefaultShapeFactory(jsonShapeDefinitionParser);
        this.shapeProcessor = new DefaultShapeProcessor(
                new DefaultGeometryBuilder(),
                new TransformationService(),
                new MeasurementService(),
                new SvgRenderer()
        );
    }

    public GeometryEngine(ShapeFactory shapeFactory, DefaultShapeProcessor shapeProcessor, JsonShapeDefinitionParser jsonShapeDefinitionParser) {
        this.shapeFactory = shapeFactory;
        this.shapeProcessor = shapeProcessor;
        this.jsonShapeDefinitionParser = jsonShapeDefinitionParser;
    }

    @Override
    public ShapeResult processShape(ShapeInput input) {
        ShapeDefinition definition = shapeFactory.create(input.shapeId(), input.shapeJson());
        return shapeProcessor.process(definition, input);
    }

    public ShapeDefinition createShapeFromJson(String json) {
        return jsonShapeDefinitionParser.fromJson(json);
    }

    public String getShapePreview(ShapeInput input) {
        return processShape(input).svg();
    }

    public ShapeMetrics calculateMetrics(ShapeInput input) {
        return processShape(input).dimensions();
    }
}
