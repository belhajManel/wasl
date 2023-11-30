package com.mzo.wasl.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    // This method is used to configure the message broker. It starts by calling enableSimpleBroker() to enable a simple memory-based message broker to carry the greeting messages back to the client on destinations prefixed with "/topic". It also designates the "/app" prefix for messages that are bound for @MessageMapping-annotated methods.
    @Override
    public void configureMessageBroker(org.springframework.messaging.simp.config.MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
    // This method is used to register the "/wasl-websocket" endpoint, enabling SockJS fallback options so that alternate transports can be used if WebSocket is not available. The SockJS client will attempt to connect to "/wasl-websocket" and use the best available transport (websocket, xhr-streaming, xhr-polling, and so on).
    @Override
    public void registerStompEndpoints(org.springframework.web.socket.config.annotation.StompEndpointRegistry registry) {
        registry.addEndpoint("/wasl-websocket").withSockJS();
    }
}