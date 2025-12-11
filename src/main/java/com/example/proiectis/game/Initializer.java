package com.example.proiectis.game;

import com.example.proiectis.game.model.Board;
import com.example.proiectis.game.model.Game;
import com.example.proiectis.game.model.State;

import static com.example.proiectis.game.model.Board.Color.BLACK;
import static com.example.proiectis.game.model.Board.Color.WHITE;

public class Initializer {

    public static int NORMAL = 0;

    public static void setupInitialState(int mode, Board board, State state) {
        if (mode == NORMAL) {
            setupInitialStateNormal(board, state);
        }
    }

    private static void setupInitialStateNormal(Board board, State state) {
        board.addPiece(BLACK, 0, 2);
        board.addPiece(WHITE, 5, 5);
        board.addPiece(WHITE, 7, 3);
        board.addPiece(BLACK, 11, 5);
        board.addPiece(WHITE, 12, 5);
        board.addPiece(BLACK, 16, 3);
        board.addPiece(BLACK, 18, 5);
        board.addPiece(WHITE, 23, 2);

        state.whiteTime = Game.MAX_TIME;
        state.blackTime = Game.MAX_TIME;
        state.currentTurn = WHITE;
        state.nextTurn = BLACK;
    }

}
