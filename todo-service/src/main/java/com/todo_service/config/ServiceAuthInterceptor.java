package com.todo_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * This injects the token into every outgoing service call.
 */

@Component
@RequiredArgsConstructor
public class ServiceAuthInterceptor implements ClientHttpRequestInterceptor {

    private final ServiceJwtProvider jwtProvider;

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {

        request.getHeaders()
                .setBearerAuth(jwtProvider.generateToken());

        return execution.execute(request, body);
    }
}
