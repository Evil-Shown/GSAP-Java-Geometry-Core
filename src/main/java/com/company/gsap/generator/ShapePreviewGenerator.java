package com.company.gsap.generator;

import com.company.gsap.loader.dto.PointExpressionDTO;
import com.company.gsap.loader.dto.ShapeDTO;
import com.company.gsap.model.ParametricExpressionEvaluator;
import com.company.gsap.model.ParametricExpressionEvaluator.PointExpression;

import java.util.*;

/**
 * Generates ShapePreview Java classes from parametric JSON.
 * ShapePreview provides visualization, metadata, and parameter information.
 */
public class ShapePreviewGenerator {

    private static final String INDENT = "        ";
    private final ShapePreviewTemplate template = new ShapePreviewTemplate();

    /**
     * Generates a ShapePreview Java file from parametric JSON.
     */
    public GeneratorResult generate(ShapeDTO dto) {
        try {
            if (dto.parametricEdges == null || dto.parametricEdges.isEmpty()) {
                return GeneratorResult.failure("No parametricEdges found. ShapePreview requires v2.0 format.");
            }
            if (dto.pointExpressions == null || dto.pointExpressions.isEmpty()) {
                return GeneratorResult.failure("No pointExpressions found. ShapePreview requires v2.0 format.");
            }

            String className = "ShapePreview_" + sanitizeName(dto.name);
            String paramInitBody = buildParameterInitializer(dto);
            String metadataBody = buildMetadataInitializer(dto);
            String pointCalcBody = buildPointCalculator(dto);
            
            String fullFile = template.buildFile(dto.name, className, 
                                                paramInitBody, metadataBody, pointCalcBody);
            return GeneratorResult.success(fullFile);
        } catch (Exception e) {
            return GeneratorResult.failure("ShapePreview generation failed: " + e.getMessage());
        }
    }

    public String deriveClassName(String shapeName) {
        return "ShapePreview_" + sanitizeName(shapeName);
    }

    private String sanitizeName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    // ── Private builders ──────────────────────────────────────

    private String buildParameterInitializer(ShapeDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append(INDENT).append("private void initializeParameters() {\n");
        
        if (dto.parameters != null && !dto.parameters.isEmpty()) {
            for (ShapeDTO.ParameterDTO param : dto.parameters) {
                sb.append(INDENT).append("    parameters.put(\"").append(param.name).append("\", ")
                  .append("new Parameter(\"").append(param.name).append("\", ")
                  .append("\"").append(param.type).append("\", ")
                  .append(param.defaultValue).append(", ")
                  .append("\"").append(param.description != null ? param.description : "").append("\"));\n");
            }
        }
        
        sb.append(INDENT).append("}\n\n");
        return sb.toString();
    }

    private String buildMetadataInitializer(ShapeDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append(INDENT).append("private void initializeMetadata() {\n");
        
        sb.append(INDENT).append("    metadata.put(\"name\", \"").append(dto.name).append("\");\n");
        sb.append(INDENT).append("    metadata.put(\"version\", \"").append(dto.version != null ? dto.version : "2.0").append("\");\n");
        sb.append(INDENT).append("    metadata.put(\"unit\", \"").append(dto.unit != null ? dto.unit : "mm").append("\");\n");
        sb.append(INDENT).append("    metadata.put(\"thickness\", \"").append(dto.thickness).append("\");\n");
        sb.append(INDENT).append("    metadata.put(\"edgeCount\", \"").append(dto.parametricEdges.size()).append("\");\n");
        sb.append(INDENT).append("    metadata.put(\"pointCount\", \"").append(dto.pointExpressions.size()).append("\");\n");
        
        if (dto.parametricCompleteness != null) {
            sb.append(INDENT).append("    metadata.put(\"fullyParametric\", \"")
              .append(dto.parametricCompleteness.fullyParametric).append("\");\n");
        }
        
        sb.append(INDENT).append("}\n\n");
        return sb.toString();
    }

    private String buildPointCalculator(ShapeDTO dto) {
        StringBuilder sb = new StringBuilder();
        
        // Build parameter map
        Map<String, String> parameterMap = new LinkedHashMap<>();
        if (dto.parameters != null) {
            for (ShapeDTO.ParameterDTO param : dto.parameters) {
                parameterMap.put(param.name, param.type);
            }
        }

        // Convert point expressions
        Map<String, PointExpression> pointExpMap = new LinkedHashMap<>();
        for (Map.Entry<String, PointExpressionDTO> entry : dto.pointExpressions.entrySet()) {
            PointExpressionDTO exprDTO = entry.getValue();
            pointExpMap.put(entry.getKey(), new PointExpression(exprDTO.x, exprDTO.y));
        }

        // Order points by dependency
        List<String> orderedPoints = orderPointsByDependency(
            new ArrayList<>(dto.pointExpressions.keySet()), pointExpMap);

        // Generate point calculation code
        for (String pointId : orderedPoints) {
            PointExpression expr = pointExpMap.get(pointId);
            String xExpr = convertExpressionForPreview(expr.x, parameterMap);
            String yExpr = convertExpressionForPreview(expr.y, parameterMap);
            
            sb.append(INDENT).append("        points.put(\"").append(pointId).append("\", ")
              .append("new Point2D.Double(").append(xExpr).append(", ").append(yExpr).append("));\n");
        }

        return sb.toString();
    }

    /**
     * Converts parametric expressions to Java code for preview.
     * Uses paramValues map and trim parameters.
     */
    private String convertExpressionForPreview(String expression, Map<String, String> parameterMap) {
        if (expression == null || expression.isBlank()) {
            return "0.0";
        }

        String result = expression;

        // Handle trim services
        result = result.replace("trimLeft", "trimLeft");
        result = result.replace("trimBottom", "trimBottom");
        result = result.replace("trimRight", "trimLeft"); // Simplified for preview
        result = result.replace("trimTop", "trimBottom"); // Simplified for preview

        // Replace point references
        result = result.replaceAll("(p\\d+)\\.(x|y)", "points.get(\"$1\").get$2()");
        result = result.replaceAll("\\.x\\(\\)", ".getX()");
        result = result.replaceAll("\\.y\\(\\)", ".getY()");

        // Replace parameters with map lookups
        for (String param : parameterMap.keySet()) {
            result = result.replaceAll("\\b" + param + "\\b", "paramValues.get(\"" + param + "\")");
        }

        return result;
    }

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
        
        Set<String> dependencies = findPointDependencies(expr);
        for (String dep : dependencies) {
            if (!dep.equals(pointId)) {
                orderPoint(dep, pointExpMap, ordered, processed);
            }
        }
        
        ordered.add(pointId);
        processed.add(pointId);
    }

    private Set<String> findPointDependencies(PointExpression expr) {
        Set<String> deps = new HashSet<>();
        String combined = expr.x + " " + expr.y;
        
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b(p\\d+)\\.");
        java.util.regex.Matcher matcher = pattern.matcher(combined);
        
        while (matcher.find()) {
            deps.add(matcher.group(1));
        }
        
        return deps;
    }
}
