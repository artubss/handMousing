package com.touchvirtual.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa um evento de toque simulado na tela
 * 
 * @author TouchVirtual Team
 * @version 1.0.0
 */
public class TouchEvent {
    
    private String id;
    private GestureType gestureType;
    private int screenX;
    private int screenY;
    private double pressure;
    private LocalDateTime timestamp;
    private boolean isPrimary;
    private int pointerId;
    private EventType eventType;
    
    public enum EventType {
        MOUSE_MOVE,
        MOUSE_CLICK,
        MOUSE_RIGHT_CLICK,
        MOUSE_DOUBLE_CLICK,
        MOUSE_DRAG_START,
        MOUSE_DRAG_MOVE,
        MOUSE_DRAG_END,
        SCROLL_VERTICAL,
        SCROLL_HORIZONTAL,
        ZOOM_IN,
        ZOOM_OUT,
        KEY_PRESS,
        KEY_RELEASE
    }
    
    public TouchEvent() {
        this.timestamp = LocalDateTime.now();
        this.id = generateId();
    }
    
    public TouchEvent(GestureType gestureType, int screenX, int screenY) {
        this();
        this.gestureType = gestureType;
        this.screenX = screenX;
        this.screenY = screenY;
    }
    
    private String generateId() {
        return "event_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public GestureType getGestureType() { return gestureType; }
    public void setGestureType(GestureType gestureType) { this.gestureType = gestureType; }
    
    public int getScreenX() { return screenX; }
    public void setScreenX(int screenX) { this.screenX = screenX; }
    
    public int getScreenY() { return screenY; }
    public void setScreenY(int screenY) { this.screenY = screenY; }
    
    public double getPressure() { return pressure; }
    public void setPressure(double pressure) { this.pressure = pressure; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean primary) { isPrimary = primary; }
    
    public int getPointerId() { return pointerId; }
    public void setPointerId(int pointerId) { this.pointerId = pointerId; }
    
    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TouchEvent that = (TouchEvent) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("TouchEvent{id='%s', gesture=%s, pos=(%d,%d), type=%s, time=%s}", 
                           id, gestureType, screenX, screenY, eventType, timestamp);
    }
} 