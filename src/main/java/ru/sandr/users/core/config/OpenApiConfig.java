package ru.sandr.users.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI usersOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Users Service API")
                        .version("v1")
                        .description("""
                                Contract for authentication, users management, university hierarchy and import operations.
                                API is intended for both frontend clients and internal backend integrations.
                                """)
                        .contact(new Contact()
                                .name("Users Service Team"))
                        .license(new License()
                                .name("Proprietary")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME))
                .schemaRequirement(BEARER_AUTH_SCHEME, new SecurityScheme()
                        .name(BEARER_AUTH_SCHEME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT access token from /auth/login"))
                .tags(List.of(
                        new Tag().name("Auth").description("Authentication and password recovery endpoints"),
                        new Tag().name("Users").description("Self-service profile endpoints"),
                        new Tag().name("Admin Users").description("Administration operations over users"),
                        new Tag().name("Hierarchy").description("University hierarchy management"),
                        new Tag().name("Teacher Access").description("Teacher to student groups access scopes"),
                        new Tag().name("Teacher Groups").description("Accessible student groups for authenticated teacher"),
                        new Tag().name("Imports").description("Bulk import endpoints"),
                        new Tag().name("JWKS").description("Public JWK set for JWT verification")
                ));
    }
}
