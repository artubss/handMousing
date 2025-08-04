package com.touchvirtual.service;

import com.touchvirtual.model.HandLandmark;
import com.touchvirtual.config.CameraConfig;
import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Serviço principal de detecção de mãos usando OpenCV
 *
 * @author TouchVirtual Team
 * @version 1.0.0
 */
@Service
public class HandDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(HandDetectionService.class);

    @Autowired
    private CameraConfig cameraConfig;

    private VideoCapture videoCapture;
    private Mat frame;
    private ScheduledExecutorService executor;
    private AtomicBoolean isRunning;
    private AtomicBoolean isInitialized;
    private List<HandLandmark> lastDetectedLandmarks;
    private double lastDetectionConfidence;

    @PostConstruct
    public void initialize() {
        logger.info("🎯 Inicializando serviço de detecção de mãos...");

        this.isRunning = new AtomicBoolean(false);
        this.isInitialized = new AtomicBoolean(false);
        this.lastDetectedLandmarks = new ArrayList<>();
        this.lastDetectionConfidence = 0.0;

        logger.info("✅ Serviço de detecção de mãos inicializado com sucesso");
    }

    @PreDestroy
    public void cleanup() {
        logger.info("🛑 Finalizando serviço de detecção de mãos...");
        stopProcessing();
        releaseResources();
        logger.info("✅ Serviço de detecção de mãos finalizado");
    }

    /**
     * Inicializa a captura de vídeo de forma lazy
     */
    private synchronized void initializeVideoCapture() {
        if (isInitialized.get()) {
            return;
        }

        try {
            logger.info("📹 Inicializando câmera...");
            videoCapture = new VideoCapture(cameraConfig.getDeviceIndex());

            if (!videoCapture.isOpened()) {
                logger.warn("⚠️ Não foi possível abrir a câmera no índice {}", cameraConfig.getDeviceIndex());
                // Tenta outros índices de câmera
                for (int i = 0; i < 3; i++) {
                    if (i != cameraConfig.getDeviceIndex()) {
                        logger.info("🔄 Tentando câmera no índice {}", i);
                        videoCapture.release();
                        videoCapture = new VideoCapture(i);
                        if (videoCapture.isOpened()) {
                            logger.info("✅ Câmera encontrada no índice {}", i);
                            break;
                        }
                    }
                }

                if (!videoCapture.isOpened()) {
                    logger.error("❌ Nenhuma câmera disponível encontrada");
                    return;
                }
            }

            // Configura as propriedades da câmera
            videoCapture.set(Videoio.CAP_PROP_FRAME_WIDTH, cameraConfig.getFrameWidth());
            videoCapture.set(Videoio.CAP_PROP_FRAME_HEIGHT, cameraConfig.getFrameHeight());
            videoCapture.set(Videoio.CAP_PROP_FPS, cameraConfig.getFps());

            if (!cameraConfig.isAutoExposure()) {
                videoCapture.set(Videoio.CAP_PROP_AUTO_EXPOSURE, 0);
                videoCapture.set(Videoio.CAP_PROP_EXPOSURE, cameraConfig.getExposure());
            }

            videoCapture.set(Videoio.CAP_PROP_GAIN, cameraConfig.getGain());

            frame = new Mat();

            logger.info("📹 Câmera inicializada: {}x{} @ {}fps",
                    cameraConfig.getFrameWidth(),
                    cameraConfig.getFrameHeight(),
                    cameraConfig.getFps());

            isInitialized.set(true);

        } catch (Exception e) {
            logger.error("❌ Erro ao inicializar câmera: {}", e.getMessage());
            // Não lança exceção, apenas loga o erro
        }
    }

    /**
     * Inicia o processamento em background
     */
    private void startProcessing() {
        if (isRunning.compareAndSet(false, true)) {
            // Inicializa a câmera se ainda não foi inicializada
            if (!isInitialized.get()) {
                initializeVideoCapture();
            }

            if (!isInitialized.get()) {
                logger.warn("⚠️ Não foi possível inicializar a câmera, processamento não iniciado");
                isRunning.set(false);
                return;
            }

            executor = Executors.newSingleThreadScheduledExecutor();

            executor.scheduleAtFixedRate(() -> {
                try {
                    processFrame();
                } catch (Exception e) {
                    logger.error("❌ Erro no processamento de frame: {}", e.getMessage());
                }
            }, 0, 1000 / cameraConfig.getFps(), TimeUnit.MILLISECONDS);

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
     * Processa um frame da câmera
     */
    private void processFrame() {
        if (!isInitialized.get() || videoCapture == null || !videoCapture.isOpened()) {
            return;
        }

        if (videoCapture.read(frame)) {
            // Pré-processamento do frame
            Mat processedFrame = preprocessFrame(frame);

            // Detecta mãos no frame
            List<HandLandmark> landmarks = detectHands(processedFrame);

            // Atualiza os landmarks detectados
            synchronized (this) {
                lastDetectedLandmarks = landmarks;
                lastDetectionConfidence = calculateConfidence(landmarks);
            }

            // Libera recursos
            processedFrame.release();
        }
    }

    /**
     * Pré-processa o frame para melhorar a detecção
     */
    private Mat preprocessFrame(Mat inputFrame) {
        Mat processed = new Mat();

        // Converte para escala de cinza
        Imgproc.cvtColor(inputFrame, processed, Imgproc.COLOR_BGR2GRAY);

        // Aplica filtro Gaussiano para reduzir ruído
        Imgproc.GaussianBlur(processed, processed, new Size(5, 5), 0);

        // Aplica equalização de histograma para melhorar contraste
        Imgproc.equalizeHist(processed, processed);

        return processed;
    }

    /**
     * Detecta mãos no frame usando algoritmos de visão computacional
     */
    private List<HandLandmark> detectHands(Mat frame) {
        List<HandLandmark> landmarks = new ArrayList<>();

        try {
            // Implementação simplificada de detecção de mãos
            // Em uma implementação real, usaríamos MediaPipe ou modelo ML

            // Detecta contornos na imagem
            Mat edges = new Mat();
            Imgproc.Canny(frame, edges, 50, 150);

            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(edges, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            // Filtra contornos que podem ser mãos
            for (MatOfPoint contour : contours) {
                double area = Imgproc.contourArea(contour);

                // Filtra por área (mãos têm área específica)
                if (area > 1000 && area < 50000) {
                    Rect boundingRect = Imgproc.boundingRect(contour);

                    // Simula landmarks da mão baseado no retângulo
                    landmarks.addAll(generateHandLandmarks(boundingRect, frame.size()));
                }
            }

            edges.release();

        } catch (Exception e) {
            logger.error("❌ Erro na detecção de mãos: {}", e.getMessage());
        }

        return landmarks;
    }

    /**
     * Gera landmarks simulados da mão baseado no retângulo detectado
     */
    private List<HandLandmark> generateHandLandmarks(Rect boundingRect, Size frameSize) {
        List<HandLandmark> landmarks = new ArrayList<>();

        // Pontos simulados da mão (21 landmarks como MediaPipe)
        double centerX = boundingRect.x + boundingRect.width / 2.0;
        double centerY = boundingRect.y + boundingRect.height / 2.0;

        // Normaliza coordenadas para 0-1
        double normalizedCenterX = centerX / frameSize.width;
        double normalizedCenterY = centerY / frameSize.height;

        // Gera 21 landmarks simulados
        for (int i = 0; i < 21; i++) {
            HandLandmark landmark = new HandLandmark();
            landmark.setId(i);
            landmark.setX(normalizedCenterX + (Math.random() - 0.5) * 0.1);
            landmark.setY(normalizedCenterY + (Math.random() - 0.5) * 0.1);
            landmark.setZ(0.0);
            landmark.setConfidence(0.8 + Math.random() * 0.2);

            landmarks.add(landmark);
        }

        return landmarks;
    }

    /**
     * Calcula a confiança da detecção
     */
    private double calculateConfidence(List<HandLandmark> landmarks) {
        if (landmarks.isEmpty()) {
            return 0.0;
        }

        double totalConfidence = 0.0;
        for (HandLandmark landmark : landmarks) {
            totalConfidence += landmark.getConfidence();
        }

        return totalConfidence / landmarks.size();
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
        // Implementação simplificada - assume uma mão por detecção
        return isHandDetected() ? 1 : 0;
    }

    /**
     * Verifica se a câmera está inicializada
     */
    public synchronized boolean isCameraInitialized() {
        return isInitialized.get();
    }

    /**
     * Libera recursos da câmera
     */
    private void releaseResources() {
        if (videoCapture != null) {
            videoCapture.release();
        }
        if (frame != null) {
            frame.release();
        }
        isInitialized.set(false);
    }
}
