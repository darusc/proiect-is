-- ===========================
-- CREARE TABELE
-- ===========================

DROP TABLE IF EXISTS matches;
DROP TABLE IF EXISTS rankings;
DROP TABLE IF EXISTS players;

CREATE TABLE players
(
    id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50)  NOT NULL UNIQUE,
    email    VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    level    INT          NOT NULL,
    score    INT          NOT NULL
);

CREATE TABLE matches
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    player1_id    BIGINT NOT NULL,
    player2_id    BIGINT NOT NULL,
    winner_id     BIGINT NOT NULL,
    score_player1 INT    NOT NULL,
    score_player2 INT    NOT NULL,
    played_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    history       TEXT,

    FOREIGN KEY (player1_id) REFERENCES players (id),
    FOREIGN KEY (player2_id) REFERENCES players (id),
    FOREIGN KEY (winner_id) REFERENCES players (id)
);

CREATE TABLE rankings
(
    player_id    BIGINT PRIMARY KEY,
    wins         INT DEFAULT 0,
    losses       INT DEFAULT 0,
    win_rate DOUBLE DEFAULT 0,
    total_points INT DEFAULT 0,

    FOREIGN KEY (player_id) REFERENCES players (id)
);

-- ===========================
-- POPULARE CU DATE DE TEST
-- ===========================

INSERT INTO players (username, email, password, level, score)
VALUES ('alex', 'alex@example.com', 'pass123', 5, 1200),
       ('maria', 'maria@example.com', 'pass123', 4, 1100),
       ('ionut', 'ionut@example.com', 'pass123', 3, 950),
       ('george', 'george@example.com', 'pass123', 6, 1400),
       ('andreea', 'andreea@example.com', 'pass123', 2, 700),
       ('cosmin', 'cosmin@example.com', 'pass123', 3, 900),
       ('mihai', 'mihai@example.com', 'pass123', 1, 500),
       ('laura', 'laura@example.com', 'pass123', 2, 650),
       ('cristina', 'cristina@example.com', 'pass123', 4, 1150),
       ('tudor', 'tudor@example.com', 'pass123', 5, 1250);

INSERT INTO matches (player1_id, player2_id, winner_id, score_player1, score_player2, played_at, history)
VALUES (1, 2, 1, 5, 3, NOW() - INTERVAL 10 DAY, '["move1","move2","move3"]'),
       (3, 1, 3, 5, 4, NOW() - INTERVAL 8 DAY, '["move1","move2"]'),
       (4, 2, 4, 5, 1, NOW() - INTERVAL 7 DAY, '["move1","move2","move3","move4"]'),
       (5, 3, 3, 5, 2, NOW() - INTERVAL 6 DAY, null),
       (6, 7, 6, 5, 0, NOW() - INTERVAL 5 DAY, null),
       (8, 9, 9, 5, 4, NOW() - INTERVAL 4 DAY, null),
       (10, 1, 10, 5, 3, NOW() - INTERVAL 3 DAY, null),
       (2, 3, 2, 5, 4, NOW() - INTERVAL 2 DAY, null),
       (4, 1, 4, 5, 2, NOW() - INTERVAL 1 DAY, null),
       (3, 10, 10, 5, 4, NOW(), null);

INSERT INTO rankings (player_id, wins, losses, win_rate, total_points)
VALUES (1, 15, 8, 15.0 / 23, 450),
       (2, 12, 10, 12.0 / 22, 360),
       (3, 18, 7, 18.0 / 25, 540),
       (4, 22, 5, 22.0 / 27, 660),
       (5, 4, 11, 4.0 / 15, 120),
       (6, 6, 9, 6.0 / 15, 180),
       (7, 3, 12, 3.0 / 15, 90),
       (8, 5, 10, 5.0 / 15, 150),
       (9, 14, 9, 14.0 / 23, 420),
       (10, 17, 6, 17.0 / 23, 510);
