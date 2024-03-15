package com.learning.mfscreener.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(
        properties = {
            "spring.jpa.hibernate.ddl-auto=validate",
            "spring.test.database.replace=none",
            "spring.datasource.url=jdbc:tc:postgresql:16-alpine:///db"
        })
class SchemaValidationPostgresTest {

    @Autowired
    EntityManager entityManager;

    @Test
    void schemaValidity() {
        assertThat(entityManager).isNotNull();
    }
}
