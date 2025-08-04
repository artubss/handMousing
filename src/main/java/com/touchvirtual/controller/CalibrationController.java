package com.touchvirtual.controller;

import com.touchvirtual.service.CalibrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para endpoints de calibração
 * 
 * @author TouchVirtual Team
 * @version 1.0.0
 */
@RestController
public class CalibrationController {
    
    private static final Logger logger = LoggerFactory.getLogger(CalibrationController.class);
    
    @Autowired
    private CalibrationService calibrationService;
    
    @PostMapping("/api/calibration/auto")
    @ResponseBody
    public Map<String, Object> autoCalibrate(@RequestParam String sessionId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean success = calibrationService.autoCalibrate(sessionId);
            response.put("status", success ? "success" : "error");
            response.put("message", success ? "Calibração automática concluída" : "Erro na calibração automática");
            
        } catch (Exception e) {
            logger.error("❌ Erro na calibração automática: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
        }

        return response;
    }

    @PostMapping("/api/calibration/reset")
    @ResponseBody
    public Map<String, Object> resetCalibration() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            calibrationService.clearAllCalibrationData();
            response.put("status", "success");
            response.put("message", "Calibração resetada");
            
        } catch (Exception e) {
            logger.error("❌ Erro ao resetar calibração: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
        }

        return response;
    }
} 