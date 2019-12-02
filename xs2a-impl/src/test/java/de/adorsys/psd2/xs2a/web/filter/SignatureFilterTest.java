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

package de.adorsys.psd2.xs2a.web.filter;

import de.adorsys.psd2.validator.signature.DigestVerifier;
import de.adorsys.psd2.validator.signature.SignatureVerifier;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageBuilder;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageWriter;
import de.adorsys.psd2.xs2a.web.request.RequestPathResolver;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class SignatureFilterTest {
    private static final JsonReader jsonReader = new JsonReader();
    private static final String XS2A_URI = "/v1/consents";
    private static final String CUSTOM_PATH = "/custom-endpoint";

    private static final String POST_METHOD = "POST";

    @InjectMocks
    private SignatureFilter signatureFilter;

    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private TppErrorMessageBuilder tppErrorMessageBuilder;
    @Mock
    private TppErrorMessageWriter tppErrorMessageWriter;
    @Mock
    private DigestVerifier digestVerifier;
    @Mock
    private SignatureVerifier signatureVerifier;
    @Mock
    private FilterChain chain;
    @Mock
    private RequestPathResolver requestPathResolver;

    private Map<String, String> headerMap = new HashMap<>();

    private String body;

    @Before
    public void setUp() {
        when(aspspProfileService.getTppSignatureRequired()).thenReturn(true);
        fillDefaultHeaders();
        body = "correct json body";

        when(requestPathResolver.resolveRequestPath(any(HttpServletRequest.class)))
            .thenReturn(XS2A_URI);
    }

    @Test
    public void doFilter_success() throws IOException, ServletException {
        // given
        MockHttpServletRequest mockRequest = getCorrectRequest(headerMap, body, POST_METHOD);
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
    public void doFilter_invalid_digest() throws IOException, ServletException {
        // given
        MockHttpServletRequest mockRequest = getCorrectRequest(headerMap, body, POST_METHOD);
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
    public void doFilter_invalid_signature() throws IOException, ServletException {
        // given
        MockHttpServletRequest mockRequest = getCorrectRequest(headerMap, body, POST_METHOD);
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
    public void doFilter_SignatureRequired_Off() throws IOException, ServletException {
        // given
        MockHttpServletRequest mockRequest = getCorrectRequest(headerMap, body, POST_METHOD);
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        when(aspspProfileService.getTppSignatureRequired()).thenReturn(false);

        // when
        signatureFilter.doFilter(mockRequest, mockResponse, chain);

        //Then
        assertThat(mockResponse.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        verify(chain).doFilter(any(), any());
        verify(digestVerifier, times(0)).verify(any(), any());
        verify(signatureVerifier, times(0)).verify(any(), any(), any(), any(), any());
    }

    @Test
    public void doFilter_HeaderEmpty_xRequestId() throws IOException, ServletException {
        // given
        headerMap.remove("x-request-id");
        MockHttpServletRequest mockRequest = getCorrectRequest(headerMap, body, POST_METHOD);
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
    public void doFilter_HeaderEmpty_signature() throws IOException, ServletException {
        // given
        headerMap.remove("signature");
        MockHttpServletRequest mockRequest = getCorrectRequest(headerMap, body, POST_METHOD);
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
    public void doFilter_HeaderEmpty_signature_certificate() throws IOException, ServletException {
        // given
        headerMap.remove("tpp-signature-certificate");
        MockHttpServletRequest mockRequest = getCorrectRequest(headerMap, body, POST_METHOD);
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
    public void doFilter_HeaderEmpty_signature_digest() throws IOException, ServletException {
        // given
        headerMap.remove("digest");
        MockHttpServletRequest mockRequest = getCorrectRequest(headerMap, body, POST_METHOD);
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
    public void doFilter_HeaderEmpty_date() throws IOException, ServletException {
        // given
        headerMap.remove("date");
        MockHttpServletRequest mockRequest = getCorrectRequest(headerMap, body, POST_METHOD);
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
    public void doFilter_onCustomEndpoint_shouldSkipFilter() throws ServletException, IOException {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        when(requestPathResolver.resolveRequestPath(mockRequest))
            .thenReturn(CUSTOM_PATH);

        // When
        signatureFilter.doFilter(mockRequest, mockResponse, chain);

        // Then
        verify(chain).doFilter(mockRequest, mockResponse);
        verifyZeroInteractions(aspspProfileService, requestProviderService);
    }

    private MockHttpServletRequest getCorrectRequest(Map<String, String> headerMap, String body, String method) {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContent(body.getBytes());
        mockRequest.setMethod(method);
        mockRequest.setRequestURI(XS2A_URI);

        headerMap.forEach(mockRequest::addHeader);

        return mockRequest;
    }

    private void mockResponseCode(MockHttpServletResponse mockResponse, int code) throws IOException {
        doAnswer((i) -> {
            mockResponse.setStatus(code);
            return null;
        }).when(tppErrorMessageWriter).writeError(eq(mockResponse), eq(code), any());
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
