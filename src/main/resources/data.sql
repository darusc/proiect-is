-- ============================================================================
-- 1. PLAYER DATA
-- ============================================================================

INSERT INTO player (id, username, email, password)
VALUES (1, 'ProGamer99', 'progamer@example.com', 'password123');

INSERT INTO player (id, username, email, password)
VALUES (2, 'StrategyQueen', 'queen@example.com', 'securepass456');

INSERT INTO player (id, username, email, password)
VALUES (3, 'NoobMaster69', 'noob@example.com', '123456');


-- ============================================================================
-- 2. RANKING DATA
-- ============================================================================

-- Ranking for ProGamer99 (ID: 1)
INSERT INTO ranking (player_id, wins, losses, win_rate, total_points)
VALUES (1, 50, 10, 0.83, 1200);

-- Ranking for StrategyQueen (ID: 2)
INSERT INTO ranking (player_id, wins, losses, win_rate, total_points)
VALUES (2, 100, 5, 0.95, 2500);

-- Ranking for NoobMaster69 (ID: 3)
INSERT INTO ranking (player_id, wins, losses, win_rate, total_points)
VALUES (3, 2, 20, 0.09, 150);

-- ============================================================================
-- Reset auto-increment counter to 4
-- ============================================================================
ALTER TABLE player AUTO_INCREMENT = 4;

-- ============================================================================
-- 3. MATCH DATA
-- ============================================================================

-- Match 1: ProGamer99 (1) vs StrategyQueen (2) -> StrategyQueen Wins
INSERT INTO matches (player1_id, player2_id, winner_id, score_player1, score_player2, played_at)
VALUES (1, 2, 2, 12, 25, '2023-11-01 14:30:00');

-- Match 2: ProGamer99 (1) vs NoobMaster69 (3) -> ProGamer99 Wins (Stomp)
INSERT INTO matches (player1_id, player2_id, winner_id, score_player1, score_player2, played_at)
VALUES (1, 3, 1, 50, 0, '2023-11-02 09:15:00');

-- Match 3: StrategyQueen (2) vs NoobMaster69 (3) -> StrategyQueen Wins
INSERT INTO matches (player1_id, player2_id, winner_id, score_player1, score_player2, played_at)
VALUES (2, 3, 2, 30, 10, '2023-11-03 18:45:00');

-- Match 4: A rematch! StrategyQueen (2) vs ProGamer99 (1) -> Close game, ProGamer Wins
INSERT INTO matches (player1_id, player2_id, winner_id, score_player1, score_player2, played_at)
VALUES (2, 1, 1, 48, 50, '2023-11-04 20:00:00');