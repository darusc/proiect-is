package com.example.proiectis.game.model;

import com.example.proiectis.game.Initializer;
import com.example.proiectis.game.exception.GameException;
import com.example.proiectis.game.logic.LegalMovesResolver;
import com.example.proiectis.game.logic.VictoryChecker;
import com.example.proiectis.game.logic.MoveExecutor;
import com.example.proiectis.game.logic.MoveValidator;
import org.antlr.v4.runtime.misc.Triple;

import java.util.*;

import static com.example.proiectis.game.model.Board.Color.BLACK;
import static com.example.proiectis.game.model.Board.Color.WHITE;

public class Game {

    public enum Status {
        WAITING,
        READY,
        IN_PROGRESS,
        ENDED
    }

    /**
     * Timpul maxim de joc pentru fiecare jucator
     */
    public static int MAX_TIME = 300;

    public interface EventListener {
        /**
         * @param winner Culoare jucatorului care a castigat
         * @param points Numarul de puncte castigate
         */
        void onGameEnd(int winner, int points);

        /**
         * @param currentTurn Jucatorul curent
         * @param whiteTime   Timpul ramas al jucatorului alb
         * @param blackTime   Timpul ramas al jucatorului negru
         */
        void onTimerUpdate(int currentTurn, long whiteTime, long blackTime);
    }

    public static class TimerData extends Triple<Integer, Long, Long> {
        public TimerData(Integer integer, Long aLong, Long aLong2) {
            super(integer, aLong, aLong2);
        }
    }

    private final Board board;
    private final State state;

    private final LegalMovesResolver movesResolver;
    private final MoveValidator moveValidator;
    private final MoveExecutor moveExecutor;

    private final VictoryChecker victoryChecker;

    private final Random random = new Random();

    private final EventListener eventListener;

    public Game(int initializerMode, EventListener eventListener) {
        this.eventListener = eventListener;

        this.board = new Board();
        this.state = new State();

        this.moveExecutor = new MoveExecutor(board, state);
        this.moveValidator = new MoveValidator(board, state);
        this.movesResolver = new LegalMovesResolver(board, state);
        this.victoryChecker = new VictoryChecker(board, state);

        Initializer.setupInitialState(initializerMode, board, state);
    }

    public Game(EventListener eventListener) {
        this(Initializer.NORMAL, eventListener);
    }

    public Map<String, Object> serialize() {
        var state = this.state.serialize();
        var board = this.board.serialize();

        Map<String, Object> serialized = new HashMap<>();
        serialized.putAll(state);
        serialized.putAll(board);

        return serialized;
    }

    public void start() {
        state.status = Status.IN_PROGRESS;
    }

    public void end(int winner, int points) {
        eventListener.onGameEnd(winner, points);
        state.status = Status.ENDED;
    }

    public void roll() {
        // Genereaza zarul random
        state.dice = new Integer[]{random.nextInt(6) + 1, random.nextInt(6) + 1};

        // Genereaza mutarile ramase in functie de zarul generate
        // Daca e dubla => 4 mutari, altfel 2 mutari
        if (state.dice[0].equals(state.dice[1])) {
            state.remainingMoves.addAll(List.of(new Integer[]{state.dice[0], state.dice[0], state.dice[0], state.dice[0]}));
        } else {
            state.remainingMoves.addAll(List.of(new Integer[]{state.dice[0], state.dice[1]}));
        }

        // Daca nu exista nicio mutare legala cu zarul generat treci la urmatoarea tura
        if (!movesResolver.hasAnyLegalMove()) {
            advance();
        }
    }

    public void handleMove(MoveRequest move) throws GameException {
        moveValidator.validate(move);
        moveExecutor.execute(move);

        // Actiunie specifice dupa fiecare tip de mutare
        switch (move.getType()) {
            case MOVE:
                checkIfAllPiecesInHome();
                break;
            case REENTRY:
                break;
            case REMOVE:
                victoryChecker.checkVictory().ifPresent(victory -> this.end(victory.getFirst(), victory.getSecond()));
                break;
        }

        // Daca s-au folosit toate mutarile sau nu mai exista nicio
        // mutare legala pentru jucatorul curent  la urmatoarea tura
        if (state.remainingMoves.isEmpty() || !movesResolver.hasAnyLegalMove()) {
            advance();
        }
    }

    public void tick() {
        if (state.status != Status.IN_PROGRESS) {
            return;
        }

        if (state.currentTurn == WHITE) {
            state.whiteTime--;
            if (state.whiteTime < 0) {
                state.whiteTime = 0;
                this.end(BLACK, 3);
            }
        } else {
            state.blackTime--;
            if (state.blackTime < 0) {
                state.blackTime = 0;
                this.end(WHITE, 3);
            }
        }
    }

    /**
     * Mergi la urmatoarea tura
     */
    private void advance() {
        int tmp = state.currentTurn;
        state.currentTurn = state.nextTurn;
        state.nextTurn = tmp;

        state.dice[0] = 0;
        state.dice[1] = 0;
        state.remainingMoves.clear();

        checkIfAllPiecesInHome();

        eventListener.onTimerUpdate(state.currentTurn, state.whiteTime, state.blackTime);
    }

    /**
     * Verifica daca toate piese sunt in casa si jucatorul poate incepe sa scoate piese
     */
    void checkIfAllPiecesInHome() {
        int count = 0;
        for (int i = 0; i < 6; i++) {
            if (board.get(i)[0] == WHITE) {
                count += board.get(i)[1];
            }
        }
        state.whiteCanRemove = (count + state.whitesRemoved == 15);

        count = 0;
        for (int i = 18; i < 24; i++) {
            if (board.get(i)[0] == BLACK) {
                count += board.get(i)[1];
            }
        }
        state.blackCanRemove = (count + state.blacksRemoved == 15);
    }

    public TimerData getTimerData() {
        return new TimerData(state.currentTurn, state.whiteTime, state.blackTime);
    }
}
