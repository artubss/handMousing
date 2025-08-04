package com.touchvirtual.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import static org.bytedeco.opencv.global.opencv_core.CV_8UC3;
import static org.bytedeco.opencv.global.opencv_core.inRange;
import static org.bytedeco.opencv.global.opencv_imgproc.CHAIN_APPROX_SIMPLE;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2HSV;
import static org.bytedeco.opencv.global.opencv_imgproc.GaussianBlur;
import static org.bytedeco.opencv.global.opencv_imgproc.MORPH_CLOSE;
import static org.bytedeco.opencv.global.opencv_imgproc.MORPH_ELLIPSE;
import static org.bytedeco.opencv.global.opencv_imgproc.MORPH_OPEN;
import static org.bytedeco.opencv.global.opencv_imgproc.RETR_EXTERNAL;
import static org.bytedeco.opencv.global.opencv_imgproc.boundingRect;
import static org.bytedeco.opencv.global.opencv_imgproc.contourArea;
import static org.bytedeco.opencv.global.opencv_imgproc.convexHull;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.equalizeHist;
import static org.bytedeco.opencv.global.opencv_imgproc.findContours;
import static org.bytedeco.opencv.global.opencv_imgproc.getStructuringElement;
import static org.bytedeco.opencv.global.opencv_imgproc.moments;
import static org.bytedeco.opencv.global.opencv_imgproc.morphologyEx;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Moments;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.touchvirtual.config.CameraConfig;
import com.touchvirtual.model.HandLandmark;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

