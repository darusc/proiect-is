package com.example.proiectis.game.exception;

public class InvalidTurnException extends GameException {

    public InvalidTurnException() {
        super("INVALID_TURN", "Not your turn");
    }
}
