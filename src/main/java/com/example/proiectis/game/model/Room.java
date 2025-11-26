package com.example.proiectis.game.model;

import com.example.proiectis.game.GameManager;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class Room {

    public enum Status {
        WAITING,
        READY,
        IN_PROGRESS
    }

    @Setter
    @Getter
    private String id;
    @Setter
    private String name;
    @Setter
    private String password;

    @Setter
    private Status status = Status.WAITING;

    private Set<Long> players = new HashSet<Long>();

    public Room(String id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    public boolean join(Long playerId, String password) {
        if (players.contains(playerId) || !this.password.equals(password) || players.size() > GameManager.MAX_ROOM_SIZE) {
            return false;
        }

        players.add(playerId);
        if (players.size() == GameManager.MAX_ROOM_SIZE) {
            status = Status.READY;
        }

        return true;
    }

    public void leave(Long playerId) {
        players.remove(playerId);
        status = Status.IN_PROGRESS;
    }

    public boolean hasPlayer(Long playerId) {
        return this.players.contains(playerId);
    }

    public boolean isWaiting() {
        return this.status == Status.WAITING;
    }

    public Object toJson() {
        return Map.of(
                "id", id,
                "name", name
        );
    }
}
