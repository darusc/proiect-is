package com.example.proiectis.websocket.exception;

import lombok.Getter;

@Getter
public class WsException extends Exception {

    private final String error;

    public WsException(String error) {
        super("");
        this.error = error;
    }

    public WsException(String error, String message) {
        super(message);
        this.error = error;
    }
}
