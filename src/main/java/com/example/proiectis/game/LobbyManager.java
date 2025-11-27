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
            broadcaster.broadcast(client, Message.rooms(getAvailableRooms()));
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
                String password = payload.get("password").isNull() ? null : payload.get("password").asText();
                Room room = createRoom(client.getId(), client.getId().toString(), password);

                broadcaster.broadcast(client, Message.roomCreated(room.getId()));
                broadcaster.broadcast(client.getChannel(), Message.rooms(getAvailableRooms()));
                break;

            case REQUEST_JOIN_ROOM:
                String roomId = payload.get("roomId").asText();
                String password_ = payload.get("password").isNull() ? null : payload.get("password").asText();
                boolean joined = joinRoom(client.getId(), roomId, password_);

                broadcaster.broadcast(client, joined ? Message.joinSuccess(roomId) : Message.joinFailed(roomId));
                broadcaster.broadcast(client.getChannel(), Message.rooms(getAvailableRooms()));
                break;

        }
    }

    private Room createRoom(Long owner, String name, String password) {
        String newId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Room room = new Room(newId, name, password);
        rooms.add(room);
        room.join(owner, password);

        return room;
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

    private static class Message {

        public static Object rooms(Object rooms) {
            return Map.of(
                    "type", "rooms",
                    "payload", Map.of(
                            "rooms", rooms
                    )
            );
        }

        public static Object roomCreated(String roomId) {
            return Map.of(
                    "type", "room_created",
                    "payload", Map.of(
                            "roomId", roomId
                    )
            );
        }

        public static Object joinSuccess(String roomId) {
            return Map.of(
                    "type", "room_join_success",
                    "payload", Map.of(
                            "roomId", roomId
                    )
            );
        }

        public static Object joinFailed(String roomId) {
            return Map.of(
                    "type", "room_join_failed",
                    "payload", Map.of()
            );
        }
    }
}
