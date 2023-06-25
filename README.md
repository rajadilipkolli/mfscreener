# mfscreener


### Run tests

```shell
$ ./gradlew clean build`
```

### Run locally

```shell
$ docker-compose -f docker/docker-compose.yml up -d
$ ./gradlew bootRun -Plocal
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
