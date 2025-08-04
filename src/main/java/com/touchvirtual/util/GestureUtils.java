package com.touchvirtual.util;

import com.touchvirtual.model.GestureType;
import com.touchvirtual.model.HandLandmark;
import java.util.List;
import java.util.ArrayList;

/**
 * Utilitários para processamento de gestos
 * 
 * @author TouchVirtual Team
 * @version 1.0.0
 */
public class GestureUtils {
    
    // Constantes para detecção de gestos
    private static final double CLICK_THRESHOLD = 0.05;
    private static final double RIGHT_CLICK_THRESHOLD = 0.03;
    private static final double DRAG_THRESHOLD = 0.05;
    private static final double SCROLL_THRESHOLD = 0.02;
    private static final double ZOOM_THRESHOLD = 0.1;
    
    /**
     * Verifica se um gesto é válido baseado na confiança
     */
    public static boolean isValidGesture(GestureType gesture, double confidence) {
        if (confidence < 0.5) {
            return false;
        }
        
        switch (gesture) {
            case CLICK:
            case RIGHT_CLICK:
            case DOUBLE_CLICK:
                return confidence >= 0.8; // Gestos precisos precisam de alta confiança
            case CURSOR_MOVE:
                return confidence >= 0.6; // Movimento é mais tolerante
            case DRAG_START:
            case DRAG_MOVE:
            case DRAG_END:
                return confidence >= 0.7; // Gestos intermediários
            case SCROLL_VERTICAL:
            case SCROLL_HORIZONTAL:
                return confidence >= 0.7;
            case ZOOM_IN:
            case ZOOM_OUT:
                return confidence >= 0.75;
            default:
                return confidence >= 0.6;
        }
    }
    
