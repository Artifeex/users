--liquibase formatted sql

--changeset AVSmirnov:0005-1
CREATE SCHEMA IF NOT EXISTS outbox;

CREATE TABLE IF NOT EXISTS outbox.outbox_message
(
    id             UUID PRIMARY KEY,                       -- Уникальный ID сообщения (важно для дедупликации)
    aggregate_id   VARCHAR(255) NOT NULL,                  -- ID самой сущности (например, ID юзера)
    type           VARCHAR(255) NOT NULL,                  -- Тип события (например, "UserCreatedEvent")
    payload        JSONB        NOT NULL,                  -- Само сообщение в формате JSON
    status         VARCHAR(50)  NOT NULL,                  -- Статус (NEW, PROCESSED, ERROR)
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW(), -- Время создания
    processed_at   TIMESTAMP WITH TIME ZONE                -- Время успешной отправки
);

-- Индекс для быстрого поиска неотправленных сообщений
CREATE INDEX IF NOT EXISTS idx_outbox_status_created_at ON outbox.outbox_message (status, created_at);

