package com.company.gsap.engine.processor;

import com.company.gsap.engine.model.EdgeDefinition;
import com.company.gsap.engine.model.Point2D;
import com.company.gsap.engine.model.ShapeMetrics;

import java.util.List;

public class SvgRenderer {

    public String render(List<EdgeDefinition> edges, ShapeMetrics metrics) {
        if (edges.isEmpty()) {
            return "<svg xmlns=\"http://www.w3.org/2000/svg\"></svg>";
        }
        Point2D start = edges.get(0).start();
        StringBuilder pathBuilder = new StringBuilder("M ").append(start.x()).append(" ").append(start.y()).append(" ");
        for (EdgeDefinition edge : edges) {
            pathBuilder.append(edge.svgPathSegment()).append(" ");
        }
        pathBuilder.append("Z");

        double width = Math.max(1.0, metrics.maxX() - metrics.minX());
        double height = Math.max(1.0, metrics.maxY() - metrics.minY());
        return "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"" +
                metrics.minX() + " " + metrics.minY() + " " + width + " " + height + "\">" +
                "<path d=\"" + pathBuilder + "\" fill=\"none\" stroke=\"black\"/></svg>";
    }
}
