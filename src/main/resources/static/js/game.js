class Game {

    static START = "game_start";
    static STATE = "state";
    static END = "game_end";
    static TIMER = "timer";

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
        this.ended = false;

        this.ws = new Ws(playerId, roomId, 'ws://localhost:8080/ws/game', msg => this.#onMessage(msg));
    }

    #onMessage(event) {
        const data = JSON.parse(event.data);
        console.log("WS:", data);

        if(this.ended) {
            return;
        }

        switch (data.type) {

            case Game.START:
                this.color = data['payload']['white'] === this.playerId ? Game.WHITE : Game.BLACK
                this.renderer.timer(data['payload']['startTime'], data['payload']['startTime'], Game.WHITE);
                break;

            case Game.STATE:
                this.state = data['payload'];
                this.renderState(this.state);
                break;

            case Game.END:
                this.renderGameEndDialogue(data['payload']);
                this.renderer.timerStop();
                this.ended = true;
                break;

            case Game.TIMER:
                this.renderer.timer(data['payload']['whiteTime'], data['payload']['blackTime'], data['payload']['turn']);
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

    get whitesRemoved() {
        return this.state['whitesRemoved'];
    }

    get blacksRemoved() {
        return this.state['blacksRemoved'];
    }

    get canRemove() {
        return this.state['canRemove'];
    }

    renderState(state) {
        this.renderer.board(state['board'], this.color);
        this.renderer.dice(state['roll'], state['remainingMoves'], this.myTurn);

        const captured = {whites: this.whitesTaken, blacks: this.blacksTaken};
        const removed = {whites: this.whitesRemoved, blacks: this.blacksRemoved};
        this.renderer.barPieces(captured, removed);

        // Highlight triunghiuri doar daca s-a dat cu zarul
        if(state['roll'] !== [0, 0] && this.myTurn) {
            if (this.color === Game.WHITE && state['whitesTaken'] > 0 || this.color === Game.BLACK && state['blacksTaken'] > 0) {
                this.renderer.highlightReentry(this.remainingMoves, this.color);
            }

            if (this.canRemove) {
                this.renderer.highlightRemove(this.remainingMoves, this.color);
            }
        }
    }

    renderGameEndDialogue(data) {
        const won = data['winner'] === this.color;
        const player1 = this.color === Game.WHITE ? data['white'] : data['black'];
        const player2 = this.color === Game.WHITE ? data['black'] : data['white'];

        this.renderer.endGame(won, player1, player2);
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

    remove(from) {
        this.ws.broadcast({
            action: "GAME",
            type: "remove",
            payload: {
                color: this.color, position: from
            }
        })
    }
}