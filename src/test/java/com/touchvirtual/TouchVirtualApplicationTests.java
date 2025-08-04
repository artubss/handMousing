package com.touchvirtual;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Teste básico para verificar se a aplicação inicia corretamente
 * 
 * @author TouchVirtual Team
 * @version 1.0.0
 */
@SpringBootTest
@TestPropertySource(properties = {
    "camera.device-index=-1", // Desabilita câmera para testes
    "mouse.enabled=false",     // Desabilita mouse para testes
    "websocket.enabled=false"  // Desabilita WebSocket para testes
})
class TouchVirtualApplicationTests {

    @Test
    void contextLoads() {
        // Teste simples para verificar se o contexto Spring carrega
    }
} 