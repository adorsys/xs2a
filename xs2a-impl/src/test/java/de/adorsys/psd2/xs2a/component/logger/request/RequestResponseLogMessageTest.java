/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class RequestResponseLogMessageTest {
    private static final String INTERNAL_REQUEST_ID_MESSAGE_FORMAT = "InR-ID: [%s]";
    private static final String REQUEST_URI_MESSAGE_FORMAT = "uri: [%s]";
    private static final String REQUEST_URI_QUERY_MESSAGE_FORMAT = "uri: [%s?%s]";
    private static final String REQUEST_HEADERS_MESSAGE_FORMAT = "requestHeaders: [%s: %s, %s: %s]";
    private static final String EMPTY_REQUEST_HEADERS_MESSAGE_FORMAT = "requestHeaders: [%s]";
    private static final String REQUEST_PAYLOAD_JSON_MESSAGE_FORMAT = "requestPayload: [%s]";
    private static final String REQUEST_PAYLOAD_MULTIPART_MESSAGE_FORMAT = "requestPayload: [%s=%s&%s=%s]";
    private static final String RESPONSE_STATUS_MESSAGE_FORMAT = "responseStatus: [%s]";
    private static final String RESPONSE_HEADERS_MESSAGE_FORMAT = "responseHeaders: [%s: %s, %s: %s]";
    private static final String EMPTY_RESPONSE_HEADERS_MESSAGE_FORMAT = "responseHeaders: [%s]";
    private static final String RESPONSE_BODY_MESSAGE_FORMAT = "responseBody: [%s]";
    private static final String RESPONSE_BODY_JSON_PATH = "json/component/logger/request/message-response-payload.json";
    private static final String REQUEST_BODY_JSON_PATH = "json/component/logger/request/message-request-payload.json";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String MULTIPART_CONTENT_TYPE = "multipart/form-data";
    private static final String MULTIPART_JSON_PART = "json_standingorderType";
    private static final String MULTIPART_XML_PART = "xml_sct";

    private JsonReader jsonReader = new JsonReader();

    private MockHttpServletRequest request = new MockHttpServletRequest();
    private MockHttpServletResponse response = new MockHttpServletResponse();

    @Test
    public void withInternalRequestId_shouldAddInternalRequestId() {
        // Given
        UUID internalRequestId = UUID.fromString("66982f5a-22bf-4fb4-8d11-92792e5e49d0");
        String expectedMessage = String.format(INTERNAL_REQUEST_ID_MESSAGE_FORMAT, internalRequestId);

        // When
        RequestResponseLogMessage logMessage = RequestResponseLogMessage.builder(request, response)
                                                   .withInternalRequestId(internalRequestId)
                                                   .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }

    @Test
    public void withRequestUri_shouldAddRequestUri() {
        // Given
        String requestUri = "some request uri";
        request.setRequestURI(requestUri);
        String expectedMessage = String.format(REQUEST_URI_MESSAGE_FORMAT, requestUri);

        // When
        RequestResponseLogMessage logMessage = RequestResponseLogMessage.builder(request, response)
                                                   .withRequestUri()
                                                   .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }

    @Test
    public void withRequestUri_shouldAddQueryParamsIfPresent() {
        // Given
        String requestUri = "some request uri";
        request.setRequestURI(requestUri);
        String requestQuery = "some query string";
        request.setQueryString(requestQuery);
        String expectedMessage = String.format(REQUEST_URI_QUERY_MESSAGE_FORMAT, requestUri, requestQuery);

        // When
        RequestResponseLogMessage logMessage = RequestResponseLogMessage.builder(request, response)
                                                   .withRequestUri()
                                                   .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }

    @Test
    public void withRequestHeaders_shouldAddRequestHeaders() {
        // Given
        String firstHeaderName = "Header 1";
        String firstHeaderValue = "Header 1 value";
        String secondHeaderName = "Header 2";
        String secondHeaderValue = "Header 2 value";

        request.addHeader(firstHeaderName, firstHeaderValue);
        request.addHeader(secondHeaderName, secondHeaderValue);
        String expectedMessage = String.format(REQUEST_HEADERS_MESSAGE_FORMAT,
                                               firstHeaderName, firstHeaderValue,
                                               secondHeaderName, secondHeaderValue);

        // When
        RequestResponseLogMessage logMessage = RequestResponseLogMessage.builder(request, response)
                                                   .withRequestHeaders()
                                                   .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }

    @Test
    public void withRequestHeaders__withNoHeadersInRequest_shouldAddEmptyHeaders() {
        // Given
        String expectedMessage = String.format(EMPTY_REQUEST_HEADERS_MESSAGE_FORMAT, "");

        // When
        RequestResponseLogMessage logMessage = RequestResponseLogMessage.builder(request, response)
                                                   .withRequestHeaders()
                                                   .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }

    @Test
    public void withRequestHeaders_withNullHeaderNamesInRequest_shouldSkipHeaders() {
        // Given
        NoHeaderNamesHttpServletRequest noHeaderNamesHttpServletRequest = new NoHeaderNamesHttpServletRequest();

        // When
        RequestResponseLogMessage logMessage = RequestResponseLogMessage.builder(noHeaderNamesHttpServletRequest, response)
                                                   .withRequestHeaders()
                                                   .build();

        // Then
        assertTrue(logMessage.getMessage().isEmpty());
    }

    @Test
    public void withRequestPayload_shouldAddJsonPayload() {
        // Given
        byte[] jsonPayload = jsonReader.getBytesFromFile(REQUEST_BODY_JSON_PATH);
        request.setContent(jsonPayload);
        request.setContentType(JSON_CONTENT_TYPE);
        MultiReadHttpServletRequest multiReadRequest = new MultiReadHttpServletRequest(request);

        String expectedMessage = String.format(REQUEST_PAYLOAD_JSON_MESSAGE_FORMAT, new String(jsonPayload));

        // When
        RequestResponseLogMessage logMessage = RequestResponseLogMessage.builder(multiReadRequest, response)
                                                   .withRequestPayload()
                                                   .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }


    @Test
    public void withRequestPayload_shouldAddMultipartPayloadIfPresent() {
        // Given
        String multipartXmlValue = "some xml part";
        request.setParameter(MULTIPART_XML_PART, multipartXmlValue);
        String jsonPart = jsonReader.getStringFromFile(REQUEST_BODY_JSON_PATH);
        request.setParameter(MULTIPART_JSON_PART, jsonPart);
        request.setContentType(MULTIPART_CONTENT_TYPE);

        String expectedMessage = String.format(REQUEST_PAYLOAD_MULTIPART_MESSAGE_FORMAT,
                                               MULTIPART_XML_PART, multipartXmlValue,
                                               MULTIPART_JSON_PART, jsonPart);

        // When
        RequestResponseLogMessage logMessage = RequestResponseLogMessage.builder(request, response)
                                                   .withRequestPayload()
                                                   .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }

    @Test
    public void withRequestPayload_withNonMultiReadServletRequest_shouldSkipPayload() {
        // Given
        byte[] jsonPayload = jsonReader.getBytesFromFile(REQUEST_BODY_JSON_PATH);
        request.setContent(jsonPayload);
        request.setContentType(JSON_CONTENT_TYPE);

        // When
        RequestResponseLogMessage logMessage = RequestResponseLogMessage.builder(request, response)
                                                   .withRequestPayload()
                                                   .build();

        // Then
        assertTrue(logMessage.getMessage().isEmpty());
    }

    @Test
    public void withRequestPayload_withoutPayloadInRequest_shouldAddEmptyPayload() {
        // Given
        MultiReadHttpServletRequest multiReadRequest = new MultiReadHttpServletRequest(request);

        String expectedMessage = String.format(REQUEST_PAYLOAD_JSON_MESSAGE_FORMAT, "");

        // When
        RequestResponseLogMessage logMessage = RequestResponseLogMessage.builder(multiReadRequest, response)
                                                   .withRequestPayload()
                                                   .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }

    @Test
    public void withResponseStatus_shouldAddResponseStatus() {
        // Given
        HttpStatus responseStatus = HttpStatus.OK;
        response.setStatus(responseStatus.value());

        String expectedMessage = String.format(RESPONSE_STATUS_MESSAGE_FORMAT, responseStatus.value());

        // When
        RequestResponseLogMessage logMessage = RequestResponseLogMessage.builder(request, response)
                                                   .withResponseStatus()
                                                   .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }

    @Test
    public void withResponseHeaders_shouldAddResponseHeaders() {
        // Given
        String firstHeaderName = "Response header 1";
        String firstHeaderValue = "Response header 1 value";
        String secondHeaderName = "Response header 2";
        String secondHeaderValue = "Response header 2 value";

        response.addHeader(firstHeaderName, firstHeaderValue);
        response.addHeader(secondHeaderName, secondHeaderValue);
        String expectedMessage = String.format(RESPONSE_HEADERS_MESSAGE_FORMAT,
                                               firstHeaderName, firstHeaderValue,
                                               secondHeaderName, secondHeaderValue);

        // When
        RequestResponseLogMessage logMessage = RequestResponseLogMessage.builder(request, response)
                                                   .withResponseHeaders()
                                                   .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }

    @Test
    public void withResponseHeaders__withNoHeadersInRequest_shouldAddEmptyHeaders() {
        // Given
        String expectedMessage = String.format(EMPTY_RESPONSE_HEADERS_MESSAGE_FORMAT, "");

        // When
        RequestResponseLogMessage logMessage = RequestResponseLogMessage.builder(request, response)
                                                   .withResponseHeaders()
                                                   .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }

    @Test
    public void withResponseBody_shouldAddResponseBody() throws IOException {
        // Given
        MultiReadHttpServletResponse multiReadResponse = new MultiReadHttpServletResponse(response);
        byte[] jsonPayload = jsonReader.getBytesFromFile(RESPONSE_BODY_JSON_PATH);
        multiReadResponse.getWriter().write(new String(jsonPayload));

        String expectedMessage = String.format(RESPONSE_BODY_MESSAGE_FORMAT, new String(jsonPayload));

        // When
        RequestResponseLogMessage logMessage = RequestResponseLogMessage.builder(request, multiReadResponse)
                                                   .withResponseBody()
                                                   .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }
}
