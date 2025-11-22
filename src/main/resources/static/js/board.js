class Board {

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
            capturedPieces: this.#renderCapturedPieces.bind(this),
            highlightReentry: this.#highlightReentry.bind(this)
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

    /**
     * @param {...Element} triangle
     */
    #onTriangleClick(triangle) {
        const point = parseInt(triangle.dataset['point']);

        const taken = (game.color === Game.WHITE) ? game.whitesTaken : game.blacksTaken;

        if (taken > 0 && triangle.classList.contains('highlight') && triangle.classList.contains('reentry')) {
            game.reenter(point - 1);
            this.#clearHighlight();
            this.selectedTriangle = null;
            return;
        }

        if (this.selectedTriangle && this.#isTriangleHighlighted(triangle)) {
            const from = parseInt(this.selectedTriangle.dataset['point']);
            const to = point;

            const pieces = this.#getPieces(this.selectedTriangle);
            if (pieces.length === 0 || this.#getPieceColor(pieces[0]) !== game.color) {
                return;
            }

            game.move(from - 1, to - 1);

            this.#clearHighlight();
            this.selectedTriangle = null;
        } else {
            this.selectedTriangle = triangle;
            this.#clearHighlight();
            this.#highlightPossibleMoves(triangle);
        }
    }

    #highlightPossibleMoves(triangle) {
        const from = parseInt(triangle.dataset['point']);
        const direction = game.color === Game.WHITE ? -1 : 1;

        game.remainingMoves.forEach(offset => {
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
            } else {
                const color = this.#getPieceColor(pieces[0]);
                if (color === game.color || pieces.length === 1) {
                    destTriangle.classList.add('highlight');
                }
            }
        });
    }

    #highlightReentry() {
        const taken = game.color === Game.WHITE ? game.whitesTaken : game.blacksTaken;
        if (taken === 0) {
            return;
        }

        game.remainingMoves.forEach(offset => {
            const dest = game.color === Game.WHITE ? 24 - offset + 1 : offset;
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
                if (color === game.color || pieces.length === 1) {
                    destTriangle.classList.add('highlight', 'reentry');
                }
            }
        });
    }

    #clearHighlight() {
        this.triangles.forEach(t => t.classList.remove('highlight', 'reentry'));
    }

    #renderBoard(board) {
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

        if (game.color === 66) {
            document.querySelector('.board').classList.add('flip');
        }
    }

    /**
     *
     * @param dice
     * @param remainingMoves
     */
    #renderDice(dice, remainingMoves) {
        document.querySelectorAll('.dice').forEach(e => {
            e.classList.remove('visible', 'double', 'grayed');
        });

        const rollButton = document.querySelector('.roll');
        const containers = document.querySelectorAll('.dice-container');

        // Afiseaza butonul de dat cu zarul
        // si dezactiveaza-l pentru oponent
        if (dice[0] === 0 && dice[1] === 0) {
            rollButton.style.display = 'block';
            if (!game.myTurn) {
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

    #renderCapturedPieces(whites, blacks) {
        const boxW = document.querySelector('.box.w');
        const boxB = document.querySelector('.box.b');

        boxW.innerHTML = "";
        boxB.innerHTML = "";

        for (let i = 0; i < whites; i++) {
            const captured = document.createElement('span');
            captured.classList.add('captured-piece', 'w');
            boxW.appendChild(captured);
        }

        for (let i = 0; i < blacks; i++) {
            const captured = document.createElement('span');
            captured.classList.add('captured-piece', 'b');
            boxB.appendChild(captured);
        }
    }
}