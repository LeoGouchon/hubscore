# Husbcore - Backend

## How to run locally

1. Clone the repository
2. `docker-compose up -build` (or `make build`)

That's it. The database schema is created by Flyway migrations in
`src/main/resources/db/migration`.
`.env` file contain credentials to access to the database.

## Identity server / OIDC configuration

Husbcore is an OAuth 2.0 resource server. The local defaults match the sibling
`identity-server` Docker setup:

```dotenv
IDENTITY_ISSUER=http://localhost:8081
IDENTITY_JWK_SET_URI=http://localhost:8081/oauth2/jwks
IDENTITY_AUDIENCE=default-api
BACKEND_PROVISIONING_SECRET=husbcore-secret
IDENTITY_CLIENT_ID=husbcore-client
```

The complete local Docker configuration is kept in `config/application-dev.yml`, which is
created from `config/application-dev.example.yml` by `make dev` and mounted into the container.
The CORS origins are configured as a YAML list:

```yaml
identity:
  provisioning-secret: ${BACKEND_PROVISIONING_SECRET:husbcore-secret}
  identity-client-id: ${IDENTITY_CLIENT_ID:husbcore-client}

husbcore:
  cors:
    allowed-origins:
      - http://localhost:4200
      - http://localhost:5173
```

Use the same issuer and audience in both applications. Each backend may use its own provisioning secret. The identity server must include
`default-api` in `identity.allowed-backends`, or set
`IDENTITY_AUDIENCE` to the backend value configured there. Husbcore validates the token signature, issuer, and audience,
and maps the token `sub` claim to
`users.identity_user_id`.

The admin invitation endpoint (`POST /api/v1/admin/invitation`) delegates invitation creation to the Identity Server.
It sends the Husbcore application client id (`husbcore-client` by default) and the optional `playerId`, so the resulting
invitation provisions only Husbcore. The baby-foot frontend can continue using this endpoint and the generated
`/signup?invitationToken=...` link.

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

Backend of the Husbcore baby-foot application

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

## Required configuration (`application.yml`)

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
