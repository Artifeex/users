--liquibase formatted sql

--changeset AVSmirnov:0007-1
ALTER TABLE users.fields_of_study DROP COLUMN code;
