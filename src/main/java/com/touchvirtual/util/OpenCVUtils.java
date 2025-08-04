package com.touchvirtual.util;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

/**
 * Utilitários para OpenCV
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
        Imgcodecs.imencode(".jpg", mat, matOfByte);
        return matOfByte.toArray();
    }
    
    /**
     * Converte array de bytes para Mat
     */
    public static Mat bytesToMat(byte[] bytes) {
        return Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_COLOR);
    }
    
    /**
     * Redimensiona uma imagem
     */
    public static Mat resize(Mat input, int width, int height) {
        Mat output = new Mat();
        Imgproc.resize(input, output, new Size(width, height));
        return output;
    }
    
    /**
     * Aplica filtro Gaussiano
     */
    public static Mat applyGaussianBlur(Mat input, int kernelSize) {
        Mat output = new Mat();
        Imgproc.GaussianBlur(input, output, new Size(kernelSize, kernelSize), 0);
        return output;
    }
    
    /**
     * Aplica equalização de histograma
     */
    public static Mat applyHistogramEqualization(Mat input) {
        Mat output = new Mat();
        Imgproc.equalizeHist(input, output);
        return output;
    }
    
    /**
     * Detecta bordas usando Canny
     */
    public static Mat detectEdges(Mat input, double threshold1, double threshold2) {
        Mat output = new Mat();
        Imgproc.Canny(input, output, threshold1, threshold2);
        return output;
    }
    
    /**
     * Encontra contornos na imagem
     */
    public static List<MatOfPoint> findContours(Mat input) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(input, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }
    
    /**
     * Desenha contornos na imagem
     */
    public static Mat drawContours(Mat input, List<MatOfPoint> contours, Scalar color) {
        Mat output = input.clone();
        Imgproc.drawContours(output, contours, -1, color, 2);
        return output;
    }
    
    /**
     * Calcula a área de um contorno
     */
    public static double calculateContourArea(MatOfPoint contour) {
        return Imgproc.contourArea(contour);
    }
    
    /**
     * Obtém o retângulo delimitador de um contorno
     */
    public static Rect getBoundingRect(MatOfPoint contour) {
        return Imgproc.boundingRect(contour);
    }
    
    /**
     * Desenha um retângulo na imagem
     */
    public static Mat drawRectangle(Mat input, Rect rect, Scalar color, int thickness) {
        Mat output = input.clone();
        Imgproc.rectangle(output, rect, color, thickness);
        return output;
    }
    
    /**
     * Converte BGR para HSV
     */
    public static Mat bgrToHsv(Mat input) {
        Mat output = new Mat();
        Imgproc.cvtColor(input, output, Imgproc.COLOR_BGR2HSV);
        return output;
    }
    
    /**
     * Converte BGR para escala de cinza
     */
    public static Mat bgrToGray(Mat input) {
        Mat output = new Mat();
        Imgproc.cvtColor(input, output, Imgproc.COLOR_BGR2GRAY);
        return output;
    }
    
    /**
     * Aplica threshold binário
     */
    public static Mat applyThreshold(Mat input, double threshold, double maxValue) {
        Mat output = new Mat();
        Imgproc.threshold(input, output, threshold, maxValue, Imgproc.THRESH_BINARY);
        return output;
    }
    
    /**
     * Aplica threshold adaptativo
     */
    public static Mat applyAdaptiveThreshold(Mat input, double maxValue, int blockSize, double C) {
        Mat output = new Mat();
        Imgproc.adaptiveThreshold(input, output, maxValue, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, 
                                 Imgproc.THRESH_BINARY, blockSize, C);
        return output;
    }
    
    /**
     * Aplica operação morfológica
     */
    public static Mat applyMorphology(Mat input, int operation, Mat kernel) {
        Mat output = new Mat();
        Imgproc.morphologyEx(input, output, operation, kernel);
        return output;
    }
    
    /**
     * Cria kernel estruturante
     */
    public static Mat createStructuringElement(int shape, Size size) {
        return Imgproc.getStructuringElement(shape, size);
    }
    
    /**
     * Calcula o centro de massa de um contorno
     */
    public static Point calculateContourCenter(MatOfPoint contour) {
        // Implementação simplificada - usa o centro do retângulo delimitador
        Rect boundingRect = getBoundingRect(contour);
        return new Point(boundingRect.x + boundingRect.width / 2.0, 
                        boundingRect.y + boundingRect.height / 2.0);
    }
    
    /**
     * Calcula a convexidade de um contorno
     */
    public static MatOfInt getConvexHull(MatOfPoint contour) {
        MatOfInt hull = new MatOfInt();
        Imgproc.convexHull(contour, hull);
        return hull;
    }
    
    /**
     * Calcula defeitos de convexidade
     */
    public static MatOfInt4 getConvexityDefects(MatOfPoint contour, MatOfInt hull) {
        MatOfInt4 defects = new MatOfInt4();
        Imgproc.convexityDefects(contour, hull, defects);
        return defects;
    }
    
    /**
     * Desenha pontos na imagem
     */
    public static Mat drawPoints(Mat input, List<Point> points, Scalar color, int radius) {
        Mat output = input.clone();
        for (Point point : points) {
            Imgproc.circle(output, point, radius, color, -1);
        }
        return output;
    }
    
    /**
     * Desenha linhas na imagem
     */
    public static Mat drawLines(Mat input, List<Point> points, Scalar color, int thickness) {
        Mat output = input.clone();
        for (int i = 0; i < points.size() - 1; i++) {
            Imgproc.line(output, points.get(i), points.get(i + 1), color, thickness);
        }
        return output;
    }
    
    /**
     * Calcula a distância entre dois pontos
     */
    public static double calculateDistance(Point p1, Point p2) {
        double dx = p1.x - p2.x;
        double dy = p1.y - p2.y;
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
    public static List<MatOfPoint> filterContoursByArea(List<MatOfPoint> contours, double minArea, double maxArea) {
        List<MatOfPoint> filtered = new ArrayList<>();
        
        for (MatOfPoint contour : contours) {
            double area = calculateContourArea(contour);
            if (area >= minArea && area <= maxArea) {
                filtered.add(contour);
            }
        }
        
        return filtered;
    }
    
    /**
     * Obtém informações da imagem
     */
    public static String getImageInfo(Mat mat) {
        return "Size: %dx%d, Type: %s, Channels: %d".formatted(
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