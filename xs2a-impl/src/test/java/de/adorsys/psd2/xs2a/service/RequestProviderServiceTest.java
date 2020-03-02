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
    private static final String TPP_QWAC_CERTIFICATE_HEADER_VALUE = "-----BEGIN CERTIFICATE-----MIIFQTCCAymgAwIBAgIESLvdaTANBgkqhkiG9w0BAQsFADB4MQswCQYDVQQGEwJERTEQMA4GA1UECAwHQkFWQVJJQTESMBAGA1UEBwwJTnVyZW1iZXJnMSIwIAYDVQQKDBlUcnVzdCBTZXJ2aWNlIFByb3ZpZGVyIEFHMR8wHQYDVQQLDBZJbmZvcm1hdGlvbiBUZWNobm9sb2d5MB4XDTE5MDMwNTE1MTIwN1oXDTIwMDMwNDAwMDAwMFowgcwxITAfBgNVBAoMGEZpY3Rpb25hbCBDb3Jwb3JhdGlvbiBBRzEJMAcGA1UEAwwAMSUwIwYKCZImiZPyLGQBGRYVcHVibGljLmNvcnBvcmF0aW9uLmRlMR8wHQYDVQQLDBZJbmZvcm1hdGlvbiBUZWNobm9sb2d5MRAwDgYDVQQGEwdHZXJtYW55MQ8wDQYDVQQIDAZCYXllcm4xEjAQBgNVBAcMCU51cmVtYmVyZzEdMBsGA1UEYQwUUFNEREUtRkFLRU5DQS04N0IyQUMwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCeDcYlVutZeGFtOkonIMGHwway2ASZl8p7/v7USIxeMo/5ppbAa6Ei7i7jH9ORBoHV6qxAwNFkdd8JDneNiNn0NSvoYTemr5mqyXYhwpzLueXth1oBgjLYvcaLFXXQGS0dd6sDcaCTbw9xdDmap+6xYDRzIrdviyiph1ewpUXlrEHNu5Oomk7R5Dpv4gM9uRwYiskRigZdnArfyQ3ZYW4VZvMlFW3t1IVvSiOWvruF24w+j1g3BOHNM7tIAOlOUYQpHV1G1ChcFt5/ICArtsGAd4/ZUzlmujktdO+hNA70fDHUxkG6vQRFQhSnszzJ/C/g632nMTJbAaGtO2OvdL9DAgMBAAGjfjB8MHoGCCsGAQUFBwEDBG4wbAYGBACBmCcCMGIwOTARBgcEAIGYJwEDDAZQU1BfQUkwEQYHBACBmCcBAgwGUFNQX1BJMBEGBwQAgZgnAQQMBlBTUF9JQwwZVHJ1c3QgU2VydmljZSBQcm92aWRlciBBRwwKREUtRkFLRU5DQTANBgkqhkiG9w0BAQsFAAOCAgEAK07yQviS7/zKm1EqQyyGkEbf/1sHb9FLPBr/BicYxc3IQGd4xG1SJ1uLudX37Yq/o6exjixZ8ywib27jNLCpsF1dEQabHNXgS4enojf7CVTyKjDkKqE1mwqPmGeoWWwaWOUsWQ2/Ja/UTW5Bn5iA+nHCXVrkcjFVnRvi+dSsRm4J3E0EdAAwBkSEqHGDZO1ZiAh20YkNExx8MKKiHAVZ0ZFCXzYcaWzaK6yeCarvyPNCb+BAsc1wf3/88tLT9Nof/Ctzv2L9OjGHcalCLf/g/qTr6/50J4IMVdBwoVkg27yRE5EC3RKJE5BFx6TNWeNGs7r8HpAhO/6hLKzVHjrsA8/SAwTWNQNWdP/azSV42DuVMjDi5o5Ax9RkHXRvjsuwTR19AKvIc6nv/8XUtwORjHW+FTXTGa28PqCD1ZACiHytIBXrETevmLIlFuh6ZaWKBYPUc3DmJbFSZkhRFybh1SEtl/WzeQjIKqkRw0MGzDIRwD0sYqeE8ENkJbXJG+Cy4c42mZmEwG6E7HQQtiT9Irt1cnUiFDRe6g+h4GaxhOC5Pluxhij4DaNHCIZm30IHcyA4vZOyj7rXcvvfGMwPgbSdqSdEeNB25FEmFmJnavESxyJKYNx3JONm//0yRpacfWos/MjmbLWynYz8Bv8EK7mCS84bmSlUrUgHoNvDeBc=-----END CERTIFICATE-----";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = MediaType.APPLICATION_JSON;
    private static final String TPP_BRAND_LOGGING_INFORMATION = "tpp-brand-logging-information";
    private static final String TPP_BRAND_LOGGING_INFORMATION_VALUE = "tppBrandLoggingInformation";

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
    void getTppBrandLoggingInformationHeader() {
        //Given
        when(httpServletRequest.getHeader(TPP_BRAND_LOGGING_INFORMATION)).thenReturn(TPP_BRAND_LOGGING_INFORMATION_VALUE);
        //When
        String tppBrandLoggingInformationHeader = requestProviderService.getTppBrandLoggingInformationHeader();
        //Then
        assertEquals(TPP_BRAND_LOGGING_INFORMATION_VALUE, tppBrandLoggingInformationHeader);
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
