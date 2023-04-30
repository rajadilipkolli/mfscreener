/* Licensed under Apache-2.0 2021-2023. */
package com.example.mfscreener;

import com.example.mfscreener.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static com.example.mfscreener.TestcontainersConfig.LOKI_CONTAINER;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        assertThat(LOKI_CONTAINER.isRunning()).isTrue();
    }
}
