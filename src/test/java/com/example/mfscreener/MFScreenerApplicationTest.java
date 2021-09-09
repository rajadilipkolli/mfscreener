package com.example.mfscreener;

import com.example.mfscreener.common.AbstractPostgreSQLContainerBase;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MFScreenerApplicationTest extends AbstractPostgreSQLContainerBase{
    
    @Test
    void contextLoads() {
        assertThat(sqlContainer.isRunning()).isTrue();
    }

}
