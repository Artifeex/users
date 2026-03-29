--liquibase formatted sql

--changeset AVSmirnov:0006-1
--comment: Таблица для net.javacrumbs.shedlock (JdbcTemplateLockProvider): распределённые блокировки планировщика
CREATE SCHEMA IF NOT EXISTS scheduler;

CREATE TABLE IF NOT EXISTS scheduler.shedlock
(
    name       VARCHAR(64)  NOT NULL PRIMARY KEY, -- Название задачи, которая выполняется, т.к. в одной таблице можем иметь множество блокировок
    lock_until TIMESTAMP    NOT NULL, -- До какого времени блокировка. При попытке занять блокировку будет сравнение lock_until <= NOW()
    locked_at  TIMESTAMP    NOT NULL, -- Когда была занята
    locked_by  VARCHAR(255) NOT NULL -- Кем была занята(hostname из JVM. В Kubernetes будет имя пода)
);
