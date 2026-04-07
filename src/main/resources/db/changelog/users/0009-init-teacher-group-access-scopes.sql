--liquibase formatted sql

--changeset AVSmirnov:0009-1
--comment: Teacher access scopes for student-group search visibility
CREATE TABLE IF NOT EXISTS users.teacher_group_access_scopes (
    teacher_id UUID NOT NULL,
    scope_type VARCHAR(20) NOT NULL,
    scope_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    CONSTRAINT pk_teacher_group_access_scopes PRIMARY KEY (teacher_id, scope_type, scope_id),
    CONSTRAINT fk_teacher_group_access_scopes_teacher FOREIGN KEY (teacher_id)
        REFERENCES users.teacher_profiles (user_id) ON DELETE CASCADE,
    CONSTRAINT chk_teacher_group_access_scopes_scope_type CHECK (
        scope_type IN ('STUDENT_GROUP', 'FIELD_OF_STUDY', 'FACULTY')
    )
);

--changeset AVSmirnov:0009-2
--comment: Indexes for teacher scope retrieval and filtering
CREATE INDEX IF NOT EXISTS idx_tgas_teacher ON users.teacher_group_access_scopes (teacher_id);
CREATE INDEX IF NOT EXISTS idx_tgas_teacher_scope ON users.teacher_group_access_scopes (teacher_id, scope_type, scope_id);
