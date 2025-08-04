package com.touchvirtual.controller;

import com.touchvirtual.dto.GestureResponse;
import com.touchvirtual.model.GestureType;
import com.touchvirtual.model.HandLandmark;
import com.touchvirtual.model.CalibrationData;
import com.touchvirtual.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller WebSocket para comunicação em tempo real
 * 
 * @author TouchVirtual Team
 * @version 1.0.0
 */
@Controller
public class WebSocketController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
    
    @Autowired
    private HandDetectionService handDetectionService;
    
    @Autowired
    private GestureRecognitionService gestureRecognitionService;
    
    @Autowired
    private CoordinateMappingService coordinateMappingService;
    
    @Autowired
    private MouseSimulationService mouseSimulationService;
    
    @Autowired
    private CalibrationService calibrationService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // Armazena sessões ativas
    private Map<String, String> activeSessions = new ConcurrentHashMap<>();
    
    /**
     * Endpoint para receber mensagens de gestos
     */
    @MessageMapping("/gestures")
    @SendTo("/topic/gestures")
    public GestureResponse handleGesture(String message) {
        try {
            // Obtém landmarks da mão detectada
            List<HandLandmark> landmarks = handDetectionService.getLastDetectedLandmarks();
            
            // Reconhece o gesto
            GestureType gesture = gestureRecognitionService.recognizeGesture(landmarks);
            
            // Cria resposta
            GestureResponse response = new GestureResponse(gesture, 
                handDetectionService.getLastDetectionConfidence());
            
            response.setLandmarks(landmarks);
            response.setHandDetected(handDetectionService.isHandDetected());
            response.setHandCount(handDetectionService.getHandCount());
            
            // Mapeia coordenadas da tela se houver mão detectada
            if (!landmarks.isEmpty()) {
                int[] screenCoords = coordinateMappingService.mapHandLandmarksToScreen(landmarks);
                response.setScreenX(screenCoords[0]);
                response.setScreenY(screenCoords[1]);
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Erro ao processar gesto via WebSocket: {}", e.getMessage());
            
            // Retorna resposta de erro
            GestureResponse errorResponse = new GestureResponse(GestureType.UNCERTAIN, 0.0);
            errorResponse.addMetadata("error", e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * Endpoint para receber comandos de controle
     */
    @MessageMapping("/control")
    @SendTo("/topic/control")
    public Map<String, Object> handleControl(Map<String, Object> command) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String action = (String) command.get("action");
            
            switch (action) {
                case "enable_mouse":
                    boolean enabled = (Boolean) command.get("enabled");
                    mouseSimulationService.setEnabled(enabled);
                    response.put("success", true);
                    response.put("message", "Mouse " + (enabled ? "habilitado" : "desabilitado"));
                    break;
                    
                case "start_calibration":
                    String sessionId = (String) command.get("sessionId");
                    boolean success = calibrationService.startCalibration(sessionId);
                    response.put("success", success);
                    response.put("message", success ? "Calibração iniciada" : "Erro ao iniciar calibração");
                    break;
                    
                case "stop_calibration":
                    calibrationService.stopCalibration();
                    response.put("success", true);
                    response.put("message", "Calibração parada");
                    break;
                    
                case "get_status":
                    response.put("handDetected", handDetectionService.isHandDetected());
                    response.put("handCount", handDetectionService.getHandCount());
                    response.put("detectionConfidence", handDetectionService.getLastDetectionConfidence());
                    response.put("lastGesture", gestureRecognitionService.getLastRecognizedGesture().getDisplayName());
                    response.put("gestureConfidence", gestureRecognitionService.getGestureConfidence());
                    response.put("mouseEnabled", mouseSimulationService.isEnabled());
                    response.put("calibrated", coordinateMappingService.isCalibrated());
                    response.put("isCalibrating", calibrationService.isCalibrating());
                    break;
                    
                default:
                    response.put("success", false);
                    response.put("error", "Ação não reconhecida: " + action);
            }
            
        } catch (Exception e) {
            logger.error("❌ Erro ao processar comando de controle: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Envia dados de landmarks em tempo real (executado a cada 100ms)
     */
    @Scheduled(fixedRate = 100)
    public void sendLandmarksUpdate() {
        try {
            List<HandLandmark> landmarks = handDetectionService.getLastDetectedLandmarks();
            
            Map<String, Object> update = new HashMap<>();
            update.put("landmarks", landmarks);
            update.put("handDetected", handDetectionService.isHandDetected());
            update.put("handCount", handDetectionService.getHandCount());
            update.put("confidence", handDetectionService.getLastDetectionConfidence());
            update.put("timestamp", System.currentTimeMillis());
            
            // Envia para todos os clientes inscritos
            messagingTemplate.convertAndSend("/topic/landmarks", update);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao enviar atualização de landmarks: {}", e.getMessage());
        }
    }
    
    /**
     * Envia atualizações de gestos em tempo real (executado a cada 200ms)
     */
    @Scheduled(fixedRate = 200)
    public void sendGestureUpdate() {
        try {
            List<HandLandmark> landmarks = handDetectionService.getLastDetectedLandmarks();
            GestureType gesture = gestureRecognitionService.recognizeGesture(landmarks);
            
            GestureResponse response = new GestureResponse(gesture, 
                handDetectionService.getLastDetectionConfidence());
            
            response.setLandmarks(landmarks);
            response.setHandDetected(handDetectionService.isHandDetected());
            response.setHandCount(handDetectionService.getHandCount());
            
            // Mapeia coordenadas da tela
            if (!landmarks.isEmpty()) {
                int[] screenCoords = coordinateMappingService.mapHandLandmarksToScreen(landmarks);
                response.setScreenX(screenCoords[0]);
                response.setScreenY(screenCoords[1]);
            }
            
            // Envia para todos os clientes inscritos
            messagingTemplate.convertAndSend("/topic/gestures", response);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao enviar atualização de gestos: {}", e.getMessage());
        }
    }
    
    /**
     * Envia estatísticas de performance (executado a cada 1 segundo)
     */
    @Scheduled(fixedRate = 1000)
    public void sendPerformanceStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            stats.put("handDetectionConfidence", handDetectionService.getLastDetectionConfidence());
            stats.put("gestureConfidence", gestureRecognitionService.getGestureConfidence());
            stats.put("lastGestureTime", gestureRecognitionService.getLastGestureTime());
            stats.put("isCalibrated", coordinateMappingService.isCalibrated());
            stats.put("mouseEnabled", mouseSimulationService.isEnabled());
            stats.put("isCalibrating", calibrationService.isCalibrating());
            stats.put("lastMouseX", mouseSimulationService.getLastX());
            stats.put("lastMouseY", mouseSimulationService.getLastY());
            stats.put("isDragging", mouseSimulationService.isDragging());
            stats.put("timestamp", System.currentTimeMillis());
            
            // Envia para todos os clientes inscritos
            messagingTemplate.convertAndSend("/topic/stats", stats);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao enviar estatísticas: {}", e.getMessage());
        }
    }
    
    /**
     * Envia informações de calibração (executado a cada 2 segundos)
     */
    @Scheduled(fixedRate = 2000)
    public void sendCalibrationInfo() {
        try {
            Map<String, Object> info = new HashMap<>();
            
            info.put("isCalibrating", calibrationService.isCalibrating());
            info.put("currentSessionId", calibrationService.getCurrentSessionId());
            info.put("isCalibrated", coordinateMappingService.isCalibrated());
            
            CalibrationData currentData = calibrationService.getCurrentCalibration();
            info.put("pointCount", currentData.getCalibrationPoints().size());
            info.put("sensitivity", currentData.getSensitivity());
            info.put("deadband", currentData.getDeadband());
            info.put("timestamp", System.currentTimeMillis());
            
            // Envia para todos os clientes inscritos
            messagingTemplate.convertAndSend("/topic/calibration", info);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao enviar informações de calibração: {}", e.getMessage());
        }
    }
    
    /**
     * Envia dados de mouse em tempo real (executado a cada 100ms)
     */
    @Scheduled(fixedRate = 100)
    public void sendMouseInfo() {
        try {
            Map<String, Object> mouseInfo = new HashMap<>();
            
            mouseInfo.put("enabled", mouseSimulationService.isEnabled());
            mouseInfo.put("lastX", mouseSimulationService.getLastX());
            mouseInfo.put("lastY", mouseSimulationService.getLastY());
            mouseInfo.put("isDragging", mouseSimulationService.isDragging());
            mouseInfo.put("lastEventTime", mouseSimulationService.getLastEventTime());
            
            if (mouseSimulationService.isDragging()) {
                int[] dragStart = mouseSimulationService.getDragStartPosition();
                mouseInfo.put("dragStartX", dragStart[0]);
                mouseInfo.put("dragStartY", dragStart[1]);
            }
            
            mouseInfo.put("timestamp", System.currentTimeMillis());
            
            // Envia para todos os clientes inscritos
            messagingTemplate.convertAndSend("/topic/mouse", mouseInfo);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao enviar informações do mouse: {}", e.getMessage());
        }
    }
    
    /**
     * Registra uma nova sessão
     */
    public void registerSession(String sessionId, String userId) {
        activeSessions.put(sessionId, userId);
        logger.info("📝 Nova sessão registrada: {} -> {}", sessionId, userId);
    }
    
    /**
     * Remove uma sessão
     */
    public void unregisterSession(String sessionId) {
        activeSessions.remove(sessionId);
        logger.info("🗑️ Sessão removida: {}", sessionId);
    }
    
    /**
     * Obtém sessões ativas
     */
    public Map<String, String> getActiveSessions() {
        return new ConcurrentHashMap<>(activeSessions);
    }
} 