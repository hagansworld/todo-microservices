package com.todo_service.clients;

import com.todo_service.exception.NotificationServiceException;
import com.todo_service.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class NotificationServiceClientConfig {

    @Bean
    public NotificationServiceClient notificationServiceClient(
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder restClientBuilder) {

        RestClient restClient = restClientBuilder
                .baseUrl("http://notification-service")
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (req, res) -> {
                    if (res.getStatusCode() == HttpStatus.NOT_FOUND) {
                        throw new NotFoundException("Notification Service: Not Found");
                    }
                    if (res.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        throw new NotificationServiceException("Notification Service: Bad Request");
                    }
                })
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(NotificationServiceClient.class);
    }
}
