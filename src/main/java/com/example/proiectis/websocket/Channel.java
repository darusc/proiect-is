package com.example.proiectis.websocket;

import com.example.proiectis.websocket.handler.CustomWebSocketHandlerImpl;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class Channel {

    private final String id;
    private final Set<Client> clients = new HashSet<>();

    public Channel(String id) {
        this.id = id;
    }

    public void addClient(Client session) {
        clients.add(session);
    }

    public void removeClient(Client session) {
        clients.remove(session);
    }

    public boolean isFull() {
        return clients.size() >= CustomWebSocketHandlerImpl.MAX_ROOM_SIZE;
    }
}
