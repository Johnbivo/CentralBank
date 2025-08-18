package com.bivolaris.centralbank.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Central Banking System API")
                        .version("1.0.0")
                        .description("Comprehensive API for central banking operations including multi-currency transactions, fraud detection, and inter-bank communication")
                        .contact(new Contact()
                                .name("Banking System Team")
                                .email("support@centralbank.com")
                                .url("https://github.com/your-repo"))
                        .license(new License()
                                .name("Proprietary Read-Only License")
                                .url("https://github.com/yourusername/central-banking-system/blob/main/LICENSE")))
                .servers(List.of(
                        new Server().url("http://localhost:8080/api").description("Development Server"),
                        new Server().url("http://localhost:8080/api").description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token")));
    }
}
