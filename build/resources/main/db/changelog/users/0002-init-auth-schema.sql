--liquibase formatted sql

--changeset AVSmirnov:0002-1
CREATE SCHEMA IF NOT EXISTS auth;

--changeset AVSmirnov:0002-2
CREATE TABLE IF NOT EXISTS auth.refresh_token(
    id UUID PRIMARY KEY,
    token_hash varchar NOT NULL UNIQUE, -- если используем UNIQUE, то БД автоматом создает UNIQUE INDEX с btree
    user_id UUID NOT NULL,
    expiry_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL,
    CONSTRAINT refresh_token_user_fk FOREIGN KEY (user_id) REFERENCES users.users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_refresh_token_user_id ON auth.refresh_token(user_id);

