package com.touchvirtual.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuração da câmera para captura de vídeo
 *
 * @author TouchVirtual Team
 * @version 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "camera")
public class CameraConfig {

    private static final Logger logger = LoggerFactory.getLogger(CameraConfig.class);

    private int deviceIndex = 0;
    private int frameWidth = 640;
    private int frameHeight = 480;
    private int fps = 30;
    private boolean autoExposure = true;
    private double exposure = 0.0;
    private double gain = 0.0;

    @Bean
    public CameraConfig cameraSettings() {
        logger.info("📹 Configuração da câmera:");
        logger.info("   - Dispositivo: {}", deviceIndex);
        logger.info("   - Resolução: {}x{}", frameWidth, frameHeight);
        logger.info("   - FPS: {}", fps);
        logger.info("   - Auto-exposição: {}", autoExposure);

        return this;
    }

    // Getters e Setters
    public int getDeviceIndex() {
        return deviceIndex;
    }

    public void setDeviceIndex(int deviceIndex) {
        this.deviceIndex = deviceIndex;
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public boolean isAutoExposure() {
        return autoExposure;
    }

    public void setAutoExposure(boolean autoExposure) {
        this.autoExposure = autoExposure;
    }

    public double getExposure() {
        return exposure;
    }

    public void setExposure(double exposure) {
        this.exposure = exposure;
    }

    public double getGain() {
        return gain;
    }

    public void setGain(double gain) {
        this.gain = gain;
    }
}
