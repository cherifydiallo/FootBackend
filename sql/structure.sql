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

-- Table de configuration des permissions REST par groupe
CREATE TABLE rest_permission_config (
    id BIGSERIAL PRIMARY KEY,
    permission_key VARCHAR(100) NOT NULL,
    group_id BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(255),
    CONSTRAINT uq_rest_permission_cfg UNIQUE (permission_key, group_id),
    CONSTRAINT fk_rest_permission_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE
);

CREATE INDEX idx_rest_permission_key ON rest_permission_config(permission_key);
CREATE INDEX idx_rest_permission_group_id ON rest_permission_config(group_id);

-- Needed for bcrypt password generation in SQL
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Groupe admin par defaut
INSERT INTO groups (name, description)
VALUES ('admins', 'System administrators with full permissions')
ON CONFLICT (name) DO NOTHING;

-- Utilisateur admin par defaut (identifiant: admin, mot de passe: Start123)
INSERT INTO users (identifiant, password, full_name, email, status, role)
VALUES (
    'admin',
    crypt('Start123', gen_salt('bf')),
    'Default Admin',
    'admin@foot.local',
    'active',
    'admin'
)
ON CONFLICT (identifiant) DO UPDATE SET
    password = EXCLUDED.password,
    full_name = EXCLUDED.full_name,
    email = EXCLUDED.email,
    status = EXCLUDED.status,
    role = EXCLUDED.role;

-- Attacher l'utilisateur admin au groupe admins
INSERT INTO user_groups (user_id, group_id)
SELECT u.id, g.id
FROM users u
JOIN groups g ON g.name = 'admins'
WHERE u.identifiant = 'admin'
ON CONFLICT (user_id, group_id) DO NOTHING;

-- Donner toutes les permissions REST au groupe admins
INSERT INTO rest_permission_config (permission_key, group_id, enabled, description)
SELECT p.permission_key, g.id, TRUE, p.description
FROM (
    VALUES
        ('group_permission_view', 'Admins can view group permissions'),
        ('group_permission_manage', 'Admins can manage group permissions'),
        ('group_create', 'Admins can create groups'),
        ('group_view', 'Admins can list groups'),
        ('group_view_group', 'Admins can view a group detail'),
        ('group_update_group', 'Admins can update groups'),
        ('group_delete_group', 'Admins can delete groups'),
        ('group_add_user', 'Admins can add user to group'),
        ('group_remove_user', 'Admins can remove user from group'),
        ('group_view_members', 'Admins can list group members'),
        ('player_read', 'Admins can read players'),
        ('player_write', 'Admins can write players'),
        ('player_edit', 'Admins can edit players'),
        ('player_delete', 'Admins can delete players'),
        ('profile_view', 'Admins can view profiles'),
        ('user_read', 'Admins can read users'),
        ('user_write', 'Admins can create users'),
        ('user_edit', 'Admins can edit users'),
        ('user_delete', 'Admins can delete users')
) AS p(permission_key, description)
CROSS JOIN (SELECT id FROM groups WHERE name = 'admins') g
ON CONFLICT (permission_key, group_id) DO UPDATE SET
    enabled = EXCLUDED.enabled,
    description = EXCLUDED.description;

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