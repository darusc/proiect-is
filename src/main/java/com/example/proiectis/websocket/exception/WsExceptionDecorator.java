package com.example.proiectis.websocket.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

import java.util.Map;

public class WsExceptionDecorator extends WebSocketHandlerDecorator {

    private final ObjectMapper mapper = new ObjectMapper();

    public WsExceptionDecorator(WebSocketHandler delegate) {
        super(delegate);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            super.handleMessage(session, message);
        } catch (WsException e) {
            session.sendMessage(new TextMessage(serializeException(e)));
            //session.close();
        } catch (Exception e) {
            session.sendMessage(new TextMessage(serializeException(new WsException("INTERNAL_ERROR", e.getMessage()))));
            session.close();
        }
    }

    private String serializeException(WsException e) throws JsonProcessingException {
        return mapper.writeValueAsString(Map.of(
            "error", e.getError(),
            "message", e.getMessage()
        ));
    }
}
