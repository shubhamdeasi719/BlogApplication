package com.blog_application.blogApp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class SwaggerConfig {

    @Bean
    OpenAPI myCustomConfig()
    {
        return new OpenAPI()
                .info(new Info()
                        .title("Blogging Application")
                        .description("Pure Backend Application for Blogging")
                        .version("v.1.0")
                        .contact(new Contact().email("shubhamdesai719@gmail.com"))
                        .license(new License().name("Copyright @ BlogApp 2025"))
                )
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080").description("Local"),
                        new Server().url("http://localhost:8081").description("Production")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")

                        )
                );
    }

}
