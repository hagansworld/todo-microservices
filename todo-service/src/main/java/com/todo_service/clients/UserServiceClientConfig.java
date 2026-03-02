package com.todo_service.clients;

import com.todo_service.exception.NotFoundException;
import com.todo_service.exception.UserServiceException;
import com.todo_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
public class UserServiceClientConfig {

    private final JwtService jwtService;

    @Bean
    public UserServiceClient userServiceClient(
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder restClientBuilder
    ) {

        RestClient restClient = restClientBuilder
                .baseUrl("http://user-service")

                //  inject fresh JWT on EVERY request
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().set(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + jwtService.generateTokenForService()
                    );
                    return execution.execute(request, body);
                })

                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (req, res) -> {
                    if (res.getStatusCode() == HttpStatus.NOT_FOUND) {
                        throw new NotFoundException("User Service: User not found");
                    }
                    if (res.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        throw new UserServiceException("User Service: Bad Request");
                    }
                })
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);

        return HttpServiceProxyFactory
                .builderFor(adapter)
                .build()
                .createClient(UserServiceClient.class);
    }
}
