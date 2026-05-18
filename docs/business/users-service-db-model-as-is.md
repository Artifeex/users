# Users Service DB Model (As-Is)

## 1. Scope and source of truth

This document describes the current database model of the `users` service based on:
- JPA entities in `src/main/java/**/entity`
- Liquibase changelog in `src/main/resources/db/changelog/users*`

For runtime schema, Liquibase is the source of truth (`spring.jpa.hibernate.ddl-auto=validate`).

## 2. Schemas

The service uses 4 PostgreSQL schemas:
- `users` - core domain (users, roles, hierarchy, profiles, teacher access scopes)
- `auth` - refresh/reset password tokens
- `outbox` - transactional outbox messages
- `scheduler` - ShedLock table for distributed scheduled jobs

## 3. Table-by-table model

## 3.1 Schema `users`

### `users.faculties`
- Purpose: top hierarchy level (faculty/institute).
- Columns:
  - `id BIGINT PK` (sequence-backed after migration `0008`)
  - `name VARCHAR(255) NOT NULL`
  - `short_name VARCHAR(50) NOT NULL`
  - `created_at`, `created_by`, `updated_at`, `updated_by` (audit, NOT NULL)
- Relationships:
  - Referenced by `departments.faculty_id`
  - Referenced by `fields_of_study.faculty_id`
  - Referenced by `student_groups.faculty_id`

### `users.departments`
- Purpose: department within faculty.
- Columns:
  - `id BIGINT PK` (sequence-backed)
  - `name VARCHAR(255) NOT NULL`
  - `faculty_id BIGINT NOT NULL`
  - audit columns
- Constraints:
  - FK `faculty_id -> users.faculties(id)` (`ON DELETE RESTRICT`)
- Indexes:
  - `idx_departments_faculty (faculty_id)`

### `users.fields_of_study`
- Purpose: educational program/direction within faculty.
- Columns:
  - `id BIGINT PK` (sequence-backed)
  - `name VARCHAR(255) NOT NULL`
  - `faculty_id BIGINT NOT NULL`
  - audit columns
- Constraints:
  - FK `faculty_id -> users.faculties(id)` (`ON DELETE RESTRICT`)
- Indexes:
  - `idx_fields_of_study_faculty (faculty_id)`
- History:
  - Column `code` existed initially and was dropped by migration `0007`.

### `users.student_groups`
- Purpose: student group linked to faculty and field of study.
- Columns:
  - `id BIGINT PK` (sequence-backed)
  - `name VARCHAR(50) NOT NULL`
  - `faculty_id BIGINT NOT NULL`
  - `field_of_study_id BIGINT NOT NULL`
  - audit columns
- Constraints:
  - FK `faculty_id -> users.faculties(id)` (`ON DELETE RESTRICT`)
  - FK `field_of_study_id -> users.fields_of_study(id)` (`ON DELETE RESTRICT`)
- Indexes:
  - `idx_students_groups_faculty (faculty_id)`
  - `idx_students_groups_field (field_of_study_id)`

### `users.users`
- Purpose: base user account entity.
- Columns:
  - `id UUID PK`
  - `username VARCHAR(255) NOT NULL UNIQUE`
  - `email VARCHAR(255) UNIQUE`
  - `password VARCHAR(255) NOT NULL`
  - `first_name VARCHAR(100) NOT NULL`
  - `last_name VARCHAR(100) NOT NULL`
  - `middle_name VARCHAR(100) NULL`
  - `is_active BOOLEAN NOT NULL DEFAULT TRUE`
  - `avatar_file_id UUID NULL` (added by migration `0010`)
  - audit columns
- Indexes:
  - `idx_users_email (email)`
  - `idx_users_username (username)`

### `users.roles`
- Purpose: RBAC role dictionary.
- Columns:
  - `id INT PK` (sequence-backed)
  - `name VARCHAR(50) NOT NULL UNIQUE`
- Seeded values (`0001`):
  - `ROLE_STUDENT`
  - `ROLE_TEACHER`
  - `ROLE_ADMIN`

### `users.users_roles`
- Purpose: many-to-many join between users and roles with audit fields.
- Columns:
  - `user_id UUID NOT NULL`
  - `role_id INT NOT NULL`
  - audit columns
- Constraints:
  - PK `(user_id, role_id)`
  - FK `user_id -> users.users(id)` (`ON DELETE CASCADE`)
  - FK `role_id -> users.roles(id)` (`ON DELETE CASCADE`)

### `users.student_profiles`
- Purpose: student extension profile (1:1 with user).
- Columns:
  - `user_id UUID PK`
  - `group_id BIGINT NOT NULL`
  - `department_id BIGINT NULL`
  - audit columns
- Constraints:
  - FK `user_id -> users.users(id)` (`ON DELETE CASCADE`)
  - FK `group_id -> users.student_groups(id)` (`ON DELETE RESTRICT`)
  - FK `department_id -> users.departments(id)` (`ON DELETE SET NULL`)
- Indexes:
  - `idx_student_profiles_group (group_id)`
  - `idx_student_profiles_department (department_id)`

### `users.teacher_profiles`
- Purpose: teacher extension profile (1:1 with user).
- Columns:
  - `user_id UUID PK`
  - `department_id BIGINT NOT NULL`
  - audit columns
- Constraints:
  - FK `user_id -> users.users(id)` (`ON DELETE CASCADE`)
  - FK `department_id -> users.departments(id)` (`ON DELETE RESTRICT`)
- Indexes:
  - `idx_teacher_profiles_department (department_id)`

