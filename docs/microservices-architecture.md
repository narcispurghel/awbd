# Microservices Architecture

## Baseline Today

The repository currently implements the platform foundation plus two real business services:

- `common`
- `config-server`
- `eureka-server`
- `api-gateway`
- `animal-service`
- `user-service`
- `web-ui`

The current runtime flow is:

1. Browser requests go to `web-ui`.
2. `web-ui` calls `api-gateway`.
3. `api-gateway` validates JWTs and forwards identity headers.
4. `animal-service` handles animal catalog, shelter, and taxonomy APIs.
5. `user-service` handles auth plus adopter account/profile operations.

## Implemented Modules

| Module          | Port | Status / Responsibility                                                   |
|-----------------|------|----------------------------------------------------------------------------|
| `common`        | n/a  | Shared JWT, charset, and `.env` support                                   |
| `config-server` | 8888 | Spring Cloud Config service for local and service-specific YAML profiles   |
| `eureka-server` | 8761 | Service discovery                                                         |
| `api-gateway`   | 8080 | Single entry point, routing, JWT validation, identity-header forwarding    |
| `animal-service`| 8082 | Animal catalog, shelter, species, breed, and medical record APIs          |
| `user-service`  | 8081 | Authentication, user account, and adopter profile APIs                    |
| `web-ui`        | 8090 | Thymeleaf web client for auth, profile, and account settings              |

## Planned Business Services

| Service                | Port | Status / Intended Responsibility                                          |
|------------------------|------|---------------------------------------------------------------------------|
| `adoption-service`     | 8083 | Planned adoption-request lifecycle APIs                                   |
| `notification-service` | 8084 | Planned event consumer for adoption notifications                         |

## Communication

- **Synchronous**: REST via Spring Cloud LoadBalancer and Eureka-registered service names
- **Asynchronous**: Not implemented yet; reserved for adoption lifecycle events
- **Resilience**: Not implemented yet; will be added only where real inter-service calls exist

## Security

- `api-gateway` validates JWT tokens and forwards user identity via headers
- `user-service` issues JWT tokens on login
- Downstream business services trust the gateway and do not re-validate tokens

## Multi-Module Gradle Layout

```
awdb/
├── common/
├── config-server/
├── eureka-server/
├── api-gateway/
├── user-service/
├── web-ui/
├── build.gradle.kts
└── settings.gradle.kts
```

## Planned Expansion

The next platform phases are:

1. Add `adoption-service`
2. Add `notification-service`
3. Introduce async messaging for adoption lifecycle events
4. Add resilience patterns where real remote calls exist
