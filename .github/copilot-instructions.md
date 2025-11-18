Purpose
-------

This file gives short, practical guidance for AI coding agents (Copilot-like) to be immediately productive in this repository.

**Big Picture**
- **Project type**: Spring Boot microservice-style web application (single Spring Boot app) — entry point is `Application.java`.
- **Layers & packages**: controllers (`web/controllers`, `web/api`), services (`service`), repositories (`repository`), entities (`entities`), DTOs/models (`models`), mappers (`mapper`), and utils/exception packages.
- **Data flow**: REST controllers → service layer → repository (Spring Data JPA) → DB (Postgres/Liquibase migrations). Nav calculation and portfolio logic live in `service` (e.g. `PortfolioService`, `NavService`, `XIRRCalculatorService`).

**Key files & dirs to read first**
- `src/main/java/com/learning/mfscreener/Application.java`: app entry and Spring Boot wiring.
- `src/main/java/com/learning/mfscreener/web/controllers`: controller surface and API contracts.
- `src/main/java/com/learning/mfscreener/service`: business logic and domain calculations.
- `src/main/java/com/learning/mfscreener/repository`: Spring Data repositories and projections.
- `src/main/java/com/learning/mfscreener/mapper`: MapStruct mappers and `MapperSpringConfig` to learn mapping conventions.
- `src/main/resources/db`: Liquibase changelogs for schema expectations.
- `build.gradle`: Gradle configuration, Java toolchain (languageVersion=25), Spotless rules, test and integration-test tasks.

**Build / Run / Test (concrete commands)**
- Build: `./gradlew clean build` (runs unit + integration tests via `check` unless disabled).
- Run app locally: `./gradlew bootRun` (default), or with local profile: `./gradlew -Plocal bootRun` or `./gradlew bootRun -Plocal` which triggers `--spring.profiles.active=local` when `local` property is present.
- Unit tests: `./gradlew test` (uses JUnit 5; excludes `*IT*` tests by default).
- Integration tests: `./gradlew integrationTest` (matches `*IT`, `*IntegrationTest`, `*IntTest`).
- Reports: `./gradlew testReport` and `./gradlew integrationTestReport` create aggregated reports.

**Test infrastructure notes**
- Tests use Testcontainers: see `src/test/java/com/learning/mfscreener/common/SQLContainersConfig.java` — Postgres image pinned to `postgres:18.1-alpine`.
- Use Testcontainers rather than relying on a host Postgres for CI. Integration test class naming matters to be included by `integrationTest` task.

**Conventions & patterns specific to this repo**
- MapStruct: mappers live under `mapper`; compiler args in `build.gradle` set `-Amapstruct.defaultComponentModel=spring` so generated mappers are Spring beans.
- Integration test discovery: class name patterns (`*IT*`, `*IntegrationTest*`, `*IntTest*`). Place long-running or container-backed tests using those suffixes.
- Formatting: Spotless is enforced (`check` depends on `spotlessCheck`) — follow the Palantir Java format used in repo.
- Toolchain: project targets Java toolchain `languageVersion 25` in Gradle — use a matching JDK toolchain in CI or local dev where possible.

**Integrations & runtime features**
- Liquibase for DB migrations (`liquibase-core` dependency and `src/main/resources/db` changelogs).
- Redis caching (`spring-boot-starter-data-redis`) and JobRunr scheduler (`org.jobrunr:jobrunr-spring-boot-3-starter`).
- Observability: Micrometer + OpenTelemetry/Zipkin and Prometheus registry.
- Docker: `docker-compose.yml` and `docker/docker-compose-app.yml` provide local stacks; use them for full-stack debugging.

**What to change carefully**
- Database schema and migration files under `src/main/resources/db` — changes must be compatible with existing liquibase history.
- MapStruct signatures — because mappers are generated, changing DTO/entity fields often requires regenerating or updating mapper config.
- Gradle `check` task ties tests, integration tests and spotless. Large changes may need adjusting `integrationTest` include patterns.

**Quick examples (what an agent should do)**
- To add a new REST endpoint: add controller under `web/controllers`, add service method in `service`, add repository query in `repository` (or a projection in `models/projection`), update mapper if new DTOs are used, and add unit + integration tests (use `*IT` suffix for container-backed tests).
- To run only integration tests locally: `./gradlew integrationTest --no-parallel` (Testcontainers can be resource-sensitive; serial runs are safer).

If anything in this file is unclear or you want more examples (e.g., a sample PR checklist, CI differences, or common test stubs), tell me what to add and I will update this file.
