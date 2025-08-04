package com.touchvirtual.model;

import java.util.Objects;

/**
 * Representa um ponto de referência (landmark) da mão
 * 
 * @author TouchVirtual Team
 * @version 1.0.0
 */
public class HandLandmark {
    
    private int id;
    private double x;
    private double y;
    private double z;
    private double confidence;
    private LandmarkType type;
    
    public enum LandmarkType {
        WRIST(0),
        THUMB_TIP(4),
        INDEX_FINGER_TIP(8),
        MIDDLE_FINGER_TIP(12),
        RING_FINGER_TIP(16),
        PINKY_TIP(20);
        
        private final int value;
        
        LandmarkType(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    public HandLandmark() {}
    
    public HandLandmark(int id, double x, double y, double z, double confidence) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.confidence = confidence;
    }
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    
    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }
    
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    
    public LandmarkType getType() { return type; }
    public void setType(LandmarkType type) { this.type = type; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HandLandmark that = (HandLandmark) o;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "HandLandmark{id=%d, x=%.2f, y=%.2f, z=%.2f, conf=%.2f}".formatted(
                id, x, y, z, confidence);
    }
} 