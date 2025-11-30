package com.example.proiectis.game.model;

import lombok.Getter;
import org.springframework.data.util.Pair;
import java.io.Serializable;
import java.util.*;
import java.util.stream.IntStream;

public class Board {

    public enum Status {
        WAITING,
        STARTED,
        ENDED
    }

    public static int SIZE = 24;
    public static int MAX_TIME = 30;

    public static int NONE = 0;
    public static int WHITE = 'W';
    public static int BLACK = 'B';

    public interface GameListener {
        /**
         * @param winner Culoare jucatorului care a castigat
         * @param points Numarul de puncte castigate
         */
        void onGameEnd(int winner, int points);

        /**
         * @param currentTurn Jucatorul curent
         * @param whiteTime Timpul ramas al jucatorului alb
         * @param blackTime Timpul ramas al jucatorului negru
         */
        void onTimerUpdate(int currentTurn, long whiteTime, long blackTime);
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

    // Piesele capturate de adversar
    private int whitesTaken;
    private int blacksTaken;

    // Piesele scoasa de jucator
    private int whitesRemoved;
    private int blacksRemoved;

    // Daca toate piesele sunt in casa
    private boolean whiteCanRemove;
    private boolean blackCanRemove;

    @Getter
    private long whiteTime;
    @Getter
    private long blackTime;

    @Getter
    private int currentTurn;
    private int nextTurn;

    private int[] dice = new int[2];
    private final List<Integer> remainingMoves = new ArrayList<>();

    private final Random rand = new Random();
    private final GameListener gameListener;

    private Status status = Status.WAITING;

    public Board(GameListener gameListener) {
        this.gameListener = gameListener;
        reset();
    }

    public void start() {
        status = Status.STARTED;
    }

    public void end(int winner, int points) {
        gameListener.onGameEnd(winner, points);
        status = Status.ENDED;
    }

    public void tick() {
        if(status != Status.STARTED) {
            return;
        }

        if(currentTurn == WHITE) {
            whiteTime--;
            if(whiteTime < 0) {
                whiteTime = 0;
                this.end(BLACK, 3);
            }
        } else {
            blackTime--;
            if(blackTime < 0) {
                blackTime = 0;
                this.end(WHITE, 3);
            }
        }
    }

    public Map<String, Serializable> serialize() {
        return Map.of(
                "turn", currentTurn,
                "roll", dice,
                "board", board,
                "whitesTaken", whitesTaken,
                "blacksTaken", blacksTaken,
                "whitesRemoved", whitesRemoved,
                "blacksRemoved", blacksRemoved,
                "canRemove", (currentTurn == WHITE ? whiteCanRemove : blackCanRemove),
                "remainingMoves", remainingMoves.toArray(new Integer[0])
        );
    }

    public boolean hasAnyLegalMove(int color) {
        return canMove(color) || canReenter(color) || canRemove(color);
    }

    public boolean isValidMove(int color, int src, int dst) {
        if (color != currentTurn)
            return false;

        if (src < 0 || src >= SIZE || dst < 0 || dst >= SIZE)
            return false;

        if (board[src][0] != color)
            return false;

        if (color == BLACK && blacksTaken > 0 || color == WHITE && whitesTaken > 0)
            return false;

        if (board[src][0] == NONE || board[src][1] == 0)
            return false;

        if (color != board[dst][0] && board[dst][1] > 1)
            return false;

        // Verifica daca mutarea necesara este disponibila
        return remainingMoves.contains(distance(src, dst));
    }

    public boolean isValidReenter(int color, int position) {
        if (color != currentTurn)
            return false;

        if ((color == Board.BLACK && blacksTaken == 0) || (color == Board.WHITE && whitesTaken == 0)) {
            return false;
        }

        if ((color == BLACK && (position > 5 || position < 0)) || (color == WHITE && (position > SIZE || position < 18))) {
            return false;
        }

        if (color == WHITE && board[position][0] == BLACK && board[position][1] > 1) {
            return false;
        }

        if (color == BLACK && board[position][0] == WHITE && board[position][1] > 1) {
            return false;
        }

        int requiredMove = (color == WHITE) ? 24 - position : position + 1;
        return remainingMoves.contains(requiredMove);
    }

