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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommonHeaderModifierAdviceTest {

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @InjectMocks
    private CommonHeaderModifierAdvice advice;

    @Mock
    private MethodParameter methodParameter;

    private ServerHttpRequest request;
    private ServerHttpResponse response;
    private MockHttpServletResponse servletResponse;
    private Class<? extends HttpMessageConverter<?>> converterType;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader("X-Request-Id", "test-request-123");
        servletRequest.setRequestURI("http://localhost:8080/v1/test-endpoint");

        servletResponse = new MockHttpServletResponse();

        request = new ServletServerHttpRequest(servletRequest);
        response = new ServletServerHttpResponse(servletResponse);
        converterType = MappingJackson2HttpMessageConverter.class;
    }

    @Test
    void supports_shouldReturnTrue() {
        boolean result = advice.supports(methodParameter, converterType);
        assertTrue(result);
    }

    @Test
    void beforeBodyWrite_shouldAddXRequestIdToAllResponses() {
        servletResponse.setStatus(HttpStatus.OK.value());

        advice.beforeBodyWrite(null, methodParameter, MediaType.APPLICATION_JSON, converterType, request, response);

        assertEquals("test-request-123", response.getHeaders().getFirst("X-Request-Id"));
    }

    @Test
    void beforeBodyWrite_shouldAddAllHeadersForNonOkResponse() {
        // Setup non-OK response (400)
        servletResponse.setStatus(HttpStatus.BAD_REQUEST.value());

        // Mock ASPSP profile service responses
        when(aspspProfileServiceWrapper.getXReferenceApiName()).thenReturn("VOP API");
        when(aspspProfileServiceWrapper.getXReferenceApiDocument()).thenReturn("https://example.com/api-doc");
        when(aspspProfileServiceWrapper.getXReferenceApiVersion()).thenReturn("1.3.15");
        when(aspspProfileServiceWrapper.getNotificationSupportedModes()).thenReturn(Collections.singletonList(NotificationSupportedMode.NONE));

        advice.beforeBodyWrite(null, methodParameter, MediaType.APPLICATION_JSON, converterType, request, response);

        HttpHeaders headers = response.getHeaders();

        // Verify X-Request-Id
        assertEquals("test-request-123", headers.getFirst("X-Request-Id"));

        // Verify ASPSP profile headers
        assertEquals("VOP API", headers.getFirst("X-Reference-API-Name"));
        assertEquals("https://example.com/api-doc", headers.getFirst("X-Reference-API-Document"));
        assertEquals("1.3.15", headers.getFirst("X-Reference-API-Version"));

        // Verify Location header
        assertNotNull(headers.getFirst("Location"));

        // Verify ASPSP-Notification-Support header
        assertEquals("none", headers.getFirst("ASPSP-Notification-Support"));
    }

    @Test
    void beforeBodyWrite_shouldNotAddExtraHeadersForOkResponse() {
        // Setup OK response (200)
        servletResponse.setStatus(HttpStatus.OK.value());

        advice.beforeBodyWrite(null, methodParameter, MediaType.APPLICATION_JSON, converterType, request, response);

        HttpHeaders headers = response.getHeaders();

        // Verify only X-Request-Id is added
        assertEquals("test-request-123", headers.getFirst("X-Request-Id"));

        // Verify ASPSP profile headers are NOT added for OK responses
        assertNull(headers.getFirst("X-Reference-API-Name"));
        assertNull(headers.getFirst("X-Reference-API-Document"));
        assertNull(headers.getFirst("X-Reference-API-Version"));
        assertNull(headers.getFirst("Location"));
        assertNull(headers.getFirst("ASPSP-Notification-Support"));
    }

    @Test
    void beforeBodyWrite_shouldHandleEmptyProfileValues() {
        // Setup non-OK response
        servletResponse.setStatus(HttpStatus.BAD_REQUEST.value());

        // Mock empty/null profile values
        when(aspspProfileServiceWrapper.getXReferenceApiName()).thenReturn("");
        when(aspspProfileServiceWrapper.getXReferenceApiDocument()).thenReturn(null);
        when(aspspProfileServiceWrapper.getXReferenceApiVersion()).thenReturn("");
        when(aspspProfileServiceWrapper.getNotificationSupportedModes()).thenReturn(Collections.emptyList());

        advice.beforeBodyWrite(null, methodParameter, MediaType.APPLICATION_JSON, converterType, request, response);

        HttpHeaders headers = response.getHeaders();

        // Verify empty values are not added
        assertNull(headers.getFirst("X-Reference-API-Name"));
        assertNull(headers.getFirst("X-Reference-API-Document"));
        assertNull(headers.getFirst("X-Reference-API-Version"));

        // Location should be added but ASPSP-Notification-Support should NOT be added (empty list)
        assertNotNull(headers.getFirst("Location"));
        assertNull(headers.getFirst("ASPSP-Notification-Support"));
    }

    @Test
    void beforeBodyWrite_shouldAddHeadersFor401Response() {
        servletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());

        when(aspspProfileServiceWrapper.getXReferenceApiName()).thenReturn("VOP API");
        when(aspspProfileServiceWrapper.getXReferenceApiDocument()).thenReturn("https://example.com/api-doc");
        when(aspspProfileServiceWrapper.getXReferenceApiVersion()).thenReturn("1.3.15");
        when(aspspProfileServiceWrapper.getNotificationSupportedModes()).thenReturn(Collections.singletonList(NotificationSupportedMode.NONE));

        advice.beforeBodyWrite(null, methodParameter, MediaType.APPLICATION_JSON, converterType, request, response);

        HttpHeaders headers = response.getHeaders();

        assertEquals("VOP API", headers.getFirst("X-Reference-API-Name"));
        assertEquals("https://example.com/api-doc", headers.getFirst("X-Reference-API-Document"));
        assertEquals("1.3.15", headers.getFirst("X-Reference-API-Version"));
        assertNotNull(headers.getFirst("Location"));
        assertEquals("none", headers.getFirst("ASPSP-Notification-Support"));
    }

    @Test
    void beforeBodyWrite_shouldAddHeadersFor500Response() {
        servletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

        when(aspspProfileServiceWrapper.getXReferenceApiName()).thenReturn("VOP API");
        when(aspspProfileServiceWrapper.getXReferenceApiDocument()).thenReturn("https://example.com/api-doc");
        when(aspspProfileServiceWrapper.getXReferenceApiVersion()).thenReturn("1.3.15");
        when(aspspProfileServiceWrapper.getNotificationSupportedModes()).thenReturn(Collections.singletonList(NotificationSupportedMode.NONE));

        advice.beforeBodyWrite(null, methodParameter, MediaType.APPLICATION_JSON, converterType, request, response);

        HttpHeaders headers = response.getHeaders();

        assertEquals("VOP API", headers.getFirst("X-Reference-API-Name"));
        assertEquals("https://example.com/api-doc", headers.getFirst("X-Reference-API-Document"));
        assertEquals("1.3.15", headers.getFirst("X-Reference-API-Version"));
        assertNotNull(headers.getFirst("Location"));
        assertEquals("none", headers.getFirst("ASPSP-Notification-Support"));
    }

    @Test
    void beforeBodyWrite_shouldAddMultipleNotificationModesAsCommaSeparated() {
        servletResponse.setStatus(HttpStatus.BAD_REQUEST.value());

        when(aspspProfileServiceWrapper.getXReferenceApiName()).thenReturn("VOP API");
        when(aspspProfileServiceWrapper.getXReferenceApiDocument()).thenReturn("https://example.com/api-doc");
        when(aspspProfileServiceWrapper.getXReferenceApiVersion()).thenReturn("1.3.15");
        List<NotificationSupportedMode> multipleModes = Arrays.asList(
            NotificationSupportedMode.SCA,
            NotificationSupportedMode.PROCESS,
            NotificationSupportedMode.LAST
        );
        when(aspspProfileServiceWrapper.getNotificationSupportedModes()).thenReturn(multipleModes);

        advice.beforeBodyWrite(null, methodParameter, MediaType.APPLICATION_JSON, converterType, request, response);

        HttpHeaders headers = response.getHeaders();

        assertEquals("sca,process,last", headers.getFirst("ASPSP-Notification-Support"));
    }

    @Test
    void beforeBodyWrite_shouldHandleNullNotificationModesList() {
        servletResponse.setStatus(HttpStatus.BAD_REQUEST.value());

        when(aspspProfileServiceWrapper.getXReferenceApiName()).thenReturn("VOP API");
        when(aspspProfileServiceWrapper.getXReferenceApiDocument()).thenReturn("https://example.com/api-doc");
        when(aspspProfileServiceWrapper.getXReferenceApiVersion()).thenReturn("1.3.15");
        when(aspspProfileServiceWrapper.getNotificationSupportedModes()).thenReturn(null);

        advice.beforeBodyWrite(null, methodParameter, MediaType.APPLICATION_JSON, converterType, request, response);

        HttpHeaders headers = response.getHeaders();

        // Should not add ASPSP-Notification-Support header when list is null
        assertNull(headers.getFirst("ASPSP-Notification-Support"));
    }
}
