package com.touchvirtual.util;

import java.util.List;
import java.util.ArrayList;

/**
 * Utilitários matemáticos para o sistema
 * 
 * @author TouchVirtual Team
 * @version 1.0.0
 */
public class MathUtils {
    
    /**
     * Calcula a distância euclidiana entre dois pontos
     */
    public static double calculateDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Calcula a distância euclidiana 3D entre dois pontos
     */
    public static double calculateDistance3D(double x1, double y1, double z1, 
                                          double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    /**
     * Calcula o ângulo entre dois vetores
     */
    public static double calculateAngle(double x1, double y1, double x2, double y2) {
        double dotProduct = x1 * x2 + y1 * y2;
        double magnitude1 = Math.sqrt(x1 * x1 + y1 * y1);
        double magnitude2 = Math.sqrt(x2 * x2 + y2 * y2);
        
        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0.0;
        }
        
        double cosAngle = dotProduct / (magnitude1 * magnitude2);
        cosAngle = Math.max(-1.0, Math.min(1.0, cosAngle)); // Clamp to [-1, 1]
        
        return Math.acos(cosAngle) * 180.0 / Math.PI;
    }
    
    /**
     * Calcula o ângulo entre três pontos
     */
    public static double calculateAngle(double x1, double y1, double x2, double y2, 
                                      double x3, double y3) {
        double a = calculateDistance(x1, y1, x2, y2);
        double b = calculateDistance(x2, y2, x3, y3);
        double c = calculateDistance(x1, y1, x3, y3);
        
        if (a == 0 || b == 0) {
            return 0.0;
        }
        
        double cosAngle = (a * a + b * b - c * c) / (2 * a * b);
        cosAngle = Math.max(-1.0, Math.min(1.0, cosAngle)); // Clamp to [-1, 1]
        
        return Math.acos(cosAngle) * 180.0 / Math.PI;
    }
    
    /**
     * Normaliza um valor para o intervalo [0, 1]
     */
    public static double normalize(double value, double min, double max) {
        if (max == min) {
            return 0.5;
        }
        return (value - min) / (max - min);
    }
    
    /**
     * Aplica suavização exponencial
     */
    public static double smoothExponential(double current, double previous, double alpha) {
        return alpha * current + (1 - alpha) * previous;
    }
    
    /**
     * Aplica filtro de média móvel
     */
    public static double smoothMovingAverage(List<Double> values, int windowSize) {
        if (values.isEmpty()) {
            return 0.0;
        }
        
        int startIndex = Math.max(0, values.size() - windowSize);
        double sum = 0.0;
        int count = 0;
        
        for (int i = startIndex; i < values.size(); i++) {
            sum += values.get(i);
            count++;
        }
        
        return count > 0 ? sum / count : 0.0;
    }
    
    /**
     * Calcula a média de uma lista de valores
     */
    public static double calculateMean(List<Double> values) {
        if (values.isEmpty()) {
            return 0.0;
        }
        
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        
        return sum / values.size();
    }
    
    /**
     * Calcula o desvio padrão de uma lista de valores
     */
    public static double calculateStandardDeviation(List<Double> values) {
        if (values.size() < 2) {
            return 0.0;
        }
        
        double mean = calculateMean(values);
        double sumSquaredDiff = 0.0;
        
        for (double value : values) {
            double diff = value - mean;
            sumSquaredDiff += diff * diff;
        }
        
        return Math.sqrt(sumSquaredDiff / (values.size() - 1));
    }
    
    /**
     * Calcula a mediana de uma lista de valores
     */
    public static double calculateMedian(List<Double> values) {
        if (values.isEmpty()) {
            return 0.0;
        }
        
        List<Double> sorted = new ArrayList<>(values);
        sorted.sort(Double::compareTo);
        
        int size = sorted.size();
        if (size % 2 == 0) {
            return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
        } else {
            return sorted.get(size / 2);
        }
    }
    
    /**
     * Calcula o valor mínimo de uma lista
     */
    public static double calculateMin(List<Double> values) {
        if (values.isEmpty()) {
            return 0.0;
        }
        
        double min = values.get(0);
        for (double value : values) {
            if (value < min) {
                min = value;
            }
        }
        
        return min;
    }
    
