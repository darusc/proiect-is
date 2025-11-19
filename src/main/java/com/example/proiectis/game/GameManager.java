package com.example.proiectis.game;

import com.example.proiectis.websocket.Channel;
import com.example.proiectis.websocket.handler.CustomWebSocketHandler;
import com.example.proiectis.websocket.CustomWebSocketListener;
import com.example.proiectis.websocket.Client;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;

@Service
public class GameManager implements CustomWebSocketListener {

    private final CustomWebSocketHandler customWebSocketHandler;
    private final Map<Channel, Board> activeGames = new HashMap<>();

    public GameManager(CustomWebSocketHandler customWebSocketHandler) {
        this.customWebSocketHandler = customWebSocketHandler;
        this.customWebSocketHandler.addListener(this);
    }

    @Override
    public void onClientJoin(Client client) {
        try {
            customWebSocketHandler.broadcast(client.getChannel(), Message.playerJoined(client.getId()));

            // Cand un client nou se conecteaza, creeaza o noua sesiune de joc
            // asociata cu canalul corespunzator clientului daca aceasta nu exista
            activeGames.putIfAbsent(client.getChannel(), new Board());

            // Start the game if the channel is full
            if(client.getChannel().isFull()) {
                Board board = activeGames.get(client.getChannel());
                Object[] playerIds = client.getChannel()
                        .getClients()
                        .stream()
                        .map(Client::getId)
                        .toArray();
                customWebSocketHandler.broadcast(client.getChannel(), Message.gameStart((int)playerIds[0], (int)playerIds[1]));
                customWebSocketHandler.broadcast(client.getChannel(), Message.state(board.serialize()));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void onClientLeave(Client client) {
        try {
            customWebSocketHandler.broadcast(client.getChannel(), "Client left");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void onMessage(Client client, JsonNode message) {
        try {
            System.out.println(message);
            process(client.getChannel(), message);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void process(Channel channel, JsonNode message) throws Exception {
        if(!message.has("type") || !message.has("payload")) {
            return;
        }

        String type = message.get("type").asText();
        JsonNode payload = message.get("payload");

        Board board = activeGames.get(channel);

        switch(type) {
            case "roll_request":
                roll(board, channel);
                break;
            case "move":
                move(board, channel, payload.get("color").asInt(), payload.get("from").asInt(), payload.get("to").asInt());
                break;
            case "advance":
                advance(board, channel);
                break;
            case "reenter":
                reenter(board, channel, payload.get("color").asInt(), payload.get("position").asInt());
                break;
        }
    }

    private void roll(Board board, Channel channel) throws Exception {
        int[] dice = board.rollDice();
        customWebSocketHandler.broadcast(channel, Message.rollResult(board.getCurrentTurn(), dice));
    }

    private void reenter(Board board, Channel channel, int color, int position) throws Exception {
        if(!board.isValidReenter(color, position)) {
            customWebSocketHandler.broadcast(channel, Message.invalidReenter("Invalid reenter"));
            return;
        }

        board.reenter(color, position);
        customWebSocketHandler.broadcast(channel, Message.state(board.serialize()));
    }

    private void move(Board board, Channel channel, int color, int src, int dst) throws Exception {
        if(!board.isValidMove(color, src, dst)) {
            customWebSocketHandler.broadcast(channel, Message.invalidMove("Invalid move"));
            return;
        }

        if(board.getCurrentTurn() != color) {
            customWebSocketHandler.broadcast(channel, Message.invalidMove("Wrong turn"));
            return;
        }

        board.move(src, dst);
        customWebSocketHandler.broadcast(channel, Message.state(board.serialize()));
    }

    private void advance(Board board, Channel channel) throws Exception {
        board.advance();
        customWebSocketHandler.broadcast(channel, Message.state(board.serialize()));
    }
}
