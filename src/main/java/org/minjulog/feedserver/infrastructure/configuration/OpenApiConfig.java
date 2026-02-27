package org.minjulog.feedserver.infrastructure.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI feedServerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Minjulog Feed Server API")
                        .description("REST API documentation for minjulog feed server")
                        .version("v1"));
    }
}
