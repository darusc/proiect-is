package com.example.proiectis.game;

import com.example.proiectis.dto.MatchDTO;
import com.example.proiectis.dto.PlayerDTO;
import com.example.proiectis.dto.RankingDTO;
import com.example.proiectis.game.exception.GameException;
import com.example.proiectis.game.model.Board;
import com.example.proiectis.game.model.Game;
import com.example.proiectis.game.model.MoveRequest;
import com.example.proiectis.service.MatchService;
import com.example.proiectis.service.PlayerService;
import com.example.proiectis.service.RankingService;
import com.example.proiectis.websocket.BaseWebSocketListener;
import com.example.proiectis.websocket.Broadcaster;
import com.example.proiectis.websocket.Channel;
import com.example.proiectis.websocket.Client;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GameManager implements BaseWebSocketListener {

    @Setter
    private Broadcaster broadcaster;

    private final LobbyManager lobbyManager;
    // Dependențe noi adăugate
    private final MatchService matchService;
    private final RankingService rankingService;
    private final PlayerService playerService;

    private final Map<Channel, Game> activeGames = new HashMap<>();

    public final static String REQUEST_ROLL = "roll_request";
    public final static String REQUEST_MOVE = "move";
    public final static String REQUEST_REENTER = "reenter";
    public final static String REQUEST_REMOVE = "remove";

    public final static int MAX_ROOM_SIZE = 2;

    // Constructor actualizat pentru a include serviciile
    public GameManager(LobbyManager lobbyManager,
                       Timer timer,
                       MatchService matchService,
                       RankingService rankingService,
                       PlayerService playerService) {
        this.lobbyManager = lobbyManager;
        this.matchService = matchService;
        this.rankingService = rankingService;
        this.playerService = playerService;

        timer.subscribe(() -> {
            for (Game game : activeGames.values()) {
                game.tick();
            }
        });
    }

    @Override
    public void onClientJoin(Client client) {
        broadcaster.broadcast(client.getChannel(), new GameResponse.PlayerJoined(client.getId()));

        activeGames.putIfAbsent(client.getChannel(), new Game(new Game.EventListener() {
            @Override
            public void onGameEnd(int winner, int points) {
                // 1. Identificăm ID-urile jucătorilor
                // playerIds[] -> jucatorul alb, playerIds[1] -> jucatorul negru
                long[] playerIds = getPlayerIdsFromChannel(client.getChannel());
                if (playerIds.length < 2) return; // Safety check

                Long whiteId = playerIds[0];
                Long blackId = playerIds[1];

                Long winnerId = (winner == Board.Color.WHITE) ? whiteId : blackId;
                Long loserId = (winner == Board.Color.WHITE) ? blackId : whiteId;

                int scoreWhite = (winner == Board.Color.WHITE) ? points : 0;
                int scoreBlack = (winner == Board.Color.BLACK) ? points : 0;

                try {
                    // 2. Salvăm meciul în DB
                    matchService.recordMatch(MatchDTO.builder()
                            .player1Id(whiteId)
                            .player2Id(blackId)
                            .winnerId(winnerId)
                            .scorePlayer1(scoreWhite)
                            .scorePlayer2(scoreBlack)
                            .build());

                    // 3. Actualizăm clasamentul (+3 puncte câștigător, update rate, etc)
                    rankingService.updateRankingAfterMatch(winnerId, loserId);

                    // 4. Obținem datele actualizate pentru a le trimite clienților
                    PlayerDTO whitePlayer = playerService.getPlayer(whiteId);
                    PlayerDTO blackPlayer = playerService.getPlayer(blackId);

                    // Putem folosi getRanking deoarece updateRankingAfterMatch asigură crearea dacă nu există
                    RankingDTO whiteRank = rankingService.getRanking(whiteId);
                    RankingDTO blackRank = rankingService.getRanking(blackId);

                    Map<String, Object> data = Map.of(
                            "winner", winner,
                            "white", Map.of(
                                    "points", scoreWhite,
                                    "username", whitePlayer.getUsername(),
                                    "total", whiteRank.getTotalPoints()
                            ),
                            "black", Map.of(
                                    "points", scoreBlack,
                                    "username", blackPlayer.getUsername(),
                                    "total", blackRank.getTotalPoints()
                            )
                    );

                    broadcaster.broadcast(client.getChannel(), new GameResponse.GameEnd(data));
                    activeGames.remove(client.getChannel());
                    lobbyManager.removeRoom(client.getChannel().getId());

                } catch (Exception e) {
                    System.err.println("Error saving game results: " + e.getMessage());
                    e.printStackTrace();
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

        if (client.getChannel().isFull(MAX_ROOM_SIZE)) {
            Game game = activeGames.get(client.getChannel());
            long[] playerIds = getPlayerIdsFromChannel(client.getChannel());

            broadcaster.broadcast(client.getChannel(), new GameResponse.GameStart(playerIds[0], playerIds[1]));
            broadcaster.broadcast(client.getChannel(), new GameResponse.Timer(game.getTimerData()));
            broadcaster.broadcast(client.getChannel(), new GameResponse.State(game.serialize()));
            game.start();
        }
    }

    @Override
    public void onClientLeave(Client client) {
        broadcaster.broadcast(client.getChannel(), new GameResponse.PlayerLeft(client.getId()));
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
            if (game == null) return;

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

    // Clasele interne GameResponse rămân neschimbate
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