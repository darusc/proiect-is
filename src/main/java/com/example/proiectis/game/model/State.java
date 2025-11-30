package com.example.proiectis.game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.proiectis.game.model.Board.Color.WHITE;

public class State {

    public Game.Status status = Game.Status.WAITING;

    // Piesele capturate de adversar
    public int whitesTaken;
    public int blacksTaken;

    // Piesele scoasa de jucator
    public int whitesRemoved;
    public int blacksRemoved;

    // Daca toate piesele sunt in casa
    public boolean whiteCanRemove;
    public boolean blackCanRemove;

    // Timpul ramas pentru fiecaer jucator
    public long whiteTime;
    public long blackTime;

    public int currentTurn;
    public int nextTurn;

    public Integer[] dice = new Integer[]{0, 0};
    public final List<Integer> remainingMoves = new ArrayList<>();

    public Map<String, Object> serialize() {
        return Map.of(
                "turn", currentTurn,
                "roll", dice,
                "whitesTaken", whitesTaken,
                "blacksTaken", blacksTaken,
                "whitesRemoved", whitesRemoved,
                "blacksRemoved", blacksRemoved,
                "canRemove", (currentTurn == WHITE ? whiteCanRemove : blackCanRemove),
                "remainingMoves", remainingMoves.toArray(new Integer[0])
        );
    }
}
