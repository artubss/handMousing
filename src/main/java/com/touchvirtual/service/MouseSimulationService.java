package com.touchvirtual.service;

import com.touchvirtual.model.GestureType;
import com.touchvirtual.model.TouchEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Serviço de simulação de eventos de mouse usando Java Robot
 * 
 * @author TouchVirtual Team
 * @version 1.0.0
 */
@Service
public class MouseSimulationService {
    
    private static final Logger logger = LoggerFactory.getLogger(MouseSimulationService.class);
    
    @Autowired
    @Lazy
    private CoordinateMappingService coordinateMappingService;
    
    private Robot robot;
    private AtomicBoolean isEnabled;
    private int lastX, lastY;
    private long lastEventTime;
    private boolean isDragging;
    private int dragStartX, dragStartY;
    
    // Constantes para controle de eventos
    private static final long MIN_EVENT_INTERVAL = 50; // 50ms entre eventos
    private static final int CLICK_DELAY = 10; // 10ms entre press e release
    private static final int DOUBLE_CLICK_DELAY = 300; // 300ms para duplo clique
    
    public MouseSimulationService() {
        this.isEnabled = new AtomicBoolean(true);
        this.lastX = 0;
        this.lastY = 0;
        this.lastEventTime = 0;
        this.isDragging = false;
        this.dragStartX = 0;
        this.dragStartY = 0;
        
        initializeRobot();
    }
    
    /**
     * Inicializa o Robot para simulação de eventos
     */
    private void initializeRobot() {
        try {
            robot = new Robot();
            robot.setAutoDelay(0); // Sem delay automático
            robot.setAutoWaitForIdle(false); // Não espera idle
            
            logger.info("🤖 Robot inicializado para simulação de eventos");
            
        } catch (AWTException e) {
            logger.error("❌ Erro ao inicializar Robot: {}", e.getMessage());
            // Não lança exceção, apenas loga o erro
        }
    }
    
    /**
     * Processa um evento de toque e simula o evento de mouse correspondente
     */
    public void processTouchEvent(TouchEvent touchEvent) {
        if (!isEnabled.get() || robot == null) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        // Verifica se há tempo suficiente desde o último evento
        if (currentTime - lastEventTime < MIN_EVENT_INTERVAL) {
            return;
        }
        
        try {
            switch (touchEvent.getEventType()) {
                case MOUSE_MOVE:
                    handleMouseMove(touchEvent);
                    break;
                case MOUSE_CLICK:
                    handleMouseClick(touchEvent);
                    break;
                case MOUSE_RIGHT_CLICK:
                    handleRightClick(touchEvent);
                    break;
                case MOUSE_DOUBLE_CLICK:
                    handleDoubleClick(touchEvent);
                    break;
                case MOUSE_DRAG_START:
                    handleDragStart(touchEvent);
                    break;
                case MOUSE_DRAG_MOVE:
                    handleDragMove(touchEvent);
                    break;
                case MOUSE_DRAG_END:
                    handleDragEnd(touchEvent);
                    break;
                case SCROLL_VERTICAL:
                    handleScrollVertical(touchEvent);
                    break;
                case SCROLL_HORIZONTAL:
                    handleScrollHorizontal(touchEvent);
                    break;
                case ZOOM_IN:
                    handleZoomIn(touchEvent);
                    break;
                case ZOOM_OUT:
                    handleZoomOut(touchEvent);
                    break;
                default:
                    logger.debug("⚠️ Tipo de evento não suportado: {}", touchEvent.getEventType());
                    break;
            }
            
            lastEventTime = currentTime;
            
        } catch (Exception e) {
            logger.error("❌ Erro ao processar evento de toque: {}", e.getMessage());
        }
    }
    
    /**
     * Manipula movimento do mouse
     */
    private void handleMouseMove(TouchEvent touchEvent) {
        if (coordinateMappingService == null) {
            return;
        }
        
        int[] screenCoords = coordinateMappingService.mapToScreenCoordinates(
            touchEvent.getX(), touchEvent.getY());
        
        robot.mouseMove(screenCoords[0], screenCoords[1]);
        lastX = screenCoords[0];
        lastY = screenCoords[1];
    }
    
    /**
     * Manipula clique do mouse
     */
    private void handleMouseClick(TouchEvent touchEvent) {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(CLICK_DELAY);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }
    
    /**
     * Manipula clique direito do mouse
     */
    private void handleRightClick(TouchEvent touchEvent) {
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.delay(CLICK_DELAY);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
    }
    
    /**
     * Manipula duplo clique do mouse
     */
    private void handleDoubleClick(TouchEvent touchEvent) {
        handleMouseClick(touchEvent);
        robot.delay(DOUBLE_CLICK_DELAY);
        handleMouseClick(touchEvent);
    }
    
    /**
     * Manipula início do arrastar
     */
    private void handleDragStart(TouchEvent touchEvent) {
        isDragging = true;
        dragStartX = lastX;
        dragStartY = lastY;
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    }
    
    /**
     * Manipula movimento durante arrastar
     */
    private void handleDragMove(TouchEvent touchEvent) {
        if (!isDragging) {
            return;
        }
        
        if (coordinateMappingService == null) {
            return;
        }
        
        int[] screenCoords = coordinateMappingService.mapToScreenCoordinates(
            touchEvent.getX(), touchEvent.getY());
        
        robot.mouseMove(screenCoords[0], screenCoords[1]);
        lastX = screenCoords[0];
        lastY = screenCoords[1];
    }
    
    /**
     * Manipula fim do arrastar
     */
    private void handleDragEnd(TouchEvent touchEvent) {
        isDragging = false;
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }
    
    /**
     * Manipula scroll vertical
     */
    private void handleScrollVertical(TouchEvent touchEvent) {
        int scrollAmount = (int) (touchEvent.getY() * 3); // Sensibilidade do scroll
        robot.mouseWheel(scrollAmount);
    }
    
    /**
     * Manipula scroll horizontal
     */
    private void handleScrollHorizontal(TouchEvent touchEvent) {
        // Simula scroll horizontal com Ctrl + scroll
        robot.keyPress(KeyEvent.VK_CONTROL);
        int scrollAmount = (int) (touchEvent.getX() * 3);
        robot.mouseWheel(scrollAmount);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }
    
    /**
     * Manipula zoom in
     */
    private void handleZoomIn(TouchEvent touchEvent) {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.mouseWheel(-3);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }
    
    /**
     * Manipula zoom out
     */
    private void handleZoomOut(TouchEvent touchEvent) {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.mouseWheel(3);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }
    
    /**
     * Habilita ou desabilita o serviço
     */
    public void setEnabled(boolean enabled) {
        isEnabled.set(enabled);
        logger.info("🤖 Mouse simulation {}", enabled ? "enabled" : "disabled");
    }
    
    /**
     * Verifica se o serviço está habilitado
     */
    public boolean isEnabled() {
        return isEnabled.get();
    }
    
    /**
     * Obtém a última posição X
     */
    public int getLastX() {
        return lastX;
    }
    
    /**
     * Obtém a última posição Y
     */
    public int getLastY() {
        return lastY;
    }
    
    /**
     * Verifica se está arrastando
     */
    public boolean isDragging() {
        return isDragging;
    }
    
    /**
     * Obtém a posição inicial do arrastar
     */
    public int[] getDragStartPosition() {
        return new int[]{dragStartX, dragStartY};
    }
    
    /**
     * Obtém o tempo do último evento
     */
    public long getLastEventTime() {
        return lastEventTime;
    }
} 