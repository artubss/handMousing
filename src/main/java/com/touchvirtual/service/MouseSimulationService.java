package com.touchvirtual.service;

import com.touchvirtual.model.GestureType;
import com.touchvirtual.model.TouchEvent;
import org.springframework.beans.factory.annotation.Autowired;
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
            throw new RuntimeException("Falha na inicialização do Robot", e);
        }
    }
    
    /**
     * Processa um evento de toque e simula o evento de mouse correspondente
     */
    public void processTouchEvent(TouchEvent touchEvent) {
        if (!isEnabled.get()) {
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
        int[] screenCoords = coordinateMappingService.mapToScreenCoordinates(
            touchEvent.getScreenX(), touchEvent.getScreenY());
        
        if (screenCoords[0] != lastX || screenCoords[1] != lastY) {
            robot.mouseMove(screenCoords[0], screenCoords[1]);
            lastX = screenCoords[0];
            lastY = screenCoords[1];
            
            logger.debug("🖱️ Mouse movido para: ({}, {})", lastX, lastY);
        }
    }
    
    /**
     * Manipula clique do mouse
     */
    private void handleMouseClick(TouchEvent touchEvent) {
        int[] screenCoords = coordinateMappingService.mapToScreenCoordinates(
            touchEvent.getScreenX(), touchEvent.getScreenY());
        
        // Move para a posição
        robot.mouseMove(screenCoords[0], screenCoords[1]);
        
        // Simula clique
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(CLICK_DELAY);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        
        lastX = screenCoords[0];
        lastY = screenCoords[1];
        
        logger.debug("🖱️ Clique em: ({}, {})", lastX, lastY);
    }
    
    /**
     * Manipula clique direito
     */
    private void handleRightClick(TouchEvent touchEvent) {
        int[] screenCoords = coordinateMappingService.mapToScreenCoordinates(
            touchEvent.getScreenX(), touchEvent.getScreenY());
        
        // Move para a posição
        robot.mouseMove(screenCoords[0], screenCoords[1]);
        
        // Simula clique direito
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.delay(CLICK_DELAY);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        
        lastX = screenCoords[0];
        lastY = screenCoords[1];
        
        logger.debug("🖱️ Clique direito em: ({}, {})", lastX, lastY);
    }
    
    /**
     * Manipula duplo clique
     */
    private void handleDoubleClick(TouchEvent touchEvent) {
        int[] screenCoords = coordinateMappingService.mapToScreenCoordinates(
            touchEvent.getScreenX(), touchEvent.getScreenY());
        
        // Move para a posição
        robot.mouseMove(screenCoords[0], screenCoords[1]);
        
        // Simula duplo clique
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(CLICK_DELAY);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(DOUBLE_CLICK_DELAY);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(CLICK_DELAY);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        
        lastX = screenCoords[0];
        lastY = screenCoords[1];
        
        logger.debug("🖱️ Duplo clique em: ({}, {})", lastX, lastY);
    }
    
    /**
     * Inicia arrastar
     */
    private void handleDragStart(TouchEvent touchEvent) {
        int[] screenCoords = coordinateMappingService.mapToScreenCoordinates(
            touchEvent.getScreenX(), touchEvent.getScreenY());
        
        // Move para a posição inicial
        robot.mouseMove(screenCoords[0], screenCoords[1]);
        
        // Pressiona o botão
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        
        isDragging = true;
        dragStartX = screenCoords[0];
        dragStartY = screenCoords[1];
        lastX = screenCoords[0];
        lastY = screenCoords[1];
        
        logger.debug("🖱️ Iniciando arrastar em: ({}, {})", lastX, lastY);
    }
    
    /**
     * Move durante arrastar
     */
    private void handleDragMove(TouchEvent touchEvent) {
        if (!isDragging) {
            return;
        }
        
        int[] screenCoords = coordinateMappingService.mapToScreenCoordinates(
            touchEvent.getScreenX(), touchEvent.getScreenY());
        
        // Move para a nova posição (botão ainda pressionado)
        robot.mouseMove(screenCoords[0], screenCoords[1]);
        
        lastX = screenCoords[0];
        lastY = screenCoords[1];
        
        logger.debug("🖱️ Arrastando para: ({}, {})", lastX, lastY);
    }
    
    /**
     * Finaliza arrastar
     */
    private void handleDragEnd(TouchEvent touchEvent) {
        if (!isDragging) {
            return;
        }
        
        int[] screenCoords = coordinateMappingService.mapToScreenCoordinates(
            touchEvent.getScreenX(), touchEvent.getScreenY());
        
        // Move para a posição final
        robot.mouseMove(screenCoords[0], screenCoords[1]);
        
        // Solta o botão
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        
        isDragging = false;
        lastX = screenCoords[0];
        lastY = screenCoords[1];
        
        logger.debug("🖱️ Finalizando arrastar em: ({}, {})", lastX, lastY);
    }
    
    /**
     * Manipula scroll vertical
     */
    private void handleScrollVertical(TouchEvent touchEvent) {
        // Simula scroll vertical usando roda do mouse
        robot.mouseWheel(-3); // Scroll para cima
        
        logger.debug("📜 Scroll vertical");
    }
    
    /**
     * Manipula scroll horizontal
     */
    private void handleScrollHorizontal(TouchEvent touchEvent) {
        // Simula scroll horizontal usando Shift + roda do mouse
        robot.keyPress(KeyEvent.VK_SHIFT);
        robot.mouseWheel(-3);
        robot.keyRelease(KeyEvent.VK_SHIFT);
        
        logger.debug("📜 Scroll horizontal");
    }
    
    /**
     * Manipula zoom in (Ctrl + +)
     */
    private void handleZoomIn(TouchEvent touchEvent) {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_ADD);
        robot.keyRelease(KeyEvent.VK_ADD);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        
        logger.debug("🔍 Zoom in");
    }
    
    /**
     * Manipula zoom out (Ctrl + -)
     */
    private void handleZoomOut(TouchEvent touchEvent) {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_MINUS);
        robot.keyRelease(KeyEvent.VK_MINUS);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        
        logger.debug("🔍 Zoom out");
    }
    
    /**
     * Habilita ou desabilita a simulação
     */
    public void setEnabled(boolean enabled) {
        isEnabled.set(enabled);
        logger.info("🔄 Simulação de mouse {}", enabled ? "habilitada" : "desabilitada");
    }
    
    /**
     * Verifica se a simulação está habilitada
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