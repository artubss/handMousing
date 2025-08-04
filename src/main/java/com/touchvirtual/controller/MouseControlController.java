package com.touchvirtual.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/mouse-control")
public class MouseControlController {

    private static final Logger logger = LoggerFactory.getLogger(MouseControlController.class);
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final String pythonServiceUrl = "http://localhost:5001/api/mouse-control";
    
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startMouseControl(@RequestBody(required = false) Map<String, Object> request) {
        try {
            logger.info("Iniciando controle do mouse...");
            
            Map<String, Object> pythonRequest = new HashMap<>();
            if (request != null) {
                pythonRequest.putAll(request);
            }
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                pythonServiceUrl + "/start",
                pythonRequest,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Controle do mouse iniciado com sucesso");
                return ResponseEntity.ok(response.getBody());
            } else {
                logger.error("Erro ao iniciar controle do mouse: {}", response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
            }
            
        } catch (Exception e) {
            logger.error("Erro ao iniciar controle do mouse: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Erro ao conectar com serviço Python: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopMouseControl() {
        try {
            logger.info("Parando controle do mouse...");
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                pythonServiceUrl + "/stop",
                null,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Controle do mouse parado com sucesso");
                return ResponseEntity.ok(response.getBody());
            } else {
                logger.error("Erro ao parar controle do mouse: {}", response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
            }
            
        } catch (Exception e) {
            logger.error("Erro ao parar controle do mouse: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Erro ao conectar com serviço Python: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getMouseControlStatus() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                pythonServiceUrl + "/status",
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok(response.getBody());
            } else {
                logger.error("Erro ao obter status do controle do mouse: {}", response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
            }
            
        } catch (Exception e) {
            logger.error("Erro ao obter status do controle do mouse: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("running", false);
            error.put("camera_opened", false);
            error.put("error", e.getMessage());
            return ResponseEntity.ok(error);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getMouseControlHealth() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                pythonServiceUrl + "/health",
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok(response.getBody());
            } else {
                logger.error("Erro no health check do controle do mouse: {}", response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
            }
            
        } catch (Exception e) {
            logger.error("Erro no health check do controle do mouse: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "unhealthy");
            error.put("error", e.getMessage());
            return ResponseEntity.ok(error);
        }
    }
} 