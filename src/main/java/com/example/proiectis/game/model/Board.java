package com.example.proiectis.game.model;

import java.util.Map;

public class Board {

    public static int SIZE = 24;

    public static class Color {
        public static int NONE = 0;
        /**
         * Ascii code for 'W'
         */
        public static int WHITE = 87;
        /**
         * Ascii code for 'B'
         */
        public static int BLACK = 66;
    }

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
    private final int[][] board = new int[SIZE][2];

    public Map<String, Object> serialize() {
        return Map.of(
                "board", board
        );
    }

    public int[] get(int position) {
        return board[position];
    }

    public void addPiece(int position, int color) {
        board[position][0] = color;
        board[position][1] = 1;
    }

    public void addPiece(int color, int position, int count) {
        board[position][0] = color;
        board[position][1] = count;
    }


    /**
     * Print pentru debugging
     */
    public void print() {
        System.out.println("    13  14  15  16  17  18    19  20  21  22  23  24");
        System.out.println("    ------------------------------------------------");

        // TOP HALF (points 13 to 24 → board index 12 to 23)
        for (int row = 0; row < 5; row++) {
            System.out.print("  |");
            for (int i = 12; i < 24; i++) {
                System.out.print(" " + getPieceAtHeight(board[i], 4 - row) + "  ");
                if (i == 17) System.out.print("|");
            }
            System.out.println("|");
        }

        System.out.println("    ------------------------------------------------");

        // BOTTOM HALF (points 12 to 1 → board index 11 to 0)
        for (int row = 0; row < 5; row++) {
            System.out.print("  |");
            for (int i = 11; i >= 0; i--) {
                System.out.print(" " + getPieceAtHeight(board[i], row) + "  ");
                if (i == 6) System.out.print("|");
            }
            System.out.println("|");
        }

        System.out.println("    ------------------------------------------------");
        System.out.println("     12  11  10  9   8   7    6   5   4   3   2   1");
    }

    // Returns "W", "B", or "." depending on stack height
    private String getPieceAtHeight(int[] point, int row) {
        int count = point[1];
        int type = point[0];

        if (count > row) {
            return type == Color.WHITE ? "W" : type == Color.BLACK ? "B" : ".";
        }
        return ".";
    }
}
