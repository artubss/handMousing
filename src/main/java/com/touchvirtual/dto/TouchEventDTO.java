package com.touchvirtual.dto;

import com.touchvirtual.model.GestureType;
import com.touchvirtual.model.TouchEvent;

/**
 * DTO para eventos de toque
 * 
 * @author TouchVirtual Team
 * @version 1.0.0
 */
public class TouchEventDTO {
    
    private String id;
    private GestureType gestureType;
    private int screenX;
    private int screenY;
    private double pressure;
    private long timestamp;
    private TouchEvent.EventType eventType;
    private boolean isPrimary;
    private int pointerId;
    private String sessionId;
    
    public TouchEventDTO() {}
    
    public TouchEventDTO(TouchEvent touchEvent) {
        this.id = touchEvent.getId();
        this.gestureType = touchEvent.getGestureType();
        this.screenX = touchEvent.getScreenX();
        this.screenY = touchEvent.getScreenY();
        this.pressure = touchEvent.getPressure();
        this.timestamp = touchEvent.getTimestamp().toEpochSecond(java.time.ZoneOffset.UTC) * 1000;
        this.eventType = touchEvent.getEventType();
        this.isPrimary = touchEvent.isPrimary();
        this.pointerId = touchEvent.getPointerId();
    }
    
    /**
     * Converte para TouchEvent
     */
    public TouchEvent toTouchEvent() {
        TouchEvent event = new TouchEvent();
        event.setId(id);
        event.setGestureType(gestureType);
        event.setScreenX(screenX);
        event.setScreenY(screenY);
        event.setPressure(pressure);
        event.setEventType(eventType);
        event.setPrimary(isPrimary);
        event.setPointerId(pointerId);
        return event;
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
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public TouchEvent.EventType getEventType() { return eventType; }
    public void setEventType(TouchEvent.EventType eventType) { this.eventType = eventType; }
    
    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean primary) { isPrimary = primary; }
    
    public int getPointerId() { return pointerId; }
    public void setPointerId(int pointerId) { this.pointerId = pointerId; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
} 