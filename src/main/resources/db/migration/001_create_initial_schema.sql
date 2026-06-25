-- Create Teams table
CREATE TABLE IF NOT EXISTS teams (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    country_code VARCHAR(3) NOT NULL UNIQUE,
    confederation VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create Groups table
CREATE TABLE IF NOT EXISTS groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    stage VARCHAR(50) NOT NULL, -- GROUP_STAGE, KNOCKOUT
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create Group Teams (Many-to-Many relationship)
CREATE TABLE IF NOT EXISTS group_teams (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    group_position INT,
    wins INT DEFAULT 0,
    losses INT DEFAULT 0,
    draws INT DEFAULT 0,
    goals_for INT DEFAULT 0,
    goals_against INT DEFAULT 0,
    goal_difference INT DEFAULT 0,
    points INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_group_teams_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_group_teams_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    UNIQUE KEY unique_group_team (group_id, team_id),
    INDEX idx_group_teams_group (group_id),
    INDEX idx_group_teams_team (team_id)
);

-- Create Players table
CREATE TABLE IF NOT EXISTS players (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    jersey_number INT NOT NULL,
    position VARCHAR(50), -- GK, DEF, MID, FWD
    date_of_birth DATE,
    height INT, -- in cm
    weight INT, -- in kg
    international_caps INT DEFAULT 0,
    goals_in_career INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_players_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    UNIQUE KEY unique_player_team_jersey (team_id, jersey_number),
    INDEX idx_players_team (team_id),
    INDEX idx_players_name (name)
);

-- Create Matches table
CREATE TABLE IF NOT EXISTS matches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    home_team_id BIGINT NOT NULL,
    away_team_id BIGINT NOT NULL,
    group_id BIGINT,
    match_type VARCHAR(50) NOT NULL, -- GROUP_MATCH, KNOCKOUT, FINAL
    match_date DATETIME NOT NULL,
    venue VARCHAR(200),
    home_team_goals INT,
    away_team_goals INT,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED', -- SCHEDULED, ONGOING, COMPLETED
    home_team_possession_percentage DECIMAL(5, 2),
    away_team_possession_percentage DECIMAL(5, 2),
    home_team_shots INT,
    away_team_shots INT,
    home_team_shots_on_target INT,
    away_team_shots_on_target INT,
    home_team_fouls INT,
    away_team_fouls INT,
    home_team_yellow_cards INT,
    away_team_yellow_cards INT,
    home_team_red_cards INT,
    away_team_red_cards INT,
    home_team_corners INT,
    away_team_corners INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_matches_home_team FOREIGN KEY (home_team_id) REFERENCES teams(id),
    CONSTRAINT fk_matches_away_team FOREIGN KEY (away_team_id) REFERENCES teams(id),
    CONSTRAINT fk_matches_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE SET NULL,
    CONSTRAINT check_different_teams CHECK (home_team_id != away_team_id),
    INDEX idx_matches_home_team (home_team_id),
    INDEX idx_matches_away_team (away_team_id),
    INDEX idx_matches_match_date (match_date),
    INDEX idx_matches_status (status),
    INDEX idx_matches_group (group_id)
);

-- Create Goals table
CREATE TABLE IF NOT EXISTS goals (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    match_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    scoring_team_id BIGINT NOT NULL,
    minute INT NOT NULL,
    goal_type VARCHAR(50) NOT NULL, -- REGULAR, PENALTY, OWN_GOAL, HEADER, FREE_KICK
    is_penalty_goal BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_goals_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE,
    CONSTRAINT fk_goals_player FOREIGN KEY (player_id) REFERENCES players(id),
    CONSTRAINT fk_goals_team FOREIGN KEY (scoring_team_id) REFERENCES teams(id),
    INDEX idx_goals_match (match_id),
    INDEX idx_goals_player (player_id),
    INDEX idx_goals_team (scoring_team_id)
);

-- Create Match Statistics table
CREATE TABLE IF NOT EXISTS match_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    match_id BIGINT NOT NULL UNIQUE,
    home_team_id BIGINT NOT NULL,
    away_team_id BIGINT NOT NULL,
    home_team_possession DECIMAL(5, 2),
    away_team_possession DECIMAL(5, 2),
    home_team_passes INT,
    away_team_passes INT,
    home_team_pass_accuracy DECIMAL(5, 2),
    away_team_pass_accuracy DECIMAL(5, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_stats_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE,
    CONSTRAINT fk_stats_home_team FOREIGN KEY (home_team_id) REFERENCES teams(id),
    CONSTRAINT fk_stats_away_team FOREIGN KEY (away_team_id) REFERENCES teams(id),
    INDEX idx_stats_match (match_id)
);

-- Create Indexes for better performance
CREATE INDEX idx_goals_minute ON goals(minute);
CREATE INDEX idx_matches_completed ON matches(status, match_date);
CREATE INDEX idx_group_teams_standings ON group_teams(group_id, points DESC, goal_difference DESC);
CREATE INDEX idx_players_position ON players(position);

