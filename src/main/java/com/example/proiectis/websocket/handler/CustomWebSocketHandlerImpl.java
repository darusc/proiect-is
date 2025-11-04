package com.example.proiectis.websocket.handler;

import com.example.proiectis.websocket.Channel;
import com.example.proiectis.websocket.Client;
import com.example.proiectis.websocket.CustomWebSocketListener;
import com.example.proiectis.websocket.exception.WsException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class CustomWebSocketHandlerImpl extends TextWebSocketHandler implements CustomWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    private final Set<Client> activeClients = new HashSet<>();
    private final Set<Channel> activeChannels = new HashSet<>();

    private final Set<CustomWebSocketListener> listeners = new HashSet<>();

    @Override
    public void addListener(CustomWebSocketListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(CustomWebSocketListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void broadcast(Channel channel, Object json) throws Exception {
        if (channel == null) {
            return;
        }

        try {
            String payload = mapper.writeValueAsString(json);
            for (Client client : channel.getClients()) {
                client.getSession().sendMessage(new TextMessage(payload));
            }
        } catch (Exception e) {
            throw new WsException("INTERNAL_ERROR", e.getMessage());
        }
    }

    @Override
    public void broadcast(Channel channel, Client client, String json) throws Exception {
        if (channel == null) {
            return;
        }

        try {
            String payload = mapper.writeValueAsString(json);
            for (Client c : channel.getClients()) {
                if (c != client) {
                    c.getSession().sendMessage(new TextMessage(payload));
                }
            }
        } catch (Exception e) {
            throw new WsException("INTERNAL_ERROR", e.getMessage());
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        int playerId = getClientIdFromSession(session);
        System.out.println("Player " + playerId + " connected. Session id: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Payload-ul trimis este in format JSON
        JsonNode node = mapper.readTree(message.getPayload());
        String action = node.get("action").asText(null);

        if (action == null) {
            throw new WsException("MISSING_ACTION", "Missing action key");
        }

        if (action.equals(ACTION_JOIN)) {
            String roomId = node.get("roomId").asText(null);
            if (roomId == null) {
                throw new WsException("MISSING_ROOM_ID", "Missing room id");
            }

            join(session, roomId);
        } else {
            // Restul actiunilor netratate de handler sunt delegate
            // catre listenerele inregistrate
            Client client = getClient(session);
            if (client == null) {
                throw new WsException("NOT_IN_ROOM", "Join a room first");
            }

            listeners.forEach(l -> l.onMessage(client, node));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Client client = getClient(session);
        if (client == null) {
            return;
        }

        // Elimina sesiunea din canalul in care a fost adaugata
        Channel channel = client.getChannel();
        channel.removeClient(client);
        System.out.println("[Room " + client.getChannel().getId() + "]: Player " + client.getId() + " left");

        // Elimina canalul daca este gol (toti membri au fost deconectati)
        if (channel.getClients().isEmpty()) {
            System.out.println("[Room " + client.getChannel().getId() + "]: Room removed");
            activeChannels.remove(channel);
        }

        listeners.forEach(l -> l.onClientLeave(client));
    }

    private void join(WebSocketSession session, String roomId) throws Exception {
        // Cauta canalul cu id dat in care se doreste sa se faca join.
        // Daca nu exista, creeaza unul nou si adauga-l in lista de canale active
        Channel channel = getChannel(roomId);
        if (channel == null) {
            channel = new Channel(roomId);
            activeChannels.add(channel);
        }

        int clientId = getClientIdFromSession(session);

        if (channel.isFull()) {
            System.out.println("[Room " + roomId + "]: Already full");
            throw new WsException("ROOM_FULL", "Room " + roomId + " is full");
        }

        Client client = new Client(clientId, channel, session);
        channel.addClient(client);
        activeClients.add(client);

        listeners.forEach(l -> l.onClientJoin(client));

        System.out.println("[Room " + roomId + "]: Player " + clientId + " joined");
    }

    /**
     * Returneaza clientId din lista de query params.
     * Daca nu exista arunca WsException
     */
    private int getClientIdFromSession(WebSocketSession session) throws WsException {
        URI uri = session.getUri();
        if (uri.getQuery() == null) {
            throw new WsException("MISSING_PLAYER_ID", "Player id is required");
        }

        String query = uri.getQuery();
        return Integer.parseInt(Arrays
                .stream(query.split("&"))
                .map(k -> k.split("="))
                .filter(k -> k.length == 2 && k[0].equals("clientId"))
                .map(k -> k[1])
                .findFirst()
                .orElseThrow(() -> new WsException("MISSING_PLAYER_ID", "Player id is required")));
    }

    /**
     * Returneaza clientul asociat cu WebSocketSession-ul dat.
     * Daca nu exista returneaza null
     */
    private Client getClient(WebSocketSession session) {
        return activeClients.stream()
                .filter(sc -> sc.getSession() == session)
                .findFirst()
                .orElse(null);
    }

    /**
     * Returneaza canalul cu id-ul dat.
     * Daca nu exista returneaza null
     */
    private Channel getChannel(String id) {
        return activeChannels.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
