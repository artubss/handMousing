package com.touchvirtual.service;

import com.touchvirtual.model.GestureType;
import com.touchvirtual.model.HandLandmark;
import com.touchvirtual.model.UserSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.touchvirtual.service.HandDetectionService;

/**
 * Serviço de reconhecimento de gestos baseado nos landmarks detectados
 * 
 * @author TouchVirtual Team
 * @version 1.0.0
 */
@Service
public class GestureRecognitionService {
    
    private static final Logger logger = LoggerFactory.getLogger(GestureRecognitionService.class);
    
    @Autowired
    private HandDetectionService handDetectionService;
    
    @Autowired
    private UserSettings userSettings;
    
    // Buffer para histórico de gestos (últimos 10 frames)
    private Queue<List<HandLandmark>> gestureHistory;
    private GestureType lastRecognizedGesture;
    private long lastGestureTime;
    private int gestureConfidence;
    
    // Constantes para reconhecimento
    private static final int GESTURE_HISTORY_SIZE = 10;
    private static final long GESTURE_TIMEOUT_MS = 500; // 500ms entre gestos
    private static final double CONFIDENCE_THRESHOLD = 0.7;
    
    public GestureRecognitionService() {
        this.gestureHistory = new ConcurrentLinkedQueue<>();
        this.lastRecognizedGesture = GestureType.NO_HAND;
        this.lastGestureTime = 0;
        this.gestureConfidence = 0;
    }
    
    /**
     * Reconhece o gesto atual baseado nos landmarks
     */
    public GestureType recognizeGesture(List<HandLandmark> landmarks) {
        if (landmarks == null || landmarks.isEmpty()) {
            return GestureType.NO_HAND;
        }
        
        // Adiciona ao histórico
        addToHistory(landmarks);
        
        // Verifica se há tempo suficiente desde o último gesto
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastGestureTime < GESTURE_TIMEOUT_MS) {
            return lastRecognizedGesture;
        }
        
        // Analisa o gesto
        GestureType gesture = analyzeGesture(landmarks);
        
        // Calcula confiança
        double confidence = calculateGestureConfidence(gesture, landmarks);
        
        if (confidence > CONFIDENCE_THRESHOLD) {
            lastRecognizedGesture = gesture;
            lastGestureTime = currentTime;
            gestureConfidence = (int) (confidence * 100);
            
            logger.debug("🎯 Gesto reconhecido: {} (confiança: {}%)", 
                        gesture.getDisplayName(), gestureConfidence);
        }
        
