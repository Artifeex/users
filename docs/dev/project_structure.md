# Project Structure

This microservice follows package per feature pattern. Inside feature package it follows a layered architecture (Controller -> Service -> Repository -> Entity). The code is organized into functional modules under `src/main/java/ru/sandr/users`.

## Java packages
The text block below presents only the top-level structure and a single example class within a Java package. It does not describe all the classes contained within the packages, but serves solely to provide a high-level overview.
```text
src/main/java/ru/sandr/users
├── UsersApplication.java
├── core # Working with core
│   ├── dto
│   │   └── ApiErrorResponse.java
│   ├── entity
│   │   └── AuditableEntity.java
│   ├── exception
│   │   ├── CustomException.java
│   └── handler
│       └── GlobalExceptionHandler.java
├── security # Working with security in app
│   ├── config
│   │   └── SecurityConfig.java
│   ├── controller
│   │   └── AuthController.java
│   ├── dto
│   │   ├── AuthenticationRequestDto.java
│   │   ├── AuthenticationResponseDto.java
│   │   └── AuthResultDto.java
│   ├── entity
│   │   └── RefreshToken.java
│   ├── filter
│   │   └── JwtAuthenticationFilter.java
│   ├── repository
│   │   └── RefreshTokenRepository.java
│   ├── service
│   │   ├── AuthenticationService.java
│   │   └── DbUserDetailsService.java
│   └── utils # utils classes for auth
│       ├── CustomUserDetails.java
│       ├── HashUtils.java
│       ├── JwtUtils.java
├── user # Working with users, users roles, user entities
│   ├── controller
│   ├── dto
│   ├── entity
│   │   ├── User.java
│   │   ├── UserRole.java
│   ├── repository
│   │   ├── UserRepository.java 
│   └── service
│       └── UserService.java
└── hierarchy # Working with all faculty and underlying hierarchy 
│   ├── controller
│   ├── dto
│   ├── entity
│   │   ├── Department.java
│   ├── repository
│   │   ├── DepartmentRepository.java # 
│   └── service
│       └── DepartmentService.java
    
```

### Notes
`ru.sandr.users.core` contains cross-cutting concerns shared across the service (exceptions + global handler).
`ru.sandr.users.security` contains the auth/JWT endpoints and related infrastructure.
`ru.sandr.users.user` contains user-related entities plus their repositories/services.


