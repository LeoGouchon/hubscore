# SquashApp - Backend

## How to run locally

1. Clone the repository
2. `docker-compose up -build` (or `make build`)

That's it! 
There's sample data for your development while building. Feel free to add another migrating file if you need to inside `src/main/resources/db/migration`.
`.env` file contain credentials to access to the database.

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