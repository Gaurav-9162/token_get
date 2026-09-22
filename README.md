# TokenGate

TokenGate is a Spring Boot service that enforces per-client API rate limiting using a token bucket strategy. It stores limiter state in Redis so requests can be evaluated consistently across app instances.

## Overview

This project is designed for APIs that need to limit the number of requests a client can make over time without blocking the entire application. Each client ID gets its own bucket with:

- a fixed maximum token capacity
- a refill rate measured in tokens per second
- an automatic expiry for idle clients

When a request is checked, the service:

1. loads the client's current bucket from Redis
2. refills tokens based on elapsed time
3. decides whether the request is allowed
4. deducts one token when permitted
5. returns the decision and remaining tokens

## Features

- Token bucket rate limiting per client ID
- Redis-backed state persistence
- Spring Boot REST API
- OpenAPI/Swagger UI support
- Graceful fallback when Redis is unavailable
- Configurable capacity and refill rate

## Technology Stack

- Java 25
- Spring Boot 4.1.1
- Spring Web MVC
- Redis
- Maven Wrapper
- SpringDoc OpenAPI

## Project Structure

```text
TokenGate/
├── src/
│   ├── main/
│   │   ├── java/com/example/tokengate/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── service/
│   │   │   └── TokenGateApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/example/tokengate/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── HELP.md
└── README.md
```

## Prerequisites

Before running the project, make sure you have:

- Java 25 installed
- Maven or the bundled Maven wrapper
- Redis running locally on port 6379

## Running Redis

If you do not already have Redis available, you can run it with Docker:

```bash
docker run --name token-gate-redis -p 6379:6379 -d redis:latest
```

## Configuration

The main rate-limit settings are defined in `src/main/resources/application.properties`:

```properties
spring.application.name=TokenGate
spring.data.redis.host=localhost
spring.data.redis.port=6379

ratelimit.capacity=10
ratelimit.refill-rate-per-sec=0.5
```

- `ratelimit.capacity`: maximum tokens in each client bucket
- `ratelimit.refill-rate-per-sec`: how quickly tokens are replenished

## Running the Application

From the project root:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
mvnw.cmd spring-boot:run
```

The application will start on:

```text
http://localhost:8080
```

## API

### Check Rate Limit

Endpoint:

```http
POST /api/v1/ratelimit/check?clientId={clientId}
```

Example:

```bash
curl -X POST "http://localhost:8080/api/v1/ratelimit/check?clientId=user123"
```

Example response:

```json
{
  "allowed": true,
  "remainingTokens": 8
}
```

Response fields:

- `allowed`: whether the current request is permitted
- `remainingTokens`: token count remaining in the bucket after the request

In degraded mode, if Redis is unavailable, the service allows the request and returns `-1` for remaining tokens.

## Notes

This project is a lightweight example of rate limiting and is useful as a reference for implementing distributed API throttling with Redis-backed token-bucket logic.

## License

This project does not currently declare a license.
