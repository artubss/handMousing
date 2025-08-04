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
    private PythonHandDetectionService handDetectionService;

    @Autowired
    private GestureRecognitionService gestureRecognitionService;

    @Autowired
    private CoordinateMappingService coordinateMappingService;

    @Autowired
    private MouseSimulationService mouseSimulationService;

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

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("title", "Configurações");
        model.addAttribute("content", "settings");
        model.addAttribute("scripts", "settings-scripts");

        return "layout";
    }

    @GetMapping("/test-page")
    public String testPage(Model model) {
        model.addAttribute("title", "Página de Teste");
        model.addAttribute("content", "test");
        model.addAttribute("scripts", "test-scripts");

        return "layout";
    }

    @GetMapping("/status")
    public String statusPage(Model model) {
        model.addAttribute("title", "Status do Sistema");
        model.addAttribute("content", "status");
        model.addAttribute("scripts", "status-scripts");

        return "layout";
    }

    @GetMapping("/api/system/status")
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
            response.put("success", true);
            response.put("enabled", mouseSimulationService.isEnabled());
            response.put("message", enabled ? "Mouse virtual habilitado" : "Mouse virtual desabilitado");

        } catch (Exception e) {
            logger.error("❌ Erro ao alternar mouse virtual: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @PostMapping("/api/detection/start")
    @ResponseBody
    public Map<String, Object> startDetection() {
        Map<String, Object> response = new HashMap<>();

        try {
            handDetectionService.startHandDetection();
            response.put("success", true);
            response.put("message", "Detecção de mãos iniciada");
            response.put("processing", handDetectionService.isProcessing());

        } catch (Exception e) {
            logger.error("❌ Erro ao iniciar detecção: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @PostMapping("/api/detection/stop")
    @ResponseBody
    public Map<String, Object> stopDetection() {
        Map<String, Object> response = new HashMap<>();

        try {
            handDetectionService.stopHandDetection();
            response.put("success", true);
            response.put("message", "Detecção de mãos parada");
            response.put("processing", handDetectionService.isProcessing());

        } catch (Exception e) {
            logger.error("❌ Erro ao parar detecção: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    @GetMapping("/api/detection/status")
    @ResponseBody
    public Map<String, Object> getDetectionStatus() {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("success", true);
            response.put("cameraInitialized", handDetectionService.isCameraInitialized());
            response.put("processing", handDetectionService.isProcessing());
            response.put("handDetected", handDetectionService.isHandDetected());
            response.put("handCount", handDetectionService.getHandCount());
            response.put("confidence", handDetectionService.getLastDetectionConfidence());
            response.put("cameraStatus", handDetectionService.getCameraStatus());

        } catch (Exception e) {
            logger.error("❌ Erro ao obter status da detecção: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }
}
