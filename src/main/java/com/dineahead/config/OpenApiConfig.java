package com.dineahead.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {


    @Bean
    public OpenAPI dineAheadOpenAPI() {

        return new OpenAPI()

                .info(
                        new Info()
                                .title(
                                        "DineAhead API"
                                )
                                .description(
                                        "Restaurant reservation, "
                                                + "ordering and payment "
                                                + "management system API."
                                )
                                .version(
                                        "1.0.0"
                                )
                                .contact(
                                        new Contact()
                                                .name(
                                                        "DineAhead"
                                                )
                                )
                )

                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",

                                        new SecurityScheme()
                                                .type(
                                                        SecurityScheme.Type.HTTP
                                                )
                                                .scheme(
                                                        "bearer"
                                                )
                                                .bearerFormat(
                                                        "JWT"
                                                )
                                )
                );
    }
}