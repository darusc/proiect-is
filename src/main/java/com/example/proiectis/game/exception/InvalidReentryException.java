package com.example.proiectis.game.exception;

public class InvalidReentryException extends GameException {

    public InvalidReentryException(String reason) {
        super("INVALID_REENTRY", reason);
    }
}
