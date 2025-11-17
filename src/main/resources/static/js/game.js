class Game {

    /**
     * @param {...number} playerId
     * @param {...string} roomId
     * @param {...{board: Function, dice: Function}} renderer
     */
    constructor(playerId, roomId, renderer) {
        this.playerId = playerId;
        this.ws = new Ws(playerId, roomId, msg => this.onMessage(msg));
        this.board = null;
        this.myTurn = false;
        this.dice = [];
        this.color = null;
        this.remainingMoves = [];
        this.renderer = renderer;

        console.log(this.renderer);
    }

    onMessage(event) {
        const data = JSON.parse(event.data);
        console.log("WS:", data);

        switch (data.type) {

            case "game_start":
                this.color = data['payload']['white'] === this.playerId ? 87 : 66;
                break;

            case "roll_result":
                this.dice = data['payload']['dice'];
                this.remainingMoves = this.dice[0] === this.dice[1]
                    ? [this.dice[0], this.dice[0], this.dice[0], this.dice[0]]
                    : [this.dice[0], this.dice[1]];

                this.renderer['dice'](this.dice);
                break;

            case "state":
                const turn = data['payload']['turn'];
                this.myTurn = turn === this.color;

                this.board = data['payload']['board'];
                this.renderer['board'](this.board);
                this.renderer['dice'](data['payload']['roll']);
                break;

            case "ERROR":
                alert(data.message);
                break;
        }
    }

    requestRoll() {
        if (!this.myTurn) {
            console.log("Wait your turn!");
            return;
        }
        this.ws.broadcast({ action: "GAME", type: "roll_request", payload: {} });
    }

    move(from, to) {
        if (!this.myTurn) {
            console.log("Not your turn!");
            return;
        }

        if (!this.remainingMoves.length) {
            console.log("No moves left! Roll first.");
            return;
        }

        const steps = Math.abs(to - from);
        const direction = this.color === 87 ? 1 : -1;
        if ((to - from) * direction >= 0) {
            console.log("Invalid direction!");
            return;
        }

        if (!this.remainingMoves.includes(steps)) {
            console.log(`Invalid move! You can only move: ${this.remainingMoves.join(", ")}`);
            return;
        }

        this.ws.broadcast({ action: "GAME", type: "move", payload: {color: this.color, from, to} });

        const idx = this.remainingMoves.indexOf(steps);
        this.remainingMoves.splice(idx, 1);
        console.log(this.remainingMoves);

        console.log(`Moved from ${from + 1} to ${to + 1} | Color: ${this.color === 87 ? 'White' : 'Black'}`);

        if (!this.remainingMoves.length) {
            setTimeout(() => {
                this.ws.broadcast({ action: "GAME", type: "advance", payload: {} });
                console.log(`Turn ended. Waiting for opponent... | Color: ${this.color === 87 ? 'White' : 'Black'}`);
            }, 200);
        }
    }
}