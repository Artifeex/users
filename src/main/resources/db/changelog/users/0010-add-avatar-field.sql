--liquibase formatted sql

--changeset AVSmirnov:0010-1
ALTER TABLE users.users
ADD COLUMN IF NOT EXISTS avatar_file_id uuid