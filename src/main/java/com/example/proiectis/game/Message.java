package com.example.proiectis.game;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public class Message {

    public static Object playerJoined(int playerId) {
        return Map.of(
                "type", "player_joined",
                "payload", Map.of(
                        "player", playerId
                )
        );
    }

    public static Object gameStart(int whitePlayer, int blackPlayer) {
        return Map.of(
                "type", "game_start",
                "payload", Map.of(
                        "white", whitePlayer,
                        "black", blackPlayer
                )
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

    public static Object state(Object boardState) {
        return Map.of(
                "type", "state",
                "payload", boardState
        );
    }

    public static Object rollResult(int color, int[] dice) {
        return Map.of(
                "type", "roll_result",
                "payload", Map.of(
                        "color", color,
                        "dice", dice
                )
        );
    }

    public static Object turn(byte color) {
        return Map.of(
                "type", "turn",
                "payload", Map.of(
                        "color", color
                )
        );
    }
}
