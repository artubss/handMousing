package com.touchvirtual.model;

/**
 * Enumeração dos tipos de gestos reconhecidos pelo sistema
 * 
 * @author TouchVirtual Team
 * @version 1.0.0
 */
public enum GestureType {
    
    // Gestos básicos de navegação
    CURSOR_MOVE("Cursor Move", "Mão aberta movendo"),
    CLICK("Click", "Dedo indicador dobrado rapidamente"),
    RIGHT_CLICK("Right Click", "Dois dedos (indicador + médio) dobrados"),
    DOUBLE_CLICK("Double Click", "Dois taps rápidos com indicador"),
    
    // Gestos de manipulação
    DRAG_START("Drag Start", "Gesto de pinça iniciado"),
    DRAG_MOVE("Drag Move", "Arrastando com pinça mantida"),
    DRAG_END("Drag End", "Soltando pinça"),
    
    // Gestos de scroll
    SCROLL_VERTICAL("Scroll Vertical", "Movimento vertical com mão fechada"),
    SCROLL_HORIZONTAL("Scroll Horizontal", "Movimento horizontal com mão fechada"),
    
    // Gestos de zoom
    ZOOM_IN("Zoom In", "Pinça abrindo"),
    ZOOM_OUT("Zoom Out", "Pinça fechando"),
    
    // Gestos especiais
    ROTATE("Rotate", "Rotação com dois dedos"),
    SWIPE_LEFT("Swipe Left", "Deslizar para esquerda"),
    SWIPE_RIGHT("Swipe Right", "Deslizar para direita"),
    SWIPE_UP("Swipe Up", "Deslizar para cima"),
    SWIPE_DOWN("Swipe Down", "Deslizar para baixo"),
    
    // Estados especiais
    NO_HAND("No Hand", "Nenhuma mão detectada"),
    MULTIPLE_HANDS("Multiple Hands", "Múltiplas mãos detectadas"),
    UNCERTAIN("Uncertain", "Gesto não reconhecido");
    
    private final String displayName;
    private final String description;
    
    GestureType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Verifica se o gesto é um gesto de clique
     */
    public boolean isClickGesture() {
        return this == CLICK || this == RIGHT_CLICK || this == DOUBLE_CLICK;
    }
    
    /**
     * Verifica se o gesto é um gesto de arrastar
     */
    public boolean isDragGesture() {
        return this == DRAG_START || this == DRAG_MOVE || this == DRAG_END;
    }
    
    /**
     * Verifica se o gesto é um gesto de scroll
     */
    public boolean isScrollGesture() {
        return this == SCROLL_VERTICAL || this == SCROLL_HORIZONTAL;
    }
    
    /**
     * Verifica se o gesto é um gesto de zoom
     */
    public boolean isZoomGesture() {
        return this == ZOOM_IN || this == ZOOM_OUT;
    }
    
    /**
     * Verifica se o gesto é um gesto de swipe
     */
    public boolean isSwipeGesture() {
        return this == SWIPE_LEFT || this == SWIPE_RIGHT || 
               this == SWIPE_UP || this == SWIPE_DOWN;
    }
} 