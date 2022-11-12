/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.mfscreener.common.AbstractPostgreSQLContainerBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MFScreenerApplicationTest extends AbstractPostgreSQLContainerBase {

    @Test
    void contextLoads() {
        assertThat(sqlContainer.isRunning()).isTrue();
    }
}
