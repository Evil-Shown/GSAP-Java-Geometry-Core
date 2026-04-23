package com.company.gsap.model;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evaluates parametric expressions for code generation.
 * Handles expressions like "p0.x + L - R1", "trimLeft", "p0.y - H", etc.
 *
 * This class converts string expressions into Java code statements.
 */
public class ParametricExpressionEvaluator {

    private final Map<String, String> parameters;
    private final Map<String, PointExpression> pointExpressions;
    
    // Pattern to identify references to other points (e.g., "p0.x", "p1.y")
    private static final Pattern POINT_REF_PATTERN = Pattern.compile("(p\\d+)\\.(x|y)");
    
    public ParametricExpressionEvaluator(Map<String, String> parameters, 
                                          Map<String, PointExpression> pointExpressions) {
        this.parameters = parameters != null ? parameters : new HashMap<>();
        this.pointExpressions = pointExpressions != null ? pointExpressions : new HashMap<>();
    }
    
    /**
     * Converts a parametric expression to a Java code string.
     * 
     * Examples:
     *   "trimLeft" → "param.getTrimLeft()"
     *   "trimBottom" → "param.getTrimBottom()"
     *   "p0.x + L" → "p0.getX() + paramList.get(\"L\")"
     *   "p0.x + L - R1" → "p0.getX() + paramList.get(\"L\") - paramList.get(\"R1\")"
     */
    public String toJavaExpression(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("Expression cannot be null or blank");
        }
        
        // Handle trim service references
        if (expression.equals("trimLeft")) {
            return "param.getTrimLeft()";
        }
        if (expression.equals("trimBottom")) {
            return "param.getTrimBottom()";
        }
        if (expression.equals("trimRight")) {
            return "param.getTrimRight()";
        }
        if (expression.equals("trimTop")) {
            return "param.getTrimTop()";
        }
        
        // Replace point references (p0.x → p0.getX())
        String result = expression;
        Matcher matcher = POINT_REF_PATTERN.matcher(expression);
        while (matcher.find()) {
            String pointId = matcher.group(1);
            String axis = matcher.group(2);
            String replacement = pointId + ".get" + axis.toUpperCase() + "()";
            result = result.replace(pointId + "." + axis, replacement);
        }
        
        // Replace parameter references (L → paramList.get("L"))
        for (String param : parameters.keySet()) {
            // Use word boundaries to avoid partial matches (e.g., R1 in R10)
            result = result.replaceAll("\\b" + Pattern.quote(param) + "\\b", 
                                       "paramList.get(\"" + param + "\")");
        }
        
        return result;
    }
    
    /**
     * Checks if an expression is a simple literal value.
     */
    public boolean isLiteral(String expression) {
        if (expression == null) return true;
        try {
            Double.parseDouble(expression);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Checks if an expression references other points.
     */
    public boolean referencesOtherPoints(String expression) {
        return expression != null && POINT_REF_PATTERN.matcher(expression).find();
    }
    
    /**
     * Checks if an expression is fully parametric (no hardcoded values).
     */
    public boolean isFullyParametric(String xExpr, String yExpr) {
        return !isLiteral(xExpr) && !isLiteral(yExpr);
    }
    
    /**
     * Represents a point expression with x and y components.
     */
    public static class PointExpression {
        public final String x;
        public final String y;
        
        public PointExpression(String x, String y) {
            this.x = x;
            this.y = y;
        }
    }
}
