package com.example.proiectis.websocket.handler;

import com.example.proiectis.game.LobbyManager;
import com.example.proiectis.websocket.Client;
import org.springframework.stereotype.Component;

@Component
public class LobbyWebSocketHandlerImpl extends BaseWebSocketHandler {

    public LobbyWebSocketHandlerImpl(LobbyManager lobbyManager) {
        super(lobbyManager);
        lobbyManager.setBroadcaster(this);
    }
}
