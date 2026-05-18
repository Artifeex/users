# Users Service As-Is Analysis

## 1. Purpose and system boundaries

The `users` service is an Identity and User Management microservice for an educational platform.  
It is responsible for authentication, user lifecycle management, university hierarchy management, teacher access scoping, and bulk import operations.

The service is not a monolith for all business domains. It integrates with external services for side effects:
- sends integration events via Kafka (through transactional outbox),
- calls external file service for avatar binding,
- exposes JWKS for token verification by other services.

Core stack and runtime:
- Java 21, Spring Boot 3.5.x
- PostgreSQL + Liquibase
- Spring Security (JWT, RS256)
- Spring Data JPA
- Kafka producer (outbox pattern)
- OpenAPI/Swagger

## 2. High-level architecture (implemented)

The codebase follows package-by-feature with layered architecture:
- Controller layer: REST contracts and input validation.
- Service layer: business rules, transaction boundaries, event publishing.
- Repository layer: data access with JPA/Specifications.
- Entity layer: persistence model.

Cross-cutting modules include:
- Global exception handling (`GlobalExceptionHandler`)
- Auditing (`createdBy`, `updatedBy`, timestamps)
- Pagination/sorting validation (`PageableValidator`)
- Outbox dispatch scheduler with distributed locking (ShedLock)

## 3. Implemented functional capabilities

### 3.1 Authentication and credential flows

Implemented:
- User login with credentials:
  - returns access JWT,
  - sets refresh token in HttpOnly cookie.
- Access token refresh endpoint using refresh cookie.
- Forgot password flow (token generation + event emission).
- Password reset by token.
- JWKS endpoint for public key distribution (`/.well-known/jwks.json`).

Behavioral details:
- Access tokens are RS256-signed.
- JWT subject is user UUID.
- Roles are taken from configurable claim (`roles` by default).
- Refresh tokens are persisted hashed in DB, with per-user token count cap.

### 3.2 End-user self-service (`/users/me`)

Implemented:
- Update own email.
- Change own password.
- Set own avatar by file identifier.

Avatar flow:
- Service calls external file service (`GET /api/v1/files/{fileId}`).
- On success, stores `avatar_file_id` and emits file-related domain event.

### 3.3 Admin user management (`/admin/users`)

Implemented:
- Create user (manual provisioning).
- Update user profile/roles.
- Soft-delete user (deactivate).
- Get detailed user profile.
- Search and filter users with pagination.

Business rules implemented:
- Username/email uniqueness checks.
- Role-driven profile requirements:
  - STUDENT requires group assignment,
  - TEACHER requires department assignment.
- Role updates synchronize profile entities (student/teacher profile creation/removal).

### 3.4 University hierarchy management (`/hierarchy/**`)

Implemented CRUD with paging for:
- Faculties
- Departments
- Fields of study
- Student groups

Additional listing operations:
- Departments by faculty
- Fields of study by faculty
- Student groups by field of study

Integrity protections:
- Deletion is blocked when dependent entities exist (according to service-level checks and FK constraints).

### 3.5 Teacher access scope management

Implemented:
- Grant teacher visibility scope to:
  - specific student group,
  - field of study,
  - faculty.
- List teacher scopes by type with pagination.

Implemented teacher-facing search:
- Teacher can query accessible student groups via `/teachers/me/student-groups`.
- Search is filtered by assigned scopes.

### 3.6 Bulk import from Excel (`/api/v1/import/**`)

Implemented import endpoints:
- Hierarchy structure import.
- Departments import.
- Students import.
- Teachers import.

Import behavior:
- Uses streaming parser (Apache POI event model), not full workbook in memory.
- Performs validation with capped number of returned errors.
- Supports bulk upsert semantics in service layer.

### 3.7 Integration events and asynchronous communication

Implemented domain events include:
- UserCreatedEvent
- PasswordChangedEvent
- ResetPasswordEvent
- FileLoadedEvent
- (type routing also supports FileDeletedEvent)

Implemented delivery model:
- Transactional outbox table stores events inside business transaction.
- Scheduled sender publishes NEW messages to Kafka.
- Message routing by event type to configured topics.
- ShedLock prevents concurrent sender execution in multi-instance deployment.

## 4. REST API surface (as implemented)

Main controller groups:
- Auth APIs: `/auth/**`
- JWKS: `/.well-known/jwks.json`
- Self-service user APIs: `/users/me/**`
- Admin APIs: `/admin/users/**`, `/admin/teachers/**`
- Hierarchy APIs: `/hierarchy/**`
- Teacher APIs: `/teachers/**`
- Import APIs: `/api/v1/import/**`

Security model by path:
- Public: `/auth/**`, OpenAPI/Swagger routes, `/.well-known/jwks.json`
- ADMIN: `/admin/**`, `/hierarchy/**`, `/api/v1/import/**`
- TEACHER: `/teachers/**`
- All other routes: authenticated

## 5. Data model and persistence features

Database schemas:
- `users`: users, roles, user-role links, hierarchy tables, student/teacher profiles, teacher access scopes
- `auth`: refresh tokens, reset password tokens
- `outbox`: outbox messages
- `scheduler`: shedlock table

Persistence and consistency characteristics:
- Liquibase-managed schema migrations.
- Hibernate `ddl-auto=validate` (schema must match entities).
- FK constraints and indexes for key relations and searches.
- Auditable entities with `created_at/updated_at` and actor fields.

## 6. Non-functional characteristics (implemented)

### 6.1 Security
- Stateless JWT authentication with RS256 signature validation.
- URL-level RBAC through Spring Security.
- Password hashing via DelegatingPasswordEncoder.
- HttpOnly refresh token cookie with strict SameSite policy.

### 6.2 Reliability and consistency
- Transaction boundaries at service layer.
- Outbox pattern for reliable event publication.
- ShedLock for distributed scheduler coordination.
- DB-level referential constraints to prevent invalid relationships.

### 6.3 Scalability and performance
- Pagination across list/search APIs.
- JPA batching configured (`batch_size=100`).
- Import parser optimized for large Excel files (streaming approach).
- Outbox sender processes bounded batches (`top 100`).

### 6.4 Operability and deployment
- OpenAPI + Swagger UI exposed.
- Dockerfile includes multi-stage build and non-root runtime user.
- Container JVM tuned with memory-percentage settings and ZGC.
- Service default timezone set to UTC at startup.

## 7. As-is limitations and implementation notes

This section lists observed as-is behavior that should be considered when transforming this document into formal requirements.

- CORS configuration currently allows origin value `"localhost"` (not full browser origin format).
- Method-level security is enabled, but authorization is currently path-based.
- No dedicated actuator/metrics/tracing stack is declared in this repository.
- File integration is currently focused on avatar linkage through external file service.

## 8. Functional readiness summary (what the service can already do)

The service is already functionally ready in these areas:
- Production-style JWT auth with refresh and password reset flows.
- Full admin management of users with role-aware profile handling.
- Full CRUD management of educational hierarchy entities.
- Teacher access scoping and constrained group discovery.
- Bulk Excel import for hierarchy and user data.
- Asynchronous integration event publication via transactional outbox.
- External file service integration for user avatar assignment.

In practical terms, this is an operational identity and organizational structure service with admin tooling, teacher visibility control, and integration hooks for the wider microservice platform.
