package com.example.proiectis.websocket;

import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

@Getter
public class Client {

    private final Long id;
    private final Channel channel;
    private final WebSocketSession session;

    public Client(Long id, Channel channel, WebSocketSession session) {
        this.id = id;
        this.channel = channel;
        this.session = session;
    }
}
