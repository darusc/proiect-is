package com.example.proiectis.game;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Map;
import java.util.Objects;

public class Response<T> {

    @Getter
    private final String type;
    @Getter
    private final T payload;


    public Response(String type, T payload) {
        this.type = Objects.requireNonNull(type);
        this.payload = payload;
    }

    @JsonValue
    public Map<String, Object> toJson() {
        return Map.of("type", type, "payload", payload);
    }
    
    public record InvalidRequestPayload(String reason) { }
    public static Response<InvalidRequestPayload> InvalidRequest(String reason) {
        return new Response<>("invalid_move", new InvalidRequestPayload(reason));
    }
}
