package com.example.proiectis.game.exception;

public class InvalidRemoveException extends GameException {

    public InvalidRemoveException(String reason) {
        super("INVALID_REMOVE", reason);
    }
}
