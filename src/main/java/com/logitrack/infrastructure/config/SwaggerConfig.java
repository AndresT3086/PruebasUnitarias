package com.logitrack.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {


    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "Bearer Authentication";

        return new OpenAPI()
                .info(new Info()
                        .title("LogiTrack API")
                        .version("1.0.0")
                        .description("""
                    # LogiTrack - Package Tracking System
                    
```
                    CREATED → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED
                                                           → DELIVERY_FAILED → IN_TRANSIT
                                                           → RETURNED → IN_TRANSIT
```
                    
                    ## Authentication
                    All endpoints (except auth endpoints) require JWT authentication.
                    Include the JWT token in the Authorization header:
```
                    Authorization: Bearer <your-jwt-token>
```
                    
                    ## Roles
                    - **ADMIN**: Full access to all operations
                    - **OPERATOR**: Can create and modify packages
                    - **VIEWER**: Read-only access to package information
                    """)
                        .contact(new Contact()
                                .name("LogiTrack")
                                .email("darwin.tangarife@quind.io")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.logitrack.com")
                                .description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT Bearer token")));
    }
}