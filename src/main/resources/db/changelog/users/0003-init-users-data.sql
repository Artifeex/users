--liquibase formatted sql

--changeset AVSmirnov:0003-1
--comment: Initial application users with base roles

INSERT INTO users.users (id,
                         username,
                         email,
                         password,
                         first_name,
                         last_name,
                         middle_name,
                         is_active,
                         created_at,
                         created_by,
                         updated_at,
                         updated_by)
VALUES
    -- Admin user
    ('11111111-1111-1111-1111-111111111111',
     'admin',
     'admin@example.com',
     '{noop}password', -- {bcrypt} hash for "password" (Spring DelegatingPasswordEncoder)
     'System',
     'Administrator',
     NULL,
     TRUE,
     NOW(),
     'system',
     NOW(),
     'system'),
    -- Teacher user
    ('22222222-2222-2222-2222-222222222222',
     'teacher',
     'teacher@example.com',
     '{noop}password', -- {bcrypt} hash for "password" (Spring DelegatingPasswordEncoder)
     'Default',
     'Teacher',
     NULL,
     TRUE,
     NOW(),
     'system',
     NOW(),
     'system'),
    -- Student user
    ('33333333-3333-3333-3333-333333333333',
     'student',
     'student@example.com',
     '{noop}password', -- {bcrypt} hash for "password" (Spring DelegatingPasswordEncoder)
     'Default',
     'Student',
     NULL,
     TRUE,
     NOW(),
     'system',
     NOW(),
     'system')
ON CONFLICT DO NOTHING;


--changeset AVSmirnov:0003-2
--comment: Link initial users to their roles

INSERT INTO users.users_roles (user_id,
                               role_id,
                               created_at,
                               created_by,
                               updated_at,
                               updated_by)
VALUES
    -- Admin -> ROLE_ADMIN
    ('11111111-1111-1111-1111-111111111111',
     (SELECT id FROM users.roles WHERE name = 'ROLE_ADMIN'),
     NOW(),
     'system',
     NOW(),
     'system'),
    -- Teacher -> ROLE_TEACHER
    ('22222222-2222-2222-2222-222222222222',
     (SELECT id FROM users.roles WHERE name = 'ROLE_TEACHER'),
     NOW(),
     'system',
     NOW(),
     'system'),
    -- Student -> ROLE_STUDENT
    ('33333333-3333-3333-3333-333333333333',
     (SELECT id FROM users.roles WHERE name = 'ROLE_STUDENT'),
     NOW(),
     'system',
     NOW(),
     'system')
ON CONFLICT DO NOTHING;

