package com.example.proiectis.game.logic;

import com.example.proiectis.game.model.State;
import com.example.proiectis.game.model.Board;
import org.springframework.data.util.Pair;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.example.proiectis.game.model.Board.Color.WHITE;
import static com.example.proiectis.game.model.Board.Color.BLACK;

public class VictoryChecker {

    private final State state;
    private final Board board;

    public VictoryChecker(Board board, State state) {
        this.state = state;
        this.board = board;
    }

    /**
     * Returneaza culoarea castigatorului si numarul de puncte castigate
     * daca exista o victorie
     */
    public Optional<Pair<Integer, Integer>> checkVictory() {
        Optional<Pair<Integer, Integer>> result =
                Arrays.stream(new int[]{WHITE, BLACK})
                        .mapToObj(color -> {
                            if (checkTripleVictory(color)) return Pair.of(color, 3);
                            if (checkDoubleVictory(color)) return Pair.of(color, 2);
                            if (checkSimpleVictory(color)) return Pair.of(color, 1);
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .findFirst();

        int winner = result.map(Pair::getFirst).orElse(-1);
        int points = result.map(Pair::getSecond).orElse(-1);

        if (winner != -1 && points != -1) {
            return Optional.of(Pair.of(winner, points));
        }
        
        return Optional.empty();
    }

    /**
     * Verifica victoria tripla.
     * Toate piesele scoase, oponentul nu a scos nicio piesa si are
     * una capturata sau in casa castigatorului
     */
    private boolean checkTripleVictory(int color) {
        int removed = color == WHITE ? state.whitesRemoved : state.blacksRemoved;
        int opponentRemoved = color == WHITE ? state.blacksRemoved : state.whitesRemoved;

        int taken = color == WHITE ? state.blacksTaken : state.whitesTaken;

        int opponentColor = color == WHITE ? BLACK : WHITE;

        int start = color == WHITE ? 0 : 19;
        int end = color == BLACK ? 6 : 24;

        boolean opponentInHome = IntStream.range(start, end)
                .anyMatch(i -> board.get(i)[0] == opponentColor && board.get(i)[1] > 0);

        return removed == 15 && opponentRemoved == 0 && (taken > 0 || opponentInHome);
    }

    /**
     * Verifica victoria dubla.
     * Toate piesele scoase, oponentul nu a scos nicio piesa
     */
    private boolean checkDoubleVictory(int color) {
        int removed = color == WHITE ? state.whitesRemoved : state.blacksRemoved;
        int opponentRemoved = color == WHITE ? state.blacksRemoved : state.whitesRemoved;

        return removed == 15 && opponentRemoved == 0;
    }

    /**
     * Victorie simpla, toate piesele scoase
     */
    private boolean checkSimpleVictory(int color) {
        return color == WHITE ? state.whitesRemoved == 15 : state.blacksRemoved == 15;
    }
}
