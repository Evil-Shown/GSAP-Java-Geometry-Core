package com.company.gsap.engine.core;

public class InvalidShapeDefinitionException extends GeometryEngineException {
    public InvalidShapeDefinitionException(String message) {
        super(message);
    }

    public InvalidShapeDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
