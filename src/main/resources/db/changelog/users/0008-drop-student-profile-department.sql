--liquibase formatted sql

--changeset AVSmirnov:0008-1
DROP INDEX IF EXISTS users.idx_student_profiles_department;
ALTER TABLE users.student_profiles DROP CONSTRAINT IF EXISTS fk_student_profile_dept;
ALTER TABLE users.student_profiles DROP COLUMN IF EXISTS department_id;
