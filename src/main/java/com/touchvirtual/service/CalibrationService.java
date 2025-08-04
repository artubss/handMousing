package com.touchvirtual.service;

import com.touchvirtual.model.CalibrationData;
import com.touchvirtual.model.HandLandmark;
import com.touchvirtual.dto.CalibrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Serviço de calibração para configurar o mapeamento de coordenadas
 *
 * @author TouchVirtual Team
 * @version 1.0.0
 */
@Service
public class CalibrationService {

    private static final Logger logger = LoggerFactory.getLogger(CalibrationService.class);

    @Autowired
    @Lazy
    private HandDetectionService handDetectionService;

    @Autowired
    @Lazy
    private CoordinateMappingService coordinateMappingService;

    // Armazena dados de calibração por sessão
    private Map<String, CalibrationData> calibrationSessions;
    private CalibrationData currentCalibration;
    private boolean isCalibrating;
    private String currentSessionId;

    // Pontos de calibração padrão (cantos da tela)
    private static final double[][] DEFAULT_CALIBRATION_POINTS = {
        {0.1, 0.1}, // Canto superior esquerdo
        {0.9, 0.1}, // Canto superior direito
        {0.9, 0.9}, // Canto inferior direito
        {0.1, 0.9} // Canto inferior esquerdo
    };

    public CalibrationService() {
        this.calibrationSessions = new ConcurrentHashMap<>();
        this.currentCalibration = new CalibrationData();
        this.isCalibrating = false;
        this.currentSessionId = null;
    }

