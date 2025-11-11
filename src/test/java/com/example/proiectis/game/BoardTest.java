package com.example.proiectis.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BoardTest {

    private Board board;

    @BeforeEach
    void setup() {
        board = new Board();
    }

    @Test
    void testInitialSetup() {
        // Check a few important initial positions
        assertEquals(Board.BLACK, board.serialize().get("board") instanceof byte[][] b ? b[0][0] : -1);
        board.print(); // just to visually check it doesn't crash
    }

    @Test
    void testResetPlacesCorrectPieces() {
        byte[][] b = (byte[][]) board.serialize().get("board");

        assertEquals(Board.BLACK, b[0][0]);
        assertEquals(2, b[0][1]);

        assertEquals(Board.WHITE, b[5][0]);
        assertEquals(5, b[5][1]);

        assertEquals(Board.WHITE, b[23][0]);
        assertEquals(2, b[23][1]);

        assertEquals(Board.BLACK, b[18][0]);
        assertEquals(5, b[18][1]);
    }

    @Test
    void testValidMove() {
        // Moving white from 23 to 22 should be valid
        assertTrue(board.isValidMove(23, 22));
    }

    @Test
    void testInvalidMoveBlocked() {
        // Invalid if trying to land on 2+ enemy pieces
        byte[][] b = (byte[][]) board.serialize().get("board");
        b[10][0] = Board.BLACK;
        b[10][1] = 2;

        b[5][0] = Board.WHITE;
        b[5][1] = 1;

        assertFalse(board.isValidMove(5, 10));
    }

    @Test
    void testMoveSimple() {
        byte[][] b = (byte[][]) board.serialize().get("board");

        byte beforeCount = b[23][1];
        byte taken = board.move(23, 22);

        assertEquals(Board.NONE, taken);
        assertEquals(beforeCount - 1, b[23][1]);
        assertEquals(Board.WHITE, b[22][0]);
        assertEquals(1, b[22][1]);
    }

    @Test
    void testCapture() {
        byte[][] b = (byte[][]) board.serialize().get("board");

        // Put 1 black piece on point 22
        board.place(Board.BLACK, 22, 1);

        // Move white from 23 → 22 (capture)
        byte taken = board.move(23, 22);

        assertEquals(Board.BLACK, taken);
        assertEquals(Board.WHITE, b[22][0]);
        assertEquals(1, b[22][1]);
    }

    @Test
    void testAdvanceTurn() {
        board.advance();
        // After advancing once, turn should be black
        assertEquals(Board.BLACK, board.serialize().get("turn"));
    }

    @Test
    void testPrintDoesNotCrash() {
        assertDoesNotThrow(() -> board.print());
    }
}
