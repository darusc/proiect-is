package com.example.proiectis;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GameWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private final Map<String, String> players = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("New connection: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        if (payload.startsWith("join:")) {
            String roomId = payload.split(":")[1];
            join(session, roomId);
        } else if (payload.startsWith("msg:")) {
            String roomId = players.get(session.getId());
            if (roomId != null) {
                broadcast(roomId, session, payload.split(":")[1]);
            }
        }
    }

    private void join(WebSocketSession session, String roomId) throws IOException {
        rooms.putIfAbsent(roomId, ConcurrentHashMap.newKeySet());
        Set<WebSocketSession> currentPlayers = rooms.get(roomId);

        if (currentPlayers.size() >= 2) {
            session.sendMessage(new TextMessage("error:full"));
            session.close();
            return;
        }

        currentPlayers.add(session);
        players.put(session.getId(), roomId);

        session.sendMessage(new TextMessage("success:joined"));

        if (currentPlayers.size() == 2) {
            for (WebSocketSession s : currentPlayers) {
                s.sendMessage(new TextMessage("game:start"));
            }
        }

        System.out.println("New player joined room: " + roomId);
    }

    private void broadcast(String roomId, WebSocketSession sender, String message) throws IOException {
        Set<WebSocketSession> currentPlayers = rooms.get(roomId);
        if (currentPlayers == null) {
            return;
        }

        for (WebSocketSession s : currentPlayers) {
            if (s.isOpen() && !s.getId().equals(sender.getId())) {
                s.sendMessage(new TextMessage("msg:" + message));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = players.get(session.getId());
        if (roomId != null) {
            Set<WebSocketSession> currentPlayers = rooms.get(roomId);
            if (currentPlayers != null) {
                currentPlayers.remove(session);

                for (WebSocketSession s : currentPlayers) {
                    s.sendMessage(new TextMessage("game:stop_opponent_left"));
                }

                if (currentPlayers.isEmpty()) {
                    rooms.remove(roomId);
                    System.out.println("Room " + roomId + " has been removed");
                }
            }
        }
    }
}