    public boolean isValidRemove(int color, int position) {
        if (color != currentTurn) {
            return false;
        }

        if (color == WHITE && !whiteCanRemove || color == BLACK && !blackCanRemove) {
            return false;
        }

        if ((color == BLACK && (position < 18 || position > 23)) || (color == WHITE && (position > 6 || position < 0))) {
            return false;
        }

        if (board[position][0] != color || board[position][1] < 1) {
            return false;
        }

        // Verifica daca piese se poate scoate direct pe baza zarului
        int requiredMove = (color == WHITE) ? position + 1 : 24 - position;
        if (remainingMoves.contains(requiredMove)) {
            return true;
        }

        // Verifica daca numarul zarului este mai mare decat ultimul spatiu
        // ocupat pentru a permite scoaterea piesei de pe ultimul spatiu
        if (color == WHITE) {
            for (int pos = position + 1; pos < 6; pos++) {
                if (board[pos][0] == WHITE && board[pos][1] > 0) {
                    return false;
                }
            }
            return remainingMoves.stream().anyMatch(m -> m > requiredMove);
        } else {
            for (int pos = position - 1; pos >= 18; pos--) {
                if (board[pos][0] == BLACK && board[pos][1] > 0) {
                    return false;
                }
            }
            return remainingMoves.stream().anyMatch(m -> m > requiredMove);
        }
    }

    /**
     * Muta o piesa de pe pozitia src pe pozitia dst. Daca in urma mutarii
     * o piesa este capturata se returneaza WHITE sau BLACK, altfel se returneaza NONE.
     * Se presupune ca mutarea e valida
     */
    public int move(int src, int dst) {
        int taken = NONE;

        if (board[dst][0] == board[src][0]) {
            // Daca pe locul unde mutam este o piesa de aceeasi culoare
            board[dst][1]++;
        } else {
            // Daca pe locul unde mutam este o piese deja o piesa, aceasta e capturata
            if (board[dst][0] == WHITE) {
                whitesTaken++;
            } else if (board[dst][0] == BLACK) {
                blacksTaken++;
            }

            taken = board[dst][0];
            board[dst][0] = board[src][0];
            board[dst][1] = 1;
        }

        board[src][1]--;
        if (board[src][1] == 0) {
            board[src][0] = NONE;
        }

        useRemainingMove(distance(src, dst));
        checkIfAllPiecesInHome();

        return taken;
    }

    /**
     * Repune o piesa capturata inapoi in joc
     */
    public int reenter(int color, int position) {
        int taken = NONE;

        if (board[position][0] == NONE) {
            board[position][0] = color;
            board[position][1] = 1;
        } else if (board[position][0] == color) {
            board[position][1]++;
        } else {
            if (board[position][0] == BLACK) {
                blacksTaken++;
            } else {
                whitesTaken++;
            }

            taken = board[position][0];
            board[position][0] = color;
            board[position][1] = 1;
        }

        if (color == WHITE) {
            whitesTaken--;
        } else {
            blacksTaken--;
        }

        int requiredMove = (color == WHITE) ? 24 - position : position + 1;
        useRemainingMove(requiredMove);

        return taken;
    }

    public void remove(int color, int position) {
        board[position][1]--;
        if (board[position][1] == 0) {
            board[position][0] = NONE;
        }

        if (color == WHITE) {
            whitesRemoved++;
        } else {
            blacksRemoved++;
        }

        int requiredMove = (color == WHITE) ? position + 1 : 24 - position;
        int moveToUse = remainingMoves.stream().filter(m -> m >= requiredMove).min(Integer::compareTo).orElse(requiredMove);
        useRemainingMove(moveToUse);

        checkIfGameEnded();
    }