        return lastRecognizedGesture;
    }
    
    /**
     * Analisa o gesto baseado nos landmarks
     */
    private GestureType analyzeGesture(List<HandLandmark> landmarks) {
        if (landmarks.size() < 21) {
            return GestureType.UNCERTAIN;
        }
        
        // Obtém landmarks específicos
        HandLandmark wrist = findLandmarkById(landmarks, 0);
        HandLandmark thumbTip = findLandmarkById(landmarks, 4);
        HandLandmark indexTip = findLandmarkById(landmarks, 8);
        HandLandmark middleTip = findLandmarkById(landmarks, 12);
        HandLandmark ringTip = findLandmarkById(landmarks, 16);
        HandLandmark pinkyTip = findLandmarkById(landmarks, 20);
        
        if (wrist == null || indexTip == null) {
            return GestureType.UNCERTAIN;
        }
        
        // Calcula distâncias e ângulos
        double indexDistance = calculateDistance(wrist, indexTip);
        double middleDistance = calculateDistance(wrist, middleTip);
        double ringDistance = calculateDistance(wrist, ringTip);
        double pinkyDistance = calculateDistance(wrist, pinkyTip);
        
        // Detecta gestos específicos
        if (isClickGesture(indexTip, middleTip, ringTip, pinkyTip)) {
            return GestureType.CLICK;
        }
        
        if (isRightClickGesture(indexTip, middleTip, ringTip, pinkyTip)) {
            return GestureType.RIGHT_CLICK;
        }
        
        if (isDoubleClickGesture()) {
            return GestureType.DOUBLE_CLICK;
        }
        
        if (isDragGesture(indexTip, middleTip, ringTip, pinkyTip)) {
            return GestureType.DRAG_START;
        }
        
        if (isScrollGesture(indexTip, middleTip, ringTip, pinkyTip)) {
            return GestureType.SCROLL_VERTICAL;
        }
        
        if (isZoomGesture(indexTip, middleTip, ringTip, pinkyTip)) {
            return GestureType.ZOOM_IN;
        }
        
        // Gesto padrão: movimento do cursor
        return GestureType.CURSOR_MOVE;
    }
    
    /**
     * Detecta gesto de clique (dedo indicador estendido, outros dobrados)
     */
    private boolean isClickGesture(HandLandmark indexTip, HandLandmark middleTip, 
                                 HandLandmark ringTip, HandLandmark pinkyTip) {
        if (indexTip == null || middleTip == null || ringTip == null || pinkyTip == null) {
            return false;
        }
        
        // Verifica se o indicador está estendido e outros dedos dobrados
        double indexHeight = indexTip.getY();
        double middleHeight = middleTip.getY();
        double ringHeight = ringTip.getY();
        double pinkyHeight = pinkyTip.getY();
        
        return indexHeight < middleHeight && 
               indexHeight < ringHeight && 
               indexHeight < pinkyHeight &&
               Math.abs(middleHeight - ringHeight) < 0.05 &&
               Math.abs(ringHeight - pinkyHeight) < 0.05;
    }
    
    /**
     * Detecta gesto de clique direito (dois dedos estendidos)
     */
    private boolean isRightClickGesture(HandLandmark indexTip, HandLandmark middleTip, 
                                      HandLandmark ringTip, HandLandmark pinkyTip) {
        if (indexTip == null || middleTip == null || ringTip == null || pinkyTip == null) {
            return false;
        }
        
        double indexHeight = indexTip.getY();
        double middleHeight = middleTip.getY();
        double ringHeight = ringTip.getY();
        double pinkyHeight = pinkyTip.getY();
        
        return indexHeight < ringHeight && 
               middleHeight < ringHeight && 
               Math.abs(indexHeight - middleHeight) < 0.03 &&
               ringHeight > pinkyHeight;
    }
    
    /**
     * Detecta duplo clique (baseado no histórico)
     */
    private boolean isDoubleClickGesture() {
        // Implementação simplificada - verifica se o último gesto foi CLICK
        return lastRecognizedGesture == GestureType.CLICK && 
               System.currentTimeMillis() - lastGestureTime < 300;
    }
    
    /**
     * Detecta gesto de arrastar (pinça)
     */
    private boolean isDragGesture(HandLandmark indexTip, HandLandmark middleTip, 
                                HandLandmark ringTip, HandLandmark pinkyTip) {
        if (indexTip == null || middleTip == null) {
            return false;
        }
        
        // Verifica se há pinça (distância pequena entre indicador e médio)
        double distance = calculateDistance(indexTip, middleTip);
        return distance < 0.05;
    }
    
    /**
     * Detecta gesto de scroll (mão fechada)
     */
    private boolean isScrollGesture(HandLandmark indexTip, HandLandmark middleTip, 
                                  HandLandmark ringTip, HandLandmark pinkyTip) {
        if (indexTip == null || middleTip == null || ringTip == null || pinkyTip == null) {
            return false;
        }
        
        // Verifica se todos os dedos estão dobrados (altura similar)
        double indexHeight = indexTip.getY();
        double middleHeight = middleTip.getY();
        double ringHeight = ringTip.getY();
        double pinkyHeight = pinkyTip.getY();
        
        double avgHeight = (indexHeight + middleHeight + ringHeight + pinkyHeight) / 4.0;
        double tolerance = 0.02;
        
        return Math.abs(indexHeight - avgHeight) < tolerance &&
               Math.abs(middleHeight - avgHeight) < tolerance &&
               Math.abs(ringHeight - avgHeight) < tolerance &&
               Math.abs(pinkyHeight - avgHeight) < tolerance;
    }
    
    /**
     * Detecta gesto de zoom (pinça abrindo/fechando)
     */
    private boolean isZoomGesture(HandLandmark indexTip, HandLandmark middleTip, 
                                HandLandmark ringTip, HandLandmark pinkyTip) {
        // Implementação simplificada - baseada na distância entre dedos
        if (indexTip == null || middleTip == null) {
            return false;
        }
        
        double distance = calculateDistance(indexTip, middleTip);
        return distance > 0.1; // Pinça aberta
    }
    
    /**
     * Encontra landmark por ID
     */
    private HandLandmark findLandmarkById(List<HandLandmark> landmarks, int id) {
        return landmarks.stream()
                .filter(landmark -> landmark.getId() == id)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Calcula distância entre dois landmarks
     */
    private double calculateDistance(HandLandmark p1, HandLandmark p2) {
        if (p1 == null || p2 == null) {
            return 0.0;
        }
        
        double dx = p1.getX() - p2.getX();
        double dy = p1.getY() - p2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Calcula confiança do gesto reconhecido
     */
    private double calculateGestureConfidence(GestureType gesture, List<HandLandmark> landmarks) {
        if (landmarks.isEmpty()) {
            return 0.0;
        }
        
        // Calcula confiança média dos landmarks
        double avgConfidence = landmarks.stream()
                .mapToDouble(HandLandmark::getConfidence)
                .average()
                .orElse(0.0);
        
        // Ajusta baseado no tipo de gesto
        switch (gesture) {
            case CLICK:
            case RIGHT_CLICK:
            case DOUBLE_CLICK:
                return avgConfidence * 0.9; // Gestos precisos
            case CURSOR_MOVE:
                return avgConfidence * 0.8; // Movimento mais tolerante
            case DRAG_START:
            case SCROLL_VERTICAL:
                return avgConfidence * 0.85; // Gestos intermediários
            default:
                return avgConfidence * 0.7; // Outros gestos
        }
    }
    
    /**
     * Adiciona landmarks ao histórico
     */
    private void addToHistory(List<HandLandmark> landmarks) {
        gestureHistory.offer(new ArrayList<>(landmarks));
        
        // Mantém apenas os últimos N frames
        while (gestureHistory.size() > GESTURE_HISTORY_SIZE) {
            gestureHistory.poll();
        }
    }
    
    /**
     * Obtém o último gesto reconhecido
     */
    public GestureType getLastRecognizedGesture() {
        return lastRecognizedGesture;
    }
    
    /**
     * Obtém a confiança do último gesto
     */
    public int getGestureConfidence() {
        return gestureConfidence;
    }
    
    /**
     * Obtém o tempo do último gesto
     */
    public long getLastGestureTime() {
        return lastGestureTime;
    }
    
    /**
     * Verifica se um gesto está habilitado nas configurações
     */
    public boolean isGestureEnabled(GestureType gestureType) {
        return userSettings.isGestureEnabled(gestureType);
    }
} 