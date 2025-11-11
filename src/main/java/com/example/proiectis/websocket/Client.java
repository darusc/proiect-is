package com.example.proiectis.websocket;

import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

@Getter
public class Client {

    private final int id;
    private final Channel channel;
    private final WebSocketSession session;

    public Client(int id, Channel channel, WebSocketSession session) {
        this.id = id;
        this.channel = channel;
        this.session = session;
    }
}
