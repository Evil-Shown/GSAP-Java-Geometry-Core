package com.company.gsap.generator;

import com.company.gsap.loader.dto.ParametricEdgeDTO;
import com.company.gsap.loader.dto.PointExpressionDTO;
import com.company.gsap.loader.dto.ShapeDTO;
import com.company.gsap.model.ParametricExpressionEvaluator;
import com.company.gsap.model.ParametricExpressionEvaluator.PointExpression;

import java.util.*;

/**
 * Generates ShapeTransformer Java code from parametric JSON (v2.0 format).
 * Reads parametricEdges and pointExpressions to produce fully parametric code.
 *
 * This is the v2.0 generator that produces code like:
 *   Point2D p0 = new Point2D.Double(param.getTrimLeft(), param.getTrimBottom());
 *   Point2D p1 = new Point2D.Double(p0.getX() + paramList.get("L"), p0.getY() - paramList.get("R1"));
 *   ...
 */
public class ParametricCodeGenerator {

    private static final String INDENT = "        "; // 8 spaces (inside method body)
    private final ShapeTemplate template = new ShapeTemplate();

    /**
     * Generates a ShapeTransformer Java file from parametric JSON.
     *
     * @param dto the ShapeDTO containing parametric data
     * @return GeneratorResult containing the Java source or an error
     */
    public GeneratorResult generate(ShapeDTO dto) {
        try {
            // Validate that we have parametric data
            if (dto.parametricEdges == null || dto.parametricEdges.isEmpty()) {
                return GeneratorResult.failure("No parametricEdges found in JSON. " +
                        "This generator requires v2.0 parametric format.");
            }
            if (dto.pointExpressions == null || dto.pointExpressions.isEmpty()) {
                return GeneratorResult.failure("No pointExpressions found in JSON. " +
                        "This generator requires v2.0 parametric format.");
            }

            String className = deriveClassName(dto.name);
            String body = buildMethodBody(dto);
            String fullFile = template.buildFile(dto.name, className, body);
            return GeneratorResult.success(fullFile);
        } catch (Exception e) {
            return GeneratorResult.failure("Code generation failed: " + e.getMessage());
        }
    }

    /**
     * Derives the generated class name from the shape name.
     */
    public String deriveClassName(String shapeName) {
        String sanitized = shapeName.replaceAll("[^a-zA-Z0-9_]", "_");
        return "ShapeTransformer_" + sanitized;
    }

    // ── Private helpers ─────────────────────────────────────────

