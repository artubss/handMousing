package com.touchvirtual.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuração do WebSocket para comunicação em tempo real
 * 
 * @author TouchVirtual Team
 * @version 1.0.0
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita o broker de mensagens simples
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefixo para mensagens enviadas pelo cliente
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefixo para mensagens de usuário específico
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint para conexão WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // Endpoint para comunicação direta WebSocket
        registry.addEndpoint("/ws-direct")
                .setAllowedOriginPatterns("*");
    }
} 