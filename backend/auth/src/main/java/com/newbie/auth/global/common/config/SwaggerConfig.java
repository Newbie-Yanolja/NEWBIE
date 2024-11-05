package com.newbie.auth.global.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.domain}")
    private String serverUrl;

    @Value("${local.domain}")
    private String localUrl;

    @Bean
    public OpenAPI openAPI() {
        String jwt = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);
        Components components = new Components().addSecuritySchemes(jwt, new SecurityScheme()
            .name(jwt)
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
        );
        // HTTPS Server 추가
        Server server1 = new Server()
                .url(serverUrl)
                .description("server_login"); // 서버 설명
        Server server2 = new Server()
                .url(localUrl)
                .description("local_login"); // 서버 설명
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo())
                .addSecurityItem(securityRequirement)
                .components(components)
                .servers(List.of(server1,server2)); // 서버 리스트에 HTTPS 서버 추가
    }

    private Info apiInfo() {
        return new Info()
            .title("Newbie 소셜로그인 APIs")
            .description("Newbie social login APIs")
            .version("1.0.0");
    }
}
