package com.learning.mfscreener;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.learning.mfscreener.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void actuatorLoaded() throws Exception {
        this.mockMvc
                .perform(get("/actuator").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.size()", is(13)))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/actuator"))
                .andExpect(jsonPath("$._links.health.href").value("http://localhost/actuator/health"))
                .andExpect(jsonPath("$._links.health-path.href").value("http://localhost/actuator/health/{*path}"))
                .andExpect(jsonPath("$._links.info.href").value("http://localhost/actuator/info"))
                .andExpect(jsonPath("$._links.configprops.href").value("http://localhost/actuator/configprops"))
                .andExpect(jsonPath("$._links.configprops-prefix.href")
                        .value("http://localhost/actuator/configprops/{prefix}"))
                .andExpect(jsonPath("$._links.env.href").value("http://localhost/actuator/env"))
                .andExpect(jsonPath("$._links.env-toMatch.href").value("http://localhost/actuator/env/{toMatch}"))
                .andExpect(jsonPath("$._links.logfile.href").value("http://localhost/actuator/logfile"))
                .andExpect(jsonPath("$._links.loggers.href").value("http://localhost/actuator/loggers"))
                .andExpect(jsonPath("$._links.loggers-name.href").value("http://localhost/actuator/loggers/{name}"))
                .andExpect(jsonPath("$._links.metrics.href").value("http://localhost/actuator/metrics"))
                .andExpect(jsonPath("$._links.metrics-requiredMetricName.href")
                        .value("http://localhost/actuator/metrics/{requiredMetricName}"));
    }

    @Test
    void actuatorInfo() throws Exception {
        this.mockMvc
                .perform(get("/actuator/info").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(4)));
    }
}
