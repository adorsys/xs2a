/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
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
 * contact us at psd2@adorsys.com.
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
    private static final Map<String, String> HEADERS = jsonReader.getObjectFromFile("json/RequestHeaders.json", new TypeReference<>() {
    });
    private static final UUID INTERNAL_REQUEST_ID = UUID.fromString("9861d849-3302-4162-b79d-c5f8e543cdb0");
    private static final String TOKEN = "111111";
    private static final String TPP_ROLES_ALLOWED_HEADER = "tpp-roles-allowed";
    private static final String TPP_ROLES_ALLOWED_HEADER_VALUE = "AISP, PISP, PIISP, ASPSP";
    private static final String ACCEPT_HEADER_JSON = "application/json";
    private static final String ACCEPT_HEADER_ANY = "*/*";
    private static final String TPP_QWAC_CERTIFICATE_HEADER = "tpp-qwac-certificate";
    private static final String TPP_QWAC_CERTIFICATE_HEADER_VALUE = "-----BEGIN CERTIFICATE-----MIIFNjCCAx6gAwIBAgIERd3y8TANBgkqhkiG9w0BAQsFADB4MQswCQYDVQQGEwJERTEQMA4GA1UECAwHQkFWQVJJQTESMBAGA1UEBwwJTnVyZW1iZXJnMSIwIAYDVQQKDBlUcnVzdCBTZXJ2aWNlIFByb3ZpZGVyIEFHMR8wHQYDVQQLDBZJbmZvcm1hdGlvbiBUZWNobm9sb2d5MB4XDTIwMDMwNTEzMzk1MFoXDTMwMDMwMzAwMDAwMFowgcExITAfBgNVBAoMGEZpY3Rpb25hbCBDb3Jwb3JhdGlvbiBBRzElMCMGCgmSJomT8ixkARkWFXB1YmxpYy5jb3Jwb3JhdGlvbi5kZTEfMB0GA1UECwwWSW5mb3JtYXRpb24gVGVjaG5vbG9neTEQMA4GA1UEBhMHR2VybWFueTEPMA0GA1UECAwGQmF5ZXJuMRIwEAYDVQQHDAlOdXJlbWJlcmcxHTAbBgNVBGEMFFBTRERFLUZBS0VOQ0EtODdCMkFDMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsHAdLWn7pEAlD5daEjKv7hE4FW+vMJRrA/Bw2M/Zsu8VFfW1ARmbTgTy7rGLFBK/Y2SToEj60+5GEkCgCvi+vI/Bdykk8XqjpVsJjTW67np1b2Av8F61zvCnn2UOxBtXBHCzR1j2yz2om1IMYieGu/cDTWLNkbuoGSnj0dq4CbHp2f8ch++goffqLRXr642j8cVlqZYsapB8y+Z8IydbtNBd/XAmRTAprmdRv9B4PC7P+lIYX8QbXw77f+9/2Kty7oVHtjle+GnTR8wH5nCiMQsA9V564/34lKwuEkzuryV1HzitQ/X7FSZoiSQRTxbxjVO+xdzI3hjF2FZjVvkqywIDAQABo34wfDB6BggrBgEFBQcBAwRuMGwGBgQAgZgnAjBiMDkwEQYHBACBmCcBAwwGUFNQX0FJMBEGBwQAgZgnAQIMBlBTUF9QSTARBgcEAIGYJwEEDAZQU1BfSUMMGVRydXN0IFNlcnZpY2UgUHJvdmlkZXIgQUcMCkRFLUZBS0VOQ0EwDQYJKoZIhvcNAQELBQADggIBACKUQc3O3TOFG8tWk4sQd3f9SGlOcBOMekSXCxRgskcYkjhWW4+EN1FYzlGuXPfq1yngKaM3ss9yCDVep0MFa4hDJ/hzSSD5upExzwWDkUa97AHCjZd39W6kLaCMAc5vTbR9r7zBvMKBcAmhZ9mWCvrvbHUOURv5yBfrrEk4AM1Vakf5l+fWP4JhA779+7JlwpQRpy5dgqROwKQ2L634d2osgXUV4CkqhSUQ5LcYI4uBFyKnM0pyGaNYdKhBC95J0y5GYa7NpKJNZXf+clTbe33gCt2SFSOMa7CV5NYpnohS201uNd/ffWLzGtFBnHLNpX8qTfFc16mtIcJo6Iiof2CYgfYAyJByBC1gZHf1wAtfQzAn6JcEaJzmehXKKl9x7X62aaGan7l+MblUT65Gd+Yed+rXLF6svefbrcIbZwt/W+v1fbfnip9QEFPV3VLjg0vk9Y30ftZCcFRSHLD3mdxcVEtmVxDDxyzDUwXF7J/mi4RQhZBb3OtwwEIWC2zUaycNMZWJRI+RqfLvanlDFFMoYeSZKTFf8jS/PPcfpKOAiTGu21iuuv+gYxh/rgjW419w26ya+Q3jabaz3E9Im/opSU5sQ9W92ALA14J9VZs6v8BVmqKTB5APKfeTYoXg9MjP9fjVM/hP26kIgQVs5Bz15ov8uQlQC+OTO+2y5ozs-----END CERTIFICATE-----";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = MediaType.APPLICATION_JSON;
    private static final String TPP_BRAND_LOGGING_INFORMATION = "tpp-brand-logging-information";
    private static final String TPP_BRAND_LOGGING_INFORMATION_VALUE = "tppBrandLoggingInformation";
    private static final String INSTANCE_ID = "bank1";
    private static final String TPP_REJECTION_NO_FUNDS_PREFERRED = "tpp-rejection-nofunds-preferred";
    private static final String TPP_REDIRECT_PREFERRED_HEADER = "tpp-redirect-preferred";
    private static final String TPP_DECOUPLED_PREFERRED_HEADER = "tpp-decoupled-preferred";

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
    void resolveTppRedirectPreferred() {
        // Given
        when(httpServletRequest.getHeader(TPP_REDIRECT_PREFERRED_HEADER)).thenReturn("true");

        // When
        Optional<Boolean> actual = requestProviderService.resolveTppRedirectPreferred();

        // Then
        assertThat(actual).contains(true);
    }

    @Test
    void resolveTppRedirectPreferred_null() {
        // Given
        when(httpServletRequest.getHeader(TPP_REDIRECT_PREFERRED_HEADER)).thenReturn(null);

        // When
        Optional<Boolean> actual = requestProviderService.resolveTppRedirectPreferred();

        // Then
        assertThat(actual).isEmpty();
    }


    @Test
    void resolveTppDecoupledPreferred() {
        // Given
        when(httpServletRequest.getHeader(TPP_DECOUPLED_PREFERRED_HEADER)).thenReturn("true");

        // When
        Optional<Boolean> actual = requestProviderService.resolveTppDecoupledPreferred();

        // Then
        assertThat(actual).contains(true);
    }

    @Test
    void resolveTppDecoupledPreferred_null() {
        // Given
        when(httpServletRequest.getHeader(TPP_DECOUPLED_PREFERRED_HEADER)).thenReturn(null);

        // When
        Optional<Boolean> actual = requestProviderService.resolveTppDecoupledPreferred();

        // Then
        assertThat(actual).isEmpty();
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
    void getInstanceId() {
        when(httpServletRequest.getHeader(RequestProviderService.INSTANCE_ID)).thenReturn(INSTANCE_ID);
        // When
        String actual = requestProviderService.getInstanceId();

        // Then
        assertEquals(INSTANCE_ID, actual);
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

    @Test
    void getTppRejectionNoFundsPreferred_shouldReturnNull() {
        //When
        Boolean actual = requestProviderService.getTppRejectionNoFundsPreferred();

        //Then
        assertThat(actual).isNull();
    }

    @Test
    void getTppRejectionNoFundsPreferred_true() {
        //Given
        when(httpServletRequest.getHeader(TPP_REJECTION_NO_FUNDS_PREFERRED)).thenReturn("true");
        Boolean expected = Boolean.TRUE;

        //When
        Boolean actual = requestProviderService.getTppRejectionNoFundsPreferred();

        //Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getTppRejectionNoFundsPreferred_false() {
        //Given
        when(httpServletRequest.getHeader(TPP_REJECTION_NO_FUNDS_PREFERRED)).thenReturn("false");
        Boolean expected = Boolean.FALSE;

        //When
        Boolean actual = requestProviderService.getTppRejectionNoFundsPreferred();

        //Then
        assertThat(actual).isEqualTo(expected);
    }
}
