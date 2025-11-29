package com.example.proiectis.game;

import com.example.proiectis.game.model.Board;
import com.example.proiectis.websocket.BaseWebSocketListener;
import com.example.proiectis.websocket.Broadcaster;
import com.example.proiectis.websocket.Channel;
import com.example.proiectis.websocket.Client;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameManager implements BaseWebSocketListener {

    @Setter
    private Broadcaster broadcaster;

    private final LobbyManager lobbyManager;
    private final Map<Channel, Board> activeGames = new HashMap<>();

    public final static String REQUEST_ROLL = "roll_request";
    public final static String REQUEST_MOVE = "move";
    public final static String REQUEST_REENTER = "reenter";
    public final static String REQUEST_REMOVE = "remove";

    public final static int MAX_ROOM_SIZE = 2;

    public GameManager(LobbyManager lobbyManager, Timer timer) {
        this.lobbyManager = lobbyManager;
        timer.subscribe(() -> {
            for (Board board : activeGames.values()) {
                board.tick();
            }
        });
    }

    @Override
    public void onClientJoin(Client client) {
        try {
            broadcaster.broadcast(client.getChannel(), Message.playerJoined(client.getId()));

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
                        broadcaster.broadcast(client.getChannel(), Message.gameEnd(data));
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }

                @Override
                public void onTimerUpdate(int currentTurn, long whiteTime, long blackTime) {
                    try {
                        broadcaster.broadcast(client.getChannel(), Message.timer(currentTurn, whiteTime, blackTime));
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }));

            // Incepe jocul daca ambii jucatori sau conectat
            if (client.getChannel().isFull(MAX_ROOM_SIZE)) {
                Board board = activeGames.get(client.getChannel());
                long[] playerIds = getPlayerIdsFromChannel(client.getChannel());
                // Primul player care a dat join va fi jucatorul alb
                broadcaster.broadcast(client.getChannel(), Message.gameStart(playerIds[0], playerIds[1]));
                broadcaster.broadcast(client.getChannel(), Message.timer(board.getCurrentTurn(), board.getWhiteTime(), board.getBlackTime()));
                broadcaster.broadcast(client.getChannel(), Message.state(board.serialize()));
                board.start();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void onClientLeave(Client client) {
        try {
            broadcaster.broadcast(client.getChannel(), Message.playerLeft(client.getId()));
            activeGames.remove(client.getChannel());
            lobbyManager.removeRoom(client.getChannel().getId());
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

    private long[] getPlayerIdsFromChannel(Channel channel) {
        return channel.getClients()
                .stream()
                .mapToLong(Client::getId)
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
        broadcaster.broadcast(channel, Message.state(board.serialize()));
    }

    private void reenter(Board board, Channel channel, int color, int position) throws Exception {
        if (!board.isValidReenter(color, position)) {
            broadcaster.broadcast(channel, Message.invalidReenter("Invalid reenter"));
            return;
        }

        board.reenter(color, position);
        broadcaster.broadcast(channel, Message.state(board.serialize()));
    }

    private void move(Board board, Channel channel, int color, int src, int dst) throws Exception {
        if (!board.isValidMove(color, src, dst)) {
            broadcaster.broadcast(channel, Message.invalidMove("Invalid move"));
            return;
        }

        board.move(src, dst);
        broadcaster.broadcast(channel, Message.state(board.serialize()));
    }

    private void remove(Board board, Channel channel, int color, int position) throws Exception {
        if(!board.isValidRemove(color, position)) {
            broadcaster.broadcast(channel, Message.invalidRemove("Invalid remove"));
            return;
        }

        board.remove(color, position);
        broadcaster.broadcast(channel, Message.state(board.serialize()));
    }

    private static class Message {
        public static Object playerJoined(Long playerId) {
            return Map.of(
                    "type", "player_joined",
                    "payload", Map.of(
                            "player", playerId
                    )
            );
        }

        public static Object playerLeft(Long playerId) {
            return Map.of(
                    "type", "player_left",
                    "payload", Map.of(
                            "player", playerId
                    )
            );
        }

        public static Object gameStart(Long whitePlayer, Long blackPlayer) {
            return Map.of(
                    "type", "game_start",
                    "payload", Map.of(
                            "white", whitePlayer,
                            "black", blackPlayer
                    )
            );
        }

        public static Object gameEnd(Object payload) {
            return Map.of(
                    "type", "game_end",
                    "payload", payload
            );
        }

        public static Object invalidMove(String reason) {
            return Map.of(
                    "type", "invalid_move",
                    "payload", Map.of(
                            "reason", reason
                    )
            );
        }

        public static Object invalidReenter(String reason) {
            return Map.of(
                    "type", "invalid_reenter",
                    "payload", Map.of(
                            "reason", reason
                    )
            );
        }

        public static Object invalidRemove(String reason) {
            return Map.of(
                    "type", "invalid_remove",
                    "payload", Map.of(
                            "reason", reason
                    )
            );
        }

        public static Object timer(int turn, long whiteTime, long blackTime) {
            return Map.of(
                    "type", "timer",
                    "payload", Map.of(
                            "turn", turn,
                            "whiteTime", whiteTime,
                            "blackTime", blackTime
                    )
            );
        }

        public static Object state(Object boardState) {
            return Map.of(
                    "type", "state",
                    "payload", boardState
            );
        }
    }
}
