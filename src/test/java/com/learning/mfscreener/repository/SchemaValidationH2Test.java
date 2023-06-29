package com.learning.mfscreener.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=validate"})
class SchemaValidationH2Test {

    @Autowired
    EntityManager entityManager;

    @Test
    void testSchemaValidity() {
        assertThat(entityManager).isNotNull();
    }
}
