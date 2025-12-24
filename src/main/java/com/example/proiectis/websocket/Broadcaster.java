package com.example.proiectis.websocket;

public interface Broadcaster {
    /**
     * Trimite un mesaj la toti clientii conectati la un canal
     * @param channel Canalul pe care se trimite mesajul
     * @param json Mesajul in format JSON
     */
    void broadcast(Channel channel, Object json);

    /**
     * Trimite un mesaj doar la clientul specificat
     * @param client Clientul la care sa transmita mesajul
     * @param json Mesajul in format JSON
     */
    void broadcast(Client client, Object json);
}
