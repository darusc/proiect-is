package com.example.proiectis.game;

import com.example.proiectis.game.exception.GameException;
import com.example.proiectis.game.model.Board;
import com.example.proiectis.game.model.Game;
import com.example.proiectis.game.model.MoveRequest;
import com.example.proiectis.websocket.BaseWebSocketListener;
import com.example.proiectis.websocket.Broadcaster;
import com.example.proiectis.websocket.Channel;
import com.example.proiectis.websocket.Client;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Setter;
import org.antlr.v4.runtime.misc.Triple;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameManager implements BaseWebSocketListener {

    @Setter
    private Broadcaster broadcaster;

    private final LobbyManager lobbyManager;
    private final Map<Channel, Game> activeGames = new HashMap<>();

    public final static String REQUEST_ROLL = "roll_request";
    public final static String REQUEST_MOVE = "move";
    public final static String REQUEST_REENTER = "reenter";
    public final static String REQUEST_REMOVE = "remove";

    public final static int MAX_ROOM_SIZE = 2;

    public GameManager(LobbyManager lobbyManager, Timer timer) {
        this.lobbyManager = lobbyManager;
        timer.subscribe(() -> {
            for (Game game : activeGames.values()) {
                game.tick();
            }
        });
    }

    @Override
    public void onClientJoin(Client client) {
        broadcaster.broadcast(client.getChannel(), new GameResponse.PlayerJoined(client.getId()));

        // Cand un client nou se conecteaza, creeaza o noua sesiune de joc
        // asociata cu canalul corespunzator clientului daca aceasta nu exista
        activeGames.putIfAbsent(client.getChannel(), new Game(new Game.EventListener() {
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
                                "points", winner == Board.Color.WHITE ? points : 0,
                                "username", "...",                       ///  provizoriu
                                "total", 0                                      ///  provizoriu
                        ),
                        "black", Map.of(
                                "points", winner == Board.Color.BLACK ? points : 0,
                                "username", "...",                       ///  provizoriu
                                "total", 0                                      ///  provizoriu
                        )
                );

                try {
                    broadcaster.broadcast(client.getChannel(), new GameResponse.GameEnd(data));
                    activeGames.remove(client.getChannel());
                    lobbyManager.removeRoom(client.getChannel().getId());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }

            @Override
            public void onTimerUpdate(int currentTurn, long whiteTime, long blackTime) {
                try {
                    broadcaster.broadcast(client.getChannel(), new GameResponse.Timer(currentTurn, whiteTime, blackTime));
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }));

        // Incepe jocul daca ambii jucatori sau conectat
        if (client.getChannel().isFull(MAX_ROOM_SIZE)) {
            Game game = activeGames.get(client.getChannel());
            long[] playerIds = getPlayerIdsFromChannel(client.getChannel());
            // Primul player care a dat join va fi jucatorul alb
            broadcaster.broadcast(client.getChannel(), new GameResponse.GameStart(playerIds[0], playerIds[1]));
            // Sincronizeaza timpul (in special la reconectare)
            broadcaster.broadcast(client.getChannel(), new GameResponse.Timer(game.getTimerData()));
            broadcaster.broadcast(client.getChannel(), new GameResponse.State(game.serialize()));
            game.start();
        }
    }

    @Override
    public void onClientLeave(Client client) {
        broadcaster.broadcast(client.getChannel(), new GameResponse.PlayerLeft(client.getId()));
        // Opreste jocul (sterge) daca ambii jucatori s-au deconectat
        if(client.getChannel().isEmpty()) {
            activeGames.remove(client.getChannel());
            lobbyManager.removeRoom(client.getChannel().getId());
        }
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

            Game game = activeGames.get(client.getChannel());
            MoveRequest moveRequest = null;

            switch (type) {
                case REQUEST_ROLL -> game.roll();
                case REQUEST_MOVE -> moveRequest = new MoveRequest(
                        payload.get("color").asInt(),
                        MoveRequest.Type.MOVE,
                        payload.get("from").asInt(),
                        payload.get("to").asInt()
                );
                case REQUEST_REENTER -> moveRequest = new MoveRequest(
                        payload.get("color").asInt(),
                        MoveRequest.Type.REENTRY,
                        0,
                        payload.get("position").asInt()
                );
                case REQUEST_REMOVE -> moveRequest = new MoveRequest(
                        payload.get("color").asInt(),
                        MoveRequest.Type.REMOVE,
                        payload.get("position").asInt(),
                        0
                );
            }

            if (moveRequest != null) {
                game.handleMove(moveRequest);
            }

            broadcaster.broadcast(client.getChannel(), new GameResponse.State(game.serialize()));

        } catch (GameException e) {
            // Trateaza exceptiile de joc. Transmite eroarea clientului care a initiat mesajul
            broadcaster.broadcast(client, e.serialize());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private long[] getPlayerIdsFromChannel(Channel channel) {
        return channel.getClients()
                .stream()
                .mapToLong(Client::getId)
                .toArray();
    }


    private static class GameResponse {

        public static class PlayerJoined extends Response<PlayerJoined.PlayerPayload> {
            public record PlayerPayload(Long player) { }

            public PlayerJoined(Long playerId) {
                super("player_joined", new PlayerPayload(playerId));
            }
        }

        public static class PlayerLeft extends Response<PlayerLeft.PlayerPayload> {
            public record PlayerPayload(Long player) { }

            public PlayerLeft(Long playerId) {
                super("player_left", new PlayerPayload(playerId));
            }
        }

        public static class GameStart extends Response<GameStart.GameStartPayload> {
            public record GameStartPayload(Long white, Long black, int startTime) { }

            public GameStart(Long whitePlayer, Long blackPlayer) {
                super("game_start", new GameStartPayload(whitePlayer, blackPlayer, Game.MAX_TIME));
            }
        }

        public static class GameEnd extends Response<Object> {
            public GameEnd(Object payload) {
                super("game_end", payload);
            }
        }

        public static class Timer extends Response<Timer.TimerPayload> {
            public record TimerPayload(int turn, long whiteTime, long blackTime) { }

            public Timer(int turn, long whiteTime, long blackTime) {
                super("timer", new TimerPayload(turn, whiteTime, blackTime));
            }

            public Timer(Game.TimerData data) {
                super("timer", new TimerPayload(data.a, data.b, data.c));
            }
        }

        public static class State extends Response<Object> {
            public State(Object boardState) {
                super("state", boardState);
            }
        }
    }
}
