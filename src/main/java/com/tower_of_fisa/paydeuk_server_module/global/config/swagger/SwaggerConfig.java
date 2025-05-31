package com.tower_of_fisa.paydeuk_server_module.global.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info =
        @Info(
                title = "TOWER OF FISA API Docs",
                description = "페이득 Project의 Core API 문서입니다.",
                version = "v1"))
@Configuration
public class SwaggerConfig {
  @Bean
  public OpenAPI openAPI() {
    SecurityRequirement securityRequirement =
            new SecurityRequirement().addList("userNameSecurityName");
    Components components =
            new Components()
                    .addSecuritySchemes(
                            "userNameSecurityName",
                            new SecurityScheme()
                                    .name("X-User-Name")
                                    .type(SecurityScheme.Type.APIKEY)
                                    .in(SecurityScheme.In.HEADER)
                                    .bearerFormat("X-User-Name"));

    return new OpenAPI().addSecurityItem(securityRequirement).components(components);
  }
}
