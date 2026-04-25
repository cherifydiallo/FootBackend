-- Création du type ENUM pour les rôles utilisateur
CREATE TYPE user_role AS ENUM ('admin', 'standard');

-- Création de la table users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    identifiant VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    status VARCHAR(50) DEFAULT 'active',
    role user_role NOT NULL,
    joined_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_active TIMESTAMP NULL
);

-- Index pour accélérer les recherches
CREATE INDEX idx_users_identifiant ON users(identifiant);
CREATE INDEX idx_users_email ON users(email);
-- Création de la table groups
CREATE TABLE groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(500),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index pour accélérer les recherches sur les groupes
CREATE INDEX idx_groups_name ON groups(name);

-- Création de la table de liaison user_groups (many-to-many)
CREATE TABLE user_groups (
    user_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, group_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE
);

-- Index pour accélérer les recherches dans la table de liaison
CREATE INDEX idx_user_groups_user_id ON user_groups(user_id);
CREATE INDEX idx_user_groups_group_id ON user_groups(group_id);

-- Création de la table players
CREATE TABLE players (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100),
    birth_date DATE,
    academy VARCHAR(50),
    category VARCHAR(10),
    register_number VARCHAR(50) UNIQUE,
    height_cm INT,
    weight_kg INT,
    father_name VARCHAR(100),
    mother_name VARCHAR(100),
    photo VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_players_register_number ON players(register_number);