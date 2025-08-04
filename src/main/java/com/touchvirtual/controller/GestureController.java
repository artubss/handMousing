package com.touchvirtual.controller;

import com.touchvirtual.dto.GestureResponse;
import com.touchvirtual.dto.TouchEventDTO;
import com.touchvirtual.model.GestureType;
import com.touchvirtual.model.HandLandmark;
import com.touchvirtual.model.TouchEvent;
import com.touchvirtual.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Controller principal para endpoints de gestos
 * 
 * @author TouchVirtual Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/gestures")
@CrossOrigin(origins = "*")
public class GestureController {
    
    private static final Logger logger = LoggerFactory.getLogger(GestureController.class);
    
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
    
    /**
     * Obtém o status atual da detecção de mãos
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            boolean handDetected = handDetectionService.isHandDetected();
            int handCount = handDetectionService.getHandCount();
            double confidence = handDetectionService.getLastDetectionConfidence();
            GestureType lastGesture = gestureRecognitionService.getLastRecognizedGesture();
            int gestureConfidence = gestureRecognitionService.getGestureConfidence();
            
            status.put("handDetected", handDetected);
            status.put("handCount", handCount);
            status.put("detectionConfidence", confidence);
            status.put("lastGesture", lastGesture != null ? lastGesture.getDisplayName() : "NONE");
            status.put("gestureConfidence", gestureConfidence);
            status.put("mouseEnabled", mouseSimulationService.isEnabled());
            status.put("calibrated", coordinateMappingService.isCalibrated());
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao obter status: {}", e.getMessage());
            status.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(status);
        }
    }
    
    /**
     * Obtém os landmarks detectados mais recentemente
     */
    @GetMapping("/landmarks")
    public ResponseEntity<List<HandLandmark>> getLandmarks() {
        try {
            List<HandLandmark> landmarks = handDetectionService.getLastDetectedLandmarks();
            return ResponseEntity.ok(landmarks);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao obter landmarks: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Reconhece gesto atual e retorna resposta
     */
    @GetMapping("/recognize")
    public ResponseEntity<GestureResponse> recognizeGesture() {
        try {
            List<HandLandmark> landmarks = handDetectionService.getLastDetectedLandmarks();
            GestureType gesture = gestureRecognitionService.recognizeGesture(landmarks);
            
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
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao reconhecer gesto: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Processa um evento de toque
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processTouchEvent(@RequestBody TouchEventDTO touchEventDTO) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            TouchEvent touchEvent = touchEventDTO.toTouchEvent();
            mouseSimulationService.processTouchEvent(touchEvent);
            
            result.put("success", true);
            result.put("eventProcessed", touchEvent.getEventType().toString());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao processar evento de toque: {}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * Habilita ou desabilita a simulação de mouse
     */
    @PostMapping("/mouse/enable")
    public ResponseEntity<Map<String, Object>> enableMouse(@RequestParam boolean enabled) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            mouseSimulationService.setEnabled(enabled);
            
            result.put("success", true);
            result.put("enabled", enabled);
            result.put("message", "Simulação de mouse " + (enabled ? "habilitada" : "desabilitada"));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao configurar mouse: {}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * Obtém informações do mouse
     */
    @GetMapping("/mouse/info")
    public ResponseEntity<Map<String, Object>> getMouseInfo() {
        Map<String, Object> info = new HashMap<>();
        
        try {
            info.put("enabled", mouseSimulationService.isEnabled());
            info.put("lastX", mouseSimulationService.getLastX());
            info.put("lastY", mouseSimulationService.getLastY());
            info.put("isDragging", mouseSimulationService.isDragging());
            info.put("lastEventTime", mouseSimulationService.getLastEventTime());
            
            if (mouseSimulationService.isDragging()) {
                int[] dragStart = mouseSimulationService.getDragStartPosition();
                info.put("dragStartX", dragStart[0]);
                info.put("dragStartY", dragStart[1]);
            }
            
            return ResponseEntity.ok(info);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao obter informações do mouse: {}", e.getMessage());
            info.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(info);
        }
    }
    
    /**
     * Obtém estatísticas de performance
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            stats.put("handDetectionConfidence", handDetectionService.getLastDetectionConfidence());
            stats.put("gestureConfidence", gestureRecognitionService.getGestureConfidence());
            stats.put("lastGestureTime", gestureRecognitionService.getLastGestureTime());
            stats.put("isCalibrated", coordinateMappingService.isCalibrated());
            stats.put("mouseEnabled", mouseSimulationService.isEnabled());
            stats.put("isCalibrating", calibrationService.isCalibrating());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao obter estatísticas: {}", e.getMessage());
            stats.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(stats);
        }
    }
    
    /**
     * Obtém informações da tela
     */
    @GetMapping("/screen/info")
    public ResponseEntity<Map<String, Object>> getScreenInfo() {
        Map<String, Object> info = new HashMap<>();
        
        try {
            java.awt.Dimension screenSize = coordinateMappingService.getScreenSize();
            info.put("width", screenSize.getWidth());
            info.put("height", screenSize.getHeight());
            info.put("isInitialized", coordinateMappingService.isInitialized());
            info.put("lastX", coordinateMappingService.getLastX());
            info.put("lastY", coordinateMappingService.getLastY());
            
            return ResponseEntity.ok(info);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao obter informações da tela: {}", e.getMessage());
            info.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(info);
        }
    }
    
    /**
     * Testa mapeamento de coordenadas
     */
    @PostMapping("/test/mapping")
    public ResponseEntity<Map<String, Object>> testMapping(@RequestParam double cameraX, 
                                                         @RequestParam double cameraY) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int[] screenCoords = coordinateMappingService.mapToScreenCoordinates(cameraX, cameraY);
            
            result.put("cameraX", cameraX);
            result.put("cameraY", cameraY);
            result.put("screenX", screenCoords[0]);
            result.put("screenY", screenCoords[1]);
            result.put("success", true);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao testar mapeamento: {}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
} 