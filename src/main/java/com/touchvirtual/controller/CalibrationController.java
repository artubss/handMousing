package com.touchvirtual.controller;

import com.touchvirtual.dto.CalibrationRequest;
import com.touchvirtual.model.CalibrationData;
import com.touchvirtual.service.CalibrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

/**
 * Controller para endpoints de calibração
 * 
 * @author TouchVirtual Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/calibration")
@CrossOrigin(origins = "*")
public class CalibrationController {
    
    private static final Logger logger = LoggerFactory.getLogger(CalibrationController.class);
    
    @Autowired
    private CalibrationService calibrationService;
    
    /**
     * Inicia o processo de calibração
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startCalibration(@RequestParam String sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = calibrationService.startCalibration(sessionId);
            
            result.put("success", success);
            result.put("sessionId", sessionId);
            result.put("message", success ? "Calibração iniciada" : "Erro ao iniciar calibração");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao iniciar calibração: {}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * Para o processo de calibração
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopCalibration() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            calibrationService.stopCalibration();
            
            result.put("success", true);
            result.put("message", "Calibração parada");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao parar calibração: {}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * Adiciona um ponto de calibração
     */
    @PostMapping("/add-point")
    public ResponseEntity<Map<String, Object>> addCalibrationPoint(
            @RequestParam String sessionId,
            @RequestParam double cameraX,
            @RequestParam double cameraY,
            @RequestParam int screenX,
            @RequestParam int screenY) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = calibrationService.addCalibrationPoint(sessionId, cameraX, cameraY, screenX, screenY);
            
            result.put("success", success);
            result.put("sessionId", sessionId);
            result.put("cameraX", cameraX);
            result.put("cameraY", cameraY);
            result.put("screenX", screenX);
            result.put("screenY", screenY);
            result.put("message", success ? "Ponto adicionado" : "Erro ao adicionar ponto");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao adicionar ponto de calibração: {}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * Calibração automática
     */
    @PostMapping("/auto")
    public ResponseEntity<Map<String, Object>> autoCalibrate(@RequestParam String sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = calibrationService.autoCalibrate(sessionId);
            
            result.put("success", success);
            result.put("sessionId", sessionId);
            result.put("message", success ? "Calibração automática realizada" : "Erro na calibração automática");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Erro na calibração automática: {}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * Obtém dados de calibração para uma sessão
     */
    @GetMapping("/data/{sessionId}")
    public ResponseEntity<CalibrationData> getCalibrationData(@PathVariable String sessionId) {
        try {
            CalibrationData data = calibrationService.getCalibrationData(sessionId);
            return ResponseEntity.ok(data);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao obter dados de calibração: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Define dados de calibração para uma sessão
     */
    @PostMapping("/data/{sessionId}")
    public ResponseEntity<Map<String, Object>> setCalibrationData(
            @PathVariable String sessionId,
            @RequestBody CalibrationData calibrationData) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            calibrationService.setCalibrationData(sessionId, calibrationData);
            
            result.put("success", true);
            result.put("sessionId", sessionId);
            result.put("message", "Dados de calibração salvos");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao salvar dados de calibração: {}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * Remove dados de calibração de uma sessão
     */
    @DeleteMapping("/data/{sessionId}")
    public ResponseEntity<Map<String, Object>> removeCalibrationData(@PathVariable String sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            calibrationService.removeCalibrationData(sessionId);
            
            result.put("success", true);
            result.put("sessionId", sessionId);
            result.put("message", "Dados de calibração removidos");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao remover dados de calibração: {}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * Verifica se uma sessão está calibrada
     */
    @GetMapping("/status/{sessionId}")
    public ResponseEntity<Map<String, Object>> getCalibrationStatus(@PathVariable String sessionId) {
        Map<String, Object> status = new HashMap<>();
        
        try {
            boolean calibrated = calibrationService.isSessionCalibrated(sessionId);
            boolean isCalibrating = calibrationService.isCalibrating();
            String currentSessionId = calibrationService.getCurrentSessionId();
            
            status.put("sessionId", sessionId);
            status.put("calibrated", calibrated);
            status.put("isCalibrating", isCalibrating);
            status.put("currentSessionId", currentSessionId);
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao obter status de calibração: {}", e.getMessage());
            status.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(status);
        }
    }
    
    /**
     * Obtém estatísticas de calibração
     */
    @GetMapping("/stats/{sessionId}")
    public ResponseEntity<CalibrationService.CalibrationStats> getCalibrationStats(@PathVariable String sessionId) {
        try {
            CalibrationService.CalibrationStats stats = calibrationService.getCalibrationStats(sessionId);
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao obter estatísticas de calibração: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtém todos os dados de calibração
     */
    @GetMapping("/data/all")
    public ResponseEntity<Map<String, CalibrationData>> getAllCalibrationData() {
        try {
            Map<String, CalibrationData> allData = calibrationService.getAllCalibrationData();
            return ResponseEntity.ok(allData);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao obter todos os dados de calibração: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Limpa todos os dados de calibração
     */
    @DeleteMapping("/data/all")
    public ResponseEntity<Map<String, Object>> clearAllCalibrationData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            calibrationService.clearAllCalibrationData();
            
            result.put("success", true);
            result.put("message", "Todos os dados de calibração foram limpos");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao limpar dados de calibração: {}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * Obtém informações da calibração atual
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentCalibration() {
        Map<String, Object> info = new HashMap<>();
        
        try {
            CalibrationData currentData = calibrationService.getCurrentCalibration();
            boolean isCalibrating = calibrationService.isCalibrating();
            String currentSessionId = calibrationService.getCurrentSessionId();
            
            info.put("isCalibrating", isCalibrating);
            info.put("currentSessionId", currentSessionId);
            info.put("pointCount", currentData.getCalibrationPoints().size());
            info.put("calibrated", currentData.isCalibrated());
            info.put("sensitivity", currentData.getSensitivity());
            info.put("deadband", currentData.getDeadband());
            
            return ResponseEntity.ok(info);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao obter calibração atual: {}", e.getMessage());
            info.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(info);
        }
    }
} 