    public int[] rollDice() {
        // Genereaza zarul random
        this.dice = new int[]{rand.nextInt(6) + 1, rand.nextInt(6) + 1};

        // Genereaza mutarile ramase in functie de zarul generate
        // Daca e dubla => 4 mutari, altfel 2 mutari
        remainingMoves.clear();
        if (dice[0] == dice[1]) {
            remainingMoves.addAll(List.of(dice[0], dice[0], dice[0], dice[0]));
        } else {
            remainingMoves.addAll(List.of(dice[0], dice[1]));
        }

        // Daca nu exista nicio mutare legala cu zarul generat treci la urmatoarea tura
        if (!hasAnyLegalMove(currentTurn)) {
            advance();
        }

        return dice;
    }

    public void reset() {
        setupInitialState();
        remainingMoves.clear();
        dice[0] = dice[1] = 0;
        whitesTaken = blacksTaken = 0;
        whitesRemoved = blacksRemoved = 0;
        whiteTime = blackTime = MAX_TIME;
        currentTurn = WHITE;
        nextTurn = BLACK;
    }

    public void place(int piece, int position, int count) {
        board[position][0] = piece;
        board[position][1] = count;
    }

    /**
     * Foloseste una din mutarile remase.
     * Daca s-a folosit ultima mutare avanseaza automat
     */
    void useRemainingMove(int move) {
        remainingMoves.remove(Integer.valueOf(move));
        // Daca s-au folosit toate mutarile sau nu mai exista nicio
        // mutare legala pentru jucatorul curent  la urmatoarea tura
        if (remainingMoves.isEmpty() || !hasAnyLegalMove(currentTurn)) {
            advance();
        }
    }

    /**
     * Verifica daca toate piese sunt in casa si jucatorul poate incepe sa scoate piese
     */
    void checkIfAllPiecesInHome() {
        int count = 0;
        for (int i = 0; i < 6; i++) {
            if (board[i][0] == WHITE) {
                count += board[i][1];
            }
        }
        whiteCanRemove = (count + whitesRemoved == 15);

        count = 0;
        for (int i = 18; i < 24; i++) {
            if (board[i][0] == BLACK) {
                count += board[i][1];
            }
        }
        blackCanRemove = (count + blacksRemoved == 15);
    }

    private void checkIfGameEnded() {
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
            this.end(winner, points);
        }
    }

    /**
     * Verifica victoria tripla.
     * Toate piesele scoase, oponentul nu a scos nicio piesa si are
     * una capturata sau in casa castigatorului
     */
    private boolean checkTripleVictory(int color) {
        int removed = color == WHITE ? whitesRemoved : blacksRemoved;
        int opponentRemoved = color == WHITE ? blacksRemoved : whitesRemoved;

        int taken = color == WHITE ? blacksTaken : whitesTaken;

        int opponentColor = color == WHITE ? BLACK : WHITE;

        int start = color == WHITE ? 0 : 19;
        int end = color == BLACK ? 6 : 24;

        boolean opponentInHome = IntStream.range(start, end)
                .anyMatch(i -> board[i][0] == opponentColor && board[i][1] > 0);

        return removed == 15 && opponentRemoved == 0 && (taken > 0 || opponentInHome);
    }

    /**
     * Verifica victoria dubla.
     * Toate piesele scoase, oponentul nu a scos nicio piesa
     */
    private boolean checkDoubleVictory(int color) {
        int removed = color == WHITE ? whitesRemoved : blacksRemoved;
        int opponentRemoved = color == WHITE ? blacksRemoved : whitesRemoved;

        return removed == 15 && opponentRemoved == 0;
    }

    /**
     * Victorie simpla, toate piesele scoase
     */
    private boolean checkSimpleVictory(int color) {
        return color == WHITE ? whitesRemoved == 15 : blacksRemoved == 15;
    }

    private int distance(int src, int dst) {
        return Math.abs(dst - src);
    }

    /**
     * Mergi la urmatoarea tura
     */
    private void advance() {
        int tmp = currentTurn;
        currentTurn = nextTurn;
        nextTurn = tmp;

        dice[0] = 0;
        dice[1] = 0;
        remainingMoves.clear();

        checkIfAllPiecesInHome();

        gameListener.onTimerUpdate(currentTurn, whiteTime, blackTime);
    }

