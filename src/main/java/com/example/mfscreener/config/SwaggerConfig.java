/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "mfscreener", version = "v1"), servers = @Server(url = "/"))
public class SwaggerConfig {}