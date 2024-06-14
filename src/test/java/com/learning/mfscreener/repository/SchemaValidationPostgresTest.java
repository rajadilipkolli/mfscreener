package com.learning.mfscreener.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.learning.mfscreener.config.BlazePersistenceConfiguration;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest(
        properties = {
            "spring.jpa.hibernate.ddl-auto=validate",
            "spring.test.database.replace=none",
            "spring.datasource.url=jdbc:tc:postgresql:16-alpine:///db"
        })
@Import(BlazePersistenceConfiguration.class)
class SchemaValidationPostgresTest {

    @Autowired
    EntityManager entityManager;

    @Test
    void schemaValidity() {
        assertThat(entityManager).isNotNull();
    }
}
