package com.touchvirtual.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

/**
 * Testes para MathUtils
 * 
 * @author TouchVirtual Team
 * @version 1.0.0
 */
class MathUtilsTest {

    @Test
    void testCalculateDistance() {
        double distance = MathUtils.calculateDistance(0, 0, 3, 4);
        assertEquals(5.0, distance, 0.001);
    }

    @Test
    void testCalculateDistance3D() {
        double distance = MathUtils.calculateDistance3D(0, 0, 0, 1, 1, 1);
        assertEquals(Math.sqrt(3), distance, 0.001);
    }

    @Test
    void testNormalize() {
        double normalized = MathUtils.normalize(5, 0, 10);
        assertEquals(0.5, normalized, 0.001);
    }

    @Test
    void testClamp() {
        assertEquals(5.0, MathUtils.clamp(5.0, 0.0, 10.0), 0.001);
        assertEquals(0.0, MathUtils.clamp(-5.0, 0.0, 10.0), 0.001);
        assertEquals(10.0, MathUtils.clamp(15.0, 0.0, 10.0), 0.001);
    }

    @Test
    void testLerp() {
        double result = MathUtils.lerp(0, 10, 0.5);
        assertEquals(5.0, result, 0.001);
    }

    @Test
    void testCalculateMean() {
        List<Double> values = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        double mean = MathUtils.calculateMean(values);
        assertEquals(3.0, mean, 0.001);
    }

    @Test
    void testCalculateStandardDeviation() {
        List<Double> values = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        double stdDev = MathUtils.calculateStandardDeviation(values);
        assertTrue(stdDev > 0);
    }

    @Test
    void testApplyDeadband() {
        assertEquals(0.0, MathUtils.applyDeadband(0.02, 0.05), 0.001);
        assertEquals(0.1, MathUtils.applyDeadband(0.1, 0.05), 0.001);
    }

    @Test
    void testApplySensitivity() {
        assertEquals(2.0, MathUtils.applySensitivity(1.0, 2.0), 0.001);
        assertEquals(0.5, MathUtils.applySensitivity(1.0, 0.5), 0.001);
    }

    @Test
    void testDegreesToRadians() {
        assertEquals(Math.PI, MathUtils.degreesToRadians(180), 0.001);
        assertEquals(Math.PI / 2, MathUtils.degreesToRadians(90), 0.001);
    }

    @Test
    void testRadiansToDegrees() {
        assertEquals(180.0, MathUtils.radiansToDegrees(Math.PI), 0.001);
        assertEquals(90.0, MathUtils.radiansToDegrees(Math.PI / 2), 0.001);
    }
} 