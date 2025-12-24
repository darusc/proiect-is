class Ws {

    #socket;

    /**
     * @param {...number} clientId
     * @param {...string} roomId
     * @param {...string} endpoint
     * @param {...(MessageEvent) => {}} onmessage
     */
    constructor(clientId, roomId, endpoint, onmessage) {
        this.#socket = new WebSocket(`${endpoint}?clientId=${clientId}`);
        this.#socket.onopen = () => this.join(roomId);
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