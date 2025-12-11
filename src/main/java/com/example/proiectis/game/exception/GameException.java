package com.example.proiectis.game.exception;

import lombok.Getter;

import java.util.Map;

public abstract class GameException extends Exception {

    @Getter
    private String error;
    @Getter
    private String message;

    public GameException(String error, String message) {
        super(message);
        this.error = error;
        this.message = message;
    }

    public Object serialize() {
        return Map.of(
                "error", error,
                "message", message
        );
    }
}
