# Microservices Architecture

## Baseline Today

The repository currently implements the platform foundation plus four real business services:

- `common`
- `config-server`
- `eureka-server`
- `api-gateway`
- `adoption-service`
- `animal-service`
- `notification-service`
- `user-service`
- `web-ui`

The current runtime flow is:

1. Browser requests go to `web-ui`.
2. `web-ui` calls `api-gateway`.
3. `api-gateway` validates JWTs and forwards identity headers.
4. `animal-service` handles animal catalog, shelter, and taxonomy APIs.
5. `adoption-service` handles adoption-request lifecycle APIs.
6. `adoption-service` publishes adoption lifecycle events to RabbitMQ.
7. `notification-service` consumes adoption lifecycle notifications from RabbitMQ.
8. `user-service` handles auth plus adopter account/profile operations.

## Implemented Modules

| Module              | Port | Status / Responsibility                                                   |
|---------------------|------|----------------------------------------------------------------------------|
| `common`            | n/a  | Shared JWT, charset, `.env`, and adoption-event message support           |
| `config-server`     | 8888 | Spring Cloud Config service for local and service-specific YAML profiles   |
| `eureka-server`     | 8761 | Service discovery                                                          |
| `api-gateway`       | 8080 | Single entry point, routing, JWT validation, identity-header forwarding    |
| `adoption-service`  | 8083 | Adoption-request lifecycle APIs plus RabbitMQ event publication            |
| `animal-service`    | 8082 | Animal catalog, shelter, species, breed, and medical record APIs          |
| `notification-service` | 8084 | RabbitMQ consumer for adoption lifecycle notifications                    |
| `user-service`      | 8081 | Authentication, user account, and adopter profile APIs                    |
| `web-ui`            | 8090 | Thymeleaf web client for auth, profile, and account settings              |

## Communication

- **Synchronous**: REST via Spring Cloud LoadBalancer and Eureka-registered service names
- **Asynchronous**: RabbitMQ-backed adoption lifecycle events between `adoption-service` and `notification-service`
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
├── adoption-service/
├── animal-service/
├── notification-service/
├── user-service/
├── web-ui/
├── build.gradle.kts
└── settings.gradle.kts
```

## Planned Expansion

The next platform phases are:

1. Add additional notification delivery adapters beyond logging/no-op
2. Add resilience patterns where real remote calls exist
3. Expand cross-service integration coverage as more business services land
