package com.example.proiectis.game;

import com.example.proiectis.websocket.handler.CustomWebSocketHandler;
import com.example.proiectis.websocket.CustomWebSocketListener;
import com.example.proiectis.websocket.Client;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class GameManager implements CustomWebSocketListener {

    private final CustomWebSocketHandler customWebSocketHandler;

    public GameManager(CustomWebSocketHandler customWebSocketHandler) {
        this.customWebSocketHandler = customWebSocketHandler;
        this.customWebSocketHandler.addListener(this);
    }

    @Override
    public void onClientJoin(Client client) {
        try {
            customWebSocketHandler.broadcast(client.getChannel(), "Client joined");
        } catch (Exception e) {

        }
    }

    @Override
    public void onClientLeave(Client client) {
        try {
            customWebSocketHandler.broadcast(client.getChannel(), "Client left");
        } catch (Exception e) {

        }
    }

    @Override
    public void onMessage(Client client, JsonNode message) {
        try {
            customWebSocketHandler.broadcast(client.getChannel(), message);
        } catch (Exception e) {

        }
    }

}
