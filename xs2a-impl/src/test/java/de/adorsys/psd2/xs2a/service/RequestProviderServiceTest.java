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

package de.adorsys.psd2.xs2a.service;

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.RequestData;
import de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestProviderServiceTest {
    private static final String URI = "http://www.adorsys.de";
    private static final String IP = "192.168.0.26";
    private static final String ACCEPT_HEADER = "accept";
    private static PsuIdData PSU_ID_DATA;
    private static final JsonReader jsonReader = new JsonReader();
    private static final Map<String, String> HEADERS = jsonReader.getObjectFromFile("json/RequestHeaders.json", new TypeReference<Map<String, String>>() {
    });
    private static final UUID INTERNAL_REQUEST_ID = UUID.fromString("9861d849-3302-4162-b79d-c5f8e543cdb0");
    private static final String TOKEN = "111111";
    private static final String TPP_ROLES_ALLOWED_HEADER = "tpp-roles-allowed";
    private static final String TPP_ROLES_ALLOWED_HEADER_VALUE = "AISP, PISP, PIISP, ASPSP";
    private static final String ACCEPT_HEADER_JSON = "application/json";
    private static final String ACCEPT_HEADER_ANY = "*/*";
    private static final String TPP_QWAC_CERTIFICATE_HEADER = "tpp-qwac-certificate";
    private static final String TPP_QWAC_CERTIFICATE_HEADER_VALUE = "-----BEGIN CERTIFICATE-----MIIFNjCCAx6gAwIBAgIEcQJvYzANBgkqhkiG9w0BAQsFADB4MQswCQYDVQQGEwJERTEQMA4GA1UECAwHQkFWQVJJQTESMBAGA1UEBwwJTnVyZW1iZXJnMSIwIAYDVQQKDBlUcnVzdCBTZXJ2aWNlIFByb3ZpZGVyIEFHMR8wHQYDVQQLDBZJbmZvcm1hdGlvbiBUZWNobm9sb2d5MB4XDTIwMDMwNDA5MTYxM1oXDTIxMDMwNDAwMDAwMFowgcExITAfBgNVBAoMGEZpY3Rpb25hbCBDb3Jwb3JhdGlvbiBBRzElMCMGCgmSJomT8ixkARkWFXB1YmxpYy5jb3Jwb3JhdGlvbi5kZTEfMB0GA1UECwwWSW5mb3JtYXRpb24gVGVjaG5vbG9neTEQMA4GA1UEBhMHR2VybWFueTEPMA0GA1UECAwGQmF5ZXJuMRIwEAYDVQQHDAlOdXJlbWJlcmcxHTAbBgNVBGEMFFBTRERFLUZBS0VOQ0EtODdCMkFDMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApLM+4noVMsJVHmdYpeLfAoT6ASyVedSTnhDc+uO9ZHBCW9CX2WdkL1M2ObG/qLJcI0kCiHE4wMmllgT+6Vec3Uce398406yGNLh95N9bEKjx+lIs7iM1KEmsrX2RjciStigMPu8nrTyZi8w8rlC7lCT0wQ63+u7W/Sr9guBUZCBmxWiWDOf4fQOsKhK0RvrP7Q4FeTsQEGdmwkQzQIzk0sNyDrVUqqs5qLqQJESTjQ1u9b2zQe0f14PPiNVFUIy0m/vxXl1O5pPiUlT8N+IAi9cHqUQxBdBtkvaAT6uFRJAPevGGtmQ66iEhl1pCfK5RpxnsS5GwS7vz81PYApPFIQIDAQABo34wfDB6BggrBgEFBQcBAwRuMGwGBgQAgZgnAjBiMDkwEQYHBACBmCcBAwwGUFNQX0FJMBEGBwQAgZgnAQIMBlBTUF9QSTARBgcEAIGYJwEEDAZQU1BfSUMMGVRydXN0IFNlcnZpY2UgUHJvdmlkZXIgQUcMCkRFLUZBS0VOQ0EwDQYJKoZIhvcNAQELBQADggIBABnAZg21fFw7qZ4ksOqj6l/Su61WmHSIxiDyoVQx3I1uaf4CM7vPn7anCduxCLmbyrqDgzV6gF00QkTZLhPeHbRf9g39wlxoOoOkuuIeo+Tq+xoIqJ7Nh+dDX4NJgbmNRlLFfpzftAQks+Xu9He+uze6twXMYYVLmPSJsfa1+aYYh1LroiXc/wcGVQeQ9mNmWR93ok4xryjKK0G1RfDM2gyRl36wBMDAf8SVCfEXlvwsMMC4Iue+CIlGXxgU7hKslRR3CPOpdi/nmXvuI+O8NXnOS4A3x8iM9erwOQgJ7g75p+kP/tMmWxU0LVKWqsQY+xBtp/UJ6LKXSS0ipK+Wc3OtIm+NSsFAo9oWwMI9wYRoyWzkfiklNLadBBJLN0RHuira87B0nNwWo7H3QomVuDkpwO2rfOgKlI+HltMYNSIUhMBxyGg18GoXZC3si0bNZx/NQFNM6oQjRVVgmnE81inc6g3KY7K/QQqYQW0d4vtR2LYEYqDYh0yiLhEDY+ztuEMa4OqgXpnmWx4Z8SE8WSRZwnBzBs6klJVDhg3SzfdX4wDh1SA3AqBx8Rl/FKkAdB/2gpMRHuTHHYJTPnJGwQOTWFO2I6raCY+yojcLpex2eIFJGJfP9IP9TulXYKzjCMBXLbUCRx+drTxd+1YAwv2rWrEyki2C5AqZQNKyXdMo-----END CERTIFICATE-----";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = MediaType.APPLICATION_JSON;

    @InjectMocks
    private RequestProviderService requestProviderService;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private InternalRequestIdService internalRequestIdService;

    @BeforeEach
    void setUp() {
        PSU_ID_DATA = buildPsuIdData();
    }

    @Test
    void getRequestDataSuccess() {
        //Given
        HEADERS.forEach((key, value) -> when(httpServletRequest.getHeader(key)).thenReturn(value));
        when(httpServletRequest.getHeaderNames()).thenReturn(Collections.enumeration(HEADERS.keySet()));
        when(httpServletRequest.getRequestURI()).thenReturn(URI);
        when(httpServletRequest.getRemoteAddr()).thenReturn(IP);
        when(internalRequestIdService.getInternalRequestId()).thenReturn(INTERNAL_REQUEST_ID);
        //When
        RequestData requestData = requestProviderService.getRequestData();
        //Then
        assertEquals(IP, requestData.getIp());
        assertEquals(URI, requestData.getUri());
        assertEquals(HEADERS.get(Xs2aHeaderConstant.X_REQUEST_ID), requestData.getRequestId().toString());
        assertEquals(PSU_ID_DATA, requestData.getPsuIdData());
        assertEquals(HEADERS, requestData.getHeaders());
        assertEquals(INTERNAL_REQUEST_ID, requestData.getInternalRequestId());
    }

    @Test
    void getPsuIpAddress() {
        //Given
        when(httpServletRequest.getHeader(Xs2aHeaderConstant.PSU_IP_ADDRESS)).thenReturn(HEADERS.get(Xs2aHeaderConstant.PSU_IP_ADDRESS));
        //When
        String psuIpAddress = requestProviderService.getPsuIpAddress();
        //Then
        assertEquals(HEADERS.get(Xs2aHeaderConstant.PSU_IP_ADDRESS), psuIpAddress);
        assertTrue(requestProviderService.isRequestFromPsu());
        assertFalse(requestProviderService.isRequestFromTPP());
    }

    @Test
    void getInternalRequestId() {
        when(internalRequestIdService.getInternalRequestId()).thenReturn(INTERNAL_REQUEST_ID);
        // When
        UUID actualInternalRequestId = requestProviderService.getInternalRequestId();

        // Then
        verify(internalRequestIdService).getInternalRequestId();
        assertEquals(INTERNAL_REQUEST_ID, actualInternalRequestId);
    }

    @Test
    void getOAuth2Token() {
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(TOKEN);
        // When
        String actualAuthorisationHeader = requestProviderService.getOAuth2Token();

        // Then
        assertEquals(TOKEN, actualAuthorisationHeader);
    }

    @Test
    void getTppRolesAllowedHeader() {
        when(httpServletRequest.getHeader(TPP_ROLES_ALLOWED_HEADER)).thenReturn(TPP_ROLES_ALLOWED_HEADER_VALUE);
        // When
        String tppRolesAllowedHeader = requestProviderService.getTppRolesAllowedHeader();

        // Then
        assertEquals(TPP_ROLES_ALLOWED_HEADER_VALUE, tppRolesAllowedHeader);
    }

    @Test
    void getEncodedTppQwacCert() {
        when(httpServletRequest.getHeader(TPP_QWAC_CERTIFICATE_HEADER)).thenReturn(TPP_QWAC_CERTIFICATE_HEADER_VALUE);
        // When
        String encodedTppQwacCert = requestProviderService.getEncodedTppQwacCert();

        // Then
        assertEquals(TPP_QWAC_CERTIFICATE_HEADER_VALUE, encodedTppQwacCert);
    }

    @Test
    void getContentTypeHeader() {
        //Given
        when(httpServletRequest.getHeader(CONTENT_TYPE_HEADER)).thenReturn(CONTENT_TYPE_VALUE);
        //When
        String contentTypeHeader = requestProviderService.getContentTypeHeader();
        //Then
        assertEquals(CONTENT_TYPE_VALUE, contentTypeHeader);
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData(HEADERS.get(Xs2aHeaderConstant.PSU_ID),
                             HEADERS.get(Xs2aHeaderConstant.PSU_ID_TYPE),
                             HEADERS.get(Xs2aHeaderConstant.PSU_CORPORATE_ID),
                             HEADERS.get(Xs2aHeaderConstant.PSU_CORPORATE_ID_TYPE),
                             HEADERS.get(Xs2aHeaderConstant.PSU_IP_ADDRESS));
    }

    @Test
    void getAcceptHeader() {
        // Given
        when(httpServletRequest.getHeader(ACCEPT_HEADER)).thenReturn(ACCEPT_HEADER_JSON);

        // When
        String actual = requestProviderService.getAcceptHeader();

        // Then
        assertEquals(ACCEPT_HEADER_JSON, actual);
    }

    @Test
    void getAcceptHeader_withNullValue_shouldReturnAny() {
        // When
        String actual = requestProviderService.getAcceptHeader();

        // Then
        assertEquals(ACCEPT_HEADER_ANY, actual);
    }
}