### `users.teacher_group_access_scopes`
- Purpose: scope-based visibility settings for teacher access to student groups.
- Columns:
  - `teacher_id UUID NOT NULL`
  - `scope_type VARCHAR(20) NOT NULL`
  - `scope_id BIGINT NOT NULL`
  - audit columns
- Constraints:
  - PK `(teacher_id, scope_type, scope_id)`
  - FK `teacher_id -> users.teacher_profiles(user_id)` (`ON DELETE CASCADE`)
  - CHECK `scope_type IN ('STUDENT_GROUP', 'FIELD_OF_STUDY', 'FACULTY')`
- Indexes:
  - `idx_tgas_teacher (teacher_id)`
  - `idx_tgas_teacher_scope (teacher_id, scope_type, scope_id)`

## 3.2 Schema `auth`

### `auth.refresh_token`
- Purpose: persistent refresh token storage (hashed token strategy).
- Columns:
  - `id UUID PK`
  - `token_hash VARCHAR NOT NULL UNIQUE`
  - `user_id UUID NOT NULL`
  - `expiry_at TIMESTAMPTZ NOT NULL`
  - `created_at TIMESTAMPTZ NOT NULL`
- Constraints:
  - FK `user_id -> users.users(id)` (`ON DELETE CASCADE`)
- Indexes:
  - `idx_refresh_token_user_id (user_id)`

### `auth.reset_password_token`
- Purpose: reset-password one-time tokens.
- Columns:
  - `id UUID PK`
  - `token_hash VARCHAR UNIQUE`
  - `user_id UUID NOT NULL`
  - `expiry_at TIMESTAMPTZ NOT NULL`
  - `created_at TIMESTAMPTZ NOT NULL`
- Constraints:
  - FK `user_id -> users.users(id)` (`ON DELETE CASCADE`)

## 3.3 Schema `outbox`

### `outbox.outbox_message`
- Purpose: transactional outbox for integration events.
- Columns:
  - `id UUID PK`
  - `aggregate_type VARCHAR(255) NOT NULL`
  - `aggregate_id VARCHAR(255) NOT NULL`
  - `type VARCHAR(255) NOT NULL`
  - `payload JSONB NOT NULL`
  - `status VARCHAR(50) NOT NULL`
  - `created_at TIMESTAMPTZ DEFAULT NOW()`
  - `processed_at TIMESTAMPTZ NULL`
- Indexes:
  - `idx_outbox_status_created_at (status, created_at)`
- Application enum values (`OutboxStatus`):
  - `NEW`
  - `SENT`

## 3.4 Schema `scheduler`

### `scheduler.shedlock`
- Purpose: distributed lock table for scheduled tasks.
- Columns:
  - `name VARCHAR(64) PK`
  - `lock_until TIMESTAMP NOT NULL`
  - `locked_at TIMESTAMP NOT NULL`
  - `locked_by VARCHAR(255) NOT NULL`

## 4. Entity mapping summary (JPA view)

- `User` (`users.users`) has:
  - one-to-many `UserRole`
  - one-to-one `StudentProfile`
  - one-to-one `TeacherProfile`
- `UserRole` uses embedded key (`user_id`, `role_id`) and maps to `users.users_roles`.
- `StudentProfile` and `TeacherProfile` use shared PK (`user_id`) via `@MapsId`.
- Hierarchy:
  - `Department` -> many-to-one `Faculty`
  - `FieldOfStudy` -> many-to-one `Faculty`
  - `StudentGroup` -> many-to-one `Faculty`, many-to-one `FieldOfStudy`
- `TeacherGroupAccessScope` uses embedded composite key:
  - `teacher_id` + `scope_type` + `scope_id`
- Auth entities:
  - `RefreshToken` -> many-to-one `User`
  - `ResetPasswordToken` -> many-to-one `User`

## 5. Cardinality map (logical)

- Faculty 1 -> N Departments
- Faculty 1 -> N FieldsOfStudy
- Faculty 1 -> N StudentGroups
- FieldOfStudy 1 -> N StudentGroups
- User 1 -> N UserRoles
- Role 1 -> N UserRoles
- User 1 -> 1 StudentProfile (optional by role)
- User 1 -> 1 TeacherProfile (optional by role)
- Department 1 -> N TeacherProfiles
- StudentGroup 1 -> N StudentProfiles
- Department 1 -> N StudentProfiles (optional department for student)
- TeacherProfile 1 -> N TeacherGroupAccessScopes
- User 1 -> N RefreshToken
- User 1 -> N ResetPasswordToken

## 6. Important as-is notes for downstream data description

- PK generation strategy for numeric IDs was switched from identity to explicit sequences in migration `0008` (`allocationSize=50` in entities).
- `fields_of_study.code` is not part of the current model (dropped in `0007`).
- `users.users.avatar_file_id` exists and is nullable.
- Audit columns are mandatory in most `users.*` tables through `AuditableEntity`.
- `scope_type` in teacher scopes is constrained both by SQL CHECK and Java enum.
- Outbox and ShedLock are infrastructure tables but part of the same service database and important for consistency/reliability behavior.
- `StudentProfile.department` is mapped in JPA as `@OneToOne`, but DB schema does not enforce uniqueness on `student_profiles.department_id`; physically this relation behaves as many students to one department.

## 7. Initial seed data

Migration `0003` inserts initial users and role links (idempotent via `ON CONFLICT DO NOTHING`):
- `admin` -> `ROLE_ADMIN`
- `teacher` -> `ROLE_TEACHER`
- `student` -> `ROLE_STUDENT`

