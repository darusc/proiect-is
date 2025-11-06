package com.example.proiectis.game;

import java.io.Serializable;
import java.util.Map;
import java.util.Random;

public class Board {

    public static int SIZE = 24;

    public static byte NONE = 0;
    public static byte WHITE = 'W';
    public static byte BLACK = 'B';

    // Tabla de joc e reprezentata de o matrice de
    // 24 de randuri x 2 coloane => fiecare rand reprezinta
    // o pozitie de pe table, prima coloana reprezinta
    // tipul piesei, a 2 a coloana reprezinta numarul de piese
    //
    //   13  14  15  16  17  18  19  20  21  22  23  24
    //   ------------------------------------------------
    // |  W   .   .   .   B   . | B   .   .   .   .   W  |
    // |  W   .   .   .   B   . | B   .   .   .   .   W  |
    // |  W   .   .   .   B   . | B   .   .   .   .   .  |
    // |  W   .   .   .   .   . | B   .   .   .   .   .  |
    // |  W   .   .   .   .   . | B   .   .   .   .   .  |
    //   ------------------------------------------------
    // |  B   .   .   .   .   . | W   .   .   .   .   .  |
    // |  B   .   .   .   .   . | W   .   .   .   .   .  |
    // |  B   .   .   .   W   . | W   .   .   .   .   .  |
    // |  B   .   .   .   W   . | W   .   .   .   .   B  |
    // |  B   .   .   .   W   . | W   .   .   .   .   B  |
    //   ------------------------------------------------
    //    12  11  10  9   8   7   6   5   4   3   2   1
    private final byte[][] board = new byte[SIZE][2];

    private int whitesTaken;
    private int blacksTaken;

    // Daca toate piesele sunt in casa
    private boolean whiteCanRemove;
    private boolean blackCanRemove;

    private byte currentTurn;
    private byte nextTurn;

    private final Random rand = new Random();

    public Board() {
        reset();
    }

    public Map<String, Serializable> serialize() {
        return Map.of(
            "turn", currentTurn,
            "roll", rollDice(),
            "board", board
        );
    }

    public boolean isValidMove(int src, int dst) {
        if (src < 0 || src >= SIZE || dst < 0 || dst >= SIZE)
            return false;

        if (board[src][0] != board[dst][0] && board[dst][1] > 1)
            return false;

        return true;
    }

    /**
     * Muta o piese de pe pozitia src pe pozitia dst. Daca in urma mutarii
     * o piesa este capturata se returneaza WHITE sau BLACK, altfel se returneaza NONE.
     * Se presupune ca mutarea e valida
     */
    public byte move(int src, int dst) {
        byte taken = NONE;

        if (board[dst][0] == board[src][0]) {
            // Daca pe locul unde mutam este o piesa de aceeasi culoare
            board[dst][1]++;
        } else {
            if (board[src][0] != board[dst][0]) {
                // Daca pe locul unde mutam este o piese de alta culoare
                if (board[dst][0] == WHITE) {
                    whitesTaken++;
                    taken = WHITE;
                } else {
                    blacksTaken++;
                    taken = BLACK;
                }
            }

            board[dst][0] = board[src][0];
            board[dst][1] = 1;
        }

        board[src][1]--;
        if(board[src][0] == 0) {
            board[src][1] = NONE;
        }

        return taken;
    }

    /**
     * Mergi la urmatoarea tura
     */
    public void advance() {
        byte tmp = currentTurn;
        currentTurn = nextTurn;
        nextTurn = tmp;
    }

    public void reset() {
        place(BLACK, 0, 2);
        place(WHITE, 5, 5);
        place(WHITE, 7, 3);
        place(BLACK, 11, 5);
        place(WHITE, 12, 5);
        place(BLACK, 16, 3);
        place(BLACK, 18, 5);
        place(WHITE, 23, 2);

        whitesTaken = 0;
        blacksTaken = 0;

        currentTurn = WHITE;
        nextTurn = BLACK;
    }

    private void place(byte piece, int position, int count) {
        board[position][0] = piece;
        board[position][1] = (byte) count;
    }

    private int[] rollDice() {
        return new int[]{rand.nextInt(6) + 1, rand.nextInt(6) + 1};
    }
}