    private boolean canReenter(int color) {
        int taken = (color == WHITE ? whitesTaken : blacksTaken);
        if (taken == 0) {
            return false;
        }

        for (int die : remainingMoves) {
            int dest = (color == WHITE) ? 24 - die : die - 1;

            if (dest < 0 || dest > 23) {
                continue;
            }

            // O piese se poate repune daca pozitia de destinatie
            // e libera, exista o singura piesa (de alta culoare) sau e aceeasi culoare
            if (board[dest][1] == 0 || board[dest][0] == color || board[dest][1] == 1) {
                return true;
            }
        }

        return false;
    }

    private boolean canMove(int color) {
        if (color == WHITE && whitesTaken > 0 || color == BLACK && blacksTaken > 0) {
            return false;
        }

        for (int pos = 0; pos < 24; pos++) {
            // Daca pe pozitia pos e o piesa de alta culoare sau nu e nimic
            if (board[pos][0] != color || board[pos][1] == 0) {
                continue;
            }

            for (int die : remainingMoves) {
                int direction = (color == WHITE ? -1 : 1);
                int dest = pos + direction * die;

                if (dest < 0 || dest > 23) {
                    continue;
                }

                // O piese se poate muta pe pozitia de destinatie daca
                // e libera, exista o singura piesa (de alta culoare) sau e aceeasi culoare
                if (board[dest][1] == 0 || board[dest][0] == color || board[dest][1] == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canRemove(int color) {
        boolean canRemove = (color == WHITE ? whiteCanRemove : blackCanRemove);
        if (!canRemove) {
            return false;
        }

        for (int die : remainingMoves) {
            if (color == WHITE) {
                int from = die - 1;
                if (from >= 0 && from <= 5 && board[from][0] == WHITE && board[from][1] > 0) {
                    return true;
                }

                // Daca nu exista o pozitie directa data de valoarea zarului
                // scoate piesa de pe pozitia ce mai inspre margine
                // (0 <= orice pozitie < valoarea zarului)
                for (int pos = 0; pos < die - 1 && pos < 6; pos++)
                    if (board[pos][0] == WHITE && board[pos][1] > 0)
                        return true;
            } else {
                int from = 24 - die;
                if (from >= 18 && from <= 23 && board[from][0] == BLACK && board[from][1] > 0) {
                    return true;
                }

                // Daca nu exista o pozitie directa data de valoarea zarului
                // scoate piesa de pe pozitia ce mai inspre margine
                // (23 >= orice pozitie > valoarea zarului)
                for (int pos = 23; pos > from && pos >= 18; pos--)
                    if (board[pos][0] == BLACK && board[pos][1] > 0)
                        return true;
            }
        }
        return false;
    }

    /**
     * Stare initiala piese pe tabla
     */
    private void setupInitialState() {
        place(BLACK, 0, 2);
        place(WHITE, 5, 5);
        place(WHITE, 7, 3);
        place(BLACK, 11, 5);
        place(WHITE, 12, 5);
        place(BLACK, 16, 3);
        place(BLACK, 18, 5);
        place(WHITE, 23, 2);
    }

    /**
     * Stare initiala piese pe tabla inainte de a avea toate piese in casa
     * (pentru testare)
     */
    void setupBeforeRemovingState() {
        place(WHITE, 0, 5);
        place(WHITE, 1, 4);
        place(WHITE, 2, 3);
        place(WHITE, 3, 2);
        place(WHITE, 10, 1);
        place(BLACK, 18, 2);
        place(BLACK, 19, 3);
        place(BLACK, 20, 4);
        place(BLACK, 21, 2);
        place(BLACK, 22, 2);
        place(BLACK, 23, 2);
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
                if (i == 17) System.out.print("|"); // middle bar
            }
            System.out.println("|");
        }

        System.out.println("    ------------------------------------------------");

        // BOTTOM HALF (points 12 to 1 → board index 11 to 0)
        for (int row = 0; row < 5; row++) {
            System.out.print("  |");
            for (int i = 11; i >= 0; i--) {
                System.out.print(" " + getPieceAtHeight(board[i], row) + "  ");
                if (i == 6) System.out.print("|"); // middle bar
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
            return type == WHITE ? "W" : type == BLACK ? "B" : ".";
        }
        return ".";
    }
}
