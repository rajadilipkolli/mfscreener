[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/rajadilipkolli/mfscreener) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)


# mfscreener
Spring Boot REST API which fetches the Net Asset Value(NAV) of an AMFI mutual fund and saves in db


### Run tests

```shell
./gradlew clean build
```

### Run locally

```shell
docker-compose -f docker/docker-compose.yml up -d
./gradlew bootRun -Plocal
```

### Using Testcontainers at Development Time
You can run `TestApplication.java` from your IDE directly.
You can also run the application using Gradle as follows:

```
./gradlew spotlessApply bootTestRun
```

### Useful Links
* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator
* PgAdmin (UI for Postgres Database) : http://localhost:5050 (pgadmin4@pgadmin.org/admin)
* Grafana : http://localhost:3000

### how to read CAS Data using [casparser](https://pypi.org/project/casparser/)
 * Install phyton
 * install casparser using command
    ```shell
    pip install casparser
    ```
 * generate json using below command and upload to system
   ```shell
   casparser 42103626220211831ZFFDEPR3H0RBD644686241F761CPIMBCP142488446.pdf -p ABCDE1234F -o pdf_parsed.json
   ```
   Here 2nd argument is the path of the pdf file, followed by password of CAS file and the output Type needed
