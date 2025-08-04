package com.touchvirtual.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import com.touchvirtual.service.HandDetectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de teste para verificar o funcionamento da aplicação
 *
 * @author TouchVirtual Team
 * @version 1.0.0
 */
@RestController
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private HandDetectionService handDetectionService;

    @GetMapping("/")
    public String home() {
        return "🎯 Touch Virtual - Sistema de Touchscreen Virtual<br>"
                + "📱 Acesse: <a href='/test'>/test</a> para verificar o status<br>"
                + "📊 Acesse: <a href='/actuator/health'>/actuator/health</a> para verificar a saúde da aplicação";
    }

    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("status", "success");
            response.put("message", "Aplicação funcionando corretamente");
            response.put("cameraInitialized", handDetectionService.isCameraInitialized());
            response.put("handDetected", handDetectionService.isHandDetected());
            response.put("handCount", handDetectionService.getHandCount());
            response.put("confidence", handDetectionService.getLastDetectionConfidence());

            logger.info("✅ Teste executado com sucesso");

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Erro durante o teste: " + e.getMessage());
            logger.error("❌ Erro durante o teste: {}", e.getMessage());
        }

        return response;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "Touch Virtual");
        response.put("version", "1.0.0");
        response.put("camera", handDetectionService.isCameraInitialized() ? "READY" : "NOT_READY");
        return response;
    }
}
