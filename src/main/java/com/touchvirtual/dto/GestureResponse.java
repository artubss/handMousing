package com.touchvirtual.dto;

import com.touchvirtual.model.GestureType;
import com.touchvirtual.model.HandLandmark;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * DTO para resposta de gestos detectados
 * 
 * @author TouchVirtual Team
 * @version 1.0.0
 */
public class GestureResponse {
    
    private String sessionId;
    private GestureType gestureType;
    private double confidence;
    private int screenX;
    private int screenY;
    private double pressure;
    private long timestamp;
    private List<HandLandmark> landmarks;
    private Map<String, Object> metadata;
    private boolean isHandDetected;
    private int handCount;
    
    public GestureResponse() {
        this.metadata = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    public GestureResponse(GestureType gestureType, double confidence) {
        this();
        this.gestureType = gestureType;
        this.confidence = confidence;
    }
    
    /**
     * Adiciona metadados à resposta
     */
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    /**
     * Obtém metadados da resposta
     */
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    /**
     * Obtém metadados com valor padrão
     */
    public Object getMetadata(String key, Object defaultValue) {
        return metadata.getOrDefault(key, defaultValue);
    }
    
    // Getters e Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public GestureType getGestureType() { return gestureType; }
    public void setGestureType(GestureType gestureType) { this.gestureType = gestureType; }
    
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    
    public int getScreenX() { return screenX; }
    public void setScreenX(int screenX) { this.screenX = screenX; }
    
    public int getScreenY() { return screenY; }
    public void setScreenY(int screenY) { this.screenY = screenY; }
    
    public double getPressure() { return pressure; }
    public void setPressure(double pressure) { this.pressure = pressure; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public List<HandLandmark> getLandmarks() { return landmarks; }
    public void setLandmarks(List<HandLandmark> landmarks) { this.landmarks = landmarks; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public boolean isHandDetected() { return isHandDetected; }
    public void setHandDetected(boolean handDetected) { isHandDetected = handDetected; }
    
    public int getHandCount() { return handCount; }
    public void setHandCount(int handCount) { this.handCount = handCount; }
} 