package com.todo_service.clients;

import com.todo_service.config.ServiceAuthInterceptor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /**
     * Plain RestClient
     * Used for non-service-discovery calls
     * Used by Eureka internally
     */
    @Bean("plainRestClientBuilder")
    @Primary
    public RestClient.Builder plainRestClientBuilder() {
        return RestClient.builder();
    }

    /**
     * Load-balanced RestClient
     * Used for service-to-service calls (Eureka)
     */
    @Bean("loadBalancedRestClientBuilder")
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder(
            ServiceAuthInterceptor interceptor
    ) {
        return RestClient.builder()
                .requestInterceptor(interceptor);
    }
}
