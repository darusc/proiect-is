package com.example.proiectis.game.logic;

import com.example.proiectis.game.model.State;
import com.example.proiectis.game.model.Board;

import static com.example.proiectis.game.model.Board.Color.WHITE;
import static com.example.proiectis.game.model.Board.Color.BLACK;

public class LegalMovesResolver {

    private final Board board;
    private final State state;

    public LegalMovesResolver(Board board, State state) {
        this.board = board;
        this.state = state;
    }

    public boolean hasAnyLegalMove() {
        return canMove(state.currentTurn) || canReenter(state.currentTurn) || canRemove(state.currentTurn);
    }

    private boolean canReenter(int color) {
        int taken = (color == WHITE ? state.whitesTaken : state.blacksTaken);
        if (taken == 0) {
            return false;
        }

        for (int die : state.remainingMoves) {
            int dest = (color == WHITE) ? 24 - die : die - 1;

            if (dest < 0 || dest > 23) {
                continue;
            }

            // O piese se poate repune daca pozitia de destinatie
            // e libera, exista o singura piesa (de alta culoare) sau e aceeasi culoare
            if (board.get(dest)[1] == 0 || board.get(dest)[0] == color || board.get(dest)[1] == 1) {
                return true;
            }
        }

        return false;
    }

    private boolean canMove(int color) {
        if (color == WHITE && state.whitesTaken > 0 || color == BLACK && state.blacksTaken > 0) {
            return false;
        }

        for (int pos = 0; pos < 24; pos++) {
            // Daca pe pozitia pos e o piesa de alta culoare sau nu e nimic
            if (board.get(pos)[0] != color || board.get(pos)[1] == 0) {
                continue;
            }

            for (int die : state.remainingMoves) {
                int direction = (color == WHITE ? -1 : 1);
                int dest = pos + direction * die;

                if (dest < 0 || dest > 23) {
                    continue;
                }

                // O piese se poate muta pe pozitia de destinatie daca
                // e libera, exista o singura piesa (de alta culoare) sau e aceeasi culoare
                if (board.get(dest)[1] == 0 || board.get(dest)[0] == color || board.get(dest)[1] == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canRemove(int color) {
        boolean canRemove = (color == WHITE ? state.whiteCanRemove : state.blackCanRemove);
        if (!canRemove) {
            return false;
        }

        for (int die : state.remainingMoves) {
            if (color == WHITE) {
                int from = die - 1;
                if (from >= 0 && from <= 5 && board.get(from)[0] == WHITE && board.get(from)[1] > 0) {
                    return true;
                }

                // Daca nu exista o pozitie directa data de valoarea zarului
                // scoate piesa de pe pozitia ce mai inspre margine
                // (0 <= orice pozitie < valoarea zarului)
                for (int pos = 0; pos < die - 1 && pos < 6; pos++)
                    if (board.get(pos)[0] == WHITE && board.get(pos)[1] > 0)
                        return true;
            } else {
                int from = 24 - die;
                if (from >= 18 && from <= 23 && board.get(from)[0] == BLACK && board.get(from)[1] > 0) {
                    return true;
                }

                // Daca nu exista o pozitie directa data de valoarea zarului
                // scoate piesa de pe pozitia ce mai inspre margine
                // (23 >= orice pozitie > valoarea zarului)
                for (int pos = 23; pos > from && pos >= 18; pos--)
                    if (board.get(pos)[0] == BLACK && board.get(pos)[1] > 0)
                        return true;
            }
        }
        return false;
    }
}
