/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.component.logger.request;

import de.adorsys.psd2.xs2a.component.MultiReadHttpServletRequest;
import de.adorsys.psd2.xs2a.component.MultiReadHttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestResponseLogMessage {
    String message;

    /**
     * Creates new {@link RequestResponseLogMessage} builder from given request and response
     *
     * @param request  information about the request
     * @param response information about the response
     * @return new builder
     */
    public static RequestResponseLogMessageBuilder builder(@NotNull HttpServletRequest request,
                                                           @NotNull HttpServletResponse response) {
        return new RequestResponseLogMessageBuilder(request, response);
    }

    public static class RequestResponseLogMessageBuilder {
        private static final String URI = "uri";
        private static final String REQUEST_HEADERS = "requestHeaders";
        private static final String RESPONSE_HEADERS = "responseHeaders";
        private static final String PAYLOAD = "requestPayload";
        private static final String RESPONSE_STATUS = "responseStatus";
        private static final String RESPONSE_BODY = "responseBody";
        private static final String MULTIPART_FORM_DATA = "multipart/form-data";
        private static final String MULTIPART_VALUES_SEPARATOR = "&";
        private static final String QUERY_SEPARATOR = "?";

        private final Map<String, String> logParams = new LinkedHashMap<>();
        private final HttpServletRequest request;
        private final HttpServletResponse response;

        private RequestResponseLogMessageBuilder(HttpServletRequest request, HttpServletResponse response) {
            this.request = request;
            this.response = response;
        }

        /**
         * Adds request URI to the log message along with a query parameters if they are present
         *
         * @return builder
         */
        public RequestResponseLogMessageBuilder withRequestUri() {
            String uri = extractUri(request);
            logParams.put(URI, uri);
            return this;
        }

        /**
         * Adds request headers to the log message
         *
         * @return builder
         */
        public RequestResponseLogMessageBuilder withRequestHeaders() {
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                String requestHeadersMessage = extractHeaders(Collections.list(headerNames), request::getHeader);
                logParams.put(REQUEST_HEADERS, requestHeadersMessage);
            }

            return this;
        }

        /**
         * Adds request payload to the log message
         * <p>
         * Payload is extracted either from request parameters (in case of multipart message) or from the request body.
         * No message will be added to the log if payload doesn't exist in the current request or if it couldn't be
         * extracted.
         *
         * @return builder
         */
        public RequestResponseLogMessageBuilder withRequestPayload() {
            String payload;
            String contentType = request.getContentType();
            if (contentType != null && contentType.contains(MULTIPART_FORM_DATA)) {
                payload = extractRequestParametersPayload(request);
            } else {
                payload = extractRequestBody(request);
            }

            if (payload != null) {
                logParams.put(PAYLOAD, payload);
            }

            return this;
        }

        /**
         * Adds response status to the log message
         *
         * @return builder
         */
        public RequestResponseLogMessageBuilder withResponseStatus() {
            logParams.put(RESPONSE_STATUS, String.valueOf(response.getStatus()));
            return this;
        }

        /**
         * Adds response headers to the log message
         *
         * @return builder
         */
        public RequestResponseLogMessageBuilder withResponseHeaders() {
            String responseHeadersMessage = extractHeaders(response.getHeaderNames(), response::getHeader);
            logParams.put(RESPONSE_HEADERS, responseHeadersMessage);
            return this;
        }

        /**
         * Adds response body to the log message
         * <p>
         * No message will be added to the log if body isn't present in the response or if it couldn't be
         * extracted.
         *
         * @return builder
         */
        public RequestResponseLogMessageBuilder withResponseBody() {
            String body = extractResponseBody(response);
            if (body != null) {
                logParams.put(RESPONSE_BODY, body);
            }

            return this;
        }

        /**
         * Constructs new instance of {@link RequestResponseLogMessage} with a message from this builder
         *
         * @return new instance of {@link RequestResponseLogMessage}
         */
        public RequestResponseLogMessage build() {
            String logMessage = logParams.entrySet()
                                    .stream()
                                    .map(e -> e.getKey() + ": [" + e.getValue() + "]")
                                    .collect(Collectors.joining(", "));

            return new RequestResponseLogMessage(logMessage);
        }

        private String extractUri(HttpServletRequest request) {
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();
            if (queryString != null) {
                uri += QUERY_SEPARATOR + queryString;
            }

            return uri;
        }

        @Nullable
        private String extractRequestBody(HttpServletRequest request) {
            MultiReadHttpServletRequest wrapper =
                WebUtils.getNativeRequest(request, MultiReadHttpServletRequest.class);
            if (wrapper != null) {
                try {
                    byte[] requestBytes = IOUtils.toByteArray(wrapper.getInputStream());
                    return extractBody(requestBytes);
                } catch (IOException e) {
                    return null;
                }
            }

            return null;
        }

        private String extractRequestParametersPayload(HttpServletRequest request) {
            Map<String, String[]> requestParams = request.getParameterMap();

            return requestParams.entrySet()
                       .stream()
                       .map(e -> e.getKey() + "=" + String.join(MULTIPART_VALUES_SEPARATOR, e.getValue()))
                       .collect(Collectors.joining(MULTIPART_VALUES_SEPARATOR));
        }

        @Nullable
        private String extractResponseBody(HttpServletResponse response) {
            MultiReadHttpServletResponse wrapper =
                WebUtils.getNativeResponse(response, MultiReadHttpServletResponse.class);

            if (wrapper != null) {
                byte[] cachedContent = wrapper.getCachedContent();
                return extractBody(cachedContent);
            }

            return null;
        }

        private String extractHeaders(Collection<String> headerNames, UnaryOperator<String> headerValueExtractor) {
            return headerNames
                       .stream()
                       .map(header -> header + ": " + headerValueExtractor.apply(header))
                       .collect(Collectors.joining(", "));
        }

        private String extractBody(byte[] body) {
            return new String(body, StandardCharsets.UTF_8);
        }
    }
}