    /**
     * Inicia o processo de calibração
     */
    public boolean startCalibration(String sessionId) {
        try {
            currentSessionId = sessionId;
            currentCalibration = new CalibrationData();
            isCalibrating = true;

            // Inicializa com pontos padrão
            initializeDefaultCalibration();

            logger.info("🎯 Iniciando calibração para sessão: {}", sessionId);
            return true;

        } catch (Exception e) {
            logger.error("❌ Erro ao iniciar calibração: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Para o processo de calibração
     */
    public void stopCalibration() {
        isCalibrating = false;
        currentSessionId = null;
        logger.info("⏹️ Calibração parada");
    }

    /**
     * Adiciona um ponto de calibração
     */
    public boolean addCalibrationPoint(String sessionId, double cameraX, double cameraY,
            int screenX, int screenY) {
        if (!isCalibrating || !sessionId.equals(currentSessionId)) {
            return false;
        }

        try {
            currentCalibration.addCalibrationPoint(cameraX, cameraY, screenX, screenY);

            logger.info("✅ Ponto de calibração adicionado: ({}, {}) -> ({}, {})",
                    cameraX, cameraY, screenX, screenY);

            // Verifica se tem pontos suficientes para considerar calibrado
            if (currentCalibration.isCalibrated()) {
                finishCalibration(sessionId);
            }

            return true;

        } catch (Exception e) {
            logger.error("❌ Erro ao adicionar ponto de calibração: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Finaliza a calibração e salva os dados
     */
    private void finishCalibration(String sessionId) {
        try {
            // Salva os dados de calibração
            calibrationSessions.put(sessionId, currentCalibration);

            // Atualiza o serviço de mapeamento
            if (coordinateMappingService != null) {
                coordinateMappingService.setCalibrationData(currentCalibration);
            }

            isCalibrating = false;
            currentSessionId = null;

            logger.info("✅ Calibração finalizada para sessão: {}", sessionId);

        } catch (Exception e) {
            logger.error("❌ Erro ao finalizar calibração: {}", e.getMessage());
        }
    }

    /**
     * Inicializa com pontos de calibração padrão
     */
    private void initializeDefaultCalibration() {
        currentCalibration.clearCalibration();

        // Adiciona pontos padrão
        for (double[] point : DEFAULT_CALIBRATION_POINTS) {
            currentCalibration.addCalibrationPoint(point[0], point[1],
                    (int) (point[0] * 1920), (int) (point[1] * 1080));
        }
    }

    /**
     * Calibração automática usando detecção de mãos
     */
    public boolean autoCalibrate(String sessionId) {
        try {
            if (handDetectionService == null || !handDetectionService.isCameraInitialized()) {
                logger.warn("⚠️ Câmera não disponível para calibração automática");
                return false;
            }

            startCalibration(sessionId);

            // Simula pontos de calibração automática
            // Em uma implementação real, detectaria as mãos nos cantos da tela
            currentCalibration.addCalibrationPoint(0.1, 0.1, 192, 108);
            currentCalibration.addCalibrationPoint(0.9, 0.1, 1728, 108);
            currentCalibration.addCalibrationPoint(0.9, 0.9, 1728, 972);
            currentCalibration.addCalibrationPoint(0.1, 0.9, 192, 972);

            if (currentCalibration.isCalibrated()) {
                finishCalibration(sessionId);
                return true;
            }

            return false;

        } catch (Exception e) {
            logger.error("❌ Erro na calibração automática: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtém dados de calibração para uma sessão
     */
    public CalibrationData getCalibrationData(String sessionId) {
        return calibrationSessions.getOrDefault(sessionId, new CalibrationData());
    }

    /**
     * Define dados de calibração para uma sessão
     */
    public void setCalibrationData(String sessionId, CalibrationData calibrationData) {
        calibrationSessions.put(sessionId, calibrationData);
    }

    /**
     * Remove dados de calibração para uma sessão
     */
    public void removeCalibrationData(String sessionId) {
        calibrationSessions.remove(sessionId);
    }

    /**
     * Verifica se uma sessão está calibrada
     */
    public boolean isSessionCalibrated(String sessionId) {
        CalibrationData data = calibrationSessions.get(sessionId);
        return data != null && data.isCalibrated();
    }

    /**
     * Obtém estatísticas de calibração
     */
    public CalibrationStats getCalibrationStats(String sessionId) {
        CalibrationData data = getCalibrationData(sessionId);
        CalibrationStats stats = new CalibrationStats();

        stats.setSessionId(sessionId);
        stats.setCalibrated(data.isCalibrated());
        stats.setPointCount(data.getCalibrationPoints().size());
        stats.setLastCalibrationTime(System.currentTimeMillis());
        stats.setSensitivity(1.0);
        stats.setDeadband(0.05);

        return stats;
    }

    /**
     * Obtém todos os dados de calibração
     */
    public Map<String, CalibrationData> getAllCalibrationData() {
        return new ConcurrentHashMap<>(calibrationSessions);
    }

    /**
     * Remove todos os dados de calibração
     */
    public void clearAllCalibrationData() {
        calibrationSessions.clear();
    }

    /**
     * Verifica se está calibrando
     */
    public boolean isCalibrating() {
        return isCalibrating;
    }

    /**
     * Obtém o ID da sessão atual
     */
    public String getCurrentSessionId() {
        return currentSessionId;
    }

    /**
     * Obtém a calibração atual
     */
    public CalibrationData getCurrentCalibration() {
        return currentCalibration;
    }

    /**
     * Classe para estatísticas de calibração
     */
    public static class CalibrationStats {

        private String sessionId;
        private boolean calibrated;
        private int pointCount;
        private long lastCalibrationTime;
        private double sensitivity;
        private double deadband;

        // Getters e Setters
        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public boolean isCalibrated() {
            return calibrated;
        }

        public void setCalibrated(boolean calibrated) {
            this.calibrated = calibrated;
        }

        public int getPointCount() {
            return pointCount;
        }

        public void setPointCount(int pointCount) {
            this.pointCount = pointCount;
        }

        public long getLastCalibrationTime() {
            return lastCalibrationTime;
        }

        public void setLastCalibrationTime(long lastCalibrationTime) {
            this.lastCalibrationTime = lastCalibrationTime;
        }

        public double getSensitivity() {
            return sensitivity;
        }

        public void setSensitivity(double sensitivity) {
            this.sensitivity = sensitivity;
        }

        public double getDeadband() {
            return deadband;
        }

        public void setDeadband(double deadband) {
            this.deadband = deadband;
        }
    }
}
