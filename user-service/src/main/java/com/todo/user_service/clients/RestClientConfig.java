package com.todo.user_service.clients;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /**
     * Single plain RestClient.Builder.
     *
     * No @LoadBalanced needed — we're calling notification-service
     * directly via the URL in services.notification-url, not via Eureka.
     */
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}