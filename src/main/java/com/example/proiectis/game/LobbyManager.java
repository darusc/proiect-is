package com.example.proiectis.game;

import com.example.proiectis.game.model.Room;
import com.example.proiectis.websocket.BaseWebSocketListener;
import com.example.proiectis.websocket.Broadcaster;
import com.example.proiectis.websocket.Client;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LobbyManager implements BaseWebSocketListener {

    public final static String REQUEST_CREATE_ROOM = "create_room";
    public final static String REQUEST_JOIN_ROOM = "join_room";

    public final static String RESPONSE_JOIN_FAILED = "join_failed";
    public final static String RESPONSE_JOIN_SUCCESS = "join_success";
    public final static String RESPONSE_ROOM_CREATED = "room_created";

    private final Set<Room> rooms = new HashSet<>();

    @Setter
    private Broadcaster broadcaster;

    @Override
    public void onClientJoin(Client client) {
        try {
            broadcaster.broadcast(client, Map.of(
                    "type", "rooms",
                    "rooms", getAvailableRooms()
            ));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void onClientLeave(Client client) {

    }

    @Override
    public void onMessage(Client client, JsonNode message) {
        try {
            System.out.println(message);
            process(client, message);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void removeRoom(String roomId) {
        rooms.removeIf(room -> room.getId().equals(roomId));
    }

    private void process(Client client, JsonNode message) throws Exception {
        if (!message.has("type") || !message.has("payload")) {
            return;
        }

        String type = message.get("type").asText();
        JsonNode payload = message.get("payload");

        switch (type) {
            case REQUEST_CREATE_ROOM:
                createRoom(client.getId(), client.getId().toString(), payload.get("password").asText());
                broadcaster.broadcast(client, Map.of("type", RESPONSE_ROOM_CREATED));
                broadcaster.broadcast(client.getChannel(), Map.of("type", "rooms", "rooms", getAvailableRooms()));
                break;

            case REQUEST_JOIN_ROOM:
                boolean joined = joinRoom(client.getId(), payload.get("roomId").asText(), payload.get("password").asText());
                broadcaster.broadcast(client, Map.of("type", joined ? RESPONSE_JOIN_SUCCESS : RESPONSE_JOIN_FAILED));
                broadcaster.broadcast(client.getChannel(), Map.of("type", "rooms", "rooms", getAvailableRooms()));
                break;

        }
    }

    private void createRoom(Long owner, String name, String password) {
        String newId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Room room = new Room(newId, name, password);
        rooms.add(room);
        room.join(owner, password);
    }

    private boolean joinRoom(Long player, String roomId, String password) {
        for (Room room : rooms) {
            if (room.getId().equals(roomId)) {
                return room.join(player, password);
            }
        }
        return false;
    }

    private Object getAvailableRooms() {
        return rooms.stream()
                .filter(Room::isWaiting)
                .map(Room::toJson)
                .toList();
    }

    public boolean isPlayerInRoom(String roomId, Long playerId) {
        if (rooms.isEmpty()) {
            return false;
        }
        return rooms.stream().anyMatch(r -> r.getId().equals(roomId) && r.hasPlayer(playerId));
    }
}
