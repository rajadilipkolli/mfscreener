[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/rajadilipkolli/mfscreener)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)


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