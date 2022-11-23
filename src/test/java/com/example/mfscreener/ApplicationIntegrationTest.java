/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.mfscreener.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        assertThat(POSTGRE_SQL_CONTAINER.isRunning()).isTrue();
    }
}
