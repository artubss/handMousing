package com.touchvirtual.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Dados de calibração para mapeamento de coordenadas
 * 
 * @author TouchVirtual Team
 * @version 1.0.0
 */
public class CalibrationData {
    
    private String id;
    private int screenWidth;
    private int screenHeight;
    private int cameraWidth;
    private int cameraHeight;
    private List<CalibrationPoint> calibrationPoints;
    private double sensitivity;
    private double deadband;
    private boolean isCalibrated;
    private long lastCalibrationTime;
    
    /**
     * Ponto de calibração para mapeamento de coordenadas
     */
    public static class CalibrationPoint {
        private double cameraX;
        private double cameraY;
        private int screenX;
        private int screenY;
        private double confidence;
        
        public CalibrationPoint() {}
        
        public CalibrationPoint(double cameraX, double cameraY, int screenX, int screenY) {
            this.cameraX = cameraX;
            this.cameraY = cameraY;
            this.screenX = screenX;
            this.screenY = screenY;
            this.confidence = 1.0;
        }
        
        // Getters e Setters
        public double getCameraX() { return cameraX; }
        public void setCameraX(double cameraX) { this.cameraX = cameraX; }
        
        public double getCameraY() { return cameraY; }
        public void setCameraY(double cameraY) { this.cameraY = cameraY; }
        
        public int getScreenX() { return screenX; }
        public void setScreenX(int screenX) { this.screenX = screenX; }
        
        public int getScreenY() { return screenY; }
        public void setScreenY(int screenY) { this.screenY = screenY; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
    }
    
    public CalibrationData() {
        this.calibrationPoints = new ArrayList<>();
        this.sensitivity = 1.0;
        this.deadband = 0.05;
        this.isCalibrated = false;
        this.lastCalibrationTime = System.currentTimeMillis();
    }
    
    /**
     * Adiciona um ponto de calibração
     */
    public void addCalibrationPoint(double cameraX, double cameraY, int screenX, int screenY) {
        CalibrationPoint point = new CalibrationPoint(cameraX, cameraY, screenX, screenY);
        calibrationPoints.add(point);
        
        // Considera calibrado se tiver pelo menos 4 pontos
        if (calibrationPoints.size() >= 4) {
            this.isCalibrated = true;
            this.lastCalibrationTime = System.currentTimeMillis();
        }
    }
    
    /**
     * Limpa todos os pontos de calibração
     */
    public void clearCalibration() {
        calibrationPoints.clear();
        this.isCalibrated = false;
    }
    
    /**
     * Converte coordenadas da câmera para coordenadas da tela
     */
    public int[] convertToScreenCoordinates(double cameraX, double cameraY) {
        if (!isCalibrated || calibrationPoints.isEmpty()) {
            // Fallback para mapeamento linear simples
            int screenX = (int) (cameraX * screenWidth);
            int screenY = (int) (cameraY * screenHeight);
            return new int[]{screenX, screenY};
        }
        
        // Implementação de interpolação bilinear ou transformação afim
        // Por simplicidade, usando média ponderada dos pontos mais próximos
        double totalWeight = 0;
        double weightedX = 0;
        double weightedY = 0;
        
        for (CalibrationPoint point : calibrationPoints) {
            double distance = Math.sqrt(
                Math.pow(cameraX - point.cameraX, 2) + 
                Math.pow(cameraY - point.cameraY, 2)
            );
            
            if (distance < 0.001) distance = 0.001; // Evita divisão por zero
            
            double weight = 1.0 / distance;
            totalWeight += weight;
            weightedX += point.screenX * weight;
            weightedY += point.screenY * weight;
        }
        
        if (totalWeight > 0) {
            int screenX = (int) (weightedX / totalWeight);
            int screenY = (int) (weightedY / totalWeight);
            return new int[]{screenX, screenY};
        }
        
        return new int[]{0, 0};
    }
    
    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public int getScreenWidth() { return screenWidth; }
    public void setScreenWidth(int screenWidth) { this.screenWidth = screenWidth; }
    
    public int getScreenHeight() { return screenHeight; }
    public void setScreenHeight(int screenHeight) { this.screenHeight = screenHeight; }
    
    public int getCameraWidth() { return cameraWidth; }
    public void setCameraWidth(int cameraWidth) { this.cameraWidth = cameraWidth; }
    
    public int getCameraHeight() { return cameraHeight; }
    public void setCameraHeight(int cameraHeight) { this.cameraHeight = cameraHeight; }
    
    public List<CalibrationPoint> getCalibrationPoints() { return calibrationPoints; }
    public void setCalibrationPoints(List<CalibrationPoint> calibrationPoints) { 
        this.calibrationPoints = calibrationPoints; 
    }
    
    public double getSensitivity() { return sensitivity; }
    public void setSensitivity(double sensitivity) { this.sensitivity = sensitivity; }
    
    public double getDeadband() { return deadband; }
    public void setDeadband(double deadband) { this.deadband = deadband; }
    
    public boolean isCalibrated() { return isCalibrated; }
    public void setCalibrated(boolean calibrated) { isCalibrated = calibrated; }
    
    public long getLastCalibrationTime() { return lastCalibrationTime; }
    public void setLastCalibrationTime(long lastCalibrationTime) { 
        this.lastCalibrationTime = lastCalibrationTime; 
    }
} 