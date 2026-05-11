package com.todo.user_service.clients;

import com.todo.user_service.exception.NotFoundException;
import com.todo.user_service.exception.NotificationServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class NotificationServiceClientConfig {

    // Pulled from application.yaml:
    //   services.notification-url: http://localhost:6062        (dev)
    //   services.notification-url: ${NOTIFICATION_SERVICE_URL}  (prod)
    @Value("${services.notification-url}")
    private String notificationServiceUrl;

    @Bean
    public NotificationServiceClient notificationServiceClient(
            RestClient.Builder restClientBuilder) {   // plain builder — no @LoadBalanced

        RestClient restClient = restClientBuilder
                .baseUrl(notificationServiceUrl)      // direct URL, not Eureka service name
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
                    if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                        throw new NotFoundException("Notification Service: Not Found");
                    }
                    if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        throw new NotificationServiceException("Notification Service: Bad Request");
                    }
                })
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(NotificationServiceClient.class);
    }
}