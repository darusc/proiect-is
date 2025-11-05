package com.example.proiectis.websocket;

import com.example.proiectis.websocket.exception.WsExceptionDecorator;
import com.example.proiectis.websocket.handler.CustomWebSocketHandlerImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    public static String PATH = "/ws/game";

    private final CustomWebSocketHandlerImpl gameWebSocketHandler;

    public WebSocketConfig(CustomWebSocketHandlerImpl gameWebSocketHandler) {
        this.gameWebSocketHandler = gameWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WsExceptionDecorator(gameWebSocketHandler), PATH).setAllowedOrigins("*");
    }
}
