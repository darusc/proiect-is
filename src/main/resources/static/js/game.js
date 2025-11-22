class Game {

    static START = "game_start";
    static STATE = "state"

    static WHITE = 87
    static BLACK = 66

    /**
     * @param {...number} playerId
     * @param {...string} roomId
     * @param {...{}} renderer
     */
    constructor(playerId, roomId, renderer) {
        this.playerId = playerId;
        this.renderer = renderer;
        this.color = null;
        this.state = null;

        this.ws = new Ws(playerId, roomId, msg => this.#onMessage(msg));
    }

    #onMessage(event) {
        const data = JSON.parse(event.data);
        console.log("WS:", data);

        switch (data.type) {

            case Game.START:
                this.color = data['payload']['white'] === this.playerId ? Game.WHITE : Game.BLACK
                break;

            case Game.STATE:
                this.state = data['payload'];
                this.renderState(this.state);
                break;

            case "ERROR":
                alert(data.message);
                break;
        }
    }

    get myTurn() {
        return this.color === this.state['turn'];
    }

    get remainingMoves() {
        return this.state['remainingMoves'];
    }

    get whitesTaken() {
        return this.state['whitesTaken'];
    }

    get blacksTaken() {
        return this.state['blacksTaken'];
    }

    renderState(state) {
        this.renderer.board(state['board']);
        this.renderer.dice(state['roll'], state['remainingMoves']);
        this.renderer.capturedPieces(state['whitesTaken'], state['blacksTaken']);

        if(this.color === Game.WHITE && state['whitesTaken'] > 0 || this.color === Game.BLACK && state['blacksTaken'] > 0) {
            this.renderer.highlightReentry();
        }
    }

    requestRoll() {
        this.ws.broadcast({
            action: "GAME",
            type: "roll_request",
            payload: {}
        });
    }

    reenter(position) {
        console.log("Requested reentry on", position);
        this.ws.broadcast({
            action: "GAME",
            type: "reenter",
            payload: {
                color: this.color,
                position
            }
        });
    }

    move(from, to) {
        console.log(`Moved from ${from + 1} to ${to + 1} | Color: ${this.color === 87 ? 'White' : 'Black'}`);
        this.ws.broadcast({
            action: "GAME",
            type: "move",
            payload: {
                color: this.color, from, to
            }
        });
    }
}