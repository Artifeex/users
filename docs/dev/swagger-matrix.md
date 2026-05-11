# Swagger/OpenAPI Matrix

This document defines the team baseline for endpoint and DTO documentation in Users Service.

## Goals

- Keep API contracts predictable for frontend and backend clients.
- Make integrations possible without reading service internals.
- Standardize style across controllers and DTOs.

## Endpoint Matrix

| Endpoint Type | Required | Recommended | Typical Status Codes |
|---|---|---|---|
| Auth (`/login`, `/refresh`, `/forgot-password`, `/reset-password`) | `@Operation`, `@ApiResponses`, `@RequestBody @Valid`, `@Schema` on DTOs | Success and error examples, explicit cookie behavior in description | `200/202`, `400`, `401` |
| Create (`POST`) | `@Operation`, `@ApiResponses`, `@RequestBody @Valid`, `@Schema` on request/response | Explicit uniqueness/conflict behavior | `201`, `400`, `409` |
| Read one (`GET /{id}`) | `@Operation`, `@ApiResponses`, `@PathVariable`, `@Schema` on response | `@Parameter` with id format details | `200`, `404` |
| Read list (`GET`) | `@Operation`, `@ApiResponses`, `@ParameterObject Pageable`, `@Schema` on filter/page model | Explicit sorting/filtering semantics | `200`, `400` |
| Update (`PATCH`/`PUT`) | `@Operation`, `@ApiResponses`, `@RequestBody @Valid`, `@Schema` | Describe partial/full update semantics | `200/204`, `400`, `404`, `409` |
| Delete (`DELETE`) | `@Operation`, `@ApiResponses` | Mention idempotency behavior | `204`, `404` |
| Search/filter endpoints | `@Operation`, `@Parameter` and/or filter DTO, `@ApiResponses` | Query examples in description | `200`, `400` |
| Import endpoints | `@Operation`, `@ApiResponses`, multipart/body constraints, error model | Describe file limits and accepted formats | `200/202`, `400`, `413`, `422` |
| Internal service endpoints | Same as public + clear `@Tag` ("Internal") | Strong role/scope description | `200`, `400`, `401`, `403`, `404` |

## Minimum Annotation Baseline

### Controller class

- `@Tag(name = "...")`

### Endpoint method

- `@Operation(summary = "...", description = "...")`
- `@ApiResponses({ ... })`
- `@SecurityRequirement(name = "bearerAuth")` for secured methods
- `@Parameter` for important path/query/header parameters
- `@ParameterObject` for `Pageable` and filter models

### DTO and enum

- `@Schema` on class/record/enum
- `@Schema` on fields/components with `description` and `example`
- Bean Validation (`@NotNull`, `@NotBlank`, `@Size`, `@Min`, `@Max`, `@Pattern`, `@Email`) as source of constraints

## DTO Documentation Rules

Every request/response DTO should include:

- Business-focused field description.
- Realistic example values.
- Required/optional behavior via validation annotations.
- Constraints for length/range/pattern when applicable.
- Enum semantics (not just names).

## Error Contract Rules

- Use unified `ApiErrorResponse` for all documented `4xx/5xx`.
- Always document at least:
  - `400` validation or bad request
  - `401` unauthorized (for secured endpoints)
  - `403` forbidden (where role/permission checks apply)
  - `404` not found (for id-based reads/updates/deletes)
  - `409` conflict (where unique/business conflicts are possible)

## Pagination Rules

- Use `PageResponse<T>` as the default list contract.
- Expose pageable params through `@ParameterObject`.
- Keep naming stable: `page`, `size`, `sort`.
- Default values must be visible via `@PageableDefault`.

## Definition Of Done (Swagger Quality Gate)

Before merge, each new endpoint must satisfy:

- Endpoint has `@Operation` and complete `@ApiResponses`.
- Request and response DTOs have `@Schema` descriptions/examples.
- Validation constraints are present and visible in OpenAPI.
- Security requirement is explicit (or intentionally omitted for public endpoints).
- Pagination/filter behavior is documented if endpoint returns a list.
- Error model is consistent with `ApiErrorResponse`.

## Notes

- Prefer contract-first mindset: consumers should not need source code.
- Keep endpoint descriptions concise and business-oriented.
- Avoid duplicate contracts or different error shapes for similar endpoints.
- For `ApiErrorResponse` consistency rules (`errorCode`, `debugErrorMessage`, validation messages), follow `docs/dev/error-contract-guidelines.md`.
