package com.example.proiectis.game.logic;

import com.example.proiectis.game.model.State;
import com.example.proiectis.game.model.Board;
import com.example.proiectis.game.model.MoveRequest;

import static com.example.proiectis.game.model.Board.Color.*;

public class MoveExecutor {

    private final State state;
    private final Board board;

    public MoveExecutor(Board board, State state) {
        this.state = state;
        this.board = board;
    }

    public void execute(MoveRequest moveRequest) {
        int from = moveRequest.getSrc();
        int to = moveRequest.getDst();
        int color = moveRequest.getColor();

        switch (moveRequest.getType()) {
            case MOVE:
                executeMove(from, to);
                break;
            case REENTRY:
                executeReentry(color, to);
                break;
            case REMOVE:
                executeRemove(color, from);
                break;
        }
    }

    /**
     * Muta o piesa de pe pozitia src pe pozitia dst. Daca in urma mutarii
     * o piesa este capturata se returneaza WHITE sau BLACK, altfel se returneaza NONE.
     * Se presupune ca mutarea e valida
     */
    private void executeMove(int src, int dst) {
        if (board.get(dst)[0] == board.get(src)[0]) {
            // Daca pe locul unde mutam este o piesa de aceeasi culoare
            board.get(dst)[1]++;
        } else {
            // Daca pe locul unde mutam este o piese deja o piesa, aceasta e capturata
            if (board.get(dst)[0] == WHITE) {
                state.whitesTaken++;
            } else if (board.get(dst)[0] == BLACK) {
                state.blacksTaken++;
            }

            board.get(dst)[0] = board.get(src)[0];
            board.get(dst)[1] = 1;
        }

        board.get(src)[1]--;
        if (board.get(src)[1] == 0) {
            board.get(src)[0] = NONE;
        }

        consumeDie(Math.abs(dst - src));
    }

    /**
     * Repune o piesa capturata inapoi in joc
     */
    private void executeReentry(int color, int position) {
        if (board.get(position)[0] == NONE) {
            board.get(position)[0] = color;
            board.get(position)[1] = 1;
        } else if (board.get(position)[0] == color) {
            board.get(position)[1]++;
        } else {
            if (board.get(position)[0] == BLACK) {
                state.blacksTaken++;
            } else {
                state.whitesTaken++;
            }

            board.get(position)[0] = color;
            board.get(position)[1] = 1;
        }

        if (color == WHITE) {
            state.whitesTaken--;
        } else {
            state.blacksTaken--;
        }

        int requiredMove = (color == WHITE) ? 24 - position : position + 1;
        consumeDie(requiredMove);
    }

    /**
     * Scoate o piese de pe tabla de joc
     */
    private void executeRemove(int color, int position) {
        board.get(position)[1]--;
        if (board.get(position)[1] == 0) {
            board.get(position)[0] = NONE;
        }

        if (color == WHITE) {
            state.whitesRemoved++;
        } else {
            state.blacksRemoved++;
        }

        int requiredMove = (color == WHITE) ? position + 1 : 24 - position;
        int moveToUse = state.remainingMoves.stream().filter(m -> m >= requiredMove).min(Integer::compareTo).orElse(requiredMove);
        consumeDie(moveToUse);
    }

    /**
     * Foloseste una din mutarile remase.
     */
    private void consumeDie(int value) {
        state.remainingMoves.remove(Integer.valueOf(value));
    }
}
