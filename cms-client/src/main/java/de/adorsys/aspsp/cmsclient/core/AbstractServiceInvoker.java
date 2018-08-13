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

package de.adorsys.aspsp.cmsclient.core;

import de.adorsys.aspsp.cmsclient.core.util.HttpUriParams;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractServiceInvoker {
    private final RestClientConfig restClientConfig;

    protected AbstractServiceInvoker(RestClientConfig restClientConfig) {
        this.restClientConfig = restClientConfig;
    }

    public <T, R> R invoke(final RestRequestMethod<T, R> restRequestMethod) throws IOException, URISyntaxException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            return httpClient.execute(buildHttpRequestBase(restRequestMethod), new CommonResponseHandler<>(restRequestMethod.responseClass()));
        }
    }

    private <T, R> HttpRequestBase buildHttpRequestBase(RestRequestMethod<T, R> restRequestMethod) throws URISyntaxException {
        RequestConfig requestConfig = RequestConfig.custom()
                                          .setConnectTimeout(restClientConfig.getConnectTimeout())
                                          .setConnectionRequestTimeout(restClientConfig.getConnectionRequestTimeout())
                                          .build();

        HttpMethod httpMethod = restRequestMethod.httpMethod();
        T requestBody = restRequestMethod.requestBody();

        HttpRequestBase requestBase = requestBody != null
                                          ? httpMethod.getHttpRequest(restRequestMethod.requestBody())
                                          : httpMethod.getHttpRequest();
        requestBase.setConfig(requestConfig);
        requestBase.setURI(buildFullPath(restRequestMethod));
        restRequestMethod.headers().forEach(requestBase::addHeader);
        return requestBase;
    }

    private <T, R> URI buildFullPath(RestRequestMethod<T, R> restMethod) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(restClientConfig.getBaseServiceUrl())
                                    .setPath(enrichPathVariable(restMethod));
        HttpUriParams uriParams = restMethod.uriParams();
        if (uriParams != null) {
            uriBuilder.addParameters(uriParams.getRequestParams());
        }
        return uriBuilder.build();
    }

    private <T, R> String enrichPathVariable(RestRequestMethod<T, R> restMethod) {
        String path = restMethod.path();
        HttpUriParams uriParams = restMethod.uriParams();

        if (uriParams != null) {
            Map<String, String> pathVariableValues = uriParams.getPathVariables();
            Pattern pattern = Pattern.compile("\\{(.+?)}");
            Matcher matcher = pattern.matcher(path);

            while (matcher.find()) {
                String pathVariableNameWithoutBrackets = matcher.group(1);
                if (!pathVariableValues.containsKey(pathVariableNameWithoutBrackets)) {
                    throw new IllegalArgumentException("Path variable name not found in parameter map");
                }
                String pathVariableName = matcher.group();
                path = path.replaceFirst(Pattern.quote(pathVariableName), pathVariableValues.get(pathVariableNameWithoutBrackets));
            }
        }
        return path;
    }
}
