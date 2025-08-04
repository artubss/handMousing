package com.touchvirtual.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Configurações do usuário para personalização do sistema
 * 
 * @author TouchVirtual Team
 * @version 1.0.0
 */
public class UserSettings {
    
    private String userId;
    private String profileName;
    private double sensitivity;
    private double deadband;
    private boolean leftHanded;
    private boolean enableSound;
    private boolean enableVibration;
    private Map<GestureType, Boolean> enabledGestures;
    private Map<String, Object> customSettings;
    
    public UserSettings() {
        this.enabledGestures = new HashMap<>();
        this.customSettings = new HashMap<>();
        this.sensitivity = 1.0;
        this.deadband = 0.05;
        this.leftHanded = false;
        this.enableSound = true;
        this.enableVibration = false;
        
        // Habilita todos os gestos por padrão
        for (GestureType gesture : GestureType.values()) {
            enabledGestures.put(gesture, true);
        }
    }
    
    /**
     * Verifica se um gesto específico está habilitado
     */
    public boolean isGestureEnabled(GestureType gestureType) {
        return enabledGestures.getOrDefault(gestureType, false);
    }
    
    /**
     * Habilita ou desabilita um gesto específico
     */
    public void setGestureEnabled(GestureType gestureType, boolean enabled) {
        enabledGestures.put(gestureType, enabled);
    }
    
    /**
     * Define uma configuração customizada
     */
    public void setCustomSetting(String key, Object value) {
        customSettings.put(key, value);
    }
    
    /**
     * Obtém uma configuração customizada
     */
    public Object getCustomSetting(String key) {
        return customSettings.get(key);
    }
    
    /**
     * Obtém uma configuração customizada com valor padrão
     */
    public Object getCustomSetting(String key, Object defaultValue) {
        return customSettings.getOrDefault(key, defaultValue);
    }
    
    // Getters e Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getProfileName() { return profileName; }
    public void setProfileName(String profileName) { this.profileName = profileName; }
    
    public double getSensitivity() { return sensitivity; }
    public void setSensitivity(double sensitivity) { this.sensitivity = sensitivity; }
    
    public double getDeadband() { return deadband; }
    public void setDeadband(double deadband) { this.deadband = deadband; }
    
    public boolean isLeftHanded() { return leftHanded; }
    public void setLeftHanded(boolean leftHanded) { this.leftHanded = leftHanded; }
    
    public boolean isEnableSound() { return enableSound; }
    public void setEnableSound(boolean enableSound) { this.enableSound = enableSound; }
    
    public boolean isEnableVibration() { return enableVibration; }
    public void setEnableVibration(boolean enableVibration) { this.enableVibration = enableVibration; }
    
    public Map<GestureType, Boolean> getEnabledGestures() { return enabledGestures; }
    public void setEnabledGestures(Map<GestureType, Boolean> enabledGestures) { 
        this.enabledGestures = enabledGestures; 
    }
    
    public Map<String, Object> getCustomSettings() { return customSettings; }
    public void setCustomSettings(Map<String, Object> customSettings) { 
        this.customSettings = customSettings; 
    }
} 