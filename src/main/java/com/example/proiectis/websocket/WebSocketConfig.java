package com.example.proiectis.websocket;

import com.example.proiectis.websocket.exception.WsExceptionDecorator;
import com.example.proiectis.websocket.handler.GameWebSocketHandlerImpl;
import com.example.proiectis.websocket.handler.LobbyWebSocketHandlerImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    public static String PATH_GAME = "/ws/game";
    public static String PATH_LOBBY = "/ws/lobby";

    private final GameWebSocketHandlerImpl gameWebSocketHandler;
    private final LobbyWebSocketHandlerImpl lobbyWebSocketHandler;

    public WebSocketConfig(
            GameWebSocketHandlerImpl gameWebSocketHandler,
            LobbyWebSocketHandlerImpl lobbyWebSocketHandler
    ) {
        this.gameWebSocketHandler = gameWebSocketHandler;
        this.lobbyWebSocketHandler = lobbyWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WsExceptionDecorator(gameWebSocketHandler), PATH_GAME).setAllowedOrigins("*");
        registry.addHandler(new WsExceptionDecorator(lobbyWebSocketHandler), PATH_LOBBY).setAllowedOrigins("*");
    }
}
