package com.example.proiectis.game.exception;

public class InvalidMoveException extends GameException {

    public InvalidMoveException(String reason) {
        super("INVALID_MOVE", reason);
    }
}
