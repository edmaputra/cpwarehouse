package io.github.edmaputra.cpwarehouse.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for CP Warehouse API documentation.
 * Provides Swagger UI and OpenAPI specification.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cpWarehouseOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Development Server");

        Contact contact = new Contact();
        contact.setName("CP Warehouse Team");
        contact.setEmail("support@cpwarehouse.io");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("CP Warehouse API")
                .version("1.0.0")
                .contact(contact)
                .description("E-Commerce Stock Management System API. " +
                        "Provides endpoints for managing items, variants, stock inventory, " +
                        "checkout operations, and payment processing with optimistic locking support.")
                .termsOfService("https://cpwarehouse.io/terms")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
}
