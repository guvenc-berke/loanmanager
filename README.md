# Loan Manager Case Study - Berke Guvenc

This project requires Java 21 to build and run. Default port is `8080`.

## Endpoints

OpenAPI is used in this project. For endpoint specs, you can check the following specs:
`src/main/resources/loanService.yaml`

To generate the specs use:

```bash
  mvn openapi-generator:generate
```
or compile the project using

```bash
    mvn clean compile
```

## Authorization

The authorization is done through a JWT token.

JWT Token can be retrieved in login: `localhost:8080/login`.
Credentials should be provided via Basic Authentication such as `Basic base64(email:password)`
For the JWT token, check the ``X-Auth-Header`` response header value. This header and its value should be provided for all endpoints.

A sample user is already added through Liquibase. The user's basic auth header is as follows:

```Basic YmVya2VAdGVzdC5jb206cGFzc3dvcmQ=```

## Database

H2 database is used.
Liquibase is also used to create the necessary tables and populate them when needed. Liquibase changelogs can be found in:

```src/main/resources/db/changelog```

## User Management

Every endpoint has an optional userId query parameter. 
If the current user has ADMIN role, userId field can be used to handle operations for different users.
Otherwise the system will use the current user.
