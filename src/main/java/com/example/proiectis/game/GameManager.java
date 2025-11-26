package com.example.proiectis.game;

import com.example.proiectis.websocket.Channel;
import com.example.proiectis.websocket.handler.CustomWebSocketHandler;
import com.example.proiectis.websocket.CustomWebSocketListener;
import com.example.proiectis.websocket.Client;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameManager implements CustomWebSocketListener {

    private final CustomWebSocketHandler customWebSocketHandler;
    private final Map<Channel, Board> activeGames = new HashMap<>();

    public final static String REQUEST_ROLL = "roll_request";
    public final static String REQUEST_MOVE = "move";
    public final static String REQUEST_REENTER = "reenter";
    public final static String REQUEST_REMOVE = "remove";

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
            activeGames.putIfAbsent(client.getChannel(), new Board(new Board.GameListener() {
                @Override
                public void onGameEnd(int winner, int points) {
                    /// TO DO salveaza meciul in baza de date si actualizeaza
                    /// clasamentul si scorul total dintre cei doi jucator
                    /// Returneaza informatii suplimentare (ex. username)
                    ///
                    /// playerIds[0] -> jucatorul alb, playerIds[1] -> jucatorul negru
                    /// int[] playerIds = getPlayerIdsFromChannel(client.getChannel());

                    Map<String, Object> data = Map.of(
                            "winner", winner,
                            "white", Map.of(
                                    "points", winner == Board.WHITE ? points : 0,
                                    "username", "...",                       ///  provizoriu
                                    "total", 0                                      ///  provizoriu
                            ),
                            "black", Map.of(
                                    "points", winner == Board.BLACK ? points : 0,
                                    "username", "...",                       ///  provizoriu
                                    "total", 0                                      ///  provizoriu
                            )
                    );

                    try {
                        customWebSocketHandler.broadcast(client.getChannel(), Message.gameEnd(data));
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }));

            // Incepe jocul daca ambii jucatori sau conectat
            if (client.getChannel().isFull()) {
                Board board = activeGames.get(client.getChannel());
                int[] playerIds = getPlayerIdsFromChannel(client.getChannel());
                // Primul player care a dat join va fi jucatorul alb
                customWebSocketHandler.broadcast(client.getChannel(), Message.gameStart(playerIds[0], playerIds[1]));
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

    private int[] getPlayerIdsFromChannel(Channel channel) {
        return channel.getClients()
                .stream()
                .mapToInt(Client::getId)
                .toArray();
    }

    private void process(Channel channel, JsonNode message) throws Exception {
        if (!message.has("type") || !message.has("payload")) {
            return;
        }

        String type = message.get("type").asText();
        JsonNode payload = message.get("payload");

        Board board = activeGames.get(channel);

        switch (type) {
            case REQUEST_ROLL:
                roll(board, channel);
                break;

            case REQUEST_MOVE:
                move(board, channel, payload.get("color").asInt(), payload.get("from").asInt(), payload.get("to").asInt());
                break;

            case REQUEST_REENTER:
                reenter(board, channel, payload.get("color").asInt(), payload.get("position").asInt());
                break;

            case REQUEST_REMOVE:
                remove(board, channel, payload.get("color").asInt(), payload.get("position").asInt());
                break;
        }
    }

    private void roll(Board board, Channel channel) throws Exception {
        board.rollDice();
        customWebSocketHandler.broadcast(channel, Message.state(board.serialize()));
    }

    private void reenter(Board board, Channel channel, int color, int position) throws Exception {
        if (!board.isValidReenter(color, position)) {
            customWebSocketHandler.broadcast(channel, Message.invalidReenter("Invalid reenter"));
            return;
        }

        board.reenter(color, position);
        customWebSocketHandler.broadcast(channel, Message.state(board.serialize()));
    }

    private void move(Board board, Channel channel, int color, int src, int dst) throws Exception {
        if (!board.isValidMove(color, src, dst)) {
            customWebSocketHandler.broadcast(channel, Message.invalidMove("Invalid move"));
            return;
        }

        board.move(src, dst);
        customWebSocketHandler.broadcast(channel, Message.state(board.serialize()));
    }

    private void remove(Board board, Channel channel, int color, int position) throws Exception {
        if(!board.isValidRemove(color, position)) {
            customWebSocketHandler.broadcast(channel, Message.invalidRemove("Invalid remove"));
            return;
        }

        board.remove(color, position);
        customWebSocketHandler.broadcast(channel, Message.state(board.serialize()));
    }
}
