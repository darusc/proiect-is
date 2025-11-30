package com.example.proiectis.game.logic;

import com.example.proiectis.game.model.State;
import com.example.proiectis.game.exception.*;
import com.example.proiectis.game.model.Board;
import com.example.proiectis.game.model.MoveRequest;

import static com.example.proiectis.game.model.Board.Color.*;

public class MoveValidator {

    private final State state;
    private final Board board;

    public MoveValidator(Board board, State state) {
        this.state = state;
        this.board = board;
    }

    /**
     * Valideaza o miscare ceruta. Daca miscarea este invalida este aruncata o exceptie
     */
    public void validate(MoveRequest moveRequest) throws GameException {
        int from = moveRequest.getSrc();
        int to = moveRequest.getDst();
        int color = moveRequest.getColor();

        if (color != state.currentTurn) {
            throw new InvalidTurnException();
        }

        switch (moveRequest.getType()) {
            case MOVE:
                validateMove(color, from, to);
                break;
            case REENTRY:
                validateReentry(color, to);
                break;
            case REMOVE:
                validateRemove(color, from);
                break;
        }
    }

    private void validateMove(int color, int src, int dst) throws GameException {
        if (src < 0 || src >= Board.SIZE || dst < 0 || dst >= Board.SIZE) {
            throw new InvalidMoveException("Invalid move position");
        }

        if (board.get(src)[0] != color) {
            throw new InvalidMoveException("Not your piece");
        }

        if (color == BLACK && state.blacksTaken > 0 || color == WHITE && state.whitesTaken > 0) {
            throw new InvalidMoveException("Cannot move if you have captured pieces");
        }

        if ((board.get(src)[0] == NONE) || (board.get(src)[1] == 0)) {
            throw new InvalidMoveException("No piece to move");
        }

        if (color != board.get(dst)[0] && board.get(dst)[1] > 1) {
            throw new InvalidMoveException("Cannot move on occupied position");
        }

        // Verifica daca mutarea necesara este disponibila
        if (!state.remainingMoves.contains(Math.abs(dst - src))) {
            throw new InvalidMoveException("No dice available for this move");
        }
    }

    private void validateReentry(int color, int position) throws GameException {
        if ((color == BLACK && state.blacksTaken == 0) || (color == WHITE && state.whitesTaken == 0)) {
            throw new InvalidReentryException("No captured piece to reenter");
        }

        if ((color == BLACK && (position > 5 || position < 0)) || (color == WHITE && (position > Board.SIZE || position < 18))) {
            throw new InvalidReentryException("Reentry position outside opponent home");
        }

        if (color == WHITE && board.get(position)[0] == BLACK && board.get(position)[1] > 1) {
            throw new InvalidReentryException("Invalid reentry position. Position occupied");
        }

        if (color == BLACK && board.get(position)[0] == WHITE && board.get(position)[1] > 1) {
            throw new InvalidReentryException("Invalid reentry position. Position occupied");
        }

        int requiredMove = (color == WHITE) ? 24 - position : position + 1;
        if (!state.remainingMoves.contains(requiredMove)) {
            throw new InvalidReentryException("No dice available for this reentry");
        }
    }

    private void validateRemove(int color, int position) throws GameException {
        if (color == WHITE && !state.whiteCanRemove || color == BLACK && !state.blackCanRemove) {
            throw new InvalidRemoveException("You cannot remove yet");
        }

        if ((color == BLACK && (position < 18 || position > 23)) || (color == WHITE && (position > 6 || position < 0))) {
            throw new InvalidRemoveException("Position is outside your home");
        }

        if (board.get(position)[0] != color || board.get(position)[1] < 1) {
            throw new InvalidRemoveException("No piece to remove at given position");
        }

        // Verifica daca piese se poate scoate direct pe baza zarului
        int requiredMove = (color == WHITE) ? position + 1 : 24 - position;
        if (state.remainingMoves.contains(requiredMove)) {
            return;
        }

        // Verifica daca numarul zarului este mai mare decat ultimul spatiu
        // ocupat pentru a permite scoaterea piesei de pe ultimul spatiu
        boolean isOvershootPossible = false;
        if (color == WHITE) {
            for (int pos = position + 1; pos < 6; pos++) {
                if (board.get(pos)[0] == WHITE && board.get(pos)[1] > 0) {
                    throw new InvalidRemoveException("No overshoot possible");
                }
            }
            isOvershootPossible = state.remainingMoves.stream().anyMatch(m -> m > requiredMove);
        } else {
            for (int pos = position - 1; pos >= 18; pos--) {
                if (board.get(pos)[0] == BLACK && board.get(pos)[1] > 0) {
                    throw new InvalidRemoveException("No overshoot possible");
                }
            }
            isOvershootPossible = state.remainingMoves.stream().anyMatch(m -> m > requiredMove);
        }

        if (!isOvershootPossible) {
            throw new InvalidRemoveException("No overshoot possible");
        }
    }
}
