package com.example.proiectis.websocket;

import com.fasterxml.jackson.databind.JsonNode;

public interface CustomWebSocketListener {

    void onClientJoin(Client client);

    void onClientLeave(Client client);

    void onMessage(Client client, JsonNode message);
}
