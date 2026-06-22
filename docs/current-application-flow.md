# Current Application Flow

```mermaid
flowchart LR
    Browser[Browser] --> WebUI[web-ui :8090]
    WebUI --> Gateway[api-gateway :8080]
    Gateway --> User[user-service :8081]
    Gateway --> Animal[animal-service :8082]
    Gateway --> Adoption[adoption-service :8083]
    Adoption --> Rabbit[RabbitMQ]
    Rabbit --> Notification[notification-service :8084]
    User --> Postgres[(PostgreSQL)]
    Animal --> Postgres
    Adoption --> Postgres
    Gateway --> Redis[(Redis)]
    User --> Redis
    Animal --> Minio[(MinIO)]
```

Browser traffic enters through `web-ui`, which calls the `api-gateway`. The gateway validates JWTs, checks token revocation in Redis, and forwards identity headers to the backend services. The animal catalog includes shelters, species, breeds, tags, animals, photos, and medical records. Adoption lifecycle events are published to RabbitMQ and consumed by `notification-service`.
