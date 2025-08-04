package com.touchvirtual.controller;

import com.touchvirtual.service.PythonHandDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/hand-detection")
public class PythonHandController {

    private static final Logger logger = LoggerFactory.getLogger(PythonHandController.class);

    @Autowired
    private PythonHandDetectionService pythonHandDetectionService;

    /**
     * Recebe dados de detecção do serviço Python
     */
    @PostMapping
    public ResponseEntity<String> receiveHandData(@RequestBody Map<String, Object> data) {
        try {
            logger.debug("📥 Recebendo dados do Python: {} landmarks", 
                data.getOrDefault("hand_count", 0));
            
            pythonHandDetectionService.receiveHandData(data);
            
            return ResponseEntity.ok("OK");
            
        } catch (Exception e) {
            logger.error("❌ Erro ao processar dados do Python: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error");
        }
    }

    /**
     * Health check para o serviço Python
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> response = Map.of(
                "status", "healthy",
                "timestamp", System.currentTimeMillis(),
                "service", "Java Hand Detection API"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Erro no health check: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("status", "error"));
        }
    }
} 