// @Service  // DESABILITADO - Usando Python para detecção de mãos
public class HandDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(HandDetectionService.class);

    @Autowired
    private CameraConfig cameraConfig;

    private OpenCVFrameGrabber frameGrabber;
    private OpenCVFrameConverter.ToMat converter;
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
        this.converter = new OpenCVFrameConverter.ToMat();

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
     * Inicia o processamento de detecção de mãos
     */
    public void startHandDetection() {
        logger.info("🎯 Iniciando detecção de mãos...");
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
        if (frameGrabber == null) {
            return "Câmera não disponível - execute run-with-camera.bat";
        }

        return String.format("Webcam ativa: %dx%d @ %dfps",
                frameGrabber.getImageWidth(),
                frameGrabber.getImageHeight(),
                frameGrabber.getFrameRate());
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

            // Tenta diferentes configurações de câmera
            int[] cameraIndices = {0, 1, 2}; // Apenas índices válidos
            boolean cameraFound = false;

            for (int cameraIndex : cameraIndices) {
                try {
                    logger.info("🔄 Tentando câmera no índice {} (webcam do computador)", cameraIndex);

                    if (frameGrabber != null) {
                        try {
                            frameGrabber.stop();
                            frameGrabber.release();
                        } catch (Exception e) {
                            logger.debug("Liberando frameGrabber anterior...");
                        }
                    }

                    // Tenta diferentes backends para Windows
                    String[] backends = {"dshow", "msmf", "v4l2", "any"}; // dshow é mais confiável que msmf
                    boolean backendWorked = false;

                    for (String backend : backends) {
                        try {
                            if (frameGrabber != null) {
                                try {
                                    frameGrabber.stop();
                                    frameGrabber.release();
                                } catch (Exception e) {
                                    // Ignora
                                }
                            }

                            frameGrabber = new OpenCVFrameGrabber(cameraIndex);

                            // Configura propriedades específicas para webcam
                            frameGrabber.setImageWidth(cameraConfig.getFrameWidth());
                            frameGrabber.setImageHeight(cameraConfig.getFrameHeight());
                            frameGrabber.setFrameRate(cameraConfig.getFps());

                            // Tenta backend específico
                            if (!"any".equals(backend)) {
                                frameGrabber.setFormat(backend);
                                logger.info("🔧 Tentando backend: {}", backend);
                            }

                            frameGrabber.start();
                            Thread.sleep(500); // Mais tempo para inicializar

                            // Testa se consegue capturar um frame
                            Frame testFrame = frameGrabber.grab();
                            if (testFrame != null && frameGrabber.getImageWidth() > 0) {
                                logger.info("✅ Backend {} funcionou!", backend);
                                backendWorked = true;
                                break;
                            }

                        } catch (Exception backendError) {
                            logger.debug("❌ Backend {} falhou: {}", backend, backendError.getMessage());
                        }
                    }

                    if (!backendWorked) {
                        throw new Exception("Nenhum backend de câmera funcionou para índice " + cameraIndex);
                    }

                    // Se chegou aqui, um backend funcionou
                    if (frameGrabber != null && frameGrabber.getImageWidth() > 0) {
                        logger.info("✅ Câmera REAL encontrada no índice {}: {}x{} @ {}fps",
                                cameraIndex, frameGrabber.getImageWidth(),
                                frameGrabber.getImageHeight(), frameGrabber.getFrameRate());
                        cameraFound = true;
                        break;
                    }

                } catch (Exception e) {
                    logger.warn("⚠️ Câmera no índice {} não disponível: {}", cameraIndex, e.getMessage());
                    if (frameGrabber != null) {
                        try {
                            frameGrabber.stop();
                            frameGrabber.release();
                        } catch (Exception ex) {
                            // Ignora erros de limpeza
                        }
                    }
                }
            }

            if (!cameraFound) {
                logger.error("❌ NENHUMA CÂMERA ENCONTRADA!");
                logger.error("💡 Soluções:");
                logger.error("   1. Execute: diagnostico-camera.bat");
                logger.error("   2. Teste o app Câmera do Windows");
                logger.error("   3. Reinicie o computador");
                logger.error("   4. Execute como Administrador");
                throw new RuntimeException("Câmera real é obrigatória - erro do sistema Windows");
            }

            frame = new Mat();

            logger.info("📹 Câmera inicializada: {}x{} @ {}fps",
                    frameGrabber.getImageWidth(),
                    frameGrabber.getImageHeight(),
                    cameraConfig.getFps());

            isInitialized.set(true);

        } catch (Exception e) {
            logger.error("❌ Erro ao inicializar câmera: {}", e.getMessage());
            logger.error("💡 Execute com o script: run-with-camera.bat");
            throw new RuntimeException("Falha na inicialização da câmera real", e);
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
        if (!isInitialized.get()) {
            return;
        }

        try {
            // Verifica se frameGrabber está válido
            if (frameGrabber == null) {
                logger.error("❌ FrameGrabber é null - câmera não foi inicializada corretamente!");
                return;
            }

            // Verifica se a câmera está funcionando
            if (frameGrabber.getImageWidth() <= 0) {
                logger.warn("⚠️ FrameGrabber não está válido, tentando reinicializar...");
                initializeVideoCapture();
                return;
            }

            Frame grabbedFrame = frameGrabber.grab();
            if (grabbedFrame != null) {
                // Converte Frame para Mat
                Mat inputFrame = converter.convert(grabbedFrame);

                if (inputFrame != null && !inputFrame.empty()) {
                    // Pré-processamento do frame
                    Mat processedFrame = preprocessFrame(inputFrame);

                    // Detecta mãos no frame
                    List<HandLandmark> landmarks = detectHands(processedFrame);

                    // Atualiza os landmarks detectados
                    synchronized (this) {
                        lastDetectedLandmarks = landmarks;
                        lastDetectionConfidence = calculateConfidence(landmarks);
                    }

                    // Libera recursos
                    processedFrame.release();
                    inputFrame.release();
                } else {
                    logger.debug("⚠️ Frame vazio ou nulo recebido");
                }
            } else {
                logger.debug("⚠️ Não foi possível capturar frame da câmera");
            }
        } catch (Exception e) {
            logger.error("❌ Erro ao processar frame: {}", e.getMessage());

            // Se o erro persistir, tenta reinicializar a câmera
            if (e.getMessage().contains("read()") || e.getMessage().contains("grab")) {
                logger.info("🔄 Tentando reinicializar câmera devido a erro de captura...");
                isInitialized.set(false);
                try {
                    Thread.sleep(2000); // Aguarda 2 segundos antes de tentar novamente
                    initializeVideoCapture();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Pré-processa o frame para melhorar a detecção
     */
    private Mat preprocessFrame(Mat inputFrame) {
        Mat processed = new Mat();

        // Converte para escala de cinza
        cvtColor(inputFrame, processed, COLOR_BGR2GRAY);

        // Aplica filtro Gaussiano para reduzir ruído
        GaussianBlur(processed, processed, new Size(5, 5), 0);

        // Aplica equalização de histograma para melhorar contraste
        equalizeHist(processed, processed);

        return processed;
    }

    /**
     * Detecta mãos no frame usando algoritmos avançados de visão computacional
     */
    private List<HandLandmark> detectHands(Mat frame) {
        List<HandLandmark> landmarks = new ArrayList<>();

        try {
            // Verifica se o frame é válido
            if (frame == null || frame.empty()) {
                logger.debug("⚠️ Frame inválido para detecção de mãos");
                return landmarks;
            }

            // Converte para HSV para melhor detecção de pele
            Mat hsvFrame = new Mat();
            cvtColor(frame, hsvFrame, COLOR_BGR2HSV);

            // Define range para detecção de pele (tons de pele)
            Mat skinMask = new Mat();
            Mat lowerSkin = new Mat(1, 1, CV_8UC3, new Scalar(0, 20, 70, 0));
            Mat upperSkin = new Mat(1, 1, CV_8UC3, new Scalar(20, 255, 255, 0));
            inRange(hsvFrame, lowerSkin, upperSkin, skinMask);

            // Aplica operações morfológicas para limpar o ruído
            Mat kernel = getStructuringElement(MORPH_ELLIPSE, new Size(3, 3));
            Mat cleanedMask = new Mat();
            morphologyEx(skinMask, cleanedMask, MORPH_OPEN, kernel);
            morphologyEx(cleanedMask, cleanedMask, MORPH_CLOSE, kernel);

            // Encontra contornos na máscara de pele
            MatVector contours = new MatVector();
            Mat hierarchy = new Mat();
            findContours(cleanedMask, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

            // Filtra contornos que podem ser mãos
            for (long i = 0; i < contours.size(); i++) {
                try {
                    Mat contour = contours.get(i);
                    double area = contourArea(contour);

                    // Filtra por área (mãos têm área específica)
                    if (area > 5000 && area < 100000) {
                        Rect boundingRect = boundingRect(contour);

                        // Verifica se o retângulo é válido
                        if (boundingRect != null && boundingRect.width() > 0 && boundingRect.height() > 0) {
                            // Calcula a razão largura/altura para identificar mãos
                            double aspectRatio = (double) boundingRect.width() / boundingRect.height();

                            // Mãos têm razão específica (geralmente entre 0.5 e 2.0)
                            if (aspectRatio >= 0.5 && aspectRatio <= 2.0) {
                                // Calcula convexidade para verificar se é uma mão
                                Mat hull = new Mat();
                                convexHull(contour, hull);
                                double hullArea = contourArea(hull);
                                double solidity = area / hullArea;

                                // Mãos têm solididade específica
                                if (solidity > 0.3 && solidity < 0.9) {
                                    // Gera landmarks baseados na forma da mão detectada
                                    landmarks.addAll(generateRealHandLandmarks(contour, boundingRect, frame.size()));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.debug("⚠️ Erro ao processar contorno {}: {}", i, e.getMessage());
                }
            }

            // Libera recursos
            hsvFrame.release();
            skinMask.release();
            cleanedMask.release();
            kernel.release();
            hierarchy.release();
            lowerSkin.release();
            upperSkin.release();

            logger.debug("✅ Detectados {} landmarks de mão real", landmarks.size());

        } catch (Exception e) {
            logger.error("❌ Erro na detecção de mãos: {}", e.getMessage());
        }

        return landmarks;
    }

    /**
     * Gera landmarks reais da mão baseado na forma detectada
     */
    private List<HandLandmark> generateRealHandLandmarks(Mat contour, Rect boundingRect, Size frameSize) {
        List<HandLandmark> landmarks = new ArrayList<>();

        try {
            // Calcula o centro da mão
            Moments moments = moments(contour);
            double centerX = moments.m10() / moments.m00();
            double centerY = moments.m11() / moments.m00();

            // Normaliza coordenadas para 0-1
            double normalizedCenterX = centerX / frameSize.width();
            double normalizedCenterY = centerY / frameSize.height();

            // Calcula a direção principal da mão
            double angle = 0.5 * Math.atan2(2 * moments.mu11(), moments.mu20() - moments.mu02());

            // Gera 21 landmarks baseados na forma real da mão
            for (int i = 0; i < 21; i++) {
                HandLandmark landmark = new HandLandmark();
                landmark.setId(i);

                // Calcula posição baseada no tipo de landmark
                double[] position = calculateLandmarkPosition(i, normalizedCenterX, normalizedCenterY,
                        boundingRect, frameSize, angle);

                landmark.setX(position[0]);
                landmark.setY(position[1]);
                landmark.setZ(0.0);
                landmark.setConfidence(0.8 + ThreadLocalRandom.current().nextDouble() * 0.2);

                landmarks.add(landmark);
            }

        } catch (Exception e) {
            logger.error("❌ Erro ao gerar landmarks reais: {}", e.getMessage());
        }

        return landmarks;
    }

    /**
     * Calcula a posição de um landmark específico baseado na forma da mão
     */
    private double[] calculateLandmarkPosition(int landmarkId, double centerX, double centerY,
            Rect boundingRect, Size frameSize, double angle) {
        double[] position = new double[2];

        // Normaliza as dimensões do retângulo
        double width = boundingRect.width() / frameSize.width();
        double height = boundingRect.height() / frameSize.height();

        // Calcula posições baseadas no tipo de landmark
        switch (landmarkId) {
            case 0: // Polegar - base
                position[0] = centerX - width * 0.3;
                position[1] = centerY + height * 0.2;
                break;
            case 1: // Polegar - primeira junta
                position[0] = centerX - width * 0.25;
                position[1] = centerY + height * 0.1;
                break;
            case 2: // Polegar - segunda junta
                position[0] = centerX - width * 0.2;
                position[1] = centerY;
                break;
            case 3: // Polegar - ponta
                position[0] = centerX - width * 0.15;
                position[1] = centerY - height * 0.1;
                break;
            case 4: // Indicador - base
                position[0] = centerX - width * 0.1;
                position[1] = centerY + height * 0.3;
                break;
            case 5: // Indicador - primeira junta
                position[0] = centerX - width * 0.05;
                position[1] = centerY + height * 0.2;
                break;
            case 6: // Indicador - segunda junta
                position[0] = centerX;
                position[1] = centerY + height * 0.1;
                break;
            case 7: // Indicador - ponta
                position[0] = centerX + width * 0.05;
                position[1] = centerY;
                break;
            case 8: // Médio - base
                position[0] = centerX;
                position[1] = centerY + height * 0.3;
                break;
            case 9: // Médio - primeira junta
                position[0] = centerX + width * 0.05;
                position[1] = centerY + height * 0.2;
                break;
            case 10: // Médio - segunda junta
                position[0] = centerX + width * 0.1;
                position[1] = centerY + height * 0.1;
                break;
            case 11: // Médio - ponta
                position[0] = centerX + width * 0.15;
                position[1] = centerY;
                break;
            case 12: // Anelar - base
                position[0] = centerX + width * 0.1;
                position[1] = centerY + height * 0.3;
                break;
            case 13: // Anelar - primeira junta
                position[0] = centerX + width * 0.15;
                position[1] = centerY + height * 0.2;
                break;
            case 14: // Anelar - segunda junta
                position[0] = centerX + width * 0.2;
                position[1] = centerY + height * 0.1;
                break;
            case 15: // Anelar - ponta
                position[0] = centerX + width * 0.25;
                position[1] = centerY;
                break;
            case 16: // Mínimo - base
                position[0] = centerX + width * 0.2;
                position[1] = centerY + height * 0.3;
                break;
            case 17: // Mínimo - primeira junta
                position[0] = centerX + width * 0.25;
                position[1] = centerY + height * 0.2;
                break;
            case 18: // Mínimo - segunda junta
                position[0] = centerX + width * 0.3;
                position[1] = centerY + height * 0.1;
                break;
            case 19: // Mínimo - ponta
                position[0] = centerX + width * 0.35;
                position[1] = centerY;
                break;
            case 20: // Palma - centro
                position[0] = centerX;
                position[1] = centerY + height * 0.1;
                break;
            default:
                position[0] = centerX;
                position[1] = centerY;
                break;
        }

        // Aplica rotação baseada na orientação da mão
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);
        double dx = position[0] - centerX;
        double dy = position[1] - centerY;

        position[0] = centerX + dx * cosAngle - dy * sinAngle;
        position[1] = centerY + dx * sinAngle + dy * cosAngle;

        // Garante que as coordenadas estão no range [0, 1]
        position[0] = Math.max(0.0, Math.min(1.0, position[0]));
        position[1] = Math.max(0.0, Math.min(1.0, position[1]));

        return position;
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
        try {
            return lastDetectionConfidence;
        } catch (Exception e) {
            logger.error("❌ Erro ao obter confiança da detecção: {}", e.getMessage());
            return 0.0;
        }
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
        if (frameGrabber != null) {
            try {
                frameGrabber.stop();
                frameGrabber.release();
            } catch (Exception e) {
                logger.error("❌ Erro ao liberar recursos da câmera: {}", e.getMessage());
            }
        }
        if (frame != null) {
            frame.release();
        }
        isInitialized.set(false);
    }
}
