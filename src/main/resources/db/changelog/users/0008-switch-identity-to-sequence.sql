--liquibase formatted sql

--changeset AVSmirnov:0008-1
--comment: Switch identity PKs to explicit sequences for Hibernate SEQUENCE strategy
ALTER TABLE users.faculties ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE users.departments ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE users.fields_of_study ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE users.student_groups ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE users.roles ALTER COLUMN id DROP IDENTITY IF EXISTS;

CREATE SEQUENCE IF NOT EXISTS users.faculties_id_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS users.departments_id_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS users.fields_of_study_id_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS users.student_groups_id_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS users.roles_id_seq START WITH 1 INCREMENT BY 50;

ALTER SEQUENCE users.faculties_id_seq OWNED BY users.faculties.id;
ALTER SEQUENCE users.departments_id_seq OWNED BY users.departments.id;
ALTER SEQUENCE users.fields_of_study_id_seq OWNED BY users.fields_of_study.id;
ALTER SEQUENCE users.student_groups_id_seq OWNED BY users.student_groups.id;
ALTER SEQUENCE users.roles_id_seq OWNED BY users.roles.id;

ALTER TABLE users.faculties ALTER COLUMN id SET DEFAULT nextval('users.faculties_id_seq');
ALTER TABLE users.departments ALTER COLUMN id SET DEFAULT nextval('users.departments_id_seq');
ALTER TABLE users.fields_of_study ALTER COLUMN id SET DEFAULT nextval('users.fields_of_study_id_seq');
ALTER TABLE users.student_groups ALTER COLUMN id SET DEFAULT nextval('users.student_groups_id_seq');
ALTER TABLE users.roles ALTER COLUMN id SET DEFAULT nextval('users.roles_id_seq');

SELECT setval('users.faculties_id_seq', COALESCE((SELECT MAX(id) FROM users.faculties), 0) + 1, false);
SELECT setval('users.departments_id_seq', COALESCE((SELECT MAX(id) FROM users.departments), 0) + 1, false);
SELECT setval('users.fields_of_study_id_seq', COALESCE((SELECT MAX(id) FROM users.fields_of_study), 0) + 1, false);
SELECT setval('users.student_groups_id_seq', COALESCE((SELECT MAX(id) FROM users.student_groups), 0) + 1, false);
SELECT setval('users.roles_id_seq', COALESCE((SELECT MAX(id) FROM users.roles), 0) + 1, false);
