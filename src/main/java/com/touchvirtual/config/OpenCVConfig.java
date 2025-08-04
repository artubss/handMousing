package com.touchvirtual.config;

import org.opencv.core.Core;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;

/**
 * Configuração do OpenCV para o sistema de detecção de gestos
 *
 * @author TouchVirtual Team
 * @version 1.0.0
 */
@Configuration
public class OpenCVConfig {

    private static final Logger logger = LoggerFactory.getLogger(OpenCVConfig.class);

    @PostConstruct
    public void initializeOpenCV() {
        try {
            // JavaCV já carrega automaticamente as bibliotecas nativas do OpenCV
            // Não é necessário carregar manualmente

            logger.info("✅ OpenCV carregado com sucesso - Versão: {}", Core.VERSION);
            logger.info("📹 JavaCV carregado com sucesso");

        } catch (Exception e) {
            logger.error("❌ Erro ao carregar OpenCV: {}", e.getMessage());
            throw new RuntimeException("Falha na inicialização do OpenCV", e);
        }
    }

    @Bean
    @Primary
    String openCVVersion() {
        return Core.VERSION;
    }
}
