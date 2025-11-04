class Ws {

    #socket;

    /**
     * @param {...number} clientId
     * @param {...(MessageEvent) => {}} onmessage
     */
    constructor(clientId, onmessage) {
        this.#socket = new WebSocket(`ws://localhost:8080/ws/game?clientId=${clientId}`);
        this.#socket.onmessage = onmessage;
    }

    /**
     * @param {...string} roomId
     */
    join(roomId) {
        this.#socket.send(JSON.stringify({
            action: "join",
            channelId: roomId
        }));
    }

    /**
     * @param {...{}} data
     */
    broadcast(data) {
        this.#socket.send(JSON.stringify(data));
    }
}