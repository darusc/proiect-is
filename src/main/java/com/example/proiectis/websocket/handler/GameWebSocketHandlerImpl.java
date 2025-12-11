package com.example.proiectis.websocket.handler;

import com.example.proiectis.game.GameManager;
import com.example.proiectis.game.LobbyManager;
import com.example.proiectis.websocket.Channel;
import com.example.proiectis.websocket.Client;
import com.example.proiectis.websocket.exception.WsException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Lob;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class GameWebSocketHandlerImpl extends BaseWebSocketHandler {

    public GameWebSocketHandlerImpl(GameManager gameManager, LobbyManager lobbyManager) {
        super(gameManager, GameManager.MAX_ROOM_SIZE);
        gameManager.setBroadcaster(this);
    }
}
