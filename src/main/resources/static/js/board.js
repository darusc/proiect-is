class Board {

    onMove = () => {};
    onReentry = () => {};
    onRemove = () => {};

    /**
     * Functie folosita pentru a obtine date necesare pentru highlight
     * Trebuie sa returneze un array de forma [remainingMoves, currentTurn, color]
     */
    highlightConfigGetter = () => []

    constructor() {
        /**
         * @type {Element}
         */
        this.selectedTriangle = null;
        this.triangles = document.querySelectorAll('.triangle');

        this.triangles.forEach(t => {
            t.addEventListener("click", () => this.#onTriangleClick(t));
        });
    }

    getRenderer() {
        return {
            board: this.#renderBoard.bind(this),
            dice: this.#renderDice.bind(this),
            barPieces: this.#renderBarPieces.bind(this),
            highlightReentry: this.#highlightReentry.bind(this),
            highlightRemove: this.#highlightRemove.bind(this)
        }
    }

    /**
     * @param {...Element} piece
     * @returns {number}
     */
    #getPieceColor(piece) {
        return piece.classList.contains('w') ? Game.WHITE : Game.BLACK;
    }

    /**
     * @param {...Element} triangle
     */
    #getPieces(triangle) {
        return triangle.querySelectorAll('.piece');
    }

    /**
     * @param {...Element} triangle
     */
    #isTriangleHighlighted(triangle) {
        return triangle.classList.contains('highlight');
    }

    #isTriangleHighlightedForReentry(triangle) {
        return triangle.classList.contains('highlight') && triangle.classList.contains('reentry');
    }

    #isTriangleHighlightedForRemoval(triangle) {
        return triangle.classList.contains('highlight') && triangle.classList.contains('remove');
    }

    /**
     * @param {...Element} triangle
     */
    #onTriangleClick(triangle) {
        const [remainingMoves, color, myTurn] = this.highlightConfigGetter()
        const point = parseInt(triangle.dataset['point']);

        if(!myTurn) {
            return;
        }

        if (this.#isTriangleHighlightedForReentry(triangle)) {
            this.onReentry(point - 1);
            this.#clearHighlight();
            return;
        }

        if (this.#isTriangleHighlightedForRemoval(triangle)) {
            this.onRemove(point - 1);
            this.#clearHighlight();
            return;
        }

        if (this.selectedTriangle && this.#isTriangleHighlighted(triangle)) {
            const from = parseInt(this.selectedTriangle.dataset['point']);
            this.onMove(from - 1, point - 1);
            this.#clearHighlight();
            return;
        }

        this.#clearHighlight();

        const pieces = triangle.querySelectorAll('.piece');
        if(this.#getPieceColor(pieces[0]) === color) {
            this.selectedTriangle = triangle;
            this.#highlightPossibleMoves(triangle, remainingMoves, color);
        }
    }

    #highlightPossibleMoves(triangle, remainingMoves, color) {
        const from = parseInt(triangle.dataset['point']);
        const direction = color === Game.WHITE ? -1 : 1;

        remainingMoves.forEach(offset => {
            const to = from + offset * direction;
            if (to < 1 || to > 24) {
                return;
            }

            const destTriangle = document.querySelector(`.triangle[data-point="${to}"]`);
            if (!destTriangle) {
                return;
            }

            const pieces = destTriangle.querySelectorAll('.piece');

            if (pieces.length === 0) {
                destTriangle.classList.add('highlight');
            } else if (this.#getPieceColor(pieces[0]) === color || pieces.length === 1) {
                destTriangle.classList.add('highlight');
            }
        });
    }

    #highlightReentry(remainingMoves, turn) {
        remainingMoves.forEach(offset => {
            const dest = turn === Game.WHITE ? 24 - offset + 1 : offset;
            if (dest < 1 || dest > 24) {
                return;
            }

            const destTriangle = document.querySelector(`.triangle[data-point="${dest}"]`);
            if (!destTriangle) {
                return;
            }

            const pieces = destTriangle.querySelectorAll('.piece');

            if (pieces.length === 0) {
                destTriangle.classList.add('highlight', 'reentry');
            } else {
                const color = this.#getPieceColor(pieces[0]);
                if (color === turn || pieces.length === 1) {
                    destTriangle.classList.add('highlight', 'reentry');
                }
            }
        });
    }

    #highlightRemove(remainingMoves, turn) {
        remainingMoves.forEach(offset => {
            const dest = turn === Game.WHITE ? offset : 24 - offset + 1;
            if (dest < 1 || dest > 24) {
                return;
            }

            const destTriangle = document.querySelector(`.triangle[data-point="${dest}"]`);
            if (!destTriangle) {
                return;
            }

            // Verifica daca se poate scoate de pe pozitia data de zar
            const pieces = destTriangle.querySelectorAll('.piece');
            if (pieces.length > 0 && this.#getPieceColor(pieces[0]) === turn) {
                destTriangle.classList.add('highlight', 'remove');
            } else {
                // Cauta prima pozitia valida de pe care se poate scoate de dupa zar
                // (Cea mai departata de 1 pentru alb, si cea mai departata de 24 pentru negru)
                if(turn === Game.WHITE) {
                    for(let pos = offset - 1; pos > 0; pos--) {
                        const t = document.querySelector(`.triangle[data-point="${pos}"]`);
                        const pcs = t.querySelectorAll('.piece');
                        if(pcs.length > 0 && this.#getPieceColor(pcs[0]) === turn) {
                            t.classList.add('highlight', 'remove');
                            break;
                        }
                    }
                } else {
                    for(let pos = 24 - offset; pos <= 24; pos++) {
                        const t = document.querySelector(`.triangle[data-point="${pos}"]`);
                        const pcs = t.querySelectorAll('.piece');
                        if(pcs.length > 0 && this.#getPieceColor(pcs[0]) === turn) {
                            t.classList.add('highlight', 'remove');
                            break;
                        }
                    }
                }
            }
        });
    }

    #clearHighlight() {
        this.selectedTriangle = null;
        this.triangles.forEach(t => t.classList.remove('highlight', 'reentry', 'remove'));
    }

    #renderBoard(board, turn) {
        this.triangles.forEach(t => t.innerHTML = "");
        for (let i = 0; i < board.length; i++) {
            const row = board[i];
            const piece = row[0];
            const count = row[1];

            const triangle = Array.from(this.triangles).find(t => t.dataset.point == (i + 1).toString());

            for (let i = 0; i < count; i++) {
                const pieceElement = document.createElement('span');
                pieceElement.classList.add('piece', piece);
                pieceElement.classList.add(piece === 87 ? 'w' : 'b');
                triangle.appendChild(pieceElement);
            }
        }

        if (turn === Game.BLACK) {
            document.querySelector('.board').classList.add('flip');
        }
    }

    /**
     * @param {...[]} dice
     * @param {...[]} remainingMoves
     * @param {...boolean} myTurn
     */
    #renderDice(dice, remainingMoves, myTurn) {
        document.querySelectorAll('.dice').forEach(e => {
            e.classList.remove('visible', 'double', 'grayed');
        });

        const rollButton = document.querySelector('.roll');
        const containers = document.querySelectorAll('.dice-container');

        // Afiseaza butonul de dat cu zarul
        // si dezactiveaza-l pentru oponent
        if (dice[0] === 0 && dice[1] === 0) {
            rollButton.style.display = 'block';
            if (!myTurn) {
                rollButton.classList.add('grayed');
                rollButton.disabled = true;
            } else {
                rollButton.classList.remove('grayed');
                rollButton.disabled = false;
            }
            return;
        }

        // Daca avem valori pentru zar, ascunde butonul
        rollButton.style.display = 'none';

        const dice1 = containers[0].querySelector(`.dice-${dice[0]}`);
        const dice2 = containers[1].querySelector(`.dice-${dice[1]}`);

        dice1.classList.add('visible');
        dice2.classList.add('visible');

        // Coloreaza zaruile in functie de cate mutari au mai ramas
        const isDouble = dice[0] === dice[1] && dice[0] !== 0;
        const remaining = remainingMoves.length;
        if (isDouble) {
            if (remaining === 4) {
                dice1.classList.add('double');
                dice2.classList.add('double');
            } else if (remaining === 3) {
                dice2.classList.add('double');
            } else if (remaining === 1) {
                dice1.classList.add('grayed');
            }
        } else {
            if (!remainingMoves.includes(dice[0])) dice1.classList.add('grayed');
            if (!remainingMoves.includes(dice[1])) dice2.classList.add('grayed');
        }
    }

    #renderBarPieces(captured, removed) {
        const boxW = document.querySelector('.box.w');
        const boxB = document.querySelector('.box.b');

        boxW.innerHTML = "";
        boxB.innerHTML = "";

        // Piesele albe scoase de jucatorul alb
        for (let i = 0; i < removed['whites']; i++) {
            const captured = document.createElement('span');
            captured.classList.add('captured-piece', 'w');
            boxW.appendChild(captured);
        }

        // Piesele negre capturate de jucatorul alb
        for(let i = 0; i < captured['blacks']; i++) {
            const captured = document.createElement('span');
            captured.classList.add('captured-piece', 'b');
            boxW.appendChild(captured);
        }

        // Piesele negre scoase de jucatorul negru
        for (let i = 0; i < removed['blacks']; i++) {
            const captured = document.createElement('span');
            captured.classList.add('captured-piece', 'b');
            boxB.appendChild(captured);
        }

        // Piesele albe capturate de jucatorul negru
        for(let i = 0; i < captured['whites']; i++) {
            const captured = document.createElement('span');
            captured.classList.add('captured-piece', 'w');
            boxW.appendChild(captured);
        }
    }
}