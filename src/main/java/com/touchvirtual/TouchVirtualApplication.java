package com.touchvirtual;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Aplicação principal do sistema de touchscreen virtual
 *
 * @author TouchVirtual Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class TouchVirtualApplication {

    public static void main(String[] args) {
        // JavaCV carrega automaticamente as bibliotecas nativas do OpenCV
        // Não é necessário carregar manualmente

        SpringApplication.run(TouchVirtualApplication.class, args);

        System.out.println("🚀 TouchVirtual iniciado com sucesso!");
        System.out.println("📱 Acesse: http://localhost:8082");
        System.out.println("🎯 Sistema de touchscreen virtual ativo");
    }
}
