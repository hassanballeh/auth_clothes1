package com.auth.auth.config;

import org.keycloak.admin.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class KeycloakConfig {

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;
    
    @Value("${keycloak.password}")
    private String password;
    
    @Bean
    public Keycloak keycloakAdmin() {
        return  KeycloakBuilder.builder()
                    .serverUrl(keycloakUrl)
                    .realm("master")
                    .clientId("admin-cli")
                    .username("admin")
                    .password(password)
                    .build();
    }
}
