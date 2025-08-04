package com.touchvirtual.service;

import com.touchvirtual.model.CalibrationData;
import com.touchvirtual.model.HandLandmark;
import com.touchvirtual.model.UserSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;

/**
 * Serviço de mapeamento de coordenadas da câmera para a tela
 *
 * @author TouchVirtual Team
 * @version 1.0.0
 */
@Service
public class CoordinateMappingService {

    private static final Logger logger = LoggerFactory.getLogger(CoordinateMappingService.class);

    @Autowired
    @Lazy
    private UserSettings userSettings;

    private CalibrationData calibrationData;
    private Dimension screenSize;
    private double lastX, lastY;
    private boolean isInitialized;

    public CoordinateMappingService() {
        this.calibrationData = new CalibrationData();
        this.lastX = 0.0;
        this.lastY = 0.0;
        this.isInitialized = false;
    }

    /**
     * Inicializa o serviço com as dimensões da tela
     */
    public void initialize() {
        if (isInitialized) {
            return;
        }

        try {
            // Obtém as dimensões da tela
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            screenSize = toolkit.getScreenSize();

            // Configura a calibração inicial
            calibrationData.setScreenWidth((int) screenSize.getWidth());
            calibrationData.setScreenHeight((int) screenSize.getHeight());
            calibrationData.setCameraWidth(640); // Valor padrão
            calibrationData.setCameraHeight(480); // Valor padrão

            // Aplica configurações do usuário se disponível
            if (userSettings != null) {
                calibrationData.setSensitivity(userSettings.getSensitivity());
                calibrationData.setDeadband(userSettings.getDeadband());
            } else {
                calibrationData.setSensitivity(1.0);
                calibrationData.setDeadband(0.05);
            }

            this.isInitialized = true;

            logger.info("🖥️ Mapeamento de coordenadas inicializado: {}x{}",
                    screenSize.getWidth(), screenSize.getHeight());

        } catch (Exception e) {
            logger.error("❌ Erro ao inicializar mapeamento de coordenadas: {}", e.getMessage());
            // Não lança exceção, apenas loga o erro
        }
    }

    /**
     * Mapeia coordenadas da câmera para coordenadas da tela
     */
    public int[] mapToScreenCoordinates(double cameraX, double cameraY) {
        if (!isInitialized) {
            initialize();
        }

        if (!isInitialized) {
            // Fallback para mapeamento simples
            return new int[]{(int) (cameraX * 1920), (int) (cameraY * 1080)};
        }

        // Aplica deadband para reduzir tremores
        if (Math.abs(cameraX - lastX) < calibrationData.getDeadband()
                && Math.abs(cameraY - lastY) < calibrationData.getDeadband()) {
            return new int[]{(int) (lastX * screenSize.getWidth()),
                (int) (lastY * screenSize.getHeight())};
        }

        // Aplica sensibilidade
        double sensitivity = (userSettings != null) ? userSettings.getSensitivity() : 1.0;
        double adjustedX = cameraX * sensitivity;
        double adjustedY = cameraY * sensitivity;

        // Converte usando calibração se disponível
        int[] screenCoords;
        if (calibrationData.isCalibrated()) {
            screenCoords = calibrationData.convertToScreenCoordinates(adjustedX, adjustedY);
        } else {
            // Mapeamento linear simples
            screenCoords = linearMapping(adjustedX, adjustedY);
        }

        // Aplica compensação para usuários canhotos
        if (userSettings != null && userSettings.isLeftHanded()) {
            screenCoords[0] = (int) screenSize.getWidth() - screenCoords[0];
        }

        // Atualiza última posição
        lastX = cameraX;
        lastY = cameraY;

        return screenCoords;
    }

    /**
     * Mapeia landmarks da mão para coordenadas da tela
     */
    public int[] mapHandLandmarksToScreen(List<HandLandmark> landmarks) {
        if (landmarks == null || landmarks.isEmpty()) {
            return new int[]{0, 0};
        }

        // Usa o dedo indicador como ponto de referência
        HandLandmark indexTip = landmarks.stream()
                .filter(landmark -> landmark.getId() == 8)
                .findFirst()
                .orElse(landmarks.get(0));

        return mapToScreenCoordinates(indexTip.getX(), indexTip.getY());
    }

    /**
     * Mapeamento linear simples
     */
    private int[] linearMapping(double cameraX, double cameraY) {
        int screenX = (int) (cameraX * screenSize.getWidth());
        int screenY = (int) (cameraY * screenSize.getHeight());

        // Garante que as coordenadas estão dentro dos limites da tela
        screenX = Math.max(0, Math.min(screenX, (int) screenSize.getWidth()));
        screenY = Math.max(0, Math.min(screenY, (int) screenSize.getHeight()));

        return new int[]{screenX, screenY};
    }

    /**
     * Adiciona um ponto de calibração
     */
    public void addCalibrationPoint(double cameraX, double cameraY, int screenX, int screenY) {
        calibrationData.addCalibrationPoint(cameraX, cameraY, screenX, screenY);
    }

    /**
     * Limpa a calibração
     */
    public void clearCalibration() {
        calibrationData.clearCalibration();
    }

    /**
     * Verifica se está calibrado
     */
    public boolean isCalibrated() {
        return calibrationData.isCalibrated();
    }

    /**
     * Obtém os dados de calibração
     */
    public CalibrationData getCalibrationData() {
        return calibrationData;
    }

    /**
     * Define os dados de calibração
     */
    public void setCalibrationData(CalibrationData calibrationData) {
        this.calibrationData = calibrationData;
    }

    /**
     * Obtém as dimensões da tela
     */
    public Dimension getScreenSize() {
        if (!isInitialized) {
            initialize();
        }
        return screenSize;
    }

    /**
     * Calcula a distância entre dois pontos na tela
     */
    public double calculateScreenDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    /**
     * Calcula a velocidade do movimento
     */
    public double calculateMovementVelocity(int currentX, int currentY, long currentTime,
            int lastX, int lastY, long lastTime) {
        if (currentTime == lastTime) {
            return 0.0;
        }

        double distance = calculateScreenDistance(currentX, currentY, lastX, lastY);
        double timeDiff = (currentTime - lastTime) / 1000.0; // Converte para segundos

        return distance / timeDiff; // pixels por segundo
    }

    /**
     * Aplica suavização às coordenadas
     */
    public int[] applySmoothing(int[] coordinates, double smoothingFactor) {
        if (coordinates == null || coordinates.length < 2) {
            return coordinates;
        }

        int smoothedX = (int) (coordinates[0] * smoothingFactor + lastX * (1 - smoothingFactor));
        int smoothedY = (int) (coordinates[1] * smoothingFactor + lastY * (1 - smoothingFactor));

        return new int[]{smoothedX, smoothedY};
    }

    /**
     * Obtém a última posição X
     */
    public double getLastX() {
        return lastX;
    }

    /**
     * Obtém a última posição Y
     */
    public double getLastY() {
        return lastY;
    }

    /**
     * Verifica se está inicializado
     */
    public boolean isInitialized() {
        return isInitialized;
    }
}