    /**
     * Calcula a confiança de um gesto baseado nos landmarks
     */
    public static double calculateGestureConfidence(List<HandLandmark> landmarks, GestureType gesture) {
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
            case DRAG_MOVE:
            case DRAG_END:
                return avgConfidence * 0.85; // Gestos intermediários
            case SCROLL_VERTICAL:
            case SCROLL_HORIZONTAL:
                return avgConfidence * 0.85;
            case ZOOM_IN:
            case ZOOM_OUT:
                return avgConfidence * 0.8;
            default:
                return avgConfidence * 0.7;
        }
    }
    
    /**
     * Detecta se há um gesto de clique
     */
    public static boolean isClickGesture(List<HandLandmark> landmarks) {
        if (landmarks.size() < 21) {
            return false;
        }
        
        // Obtém landmarks dos dedos
        HandLandmark indexTip = findLandmarkById(landmarks, 8);
        HandLandmark middleTip = findLandmarkById(landmarks, 12);
        HandLandmark ringTip = findLandmarkById(landmarks, 16);
        HandLandmark pinkyTip = findLandmarkById(landmarks, 20);
        
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
               Math.abs(middleHeight - ringHeight) < CLICK_THRESHOLD &&
               Math.abs(ringHeight - pinkyHeight) < CLICK_THRESHOLD;
    }
    
    /**
     * Detecta se há um gesto de clique direito
     */
    public static boolean isRightClickGesture(List<HandLandmark> landmarks) {
        if (landmarks.size() < 21) {
            return false;
        }
        
        HandLandmark indexTip = findLandmarkById(landmarks, 8);
        HandLandmark middleTip = findLandmarkById(landmarks, 12);
        HandLandmark ringTip = findLandmarkById(landmarks, 16);
        HandLandmark pinkyTip = findLandmarkById(landmarks, 20);
        
        if (indexTip == null || middleTip == null || ringTip == null || pinkyTip == null) {
            return false;
        }
        
        double indexHeight = indexTip.getY();
        double middleHeight = middleTip.getY();
        double ringHeight = ringTip.getY();
        double pinkyHeight = pinkyTip.getY();
        
        return indexHeight < ringHeight && 
               middleHeight < ringHeight && 
               Math.abs(indexHeight - middleHeight) < RIGHT_CLICK_THRESHOLD &&
               ringHeight > pinkyHeight;
    }
    
    /**
     * Detecta se há um gesto de arrastar
     */
    public static boolean isDragGesture(List<HandLandmark> landmarks) {
        if (landmarks.size() < 21) {
            return false;
        }
        
        HandLandmark indexTip = findLandmarkById(landmarks, 8);
        HandLandmark middleTip = findLandmarkById(landmarks, 12);
        
        if (indexTip == null || middleTip == null) {
            return false;
        }
        
        // Verifica se há pinça (distância pequena entre indicador e médio)
        double distance = MathUtils.calculateDistance(
            indexTip.getX(), indexTip.getY(),
            middleTip.getX(), middleTip.getY()
        );
        
        return distance < DRAG_THRESHOLD;
    }
    
    /**
     * Detecta se há um gesto de scroll
     */
    public static boolean isScrollGesture(List<HandLandmark> landmarks) {
        if (landmarks.size() < 21) {
            return false;
        }
        
        HandLandmark indexTip = findLandmarkById(landmarks, 8);
        HandLandmark middleTip = findLandmarkById(landmarks, 12);
        HandLandmark ringTip = findLandmarkById(landmarks, 16);
        HandLandmark pinkyTip = findLandmarkById(landmarks, 20);
        
        if (indexTip == null || middleTip == null || ringTip == null || pinkyTip == null) {
            return false;
        }
        
        // Verifica se todos os dedos estão dobrados (altura similar)
        double indexHeight = indexTip.getY();
        double middleHeight = middleTip.getY();
        double ringHeight = ringTip.getY();
        double pinkyHeight = pinkyTip.getY();
        
        double avgHeight = (indexHeight + middleHeight + ringHeight + pinkyHeight) / 4.0;
        
        return Math.abs(indexHeight - avgHeight) < SCROLL_THRESHOLD &&
               Math.abs(middleHeight - avgHeight) < SCROLL_THRESHOLD &&
               Math.abs(ringHeight - avgHeight) < SCROLL_THRESHOLD &&
               Math.abs(pinkyHeight - avgHeight) < SCROLL_THRESHOLD;
    }
    
    /**
     * Detecta se há um gesto de zoom
     */
    public static boolean isZoomGesture(List<HandLandmark> landmarks) {
        if (landmarks.size() < 21) {
            return false;
        }
        
        HandLandmark indexTip = findLandmarkById(landmarks, 8);
        HandLandmark middleTip = findLandmarkById(landmarks, 12);
        
        if (indexTip == null || middleTip == null) {
            return false;
        }
        
        // Verifica se há pinça aberta (distância grande entre dedos)
        double distance = MathUtils.calculateDistance(
            indexTip.getX(), indexTip.getY(),
            middleTip.getX(), middleTip.getY()
        );
        
        return distance > ZOOM_THRESHOLD;
    }
    
    /**
     * Detecta se há movimento de cursor
     */
    public static boolean isCursorMoveGesture(List<HandLandmark> landmarks) {
        if (landmarks.isEmpty()) {
            return false;
        }
        
        // Verifica se há pelo menos um landmark com confiança suficiente
        return landmarks.stream()
                .anyMatch(landmark -> landmark.getConfidence() > 0.6);
    }
    
    /**
     * Encontra landmark por ID
     */
    public static HandLandmark findLandmarkById(List<HandLandmark> landmarks, int id) {
        return landmarks.stream()
                .filter(landmark -> landmark.getId() == id)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Obtém o ponto de referência da mão (dedo indicador)
     */
    public static HandLandmark getReferencePoint(List<HandLandmark> landmarks) {
        // Tenta usar o dedo indicador como ponto de referência
        HandLandmark indexTip = findLandmarkById(landmarks, 8);
        if (indexTip != null && indexTip.getConfidence() > 0.5) {
            return indexTip;
        }
        
        // Fallback para o primeiro landmark com boa confiança
        return landmarks.stream()
                .filter(landmark -> landmark.getConfidence() > 0.5)
                .findFirst()
                .orElse(landmarks.isEmpty() ? null : landmarks.get(0));
    }
    
    /**
     * Calcula o centro da mão
     */
    public static double[] calculateHandCenter(List<HandLandmark> landmarks) {
        if (landmarks.isEmpty()) {
            return new double[]{0.0, 0.0};
        }
        
        double sumX = 0.0;
        double sumY = 0.0;
        int count = 0;
        
        for (HandLandmark landmark : landmarks) {
            if (landmark.getConfidence() > 0.3) {
                sumX += landmark.getX();
                sumY += landmark.getY();
                count++;
            }
        }
        
        if (count == 0) {
            return new double[]{0.0, 0.0};
        }
        
        return new double[]{sumX / count, sumY / count};
    }
    
    /**
     * Calcula a velocidade de movimento da mão
     */
    public static double calculateHandVelocity(List<HandLandmark> landmarks, 
                                            List<HandLandmark> previousLandmarks,
                                            long currentTime, long previousTime) {
        if (landmarks.isEmpty() || previousLandmarks.isEmpty()) {
            return 0.0;
        }
        
        double[] currentCenter = calculateHandCenter(landmarks);
        double[] previousCenter = calculateHandCenter(previousLandmarks);
        
        return MathUtils.calculateVelocity(
            previousCenter[0], previousCenter[1], previousTime,
            currentCenter[0], currentCenter[1], currentTime
        );
    }
    
    /**
     * Verifica se a mão está estável (pouco movimento)
     */
    public static boolean isHandStable(List<HandLandmark> landmarks, 
                                     List<HandLandmark> previousLandmarks,
                                     double stabilityThreshold) {
        if (landmarks.isEmpty() || previousLandmarks.isEmpty()) {
            return false;
        }
        
        double[] currentCenter = calculateHandCenter(landmarks);
        double[] previousCenter = calculateHandCenter(previousLandmarks);
        
        double distance = MathUtils.calculateDistance(
            currentCenter[0], currentCenter[1],
            previousCenter[0], previousCenter[1]
        );
        
        return distance < stabilityThreshold;
    }
    
    /**
     * Calcula a área da mão baseada nos landmarks
     */
    public static double calculateHandArea(List<HandLandmark> landmarks) {
        if (landmarks.size() < 3) {
            return 0.0;
        }
        
        // Converte landmarks para pontos para cálculo de área
        List<double[]> points = new ArrayList<>();
        for (HandLandmark landmark : landmarks) {
            if (landmark.getConfidence() > 0.3) {
                points.add(new double[]{landmark.getX(), landmark.getY()});
            }
        }
        
        if (points.size() < 3) {
            return 0.0;
        }
        
        return MathUtils.calculatePolygonArea(points);
    }
    
    /**
     * Verifica se há múltiplas mãos
     */
    public static boolean hasMultipleHands(List<HandLandmark> landmarks) {
        // Implementação simplificada - verifica se há clusters de landmarks
        if (landmarks.size() < 42) { // Menos que 2 mãos * 21 landmarks
            return false;
        }
        
        // Agrupa landmarks por proximidade
        List<List<HandLandmark>> clusters = new ArrayList<>();
        
        for (HandLandmark landmark : landmarks) {
            boolean addedToCluster = false;
            
            for (List<HandLandmark> cluster : clusters) {
                if (!cluster.isEmpty()) {
                    HandLandmark clusterCenter = cluster.get(0);
                    double distance = MathUtils.calculateDistance(
                        landmark.getX(), landmark.getY(),
                        clusterCenter.getX(), clusterCenter.getY()
                    );
                    
                    if (distance < 0.2) { // Threshold para considerar mesmo cluster
                        cluster.add(landmark);
                        addedToCluster = true;
                        break;
                    }
                }
            }
            
            if (!addedToCluster) {
                List<HandLandmark> newCluster = new ArrayList<>();
                newCluster.add(landmark);
                clusters.add(newCluster);
            }
        }
        
        return clusters.size() > 1;
    }
    
    /**
     * Filtra landmarks por confiança
     */
    public static List<HandLandmark> filterLandmarksByConfidence(List<HandLandmark> landmarks, 
                                                               double minConfidence) {
        List<HandLandmark> filtered = new ArrayList<>();
        
        for (HandLandmark landmark : landmarks) {
            if (landmark.getConfidence() >= minConfidence) {
                filtered.add(landmark);
            }
        }
        
        return filtered;
    }
    
    /**
     * Suaviza landmarks usando média móvel
     */
    public static List<HandLandmark> smoothLandmarks(List<HandLandmark> landmarks,
                                                   List<List<HandLandmark>> history,
                                                   int windowSize) {
        if (landmarks.isEmpty()) {
            return landmarks;
        }
        
        List<HandLandmark> smoothed = new ArrayList<>();
        
        for (HandLandmark landmark : landmarks) {
            // Coleta histórico para este landmark específico
            List<Double> xHistory = new ArrayList<>();
            List<Double> yHistory = new ArrayList<>();
            List<Double> zHistory = new ArrayList<>();
            
            // Adiciona landmark atual
            xHistory.add(landmark.getX());
            yHistory.add(landmark.getY());
            zHistory.add(landmark.getZ());
            
            // Adiciona histórico
            for (List<HandLandmark> historicalLandmarks : history) {
                HandLandmark historicalLandmark = findLandmarkById(historicalLandmarks, landmark.getId());
                if (historicalLandmark != null) {
                    xHistory.add(historicalLandmark.getX());
                    yHistory.add(historicalLandmark.getY());
                    zHistory.add(historicalLandmark.getZ());
                }
            }
            
            // Aplica suavização
            double smoothedX = MathUtils.smoothMovingAverage(xHistory, windowSize);
            double smoothedY = MathUtils.smoothMovingAverage(yHistory, windowSize);
            double smoothedZ = MathUtils.smoothMovingAverage(zHistory, windowSize);
            
            // Cria landmark suavizado
            HandLandmark smoothedLandmark = new HandLandmark();
            smoothedLandmark.setId(landmark.getId());
            smoothedLandmark.setX(smoothedX);
            smoothedLandmark.setY(smoothedY);
            smoothedLandmark.setZ(smoothedZ);
            smoothedLandmark.setConfidence(landmark.getConfidence());
            
            smoothed.add(smoothedLandmark);
        }
        
        return smoothed;
    }
} 