[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=rajadilipkolli_mfscreener)](https://sonarcloud.io/dashboard?id=rajadilipkolli_mfscreener) [![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/rajadilipkolli/mfscreener)

# mfscreener
Spring Boot REST API which fetches the Net Asset Value(NAV) of an AMFI mutual fund and saves in db

### How to access PG Admin

> http://localhost:5050/

### How to access Swagger

> http://localhost:8080/swagger-ui.html

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
