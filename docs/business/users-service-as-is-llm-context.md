# Users Service As-Is Context (LLM Handoff)

## 1) Назначение сервиса и границы ответственности

`users-service` в текущем состоянии — это сервис идентификации и управления пользователями для e-learning платформы.

Что реально делает сейчас:
- аутентификация (login + refresh) и сброс пароля;
- выдача JWKS (`/.well-known/jwks.json`) для валидации JWT в других сервисах;
- админское управление пользователями (создание, обновление, деактивация, поиск);
- управление учебной иерархией (факультеты/кафедры/направления/группы);
- управление scope-доступом преподавателя к группам;
- массовый импорт пользователей и иерархии из Excel;
- публикация доменных событий через transactional outbox в Kafka;
- проверка `fileId` через `file-service` при смене аватара.

Что не делает:
- нет self-registration;
- нет email verification flow;
- нет Kafka consumer логики в самом сервисе;
- не отправляет email напрямую (только события в Kafka).

---

## 2) Технологический стек и runtime

- Java 21, Spring Boot 3.5.x
- Spring Web, Spring Security
- PostgreSQL + Spring Data JPA
- Liquibase (`ddl-auto=validate`)
- JWT RS256 (собственные PEM ключи в `resources/certs`)
- Spring Kafka (producer через outbox)
- ShedLock (для scheduler)
- Apache POI (streaming импорт Excel)
- Actuator + Prometheus

Ключевые файлы:
- `users/src/main/resources/application.yml`
- `users/build.gradle.kts`
- `users/src/main/java/ru/sandr/users/UsersApplication.java`

---

## 3) Архитектура кода (as-is)

Структура package-by-feature + layered architecture:

- `security/*` — auth, JWT, refresh/reset flows, фильтр безопасности, JWKS endpoint.
- `user/*` — управление пользователями, роли, профили студента/преподавателя.
- `hierarchy/*` — факультеты, кафедры, направления, группы.
- `teacheraccess/*` — scope-модель видимости групп для преподавателей.
- `imports/*` — загрузка данных из Excel.
- `core/*` — cross-cutting: outbox, exception handling, paging validation, config.

Слои:
- Controller -> Service -> Repository;
- контроллеры не работают с репозиториями напрямую;
- транзакционные границы в сервисах (`@Transactional`).

---

## 4) Безопасность и auth модель

## 4.1 JWT и авторизация

- Stateless JWT (RS256).
- Access token подписывается в `users-service`.
- Subject токена: `userId` (UUID).
- Роли кладутся в claim `roles` (конфигурируемо).
- Другие микросервисы валидируют JWT через JWKS endpoint этого сервиса.

Основные классы:
- `security/config/SecurityConfig.java`
- `security/filter/JwtAuthenticationFilter.java`
- `security/utils/JwtUtils.java`
- `security/controller/JwksController.java`

## 4.2 Правила доступа по URL (as-is)

- `permitAll`: `/auth/**`, `/.well-known/jwks.json`, swagger/openapi, `/actuator/health`, `/actuator/prometheus`.
- `ROLE_ADMIN`: `/admin/**`, `/hierarchy/**`, `/api/v1/import/**`.
- `ROLE_TEACHER`: `/teachers/**`.
- остальное: только authenticated.

## 4.3 Refresh token модель

- refresh хранится в БД в hash-виде;
- refresh передается через HttpOnly cookie;
- токены ротируются;
- ограничение количества refresh-токенов на пользователя (`tokens.refresh.tokensForOneUser`, default 5).

---

## 5) API поверхность (endpoints)

Базовый host в dev: `http://localhost:8080`

## 5.1 Public endpoints

- `POST /auth/login` — логин, возвращает access token + выставляет refresh cookie.
- `GET /auth/refresh` — обновление access token по refresh cookie.
- `POST /auth/forgot-password` — генерация reset токена + event в outbox/Kafka.
- `POST /auth/reset-password` — смена пароля по reset token.
- `GET /.well-known/jwks.json` — JWKS для внешней валидации JWT.

