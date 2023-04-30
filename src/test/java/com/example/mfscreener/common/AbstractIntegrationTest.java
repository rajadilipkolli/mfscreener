/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.common;

import static com.example.mfscreener.utils.AppConstants.PROFILE_TEST;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.example.mfscreener.TestcontainersConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({PROFILE_TEST})
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
public class AbstractIntegrationTest {}
