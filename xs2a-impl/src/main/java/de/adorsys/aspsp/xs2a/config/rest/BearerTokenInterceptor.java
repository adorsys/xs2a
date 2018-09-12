/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.xs2a.config.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

import static de.adorsys.aspsp.xs2a.spi.domain.constant.AuthorizationConstant.AUTHORIZATION_HEADER;
import static de.adorsys.aspsp.xs2a.spi.domain.constant.AuthorizationConstant.BEARER_TOKEN_PREFIX;

public class BearerTokenInterceptor implements ClientHttpRequestInterceptor {
    private String bearerToken;

    public BearerTokenInterceptor(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpHeaders headers = request.getHeaders();
        headers.add(AUTHORIZATION_HEADER, BEARER_TOKEN_PREFIX + this.bearerToken);
        return execution.execute(request, body);
    }
}
