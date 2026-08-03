# SquashApp - Backend

## How to run locally

1. Clone the repository
2. `docker-compose up -build` (or `make build`)

That's it!
There's sample data for your development while building. Feel free to add another migrating file if you need to inside
`src/main/resources/db/migration`.
`.env` file contain credentials to access to the database.

## Identity server / OIDC configuration

Hubscore is an OAuth 2.0 resource server. The local defaults match the sibling
`identity-server` Docker setup:

```dotenv
IDENTITY_ISSUER=http://localhost:8081
IDENTITY_JWK_SET_URI=http://localhost:8081/oauth2/jwks
IDENTITY_AUDIENCE=default-api
BACKEND_PROVISIONING_SECRET=change-me
HUBSCORE_CORS_ALLOWED_ORIGINS=http://localhost:4200
```

Use the same issuer, audience, and provisioning secret in both applications. The identity server must include
`default-api` in `identity.allowed-backends`, or set
`IDENTITY_AUDIENCE` to the backend value configured there. Hubscore validates the token signature, issuer, and audience,
and maps the token `sub` claim to
`users.identity_user_id`.

## How to run tests

Tests can be run without a local Java installation:

```sh
docker compose run --rm tests
```

or:

```sh
make test
```

`make test` also prints a quick JaCoCo coverage summary in the terminal.

Coverage can be generated with:

```sh
make coverage
```

The HTML report is written to `target/site/jacoco/index.html`.

## Description

Backend of mobile application SquashApp

## Language and tools

* Java 21
* Spring Boot 3.4.2
* Spring Data JPA
* Spring Web
* Spring Boot DevTools
* **Database**
    + PostgreSQL 17
* **Libraries**
    + Lombok
    + SpringDoc OpenAPI
    + Spring Security Crypto
* **Testing**
    + Mockito
    + JUnit
* **Migration**
    + Flyway

## Required configuration (`application.properties`)

* **Flyway**
    * `spring.flyway.enabled` : Enable database migration
    * `spring.flyway.locations` : Location of migration files
    * `spring.flyway.schemas` : Database schema
    * `spring.flyway.url` : Database URL
    * `spring.flyway.user` : Database user
    * `spring.flyway.password` : User password
* **Hibernate**
    * `spring.jpa.hibernate.ddl-auto=validate` : Validate database integrity
    * `spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect` : Database dialect
    * `spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect` : Database platform
* **Jwt**
    * `jwt.secret` : Secret key for JWT
    * `jwt.expirationMs=86400000` : JWT expiration time in milliseconds
