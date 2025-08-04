package com.touchvirtual.service;

import com.touchvirtual.model.HandLandmark;
import com.touchvirtual.config.CameraConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.HashMap;

@Service
public class PythonHandDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(PythonHandDetectionService.class);

    @Autowired
    private CameraConfig cameraConfig;

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private ScheduledExecutorService executor;
    private AtomicBoolean isRunning;
    private AtomicBoolean isInitialized;
    private List<HandLandmark> lastDetectedLandmarks;
    private double lastDetectionConfidence;

    private static final String PYTHON_SERVICE_URL = "http://localhost:5000/api/hand-detection";

    @PostConstruct
    public void initialize() {
        logger.info("🎯 Inicializando serviço de detecção de mãos Python...");

        this.isRunning = new AtomicBoolean(false);
        this.isInitialized = new AtomicBoolean(false);
        this.lastDetectedLandmarks = new ArrayList<>();
        this.lastDetectionConfidence = 0.0;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();

        logger.info("✅ Serviço de detecção de mãos Python inicializado");
    }

    @PreDestroy
    public void cleanup() {
        logger.info("🛑 Finalizando serviço de detecção de mãos Python...");
        stopProcessing();
        logger.info("✅ Serviço de detecção de mãos Python finalizado");
    }

    /**
     * Inicia o processamento de detecção de mãos
     */
    public void startHandDetection() {
        logger.info("🎯 Iniciando detecção de mãos via Python...");
        startProcessing();
    }

    /**
     * Para o processamento de detecção de mãos
     */
    public void stopHandDetection() {
        logger.info("⏹️ Parando detecção de mãos...");
        stopProcessing();
    }

    /**
     * Verifica se o processamento está ativo
     */
    public boolean isProcessing() {
        return isRunning.get();
    }

    /**
     * Obtém informações de status da câmera
     */
    public String getCameraStatus() {
        if (!isInitialized.get()) {
            return "Câmera não inicializada";
        }

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    PYTHON_SERVICE_URL + "/status", Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> status = response.getBody();
                boolean running = (Boolean) status.getOrDefault("running", false);
                boolean cameraOpened = (Boolean) status.getOrDefault("camera_opened", false);

                if (running && cameraOpened) {
                    return "Webcam ativa via Python MediaPipe";
                } else {
                    return "Câmera não disponível - execute o serviço Python";
                }
            }
        } catch (Exception e) {
            logger.debug("⚠️ Erro ao verificar status Python: {}", e.getMessage());
        }

        return "Câmera não disponível - execute o serviço Python";
    }

    /**
     * Inicia o processamento em background
     */
    private void startProcessing() {
        if (isRunning.compareAndSet(false, true)) {
            // Inicializa a câmera se ainda não foi inicializada
            if (!isInitialized.get()) {
                initializePythonService();
            }

            if (!isInitialized.get()) {
                logger.warn("⚠️ Não foi possível inicializar o serviço Python, processamento não iniciado");
                isRunning.set(false);
                return;
            }

            executor = Executors.newSingleThreadScheduledExecutor();

            executor.scheduleAtFixedRate(() -> {
                try {
                    checkPythonHealth();
                } catch (Exception e) {
                    logger.error("❌ Erro no processamento: {}", e.getMessage());
                }
            }, 0, 1000, TimeUnit.MILLISECONDS);

            logger.info("🔄 Processamento iniciado em background");
        }
    }

    /**
     * Para o processamento em background
     */
    private void stopProcessing() {
        if (isRunning.compareAndSet(true, false)) {
            if (executor != null) {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
            logger.info("⏹️ Processamento parado");
        }
    }

    /**
     * Inicializa o serviço Python
     */
    private synchronized void initializePythonService() {
        if (isInitialized.get()) {
            return;
        }

        try {
            logger.info("🐍 Inicializando serviço Python...");

            // Verifica se o serviço Python está rodando
            ResponseEntity<Map> healthResponse = restTemplate.getForEntity(
                    PYTHON_SERVICE_URL + "/health", Map.class);

            if (healthResponse.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Serviço Python não está respondendo");
            }

            // Inicia o serviço de detecção
            Map<String, Object> startRequest = new HashMap<>();
            startRequest.put("camera_index", cameraConfig.getDeviceIndex());
            startRequest.put("fps", cameraConfig.getFps());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(startRequest, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    PYTHON_SERVICE_URL + "/start", request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("✅ Serviço Python iniciado com sucesso");
                isInitialized.set(true);
            } else {
                throw new RuntimeException("Falha ao iniciar serviço Python");
            }

        } catch (Exception e) {
            logger.error("❌ Erro ao inicializar serviço Python: {}", e.getMessage());
            logger.error("💡 Certifique-se de que o serviço Python está rodando:");
            logger.error("   python hand_detection_service.py");
            throw new RuntimeException("Falha na inicialização do serviço Python", e);
        }
    }

    /**
     * Verifica saúde do serviço Python
     */
    private void checkPythonHealth() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    PYTHON_SERVICE_URL + "/health", Map.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                logger.warn("⚠️ Serviço Python não está respondendo");
                isInitialized.set(false);
            }
        } catch (Exception e) {
            logger.debug("⚠️ Erro ao verificar saúde Python: {}", e.getMessage());
        }
    }

    /**
     * Recebe dados de detecção do Python (chamado via REST)
     */
    public void receiveHandData(Map<String, Object> data) {
        try {
            @SuppressWarnings("unchecked")
            List<List<Map<String, Object>>> landmarksData
                    = (List<List<Map<String, Object>>>) data.get("landmarks");

            List<HandLandmark> landmarks = new ArrayList<>();

            if (landmarksData != null && !landmarksData.isEmpty()) {
                // Pega a primeira mão detectada
                List<Map<String, Object>> handLandmarks = landmarksData.get(0);

                for (int i = 0; i < handLandmarks.size(); i++) {
                    Map<String, Object> landmark = handLandmarks.get(i);

                    HandLandmark handLandmark = new HandLandmark();
                    handLandmark.setId(i);
                    handLandmark.setX(((Number) landmark.get("x")).doubleValue());
                    handLandmark.setY(((Number) landmark.get("y")).doubleValue());
                    handLandmark.setZ(((Number) landmark.get("z")).doubleValue());
                    handLandmark.setConfidence(((Number) landmark.get("confidence")).doubleValue());

                    landmarks.add(handLandmark);
                }
            }

            synchronized (this) {
                lastDetectedLandmarks = landmarks;
                lastDetectionConfidence = ((Number) data.get("confidence")).doubleValue();
            }

            logger.debug("✅ Recebidos {} landmarks do Python", landmarks.size());

        } catch (Exception e) {
            logger.error("❌ Erro ao processar dados do Python: {}", e.getMessage());
        }
    }

    /**
     * Obtém os landmarks detectados mais recentemente
     */
    public synchronized List<HandLandmark> getLastDetectedLandmarks() {
        return new ArrayList<>(lastDetectedLandmarks);
    }

    /**
     * Obtém a confiança da última detecção
     */
    public synchronized double getLastDetectionConfidence() {
        return lastDetectionConfidence;
    }

    /**
     * Verifica se há mãos detectadas
     */
    public synchronized boolean isHandDetected() {
        return !lastDetectedLandmarks.isEmpty() && lastDetectionConfidence > 0.5;
    }

    /**
     * Obtém o número de mãos detectadas
     */
    public synchronized int getHandCount() {
        return isHandDetected() ? 1 : 0;
    }

    /**
     * Verifica se a câmera está inicializada
     */
    public synchronized boolean isCameraInitialized() {
        return isInitialized.get();
    }
}
