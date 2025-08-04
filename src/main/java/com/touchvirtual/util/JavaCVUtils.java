package com.touchvirtual.util;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.*;
import org.bytedeco.opencv.opencv_imgcodecs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;

/**
 * Utilitários para OpenCV via JavaCV
 *
 * @author TouchVirtual Team
 * @version 1.0.0
 */
public class OpenCVUtils {

    private static final Logger logger = LoggerFactory.getLogger(OpenCVUtils.class);

    /**
     * Converte Mat para array de bytes
     */
    public static byte[] matToBytes(Mat mat) {
        MatOfByte matOfByte = new MatOfByte();
        imencode(".jpg", mat, matOfByte);
        return matOfByte.get();
    }

    /**
     * Converte array de bytes para Mat
     */
    public static Mat bytesToMat(byte[] bytes) {
        return imdecode(new MatOfByte(bytes), IMREAD_COLOR);
    }

    /**
     * Redimensiona uma imagem
     */
    public static Mat resize(Mat input, int width, int height) {
        Mat output = new Mat();
        resize(input, output, new Size(width, height));
        return output;
    }

    /**
     * Aplica filtro Gaussiano
     */
    public static Mat applyGaussianBlur(Mat input, int kernelSize) {
        Mat output = new Mat();
        GaussianBlur(input, output, new Size(kernelSize, kernelSize), 0);
        return output;
    }

    /**
     * Aplica equalização de histograma
     */
    public static Mat applyHistogramEqualization(Mat input) {
        Mat output = new Mat();
        equalizeHist(input, output);
        return output;
    }

    /**
     * Detecta bordas usando Canny
     */
    public static Mat detectEdges(Mat input, double threshold1, double threshold2) {
        Mat output = new Mat();
        Canny(input, output, threshold1, threshold2);
        return output;
    }

    /**
     * Encontra contornos na imagem
     */
    public static MatVector findContours(Mat input) {
        MatVector contours = new MatVector();
        Mat hierarchy = new Mat();
        findContours(input, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        return contours;
    }

    /**
     * Calcula a área de um contorno
     */
    public static double calculateContourArea(Mat contour) {
        return contourArea(contour);
    }

    /**
     * Obtém o retângulo delimitador de um contorno
     */
    public static Rect getBoundingRect(Mat contour) {
        return boundingRect(contour);
    }

    /**
     * Converte BGR para HSV
     */
    public static Mat bgrToHsv(Mat input) {
        Mat output = new Mat();
        cvtColor(input, output, COLOR_BGR2HSV);
        return output;
    }

    /**
     * Converte BGR para escala de cinza
     */
    public static Mat bgrToGray(Mat input) {
        Mat output = new Mat();
        cvtColor(input, output, COLOR_BGR2GRAY);
        return output;
    }

    /**
     * Aplica threshold binário
     */
    public static Mat applyThreshold(Mat input, double threshold, double maxValue) {
        Mat output = new Mat();
        threshold(input, output, threshold, maxValue, THRESH_BINARY);
        return output;
    }

    /**
     * Aplica threshold adaptativo
     */
    public static Mat applyAdaptiveThreshold(Mat input, double maxValue, int blockSize, double C) {
        Mat output = new Mat();
        adaptiveThreshold(input, output, maxValue, ADAPTIVE_THRESH_GAUSSIAN_C,
                THRESH_BINARY, blockSize, C);
        return output;
    }

    /**
     * Aplica operação morfológica
     */
    public static Mat applyMorphology(Mat input, int operation, Mat kernel) {
        Mat output = new Mat();
        morphologyEx(input, output, operation, kernel);
        return output;
    }

    /**
     * Cria kernel estruturante
     */
    public static Mat createStructuringElement(int shape, Size size) {
        return getStructuringElement(shape, size);
    }

    /**
     * Calcula o centro de massa de um contorno
     */
    public static Point calculateContourCenter(Mat contour) {
        // Implementação simplificada - usa o centro do retângulo delimitador
        Rect boundingRect = getBoundingRect(contour);
        return new Point((int) (boundingRect.x() + boundingRect.width() / 2.0),
                (int) (boundingRect.y() + boundingRect.height() / 2.0));
    }

    /**
     * Calcula a distância entre dois pontos
     */
    public static double calculateDistance(Point p1, Point p2) {
        double dx = p1.x() - p2.x();
        double dy = p1.y() - p2.y();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Calcula o ângulo entre três pontos
     */
    public static double calculateAngle(Point p1, Point p2, Point p3) {
        double a = calculateDistance(p1, p2);
        double b = calculateDistance(p2, p3);
        double c = calculateDistance(p1, p3);

        if (a == 0 || b == 0) {
            return 0.0;
        }

        double cosAngle = (a * a + b * b - c * c) / (2 * a * b);
        cosAngle = Math.max(-1.0, Math.min(1.0, cosAngle)); // Clamp to [-1, 1]

        return Math.acos(cosAngle) * 180.0 / Math.PI;
    }

    /**
     * Filtra contornos por área
     */
    public static MatVector filterContoursByArea(MatVector contours, double minArea, double maxArea) {
        MatVector filtered = new MatVector();

        for (long i = 0; i < contours.size(); i++) {
            Mat contour = contours.get(i);
            double area = calculateContourArea(contour);
            if (area >= minArea && area <= maxArea) {
                filtered.push_back(contour);
            }
        }

        return filtered;
    }

    /**
     * Obtém informações da imagem
     */
    public static String getImageInfo(Mat mat) {
        return "Size: %dx%d, Type: %d, Channels: %d".formatted(
                mat.rows(), mat.cols(), mat.type(), mat.channels());
    }

    /**
     * Libera recursos de uma lista de Mats
     */
    public static void releaseMats(List<Mat> mats) {
        for (Mat mat : mats) {
            if (mat != null && !mat.empty()) {
                mat.release();
            }
        }
    }

    /**
     * Libera recursos de um array de Mats
     */
    public static void releaseMats(Mat... mats) {
        for (Mat mat : mats) {
            if (mat != null && !mat.empty()) {
                mat.release();
            }
        }
    }
}
