package com.touchvirtual.util;

import com.touchvirtual.model.GestureType;
import com.touchvirtual.model.HandLandmark;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Testes para GestureUtils
 * 
 * @author TouchVirtual Team
 * @version 1.0.0
 */
class GestureUtilsTest {

    @Test
    void testIsValidGesture() {
        assertTrue(GestureUtils.isValidGesture(GestureType.CLICK, 0.8));
        assertFalse(GestureUtils.isValidGesture(GestureType.CLICK, 0.3));
        assertTrue(GestureUtils.isValidGesture(GestureType.CURSOR_MOVE, 0.6));
    }

    @Test
    void testCalculateGestureConfidence() {
        List<HandLandmark> landmarks = createTestLandmarks();
        double confidence = GestureUtils.calculateGestureConfidence(landmarks, GestureType.CLICK);
        assertTrue(confidence > 0);
    }

    @Test
    void testFindLandmarkById() {
        List<HandLandmark> landmarks = createTestLandmarks();
        HandLandmark landmark = GestureUtils.findLandmarkById(landmarks, 8);
        assertNotNull(landmark);
        assertEquals(8, landmark.getId());
    }

    @Test
    void testGetReferencePoint() {
        List<HandLandmark> landmarks = createTestLandmarks();
        HandLandmark reference = GestureUtils.getReferencePoint(landmarks);
        assertNotNull(reference);
    }

    @Test
    void testCalculateHandCenter() {
        List<HandLandmark> landmarks = createTestLandmarks();
        double[] center = GestureUtils.calculateHandCenter(landmarks);
        assertEquals(2, center.length);
        assertTrue(center[0] >= 0 && center[0] <= 1);
        assertTrue(center[1] >= 0 && center[1] <= 1);
    }

    @Test
    void testFilterLandmarksByConfidence() {
        List<HandLandmark> landmarks = createTestLandmarks();
        List<HandLandmark> filtered = GestureUtils.filterLandmarksByConfidence(landmarks, 0.8);
        assertNotNull(filtered);
        assertTrue(filtered.size() <= landmarks.size());
    }

    @Test
    void testCalculateHandArea() {
        List<HandLandmark> landmarks = createTestLandmarks();
        double area = GestureUtils.calculateHandArea(landmarks);
        assertTrue(area >= 0);
    }

    @Test
    void testHasMultipleHands() {
        List<HandLandmark> landmarks = createTestLandmarks();
        boolean hasMultiple = GestureUtils.hasMultipleHands(landmarks);
        // Com poucos landmarks, deve retornar false
        assertFalse(hasMultiple);
    }

    @Test
    void testIsCursorMoveGesture() {
        List<HandLandmark> landmarks = createTestLandmarks();
        boolean isCursorMove = GestureUtils.isCursorMoveGesture(landmarks);
        assertTrue(isCursorMove);
    }

    /**
     * Cria landmarks de teste
     */
    private List<HandLandmark> createTestLandmarks() {
        List<HandLandmark> landmarks = new ArrayList<>();
        
        // Cria 21 landmarks simulados
        for (int i = 0; i < 21; i++) {
            HandLandmark landmark = new HandLandmark();
            landmark.setId(i);
            landmark.setX(0.5 + (i * 0.01));
            landmark.setY(0.5 + (i * 0.01));
            landmark.setZ(0.0);
            landmark.setConfidence(0.8 + (i * 0.01));
            landmarks.add(landmark);
        }
        
        return landmarks;
    }
} 