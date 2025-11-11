class Game {

    constructor(playerId) {
        this.playerId = playerId;
        this.ws = new Ws(playerId, msg => this.onMessage(msg));
        this.board = null;
        this.myTurn = false;
        this.dice = [];
        this.color = null;
        this.remainingMoves = [];

        // DOM
        this.boardDiv = document.getElementById("board");
        this.statusDiv = document.getElementById("status");
        this.diceDiv = document.getElementById("dice");
        this.turnDiv = document.getElementById("turn");
    }

    joinRoom(room) {
        this.ws.join(room);
        this.setStatus("Joining room " + room + "...");
    }

    onMessage(event) {
        const data = JSON.parse(event.data);
        console.log("WS:", data);

        switch (data.type) {

            case "game_start":
                this.color = data['payload']['white'] === this.playerId ? 87 : 66;
                document.getElementById("playerColor").innerHTML = `Color: ${this.color === 87 ? 'White' : 'Black'}`;
                break;

            case "roll_result":
                this.dice = data['payload']['dice'];
                this.remainingMoves = this.dice[0] === this.dice[1]
                    ? [this.dice[0], this.dice[0], this.dice[0], this.dice[0]]
                    : [this.dice[0], this.dice[1]];

                this.diceDiv.innerText = (this.dice.join("  "));
                break;

            case "state":
                const turn = data['payload']['turn'];
                this.myTurn = turn === this.color;
                this.turnDiv.innerText = this.myTurn ? "Your turn!" : "Opponent turn";

                this.board = data['payload']['board'];
                const normalized = this.normalizeBoard(this.board);
                this.renderBoard(normalized);
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

        // Păstrăm turul și culoarea
        console.log(`Moved from ${from + 1} to ${to + 1} | Color: ${this.color === 87 ? 'White' : 'Black'}`);
        this.renderRemainingMoves();

        if (!this.remainingMoves.length) {
            setTimeout(() => {
                this.ws.broadcast({ action: "GAME", type: "advance", payload: {} });
                console.log(`Turn ended. Waiting for opponent... | Color: ${this.color === 87 ? 'White' : 'Black'}`);
                this.renderRemainingMoves();
            }, 200);
        }
    }

    renderRemainingMoves() {
        const remEl = document.getElementById("remainingMoves");
        remEl.innerHTML = "Moves left: " + this.remainingMoves.map((d) => `<span style="color: yellow; font-weight: bold;">${d}</span>`).join("  ");
    }
    
    setStatus(text) {
        this.statusDiv.innerText = text;
    }

    renderBoard(board) {
        const boardEl = this.boardDiv;
        if (!board || board.length !== 24) return;

        const top = board.slice(12, 24);
        const bottom = board.slice(0, 12).reverse();
        const rows = 5;
        let out = "";
        out += "    13  14  15  16  17  18    19  20  21  22  23  24\n";
        out += "    ------------------------------------------------\n";

        for (let r = rows - 1; r >= 0; r--) {
            out += "  |";
            for (let i = 0; i < 12; i++) {
                const [type, count] = top[i];
                out += " " + (count > r ? type : ".") + "  ";
                if (i === 5) out += "|";
            }
            out += "|\n";
        }

        out += "    ------------------------------------------------\n";

        for (let r = 0; r < rows; r++) {
            out += "  |";
            for (let i = 0; i < 12; i++) {
                const [type, count] = bottom[i];
                out += " " + (count > r ? type : ".") + "  ";
                if (i === 5) out += "|";
            }
            out += "|\n";
        }

        out += "    ------------------------------------------------\n";
        out += "     12  11  10  9   8   7    6   5   4   3   2   1";

        boardEl.innerText = out;
    }

    normalizeBoard(board) {
        return board.map(([type, count]) => {
            let t;
            if (type === 87) t = "W";
            else if (type === 66) t = "B";
            else t = ".";
            return [t, count];
        });
    }
}
