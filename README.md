[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=rajadilipkolli_mfscreener)](https://sonarcloud.io/summary/new_code?id=rajadilipkolli_mfscreener)[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)


# mfscreener

**Description:** MF Screener is a Java-based application designed to provide mutual fund screening capabilities. It allows users to fetch mutual fund data, including historical NAVs, scheme details, and portfolio information, through a set of RESTful APIs.

**Setup:**

1. Clone the repository.
2. Ensure Java 25, docker and gradle are installed.
3. Navigate to the project directory and follow steps at [run-locally](#run-locally) to start the application.

**Usage:**

* Fetch mutual fund schemes: `GET /api/scheme/{schemeName}`
* Upload portfolio details: `POST /api/portfolio/upload`
* Calculate XIRR: `GET /api/xirr/{pan}`

### Acronyms
* **_ISIN_** - International Security Identification Number
* **_PAN_** - Personal Account Number
* **_CAS_** - Consolidated Account Statement

### Simplified Class Diagram Concept
 ```mermaid
 classDiagram
     class NAVController {
         +getScheme(schemeCode)
         +getSchemeNavOnDate(schemeCode, date)
     }
     class SchemeController {
         +fetchSchemes(schemeName)
         +fetchSchemesByFundName(fundName)
     }
     class PortfolioController {
         +upload(multipartFile)
         +getPortfolio(panNumber, date)
     }
     class XIRRCalculatorController {
         +getXIRR(pan)
     }
     class SchemeService {
         +fetchSchemeDetails(schemeCode)
         +fetchSchemes(schemeName)
     }
     class PortfolioService {
         +upload(multipartFile)
         +getPortfolioByPAN(panNumber, asOfDate)
     }
     class NavService {
         +getNav(schemeCode)
         +getNavOnDate(schemeCode, inputDate)
     }
     class LocalDateUtility {
         +getAdjustedDate(adjustedDate)
     }
     NAVController --> SchemeService : uses
     SchemeController --> SchemeService : uses
     PortfolioController --> PortfolioService : uses
     XIRRCalculatorController --> NavService : uses
     SchemeService --> LocalDateUtility : uses
     PortfolioService --> LocalDateUtility : uses
     NavService --> LocalDateUtility : uses
 ```
This overview and class diagram provide a conceptual understanding of the project's structure. For detailed class relationships and method signatures, please refer to the source code directly.

### Run tests

```shell
./gradlew clean test integrationTest
```
### Run locally with docker

```shell
docker-compose -f docker/docker-compose.yml up -d
./gradlew bootRun -Plocaldocker
```

### Run locally

```shell
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
* Grafana : http://localhost:3000 (admin/admin)
* JobRunr Dashboard: http://localhost:8000/dashboard/overview

### Check redis keys

* To check redis keys from docker issue below command
```shell
docker exec -it docker-redis-1 redis-cli
```

```shell
127.0.0.1:6379> keys *
```

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