    /**
     * Calcula o valor máximo de uma lista
     */
    public static double calculateMax(List<Double> values) {
        if (values.isEmpty()) {
            return 0.0;
        }
        
        double max = values.get(0);
        for (double value : values) {
            if (value > max) {
                max = value;
            }
        }
        
        return max;
    }
    
    /**
     * Aplica deadband a um valor
     */
    public static double applyDeadband(double value, double deadband) {
        if (Math.abs(value) < deadband) {
            return 0.0;
        }
        return value;
    }
    
    /**
     * Aplica sensibilidade a um valor
     */
    public static double applySensitivity(double value, double sensitivity) {
        return value * sensitivity;
    }
    
    /**
     * Limita um valor a um intervalo
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Interpola linearmente entre dois valores
     */
    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
    
    /**
     * Interpola bilinearmente entre quatro valores
     */
    public static double bilinearLerp(double q11, double q12, double q21, double q22,
                                    double x1, double x2, double y1, double y2,
                                    double x, double y) {
        double x2x1 = x2 - x1;
        double y2y1 = y2 - y1;
        double x2x = x2 - x;
        double y2y = y2 - y;
        double yy1 = y - y1;
        double xx1 = x - x1;
        
        return 1.0 / (x2x1 * y2y1) * (
            q11 * x2x * y2y + q21 * xx1 * y2y + q12 * x2x * yy1 + q22 * xx1 * yy1
        );
    }
    
    /**
     * Calcula a velocidade entre dois pontos
     */
    public static double calculateVelocity(double x1, double y1, long time1,
                                        double x2, double y2, long time2) {
        if (time2 <= time1) {
            return 0.0;
        }
        
        double distance = calculateDistance(x1, y1, x2, y2);
        double timeDelta = (time2 - time1) / 1000.0; // Converte para segundos
        
        return timeDelta > 0 ? distance / timeDelta : 0.0;
    }
    
    /**
     * Calcula a aceleração entre duas velocidades
     */
    public static double calculateAcceleration(double velocity1, long time1,
                                            double velocity2, long time2) {
        if (time2 <= time1) {
            return 0.0;
        }
        
        double velocityDelta = velocity2 - velocity1;
        double timeDelta = (time2 - time1) / 1000.0; // Converte para segundos
        
        return timeDelta > 0 ? velocityDelta / timeDelta : 0.0;
    }
    
    /**
     * Calcula o centro de massa de um conjunto de pontos
     */
    public static double[] calculateCentroid(List<double[]> points) {
        if (points.isEmpty()) {
            return new double[]{0.0, 0.0};
        }
        
        double sumX = 0.0;
        double sumY = 0.0;
        
        for (double[] point : points) {
            if (point.length >= 2) {
                sumX += point[0];
                sumY += point[1];
            }
        }
        
        return new double[]{sumX / points.size(), sumY / points.size()};
    }
    
    /**
     * Calcula a área de um polígono usando a fórmula do shoelace
     */
    public static double calculatePolygonArea(List<double[]> points) {
        if (points.size() < 3) {
            return 0.0;
        }
        
        double area = 0.0;
        int n = points.size();
        
        for (int i = 0; i < n; i++) {
            double[] current = points.get(i);
            double[] next = points.get((i + 1) % n);
            
            area += current[0] * next[1];
            area -= next[0] * current[1];
        }
        
        return Math.abs(area) / 2.0;
    }
    
    /**
     * Verifica se um ponto está dentro de um polígono
     */
    public static boolean isPointInPolygon(double x, double y, List<double[]> polygon) {
        if (polygon.size() < 3) {
            return false;
        }
        
        boolean inside = false;
        int n = polygon.size();
        
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double[] current = polygon.get(i);
            double[] previous = polygon.get(j);
            
            if (((current[1] > y) != (previous[1] > y)) &&
                (x < (previous[0] - current[0]) * (y - current[1]) / 
                 (previous[1] - current[1]) + current[0])) {
                inside = !inside;
            }
        }
        
        return inside;
    }
    
    /**
     * Converte graus para radianos
     */
    public static double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180.0;
    }
    
    /**
     * Converte radianos para graus
     */
    public static double radiansToDegrees(double radians) {
        return radians * 180.0 / Math.PI;
    }
} 