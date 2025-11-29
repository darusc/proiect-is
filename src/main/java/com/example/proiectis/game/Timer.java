package com.example.proiectis.game;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class Timer {

    @FunctionalInterface
    public interface TickFunction {
        void onTick();
    }

    private final List<TickFunction> subscribers = new CopyOnWriteArrayList<>();

    public void subscribe(TickFunction tickFunction) {
        subscribers.add(tickFunction);
    }

    public void unsubscribe(TickFunction tickFunction) {
        subscribers.remove(tickFunction);
    }

    @Scheduled(fixedRate = 1000)
    public void tick() {
        for (TickFunction tickFunction : subscribers) {
            tickFunction.onTick();
        }
    }
}
