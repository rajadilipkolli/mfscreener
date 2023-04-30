# mfscreener
Spring Boot REST API which fetches the Net Asset Value(NAV) of an AMFI mutual fund and saves in db

### Run tests
`$ ./mvnw clean verify`

### Run locally
```shell
$ docker compose -f docker-compose.yml up -d
$ ./gradlew bootRun --args='--spring.profiles.active=local'
```

### Useful Links
* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator