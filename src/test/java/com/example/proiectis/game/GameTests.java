package com.example.proiectis.game;

import com.example.proiectis.game.logic.*;
import com.example.proiectis.game.model.*;
import com.example.proiectis.game.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.example.proiectis.game.model.Board.Color.*;
import static org.junit.jupiter.api.Assertions.*;

class GameTests {

    Board board;
    State state;
    LegalMovesResolver resolver;
    MoveValidator validator;
    MoveExecutor executor;
    VictoryChecker victoryChecker;

    @BeforeEach
    void setup() {
        board = new Board();
        state = new State();
        resolver = new LegalMovesResolver(board, state);
        validator = new MoveValidator(board, state);
        executor = new MoveExecutor(board, state);
        victoryChecker = new VictoryChecker(board, state);
    }

    @Test
    void testBoardAddPieceAndSerialize() {
        board.addPiece(0, WHITE, 2);
        int[] pos = board.get(0);
        assertEquals(WHITE, pos[0]);
        assertEquals(2, pos[1]);

        Map<String, Object> serialized = board.serialize();
        assertTrue(serialized.containsKey("board"));
    }

    @Test
    void testStateSerialization() {
        state.currentTurn = WHITE;
        state.remainingMoves.addAll(List.of(3, 5));
        Map<String, Object> serialized = state.serialize();
        assertEquals(state.currentTurn, serialized.get("turn"));
        assertArrayEquals(new Integer[]{3, 5}, (Integer[]) serialized.get("remainingMoves"));
    }

    @Test
    void testMoveRequestInitialization() {
        MoveRequest req = new MoveRequest(WHITE, MoveRequest.Type.MOVE, 0, 1);
        assertEquals(WHITE, req.getColor());
        assertEquals(MoveRequest.Type.MOVE, req.getType());
        assertEquals(0, req.getSrc());
        assertEquals(1, req.getDst());
    }

    @Test
    void testLegalMovesResolver() {
        state.currentTurn = WHITE;
        board.addPiece(0, WHITE);
        state.remainingMoves.add(1);
        assertTrue(resolver.hasAnyLegalMove());
    }

    @Test
    void testMoveValidatorValidMove() {
        state.currentTurn = WHITE;
        board.addPiece(0, WHITE);
        state.remainingMoves.add(1);
        MoveRequest move = new MoveRequest(WHITE, MoveRequest.Type.MOVE, 0, 1);
        assertDoesNotThrow(() -> validator.validate(move));
    }

    @Test
    void testMoveValidatorInvalidMove() {
        state.currentTurn = WHITE;
        board.addPiece(0, BLACK); // wrong color
        state.remainingMoves.add(1);
        MoveRequest move = new MoveRequest(WHITE, MoveRequest.Type.MOVE, 0, 1);
        assertThrows(InvalidMoveException.class, () -> validator.validate(move));
    }

    @Test
    void testMoveExecutorMove() {
        state.currentTurn = WHITE;
        board.addPiece(0, WHITE, 1);
        state.remainingMoves.add(1);
        MoveRequest move = new MoveRequest(WHITE, MoveRequest.Type.MOVE, 0, 1);
        executor.execute(move);
        assertEquals(WHITE, board.get(1)[0]);
        assertEquals(1, board.get(1)[1]);
        assertEquals(0, board.get(0)[1]);
        assertFalse(state.remainingMoves.contains(1));
    }

    @Test
    void testMoveExecutorReentry() {
        state.currentTurn = WHITE;
        state.whitesTaken = 1;
        state.remainingMoves.add(1);
        executor.execute(new MoveRequest(WHITE, MoveRequest.Type.REENTRY, -1, 23));
        assertEquals(WHITE, board.get(23)[0]);
        assertEquals(1, board.get(23)[1]);
        assertEquals(0, state.whitesTaken);
        assertFalse(state.remainingMoves.contains(1));
    }

    @Test
    void testMoveExecutorRemove() {
        state.currentTurn = WHITE;
        state.whiteCanRemove = true;
        board.addPiece(0, WHITE, 1);
        state.remainingMoves.add(1);
        executor.execute(new MoveRequest(WHITE, MoveRequest.Type.REMOVE, 0, -1));
        assertEquals(0, board.get(0)[1]);
        assertEquals(WHITE, board.get(0)[0] == WHITE ? WHITE : 0);
        assertEquals(1, state.whitesRemoved);
        assertFalse(state.remainingMoves.contains(1));
    }

    @Test
    void testVictoryCheckerSimpleVictory() {
        state.whitesRemoved = 15;
        Optional<Pair<Integer, Integer>> result = victoryChecker.checkVictory();
        assertTrue(result.isPresent());
        assertEquals(WHITE, result.get().getFirst());
        assertEquals(1, result.get().getSecond());
    }

    @Test
    void testVictoryCheckerDoubleVictory() {
        state.whitesRemoved = 15;
        state.blacksRemoved = 0;
        Optional<Pair<Integer, Integer>> result = victoryChecker.checkVictory();
        assertTrue(result.isPresent());
        assertEquals(WHITE, result.get().getFirst());
        assertEquals(2, result.get().getSecond());
    }
}