## 5.2 End-user (`/users/me`)

- `PATCH /users/me/email` — смена email текущего пользователя.
- `PATCH /users/me/password` — смена пароля.
- `PATCH /users/me/avatar` — привязка `avatar_file_id` после проверки `fileId` в `file-service`.

Важно: `GET /users/me` в текущей реализации отсутствует.

## 5.3 Admin users (`/admin/users`)

- `POST /admin/users` — создать пользователя.
- `PATCH /admin/users/{id}` — обновить пользователя.
- `DELETE /admin/users/{id}` — деактивировать (soft delete).
- `GET /admin/users/{id}` — получить карточку пользователя.
- `GET /admin/users` — поиск/фильтрация/пагинация.

Бизнес-правила:
- уникальность `username` и `email`;
- `ROLE_STUDENT` требует `groupId`;
- `ROLE_TEACHER` требует `departmentId`.

## 5.4 Teacher access scopes

- `POST /admin/teachers/{teacherId}/group-access` — выдать scope преподавателю.
- `GET /admin/teachers/{teacherId}/group-access/by-type/{scopeType}` — посмотреть scope по типу.
- `GET /teachers/me/student-groups` — список доступных преподавателю групп.

## 5.5 Hierarchy (`/hierarchy/**`)

Для каждой сущности (`faculties`, `departments`, `fields-of-study`, `student-groups`) реализованы CRUD-эндпоинты с пагинацией:
- `POST`
- `GET /{id}`
- `GET` list
- `PATCH /{id}`
- `DELETE /{id}`

Дополнительно:
- `GET /hierarchy/departments/by-faculty/{facultyId}`
- `GET /hierarchy/fields-of-study/by-faculty/{facultyId}`
- `GET /hierarchy/student-groups/by-field-of-study/{fieldOfStudyId}`

## 5.6 Import (`/api/v1/import/**`)

- `POST /api/v1/import/hierarchy/structure`
- `POST /api/v1/import/hierarchy/departments`
- `POST /api/v1/import/users/students`
- `POST /api/v1/import/users/teachers`

Формат: multipart (`file`), parsing через streaming Apache POI.

---

## 6) Ключевые бизнес-флоу

## 6.1 Provisioning пользователя

1. Админ вызывает `POST /admin/users` (или импорт через Excel).
2. Сервис создает `users.users`, роли в `users.users_roles`, профиль студента/преподавателя при необходимости.
3. Публикуется доменное событие `UserCreatedEvent` в outbox.
4. Отправка в Kafka выполняется outbox sender-ом.

## 6.2 Login / Refresh

1. `POST /auth/login`: проверка username/email + password.
2. Выдается access JWT + refresh cookie.
3. `GET /auth/refresh`: refresh проверяется по hash в БД, затем ротируется.

## 6.3 Forgot / Reset password

1. `POST /auth/forgot-password`: создается reset token (hash в БД), событие `ResetPasswordEvent`.
2. `POST /auth/reset-password`: проверка token hash, обновление пароля, событие `PasswordChangedEvent`.

## 6.4 Change avatar

1. Пользователь вызывает `PATCH /users/me/avatar` с `fileId`.
2. `users-service` делает HTTP-запрос в `file-service` для проверки существования файла.
3. При успехе сохраняет `avatar_file_id` у пользователя.
4. Публикует `FileLoadedEvent` (маршрутизируется в file topic).

---

## 7) База данных и схема

СУБД: PostgreSQL.  
Источник истины: Liquibase migrations (`users-changelog-master.yml`).

Используются схемы:
- `users` — доменные сущности;
- `auth` — refresh/reset токены;
- `outbox` — integration events;
- `scheduler` — таблица shedlock.

## 7.1 Основные таблицы (кратко)

`users` schema:
- `users`, `roles`, `users_roles`
- `student_profiles`, `teacher_profiles`
- `faculties`, `departments`, `fields_of_study`, `student_groups`
- `teacher_group_access_scopes`

`auth` schema:
- `refresh_token`
- `reset_password_token`

