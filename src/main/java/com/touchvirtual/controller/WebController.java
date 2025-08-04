package com.touchvirtual.controller;

import com.touchvirtual.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Controller
public class WebController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebController.class);
    
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
    

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Início");
        model.addAttribute("content", "home");
        model.addAttribute("scripts", "home-scripts");
        
        // Adiciona dados iniciais
        Map<String, Object> initialData = new HashMap<>();
        initialData.put("cameraInitialized", handDetectionService.isCameraInitialized());
        initialData.put("handDetected", handDetectionService.isHandDetected());
        initialData.put("handCount", handDetectionService.getHandCount());
        initialData.put("confidence", handDetectionService.getLastDetectionConfidence());
        initialData.put("mouseEnabled", mouseSimulationService.isEnabled());
        initialData.put("calibrated", coordinateMappingService.isCalibrated());
        
        model.addAttribute("initialData", initialData);
        
        return "layout";
    }
    

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("title", "Dashboard");
        model.addAttribute("content", "dashboard");
        model.addAttribute("scripts", "dashboard-scripts");
        
        // Adiciona dados do dashboard
        Map<String, Object> dashboardData = new HashMap<>();
        dashboardData.put("cameraInitialized", handDetectionService.isCameraInitialized());
        dashboardData.put("handDetected", handDetectionService.isHandDetected());
        dashboardData.put("handCount", handDetectionService.getHandCount());
        dashboardData.put("confidence", handDetectionService.getLastDetectionConfidence());
        dashboardData.put("mouseEnabled", mouseSimulationService.isEnabled());
        dashboardData.put("calibrated", coordinateMappingService.isCalibrated());
        dashboardData.put("lastGesture", gestureRecognitionService.getLastRecognizedGesture());
        dashboardData.put("gestureConfidence", gestureRecognitionService.getGestureConfidence());
        
        model.addAttribute("dashboardData", dashboardData);
        
        return "layout";
    }
    
    @GetMapping("/camera")
    public String camera(Model model) {
        model.addAttribute("title", "Câmera");
        model.addAttribute("content", "camera");
        model.addAttribute("scripts", "camera-scripts");
        
        return "layout";
    }
    

    @GetMapping("/gestures")
    public String gestures(Model model) {
        model.addAttribute("title", "Reconhecimento de Gestos");
        model.addAttribute("content", "gestures");
        model.addAttribute("scripts", "gestures-scripts");
        
        return "layout";
    }
    
    @GetMapping("/calibration")
    public String calibration(Model model) {
        model.addAttribute("title", "Calibração");
        model.addAttribute("content", "calibration");
        model.addAttribute("scripts", "calibration-scripts");
        
        return "layout";
    }
    
    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("title", "Configurações");
        model.addAttribute("content", "settings");
        model.addAttribute("scripts", "settings-scripts");
        
        return "layout";
    }
    
    @GetMapping("/api/status")
    @ResponseBody
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            status.put("cameraInitialized", handDetectionService.isCameraInitialized());
            status.put("handDetected", handDetectionService.isHandDetected());
            status.put("handCount", handDetectionService.getHandCount());
            status.put("confidence", handDetectionService.getLastDetectionConfidence());
            status.put("mouseEnabled", mouseSimulationService.isEnabled());
            status.put("calibrated", coordinateMappingService.isCalibrated());
            status.put("lastGesture", gestureRecognitionService.getLastRecognizedGesture());
            status.put("gestureConfidence", gestureRecognitionService.getGestureConfidence());
            status.put("status", "success");
            
        } catch (Exception e) {
            logger.error("❌ Erro ao obter status: {}", e.getMessage());
            status.put("status", "error");
            status.put("message", e.getMessage());
        }
        
        return status;
    }
    
    @PostMapping("/api/mouse/toggle")
    @ResponseBody
    public Map<String, Object> toggleMouse(@RequestParam boolean enabled) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            mouseSimulationService.setEnabled(enabled);
            response.put("status", "success");
            response.put("mouseEnabled", mouseSimulationService.isEnabled());
            response.put("message", enabled ? "Mouse habilitado" : "Mouse desabilitado");
            
            logger.info("🤖 Mouse {}", enabled ? "habilitado" : "desabilitado");
            
        } catch (Exception e) {
            logger.error("❌ Erro ao alterar estado do mouse: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    @PostMapping("/api/calibration/start")
    @ResponseBody
    public Map<String, Object> startCalibration(@RequestParam String sessionId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean success = calibrationService.startCalibration(sessionId);
            response.put("status", success ? "success" : "error");
            response.put("message", success ? "Calibração iniciada" : "Erro ao iniciar calibração");
            response.put("calibrating", success);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao iniciar calibração: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    @PostMapping("/api/calibration/stop")
    @ResponseBody
    public Map<String, Object> stopCalibration() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            calibrationService.stopCalibration();
            response.put("status", "success");
            response.put("message", "Calibração parada");
            response.put("calibrating", false);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao parar calibração: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        
        return response;
    }
} 