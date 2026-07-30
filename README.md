# AI URL Shortener

Java 21 / Maven skeleton built on Spring Boot 4.1.0 with an H2 in-memory database.

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

Use `GET /api/urls` to list records and `GET /api/urls/{shortCode}` to retrieve one.