    private String buildMethodBody(ShapeDTO dto) {
        StringBuilder body = new StringBuilder();

        // Build parameter map for expression evaluation
        Map<String, String> parameterMap = new LinkedHashMap<>();
        if (dto.parameters != null) {
            for (ShapeDTO.ParameterDTO param : dto.parameters) {
                parameterMap.put(param.name, param.type);
            }
        }

        // Convert pointExpressions to PointExpression objects
        Map<String, PointExpression> pointExpMap = new LinkedHashMap<>();
        for (Map.Entry<String, PointExpressionDTO> entry : dto.pointExpressions.entrySet()) {
            PointExpressionDTO exprDTO = entry.getValue();
            pointExpMap.put(entry.getKey(), new PointExpression(exprDTO.x, exprDTO.y));
        }

        ParametricExpressionEvaluator evaluator = 
                new ParametricExpressionEvaluator(parameterMap, pointExpMap);

        // ── Write point declarations in dependency order ──
        body.append(INDENT).append("// Point declarations (parametric)\n");
        
        Set<String> declaredPoints = new LinkedHashSet<>();
        List<String> pointIds = new ArrayList<>(dto.pointExpressions.keySet());
        
        // Sort points to ensure dependencies are declared first
        List<String> orderedPoints = orderPointsByDependency(pointIds, pointExpMap);
        
        for (String pointId : orderedPoints) {
            PointExpression expr = pointExpMap.get(pointId);
            String xJava = evaluator.toJavaExpression(expr.x);
            String yJava = evaluator.toJavaExpression(expr.y);
            
            body.append(INDENT)
                .append("Point2D ").append(pointId)
                .append(" = new Point2D.Double(")
                .append(xJava).append(", ")
                .append(yJava)
                .append(");\n");
            
            declaredPoints.add(pointId);
        }

        // ── Write arc center declarations (if any) ──
        Map<String, String> centerVars = new LinkedHashMap<>();
        int centerCounter = 0;
        
        for (ParametricEdgeDTO edge : dto.parametricEdges) {
            if ("arc".equals(edge.type) && edge.centerExpression != null) {
                String centerKey = edge.centerExpression.x + "," + edge.centerExpression.y;
                if (!centerVars.containsKey(centerKey)) {
                    String varName = "center" + centerCounter++;
                    centerVars.put(centerKey, varName);
                    
                    String xJava = evaluator.toJavaExpression(edge.centerExpression.x);
                    String yJava = evaluator.toJavaExpression(edge.centerExpression.y);
                    
                    if (centerCounter == 1) {
                        body.append("\n").append(INDENT).append("// Arc center points\n");
                    }
                    
                    body.append(INDENT)
                        .append("Point2D ").append(varName)
                        .append(" = new Point2D.Double(")
                        .append(xJava).append(", ")
                        .append(yJava)
                        .append(");\n");
                }
            }
        }

        // ── Write EdgeBuilder chain ──
        body.append("\n").append(INDENT).append("// Build edges\n");
        body.append(INDENT).append("List<Edge> edges = new EdgeBuilder()\n");
        
        if (!dto.parametricEdges.isEmpty()) {
            body.append(INDENT).append("        .startPoint(")
                .append(dto.parametricEdges.get(0).startPoint).append(")\n");
        }

        for (int i = 0; i < dto.parametricEdges.size(); i++) {
            ParametricEdgeDTO edge = dto.parametricEdges.get(i);
            
            if ("line".equals(edge.type)) {
                body.append(INDENT).append("        .straightEdge(")
                    .append(edge.endPoint).append(")");
            } else if ("arc".equals(edge.type)) {
                String centerKey = edge.centerExpression.x + "," + edge.centerExpression.y;
                String centerVar = centerVars.get(centerKey);
                
                body.append(INDENT).append("        .arcEdge(")
                    .append(centerVar).append(", ")
                    .append("paramList.get(\"").append(edge.radiusParam).append("\"), ")
                    .append(edge.largeArc).append(", ")
                    .append(edge.sweep).append(", ")
                    .append(edge.endPoint).append(")");
            }

            if (i < dto.parametricEdges.size() - 1) {
                body.append("\n");
            } else {
                body.append("\n");
                body.append(INDENT).append("        .build();\n");
            }
        }

        return body.toString();
    }

    /**
     * Orders points by dependency so that referenced points are declared before
     * points that reference them.
     */
    private List<String> orderPointsByDependency(List<String> pointIds, 
                                                   Map<String, PointExpression> pointExpMap) {
        List<String> ordered = new ArrayList<>();
        Set<String> processed = new HashSet<>();
        
        for (String pointId : pointIds) {
            orderPoint(pointId, pointExpMap, ordered, processed);
        }
        
        return ordered;
    }

    private void orderPoint(String pointId, Map<String, PointExpression> pointExpMap,
                            List<String> ordered, Set<String> processed) {
        if (processed.contains(pointId)) {
            return;
        }
        
        PointExpression expr = pointExpMap.get(pointId);
        if (expr == null) {
            return;
        }
        
        // Find dependencies (other points referenced in this expression)
        Set<String> dependencies = findPointDependencies(expr);
        
        // Process dependencies first
        for (String dep : dependencies) {
            if (!dep.equals(pointId)) {
                orderPoint(dep, pointExpMap, ordered, processed);
            }
        }
        
        // Now add this point
        ordered.add(pointId);
        processed.add(pointId);
    }

    private Set<String> findPointDependencies(PointExpression expr) {
        Set<String> deps = new HashSet<>();
        String combined = expr.x + " " + expr.y;
        
        // Find all p0, p1, p2, etc. references
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b(p\\d+)\\.");
        java.util.regex.Matcher matcher = pattern.matcher(combined);
        
        while (matcher.find()) {
            deps.add(matcher.group(1));
        }
        
        return deps;
    }
}