`outbox` schema:
- `outbox_message`

`scheduler` schema:
- `shedlock`

Подробная DB-модель: `users/docs/business/users-service-db-model-as-is.md`.

## 7.2 Ключевые свойства модели

- `users.users`: UUID PK, unique `username`, unique `email`, `is_active`, `avatar_file_id`.
- soft delete пользователя через `is_active=false`.
- role model через join-таблицу `users_roles`.
- 1:1 профили студента/преподавателя по `user_id` (`@MapsId`).
- teacher scopes: composite key `(teacher_id, scope_type, scope_id)`.
- outbox payload хранится в JSONB.

---

## 8) Миграции и эволюция схемы

Текущий master включает `0001..0010`:
- инициализация user/auth/outbox/shedlock схем;
- seed ролей и базовых пользователей;
- drop legacy поля `fields_of_study.code`;
- переход на sequence-генерацию;
- добавление `teacher_group_access_scopes`;
- добавление `avatar_file_id`.

Файл: `users/src/main/resources/db/changelog/users-changelog-master.yml`.

---

## 9) Интеграции

## 9.1 Kafka (исходящие события)

События:
- `UserCreatedEvent`
- `PasswordChangedEvent`
- `ResetPasswordEvent`
- `FileLoadedEvent`
- (`FileDeletedEvent` поддержан роутингом)

Механика:
- доменное событие -> `outbox.outbox_message` в той же транзакции;
- `OutboxSenderService` читает NEW записи и отправляет в Kafka;
- статусы outbox в коде: `NEW`, `SENT`.

Топики (конфиг):
- `router.user-topic-name` (default `user-topic`)
- `router.file-topic-name` (default `file-topic`)

## 9.2 HTTP интеграция с file-service

- base URL: `api.file-service-base-url` (default `http://localhost:8087`)
- используется для проверки `fileId` при смене аватара (`GET /api/v1/files/{fileId}`)
- Bearer токен пробрасывается через `JwtBearerTokenInterceptor`.

## 9.3 JWKS как интеграционная точка

- endpoint `/.well-known/jwks.json` используется другими сервисами как источник публичного ключа для JWT валидации.

---

## 10) Ошибки и контракт обработки

- Все бизнес-ошибки выбрасываются как доменные исключения (наследники `CustomException`) из service layer.
- Контроллеры без `try/catch`.
- `GlobalExceptionHandler` приводит исключения к унифицированному `ApiErrorResponse`.
- Ошибки из security filter-chain также прокидываются в общий handler через `HandlerExceptionResolver`.

Файл контракта: `users/docs/dev/error-contract-guidelines.md`.

---

## 11) Наблюдаемость и операционка

- `actuator` экспортирует `health`, `info`, `prometheus`.
- HTTP метрики включают histogram/SLO buckets.
- В репозитории есть docker-compose для запуска сервиса с PostgreSQL и отдельным observability стеком.

---

## 12) Важные as-is ограничения и риски

- В `OutboxSenderService` `@Scheduled` сейчас закомментирован — автодоставка outbox может быть неактивна без ручного вызова/изменения конфигурации.
- Нет logout/revoke endpoint для refresh token.
- Нет self-registration и email verification.
- Нет `GET /users/me`.
- В reset password flow часть edge-cases может уходить в generic 500 при необработанных runtime-ошибках.
- `reset_password_token` не имеет явной cleanup job в текущем коде.

---

## 13) Что важно передавать LLM в первую очередь

Если другой LLM должен быстро понять сервис, критично передать:

1. `users-service` — это issuer JWT (RS256) + источник JWKS для остальных сервисов.
2. Пользователи в основном provision-ятся админом/импортом, не через публичную регистрацию.
3. Модель данных разделена на `users/auth/outbox/scheduler` схемы.
4. Интеграция с Kafka построена через transactional outbox.
5. Аватар хранится как `avatar_file_id`, а фактический файл валидируется через `file-service`.
6. Доступ преподавателей к группам реализован отдельной scope-моделью (`teacher_group_access_scopes`).

