# Error Contract Guidelines

This guide defines the team baseline for `ApiErrorResponse` consistency in Users Service.

## Scope

- Applies to all API errors returned to frontend clients (`4xx/5xx`).
- Applies to messages produced by:
  - `CustomException` descendants
  - `GlobalExceptionHandler`
  - Bean Validation annotations used in request DTOs

## Response Shape

Use one unified error shape:

- `errorCode` - machine-readable stable identifier.
- `debugErrorMessage` - English technical message for troubleshooting.
- `placeHolders` - optional key/value context.

## `errorCode` Rules

- Keep codes stable; treat them as client-facing contract.
- Use uppercase snake case (`USER_NOT_FOUND`, `VALIDATION_FAILED`).
- Use one code per one semantic error type.
- Do not introduce aliases for the same meaning.
- Reuse existing code if meaning is identical.

### Canonical Mapping Principle

If two failures mean the same thing, return the same `errorCode`.

Examples:

- Not authenticated:
  - use `AUTHENTICATION_REQUIRED`
  - do not use alternative aliases like `NO_AUTHORIZED`
- Student group lookup failed:
  - use `STUDENT_GROUP_NOT_FOUND`
  - do not mix with generic aliases for the same case

## `debugErrorMessage` Rules

- English only.
- Keep it short and technical.
- Describe what failed, not user-facing localization text.
- Allow entity ids and key technical context.
- Do not expose sensitive data (passwords, secrets, raw tokens).

Good examples:

- `User not found: <id>`
- `Invalid request parameter type`
- `Token has expired`

## `placeHolders` Rules

- English only for values that represent reasons/messages.
- Use stable, predictable keys (`field`, `reason`, `expectedType`, `rejectedValue`).
- Keep keys in lower camel case.
- Do not put localized/free-form frontend text there.
- Use placeholders for structured context, not for full sentence duplication.

## Bean Validation Rules

- Every Bean Validation annotation in request contracts must define explicit `message`.
- `message` values must be English only.
- Use deterministic, field-oriented wording:
  - `<field> must not be blank`
  - `<field> is required`
  - `<field> must be at most N characters`
  - `<field> must be at least N characters`
  - `<field> must be a well-formed email address`

This ensures `MethodArgumentNotValidException` placeholders (`reason`, `reason_*`) are consistent and predictable.

## Change Checklist

When adding/changing validation or exceptions:

- Verify no Russian text is introduced in `debugErrorMessage`.
- Verify no Russian text is introduced in validation `message`.
- Verify same semantic error maps to the same `errorCode`.
- Verify new/changed code path still returns `ApiErrorResponse`.

## PR Review Checklist

- Are all new error messages English?
- Are any duplicate error codes used for different meanings?
- Are any same-meaning cases using different codes?
- Are validation messages explicit and consistent with naming pattern?
