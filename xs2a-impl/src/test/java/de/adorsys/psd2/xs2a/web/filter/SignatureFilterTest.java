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

package de.adorsys.psd2.xs2a.web.filter;

import de.adorsys.psd2.validator.signature.DigestVerifier;
import de.adorsys.psd2.validator.signature.SignatureVerifier;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.Xs2aEndpointChecker;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageWriter;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class SignatureFilterTest {
    private static final JsonReader jsonReader = new JsonReader();

    private static final String POST_METHOD = "POST";

    @InjectMocks
    private SignatureFilter signatureFilter;

    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private Xs2aEndpointChecker xs2aEndpointChecker;
    @Mock
    private TppErrorMessageWriter tppErrorMessageWriter;
    @Mock
    private DigestVerifier digestVerifier;
    @Mock
    private SignatureVerifier signatureVerifier;
    @Mock
    private FilterChain chain;

    private final Map<String, String> headerMap = new HashMap<>();

    private String body;

    @BeforeEach
    void setUp() {
        fillDefaultHeaders();
        body = "correct json body";

        when(xs2aEndpointChecker.isXs2aEndpoint(any())).thenReturn(true);
    }

    @Test
    void doFilter_success() throws IOException, ServletException {
        // given
        when(aspspProfileService.isTppSignatureRequired()).thenReturn(true);

        MockHttpServletRequest mockRequest = getCorrectRequest(headerMap, body);
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        String fullUrl = mockRequest.getRequestURL().toString();

        when(digestVerifier.verify(headerMap.get("digest"), body)).thenReturn(true);
        when(signatureVerifier.verify(headerMap.get("signature"), headerMap.get("tpp-signature-certificate"), headerMap, POST_METHOD, fullUrl)).thenReturn(true);

        // when
        signatureFilter.doFilter(mockRequest, mockResponse, chain);

        //Then
        assertThat(mockResponse.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        verify(chain).doFilter(any(), any());
    }

    @Test
    void doFilter_invalid_digest() throws IOException, ServletException {
        // given
        when(aspspProfileService.isTppSignatureRequired()).thenReturn(true);

        MockHttpServletRequest mockRequest = getCorrectRequest(headerMap, body);
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        mockResponseCode(mockResponse, HttpServletResponse.SC_BAD_REQUEST);

        when(digestVerifier.verify(headerMap.get("digest"), body)).thenReturn(false);

        // when
        signatureFilter.doFilter(mockRequest, mockResponse, chain);

        //Then
        assertThat(mockResponse.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
        verify(chain, times(0)).doFilter(any(), any());
    }

    @Test
    void doFilter_invalid_signature() throws IOException, ServletException {
        // given
        when(aspspProfileService.isTppSignatureRequired()).thenReturn(true);

        MockHttpServletRequest mockRequest = getCorrectRequest(headerMap, body);
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        String fullUrl = mockRequest.getRequestURL().toString();
        mockResponseCode(mockResponse, HttpServletResponse.SC_UNAUTHORIZED);

        when(digestVerifier.verify(headerMap.get("digest"), body)).thenReturn(true);
        when(signatureVerifier.verify(headerMap.get("signature"), headerMap.get("tpp-signature-certificate"), headerMap, POST_METHOD, fullUrl)).thenReturn(false);

        // when
        signatureFilter.doFilter(mockRequest, mockResponse, chain);

        //Then
        assertThat(mockResponse.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, times(0)).doFilter(any(), any());
    }

    @Test
    void doFilter_SignatureRequired_Off() throws IOException, ServletException {
        // given
        when(aspspProfileService.isTppSignatureRequired()).thenReturn(true);

        MockHttpServletRequest mockRequest = getCorrectRequest(headerMap, body);
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        when(aspspProfileService.isTppSignatureRequired()).thenReturn(false);

        // when
        signatureFilter.doFilter(mockRequest, mockResponse, chain);

        //Then
        assertThat(mockResponse.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        verify(chain).doFilter(any(), any());
        verify(digestVerifier, times(0)).verify(any(), any());
        verify(signatureVerifier, times(0)).verify(any(), any(), any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"x-request-id", "tpp-signature-certificate", "digest","date"})
    void doFilter_HeaderEmpt(String header) throws IOException, ServletException {
        // given
        when(aspspProfileService.isTppSignatureRequired()).thenReturn(true);

        headerMap.remove(header);
        MockHttpServletRequest mockRequest = getCorrectRequest(headerMap, body);
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        mockResponseCode(mockResponse, HttpServletResponse.SC_BAD_REQUEST);

        // when
        signatureFilter.doFilter(mockRequest, mockResponse, chain);

        //Then
        verify(chain, times(0)).doFilter(any(), any());
        verify(digestVerifier, times(0)).verify(any(), any());
        verify(signatureVerifier, times(0)).verify(any(), any(), any(), any(), any());
        assertThat(mockResponse.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void doFilter_HeaderEmpty_signature() throws IOException, ServletException {
        // given
        when(aspspProfileService.isTppSignatureRequired()).thenReturn(true);

        headerMap.remove("signature");
        MockHttpServletRequest mockRequest = getCorrectRequest(headerMap, body);
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        mockResponseCode(mockResponse, HttpServletResponse.SC_UNAUTHORIZED);

        // when
        signatureFilter.doFilter(mockRequest, mockResponse, chain);

        //Then
        verify(chain, times(0)).doFilter(any(), any());
        verify(digestVerifier, times(0)).verify(any(), any());
        verify(signatureVerifier, times(0)).verify(any(), any(), any(), any(), any());
        assertThat(mockResponse.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void doFilter_onCustomEndpoint_shouldSkipFilter() throws ServletException, IOException {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        when(xs2aEndpointChecker.isXs2aEndpoint(mockRequest)).thenReturn(false);

        // When
        signatureFilter.doFilter(mockRequest, mockResponse, chain);

        // Then
        verify(chain).doFilter(mockRequest, mockResponse);
        verifyNoMoreInteractions(aspspProfileService, requestProviderService);
    }

    private MockHttpServletRequest getCorrectRequest(Map<String, String> headerMap, String body) {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContent(body.getBytes());
        mockRequest.setMethod(SignatureFilterTest.POST_METHOD);

        headerMap.forEach(mockRequest::addHeader);

        return mockRequest;
    }

    private void mockResponseCode(MockHttpServletResponse mockResponse, int code) throws IOException {
        doAnswer((i) -> {
            mockResponse.setStatus(code);
            return null;
        }).when(tppErrorMessageWriter).writeError(eq(mockResponse), any(TppErrorMessage.class));
    }

    private void fillDefaultHeaders() {
        headerMap.put("psu-id", "anton.brueckner");
        headerMap.put("x-request-id", "2f77a125-aa7a-45c0-b414-cea25a116035");
        headerMap.put("signature", signature());
        headerMap.put("digest", "SHA-256=cE4iyBDKyP5qdfUyHuB4eZf5iqA5pSAjTzl8l89Oh20=");
        headerMap.put("date", "Sun, 06 Aug 2019 15:02:37 GMT");
        headerMap.put("tpp-signature-certificate", certificate());
        headerMap.put("tpp-redirect-uri", "http://bank.de.com/redirect-uri");
    }

    private String certificate() {
        return jsonReader.getStringFromFile("json/web/filter/signature/tpp_signature_certificate.txt");
    }

    private String signature() {
        return jsonReader.getStringFromFile("json/web/filter/signature/correct_signature.txt");
    }
}
