class Lobby {

    static ROOMS = "rooms";
    static ROOM_CREATED = "room_created";
    static ROOM_JOINED_SUCCESSFULLY = "room_join_success";
    static ROOM_JOINED_FAILED = "room_join_failed"

    /**
     * @param {...number} playerId
     * @param {...{}} renderer
     */
    constructor(playerId, renderer) {
        this.playerId = playerId;
        this.renderer = renderer;

        this.ws = new Ws(playerId, 1, 'ws://localhost:8080/ws/lobby', msg => this.#onMessage(msg));
    }

    #onMessage(event) {
        const data = JSON.parse(event.data);
        console.log("WS:", data);

        switch (data.type) {

            case Lobby.ROOMS:
                this.renderer.rooms(data['payload']['rooms']);
                break;

            case Lobby.ROOM_CREATED:
                const id = data['payload']['roomId'];
                window.location.replace(`/game/${id}?player=${this.playerId}`);
                break;

            case Lobby.ROOM_JOINED_SUCCESSFULLY:
                const id_ = data['payload']['roomId'];
                window.location.href = `/game/${id_}?player=${this.playerId}`;
                break;

            case Lobby.ROOM_JOINED_FAILED:
                window.location.reload();
                break;

            case "ERROR":
                alert(data.message);
                break;
        }
    }

    create(password) {
        this.ws.broadcast({
            action: "LOBBY",
            type: "create_room",
            payload: {
                password
            }
        })
    }

    join(roomId, password) {
        this.ws.broadcast({
            action: "LOBBY",
            type: "join_room",
            payload: {
                roomId, password
            }
        });
    }
}