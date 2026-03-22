--liquibase formatted sql

--changeset AVSmirnov:0004-1
CREATE TABLE IF NOT EXISTS auth.reset_password_token(
    id UUID PRIMARY KEY,
    token_hash VARCHAR UNIQUE,
    user_id VARCHAR NOT NULL,
    expiry_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL,
    CONSTRAINT reset_password_token_users_id_fk FOREIGN KEY (user_id) REFERENCES users.users(id) ON DELETE CASCADE
);
