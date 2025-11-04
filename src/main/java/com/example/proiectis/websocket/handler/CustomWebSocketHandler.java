package com.example.proiectis.websocket.handler;

import com.example.proiectis.websocket.Channel;
import com.example.proiectis.websocket.Client;
import com.example.proiectis.websocket.CustomWebSocketListener;

public interface CustomWebSocketHandler {

    String ACTION_JOIN = "join";
    String ACTION_MSG = "msg";

    int MAX_ROOM_SIZE = 2;

    void addListener(CustomWebSocketListener listener);
    void removeListener(CustomWebSocketListener listener);

    /**
     * Trimite un mesaj la toti clientii conectati la un canal
     * @param channel Canalul pe care se trimite mesajul
     * @param json Mesajul in format JSON
     */
    void broadcast(Channel channel, Object json) throws Exception;

    /**
     * Trimite un mesaj la toti clientii conectati la un canal,
     * excluzand clientul care transmite
     * @param channel Canalul pe care se trimite mesajul
     * @param client Clientul care transmite
     * @param json Mesajul in format JSON
     */
    void broadcast(Channel channel, Client client, String json) throws Exception;
}
