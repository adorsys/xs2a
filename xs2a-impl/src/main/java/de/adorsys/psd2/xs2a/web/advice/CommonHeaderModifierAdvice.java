/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.advice;

import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@ControllerAdvice(basePackages = "de.adorsys.psd2.xs2a.web.controller")
public class CommonHeaderModifierAdvice implements ResponseBodyAdvice<Object> {

    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @Override
    public boolean supports(@NotNull MethodParameter returnType, @NotNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, @NotNull MethodParameter returnType, @NotNull MediaType selectedContentType, @NotNull Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        // Add X-Request-ID to all responses
        String requestId = getRequestIdFromHeaders(request);
        if (requestId != null) {
            safelyAddHeader(response.getHeaders(), "X-Request-ID", requestId);
        }

        // Add additional headers for non-OK responses (status code != 200)
        if (isNonOkResponse(response)) {
            addNonOkResponseHeaders(request, response);
        }
        return body;
    }

    /**
     * Safely adds a header to the response, handling the case where headers might be immutable.
     * If headers are immutable (from ResponseEntity.headers()), this method will log a debug message.
     * This is expected behavior when a controller returns ResponseEntity with pre-set headers.
     *
     * @param headers the HttpHeaders to modify
     * @param headerName the name of the header to add
     * @param headerValue the value of the header to add
     */
    private void safelyAddHeader(org.springframework.http.HttpHeaders headers, String headerName, String headerValue) {
        try {
            headers.add(headerName, headerValue);
        } catch (UnsupportedOperationException e) {
            // Headers are immutable (likely from ResponseEntity.headers())
            // This is expected when controller returns ResponseEntity with pre-set headers
            log.debug("Cannot add header '{}' - headers are immutable. This is expected when ResponseEntity includes pre-set headers.", headerName);
        }
    }

    private String getRequestIdFromHeaders(ServerHttpRequest request) {
        String requestId = request.getHeaders().getFirst("X-Request-ID");
        if (requestId == null) {
            // Fallback to other common casings if necessary
            requestId = request.getHeaders().getFirst("X-Request-Id");
        }
        return requestId;
    }

    private boolean isNonOkResponse(ServerHttpResponse response) {
        if (!(response instanceof ServletServerHttpResponse)) {
            return false;
        }

        ServletServerHttpResponse servletResponse = (ServletServerHttpResponse) response;
        HttpServletResponse httpServletResponse = servletResponse.getServletResponse();
        int actualStatusCode = httpServletResponse.getStatus();
        int expectedStatusCode = HttpStatus.OK.value();

        return actualStatusCode != expectedStatusCode;
    }

    private void addNonOkResponseHeaders(ServerHttpRequest request, ServerHttpResponse response) {
        org.springframework.http.HttpHeaders headers = response.getHeaders();
        addHeaderIfPresent(headers, "X-Reference-API-Name", aspspProfileServiceWrapper.getXReferenceApiName());
        addHeaderIfPresent(headers, "X-Reference-API-Document", aspspProfileServiceWrapper.getXReferenceApiDocument());
        addHeaderIfPresent(headers, "X-Reference-API-Version", aspspProfileServiceWrapper.getXReferenceApiVersion());
        addLocationHeaderIfAbsent(request, headers);
        addHeaderIfPresent(headers, "ASPSP-Notification-Support", getNotificationSupportedModesAsString());
    }

    private void addHeaderIfPresent(org.springframework.http.HttpHeaders headers, String headerName, String headerValue) {
        if (headerValue != null && !headerValue.isEmpty()) {
            safelyAddHeader(headers, headerName, headerValue);
        }
    }

    private void addLocationHeaderIfAbsent(ServerHttpRequest request, org.springframework.http.HttpHeaders headers) {
        if (!headers.containsKey("Location")) {
            String requestUri = request.getURI().toString();
            safelyAddHeader(headers, "Location", requestUri);
        }
    }

    private String getNotificationSupportedModesAsString() {
        List<NotificationSupportedMode> supportedModes = aspspProfileServiceWrapper.getNotificationSupportedModes();

        if (supportedModes == null || supportedModes.isEmpty()) {
            return null;
        }

        return supportedModes.stream()
                   .map(NotificationSupportedMode::getValue)
                   .collect(Collectors.joining(","));
    }

    protected boolean hasError(Object body, Class expectedClass) {
        return Optional.ofNullable(body).isPresent() && !body.getClass()
                                                             .isAssignableFrom(expectedClass);
    }
}
