# AI URL Shortener

Java 21 / Maven prototype built on Spring Boot 4.1.0 with an H2 in-memory database. It includes a URL shortener with redirect analytics and a governed, stateful SDLC orchestration demonstration.

## Run

```powershell
mvn spring-boot:run
```

The H2 console is at `http://localhost:8080/h2-console`. Use JDBC URL `jdbc:h2:mem:urlshortener` and user `sa`.

## API

```http
POST /api/urls
Content-Type: application/json

{"shortCode":"docs","originalUrl":"https://spring.io/projects/spring-boot"}
```

Use `GET /api/urls` to list records, `GET /api/urls/{shortCode}` to retrieve one, `GET /{shortCode}` to redirect, and `GET /api/urls/{shortCode}/analytics` for redirect counts.

## Agentic workflow demo

Create and execute a persisted workflow, inspect its graph/audit trail, then approve the release gate:

```powershell
curl.exe -X POST http://localhost:8080/api/workflows -H "Content-Type: application/json" -d '{"scenario":"greenfield","requirement":"Add click analytics","actor":"product-owner"}'
curl.exe -X POST http://localhost:8080/api/workflows/1/execute -H "Content-Type: application/json" -d '{"actor":"engineering-agent"}'
curl.exe http://localhost:8080/api/workflows/1/graph
curl.exe -X POST http://localhost:8080/api/workflows/1/nodes/release-readiness/approve -H "Content-Type: application/json" -d '{"actor":"release-manager"}'
```

See [architecture](docs/ARCHITECTURE.md) and [scenario/decomposition notes](docs/SCENARIOS.md) for controls, trade-offs, and validation approach.
