package com.example.proiectis.game;

import com.example.proiectis.game.exception.GameException;
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

    private final Set<Room> rooms = new HashSet<>();

    @Setter
    private Broadcaster broadcaster;

    @Override
    public void onClientJoin(Client client) {
        try {
            broadcaster.broadcast(client, new LobbyResponse.Rooms(getAvailableRooms()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void onClientLeave(Client client) {

    }

    @Override
    public void onMessage(Client client, JsonNode message) {
        System.out.println(message);

        try {
            if (!message.has("type") || !message.has("payload")) {
                broadcaster.broadcast(client, Response.InvalidRequest("Missing type and/or payload"));
                return;
            }

            String type = message.get("type").asText();
            JsonNode payload = message.get("payload");

            switch (type) {
                case REQUEST_CREATE_ROOM:
                    String password = payload.get("password").isNull() ? null : payload.get("password").asText();
                    Room room = createRoom(client.getId(), client.getId().toString(), password);

                    broadcaster.broadcast(client, new LobbyResponse.RoomCreated(room.getId()));
                    broadcaster.broadcast(client.getChannel(), new LobbyResponse.Rooms(getAvailableRooms()));
                    break;

                case REQUEST_JOIN_ROOM:
                    String roomId = payload.get("roomId").asText();
                    String password_ = payload.get("password").isNull() ? null : payload.get("password").asText();
                    boolean joined = joinRoom(client.getId(), roomId, password_);

                    broadcaster.broadcast(client, joined ? new LobbyResponse.JoinSuccess(roomId) : new LobbyResponse.JoinFailed());
                    broadcaster.broadcast(client.getChannel(), new LobbyResponse.Rooms(getAvailableRooms()));
                    break;
            }
        } catch (GameException e) {
            // Trateaza exceptiile de joc. Transmite eroarea clientului care a initiat mesajul
            try {
                broadcaster.broadcast(client, e.serialize());
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void removeRoom(String roomId) {
        rooms.removeIf(room -> room.getId().equals(roomId));
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


    private static class LobbyResponse {

        public static class Rooms extends Response<Rooms.RoomsPayload> {
            public record RoomsPayload(Object rooms) { }
            public Rooms(Object rooms) {
                super("rooms", new RoomsPayload(rooms));
            }
        }

        public static class RoomCreated extends Response<RoomCreated.RoomCreatedPayload> {
            public record RoomCreatedPayload(String roomId) { }

            public RoomCreated(String roomId) {
                super("room_created", new RoomCreatedPayload(roomId));
            }
        }

        public static class JoinSuccess extends Response<JoinSuccess.JoinSuccessPayload> {
            public record JoinSuccessPayload(String roomId) { }

            public JoinSuccess(String roomId) {
                super("room_join_success", new JoinSuccessPayload(roomId));
            }
        }

        public static class JoinFailed extends Response<Object> {
            public JoinFailed() {
                super("room_join_failed", Map.of());
            }
        }
    }
}