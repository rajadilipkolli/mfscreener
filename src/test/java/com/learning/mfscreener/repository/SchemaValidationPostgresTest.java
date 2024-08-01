package com.learning.mfscreener.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.learning.mfscreener.common.SQLContainersConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=validate", "spring.test.database.replace=none"})
@Import(SQLContainersConfig.class)
class SchemaValidationPostgresTest {

    @Autowired
    DataSource dataSource;

    @Test
    void validateJpaMappingsWithDbSchema() {
        assertThat(dataSource).isNotNull().isInstanceOf(HikariDataSource.class);
    }
}
