package com.example.proiectis.game.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MoveRequest {

    public enum Type {
        MOVE,
        REENTRY,
        REMOVE
    }

    /**
     * Culoare pentru care se cere mutarea
     */
    private int color;

    /**
     * Tipul request-ului: MOVE, REENTRY sau REMOVE
     */
    private Type type;

    /**
     * Pozitia de pe care se muta sau se scoate
     */
    private int src;

    /**
     * Pozitia pe care se muta sau pozitia pe care se reintroduce
     */
    private int dst;
}
