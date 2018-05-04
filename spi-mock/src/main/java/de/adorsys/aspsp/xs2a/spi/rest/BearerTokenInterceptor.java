package de.adorsys.aspsp.xs2a.spi.rest;

import de.adorsys.aspsp.xs2a.spi.rest.exception.RestException;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

import static de.adorsys.aspsp.xs2a.spi.domain.constant.AuthorizationConstant.AUTHORIZATION_HEADER;
import static de.adorsys.aspsp.xs2a.spi.domain.constant.AuthorizationConstant.BEARER_TOKEN_PREFIX;
import static org.springframework.util.StringUtils.isEmpty;

public class BearerTokenInterceptor implements ClientHttpRequestInterceptor {
    private String bearerToken;

    public BearerTokenInterceptor(String bearerToken) {
        if(isEmpty(bearerToken)){
            throw new RestException(HttpStatus.UNAUTHORIZED, "Token must not be empty");
        }
        this.bearerToken = bearerToken;
    }

    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().add(AUTHORIZATION_HEADER, BEARER_TOKEN_PREFIX + this.bearerToken);
        return execution.execute(request, body);
    }
}
