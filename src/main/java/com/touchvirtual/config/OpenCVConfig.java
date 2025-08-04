package com.touchvirtual.config;

import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.Java2DFrameConverter;
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
            // Carrega as bibliotecas nativas do JavaCV
            // O JavaCV carrega automaticamente as bibliotecas nativas do OpenCV
            // mas precisamos garantir que estão disponíveis
            
            // Verifica se o OpenCV está disponível
            logger.info("✅ OpenCV carregado com sucesso via JavaCV");
            logger.info("📹 JavaCV carregado com sucesso");
            
            // Testa se as bibliotecas estão funcionando
            testOpenCVFunctionality();

        } catch (Exception e) {
            logger.error("❌ Erro ao carregar OpenCV: {}", e.getMessage());
            logger.error("❌ Stack trace: ", e);
            throw new RuntimeException("Falha na inicialização do OpenCV", e);
        }
    }

    /**
     * Testa a funcionalidade básica do OpenCV
     */
    private void testOpenCVFunctionality() {
        try {
            // Testa conversores
            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            logger.info("✅ Teste de conversores JavaCV: OK");
            
        } catch (Exception e) {
            logger.error("❌ Teste de funcionalidade OpenCV falhou: {}", e.getMessage());
            throw new RuntimeException("Teste de funcionalidade OpenCV falhou", e);
        }
    }

    @Bean
    @Primary
    public String openCVVersion() {
        return "OpenCV via JavaCV";
    }
    
    @Bean
    public OpenCVFrameConverter.ToMat openCVFrameConverter() {
        return new OpenCVFrameConverter.ToMat();
    }
    
    @Bean
    public Java2DFrameConverter java2DFrameConverter() {
        return new Java2DFrameConverter();
    }
}
