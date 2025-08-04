package com.touchvirtual.dto;

import com.touchvirtual.model.CalibrationData;
import java.util.List;

/**
 * DTO para requisições de calibração
 * 
 * @author TouchVirtual Team
 * @version 1.0.0
 */
public class CalibrationRequest {
    
    private String sessionId;
    private int screenWidth;
    private int screenHeight;
    private int cameraWidth;
    private int cameraHeight;
    private List<CalibrationPoint> calibrationPoints;
    private double sensitivity;
    private double deadband;
    private boolean autoCalibrate;
    
    /**
     * Ponto de calibração para requisição
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
    
    public CalibrationRequest() {
        this.autoCalibrate = true;
        this.sensitivity = 1.0;
        this.deadband = 0.05;
    }
    
    /**
     * Converte para CalibrationData
     */
    public CalibrationData toCalibrationData() {
        CalibrationData data = new CalibrationData();
        data.setScreenWidth(screenWidth);
        data.setScreenHeight(screenHeight);
        data.setCameraWidth(cameraWidth);
        data.setCameraHeight(cameraHeight);
        data.setSensitivity(sensitivity);
        data.setDeadband(deadband);
        
        if (calibrationPoints != null) {
            for (CalibrationPoint point : calibrationPoints) {
                data.addCalibrationPoint(
                    point.cameraX, 
                    point.cameraY, 
                    point.screenX, 
                    point.screenY
                );
            }
        }
        
        return data;
    }
    
    // Getters e Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
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
    
    public boolean isAutoCalibrate() { return autoCalibrate; }
    public void setAutoCalibrate(boolean autoCalibrate) { this.autoCalibrate = autoCalibrate; }
} 