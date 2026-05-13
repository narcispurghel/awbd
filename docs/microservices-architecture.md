# Microservices Architecture

## Infrastructure Services

| Service         | Port | Description                                                                   |
|-----------------|------|-------------------------------------------------------------------------------|
| `config-server` | 8888 | Centralized Spring Cloud Config — all services pull config from here          |
| `eureka-server` | 8761 | Service discovery — all services register here                                |
| `api-gateway`   | 8080 | Spring Cloud Gateway — single entry point, handles routing and JWT validation |
| `web-ui`        | 8090 | Thymeleaf web client (auth, profile, account settings) — calls api-gateway     |

## Business Services

| Service                | Port | Entities                                               | Database                      |
|------------------------|------|--------------------------------------------------------|-------------------------------|
| `user-service`         | 8081 | `UTILIZATOR`, `PROFIL_ADOPTATOR`                       | PostgreSQL — `db_users`       |
| `animal-service`       | 8082 | `ADAPOST`, `SPECIE`, `RASA`, `ANIMAL`, `FISA_MEDICALA` | PostgreSQL — `db_animals`     |
| `adoption-service`     | 8083 | `CERE_ADOPTIE`                                         | PostgreSQL — `db_adoptions`   |
| `notification-service` | 8084 | (no DB)                                                | Consumes events from RabbitMQ |

## Communication

- **Synchronous**: REST via Spring Cloud LoadBalancer (service-to-service calls use Eureka-registered names)
- **Asynchronous**: RabbitMQ or Kafka for adoption events (e.g., adoption approved → notification-service sends email)
- **Resilience**: Resilience4j circuit breakers on inter-service calls

## Security

- `api-gateway` validates JWT tokens and forwards user identity via headers
- `user-service` issues JWT tokens on login (OAuth2 / Spring Security)
- Business services trust the gateway — they do not re-validate tokens

## Multi-Module Gradle Layout

```
awdb/
├── config-server/
├── eureka-server/
├── api-gateway/
├── user-service/
├── animal-service/
├── adoption-service/
├── notification-service/
├── build.gradle.kts        
└── settings.gradle.kts
```