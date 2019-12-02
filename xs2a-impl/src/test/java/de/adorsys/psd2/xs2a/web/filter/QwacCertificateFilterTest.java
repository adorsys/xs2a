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

import de.adorsys.psd2.consent.api.service.TppService;
import de.adorsys.psd2.validator.certificate.util.TppCertificateData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppInfoHolder;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppRoleValidationService;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageBuilder;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageWriter;
import de.adorsys.psd2.xs2a.web.mapper.TppInfoRolesMapper;
import de.adorsys.psd2.xs2a.web.mapper.Xs2aTppInfoMapper;
import de.adorsys.psd2.xs2a.web.request.RequestPathResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CERTIFICATE_EXPIRED;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.ROLE_INVALID;
import static de.adorsys.psd2.xs2a.exception.MessageCategory.ERROR;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QwacCertificateFilterTest {
    private static final String TEST_QWAC_CERTIFICATE_VALID = "-----BEGIN CERTIFICATE-----MIIFQTCCAymgAwIBAgIESLvdaTANBgkqhkiG9w0BAQsFADB4MQswCQYDVQQGEwJERTEQMA4GA1UECAwHQkFWQVJJQTESMBAGA1UEBwwJTnVyZW1iZXJnMSIwIAYDVQQKDBlUcnVzdCBTZXJ2aWNlIFByb3ZpZGVyIEFHMR8wHQYDVQQLDBZJbmZvcm1hdGlvbiBUZWNobm9sb2d5MB4XDTE5MDMwNTE1MTIwN1oXDTIwMDMwNDAwMDAwMFowgcwxITAfBgNVBAoMGEZpY3Rpb25hbCBDb3Jwb3JhdGlvbiBBRzEJMAcGA1UEAwwAMSUwIwYKCZImiZPyLGQBGRYVcHVibGljLmNvcnBvcmF0aW9uLmRlMR8wHQYDVQQLDBZJbmZvcm1hdGlvbiBUZWNobm9sb2d5MRAwDgYDVQQGEwdHZXJtYW55MQ8wDQYDVQQIDAZCYXllcm4xEjAQBgNVBAcMCU51cmVtYmVyZzEdMBsGA1UEYQwUUFNEREUtRkFLRU5DQS04N0IyQUMwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCeDcYlVutZeGFtOkonIMGHwway2ASZl8p7/v7USIxeMo/5ppbAa6Ei7i7jH9ORBoHV6qxAwNFkdd8JDneNiNn0NSvoYTemr5mqyXYhwpzLueXth1oBgjLYvcaLFXXQGS0dd6sDcaCTbw9xdDmap+6xYDRzIrdviyiph1ewpUXlrEHNu5Oomk7R5Dpv4gM9uRwYiskRigZdnArfyQ3ZYW4VZvMlFW3t1IVvSiOWvruF24w+j1g3BOHNM7tIAOlOUYQpHV1G1ChcFt5/ICArtsGAd4/ZUzlmujktdO+hNA70fDHUxkG6vQRFQhSnszzJ/C/g632nMTJbAaGtO2OvdL9DAgMBAAGjfjB8MHoGCCsGAQUFBwEDBG4wbAYGBACBmCcCMGIwOTARBgcEAIGYJwEDDAZQU1BfQUkwEQYHBACBmCcBAgwGUFNQX1BJMBEGBwQAgZgnAQQMBlBTUF9JQwwZVHJ1c3QgU2VydmljZSBQcm92aWRlciBBRwwKREUtRkFLRU5DQTANBgkqhkiG9w0BAQsFAAOCAgEAK07yQviS7/zKm1EqQyyGkEbf/1sHb9FLPBr/BicYxc3IQGd4xG1SJ1uLudX37Yq/o6exjixZ8ywib27jNLCpsF1dEQabHNXgS4enojf7CVTyKjDkKqE1mwqPmGeoWWwaWOUsWQ2/Ja/UTW5Bn5iA+nHCXVrkcjFVnRvi+dSsRm4J3E0EdAAwBkSEqHGDZO1ZiAh20YkNExx8MKKiHAVZ0ZFCXzYcaWzaK6yeCarvyPNCb+BAsc1wf3/88tLT9Nof/Ctzv2L9OjGHcalCLf/g/qTr6/50J4IMVdBwoVkg27yRE5EC3RKJE5BFx6TNWeNGs7r8HpAhO/6hLKzVHjrsA8/SAwTWNQNWdP/azSV42DuVMjDi5o5Ax9RkHXRvjsuwTR19AKvIc6nv/8XUtwORjHW+FTXTGa28PqCD1ZACiHytIBXrETevmLIlFuh6ZaWKBYPUc3DmJbFSZkhRFybh1SEtl/WzeQjIKqkRw0MGzDIRwD0sYqeE8ENkJbXJG+Cy4c42mZmEwG6E7HQQtiT9Irt1cnUiFDRe6g+h4GaxhOC5Pluxhij4DaNHCIZm30IHcyA4vZOyj7rXcvvfGMwPgbSdqSdEeNB25FEmFmJnavESxyJKYNx3JONm//0yRpacfWos/MjmbLWynYz8Bv8EK7mCS84bmSlUrUgHoNvDeBc=-----END CERTIFICATE-----";
    private static final String TEST_QWAC_CERTIFICATE_EXPIRED = "-----BEGIN CERTIFICATE-----MIIEBjCCAu6gAwIBAgIEAmCHWTANBgkqhkiG9w0BAQsFADCBlDELMAkGA1UEBhMCREUxDzANBgNVBAgMBkhlc3NlbjESMBAGA1UEBwwJRnJhbmtmdXJ0MRUwEwYDVQQKDAxBdXRob3JpdHkgQ0ExCzAJBgNVBAsMAklUMSEwHwYDVQQDDBhBdXRob3JpdHkgQ0EgRG9tYWluIE5hbWUxGTAXBgkqhkiG9w0BCQEWCmNhQHRlc3QuZGUwHhcNMTgwODE3MDcxNzAyWhcNMTgwOTAzMDc1NzMxWjB6MRMwEQYDVQQDDApUUFAgU2FtcGxlMQwwCgYDVQQKDANvcmcxCzAJBgNVBAsMAm91MRAwDgYDVQQGEwdHZXJtYW55MQ8wDQYDVQQIDAZCYXllcm4xEjAQBgNVBAcMCU51cmVtYmVyZzERMA8GA1UEYQwIMTIzNDU5ODcwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCMMnLvNLvqxkHbxdcWRcyUrZ4oy++R/7hWMiWH4U+5kLTLICnlFofN3EgIuP5hZz9Zm8aPoJkr8Y1xEyP8X4a5YTFtMmrXwAOgW6BVTaBeO7eV6Me1yc2NawzWMNp0Zz/Lsnrmj2h7/dRYaYofFHjWPFRW+gjVwv95NFhcD9+H5rr+fMwoci0ERFvy70TYnLfuRrG1BpYOwEV+wVFRIciXE3CKjEh2wbz1Yr4DhD+6FtOElU8VPkWqGRZmr1n54apuLrxL9vIbt7qsaQirsUp5ez2SFGFTydUv+WqZaPGzONVptAymOfTcIsgcxDWx/liKlpdqwyXpJaOIrrXcEnQ1AgMBAAGjeTB3MHUGCCsGAQUFBwEDBGkwZwYGBACBmCcCMF0wTDARBgcEAIGYJwEBDAZQU1BfQVMwEQYHBACBmCcBAgwGUFNQX1BJMBEGBwQAgZgnAQMMBlBTUF9BSTARBgcEAIGYJwEEDAZQU1BfSUMMBEF1dGgMBzEyMTkwODgwDQYJKoZIhvcNAQELBQADggEBAKrHWMriNquiC1vfNKkJFPINi2T2J5FmRQfamrkzS3AI5zPPXx32MzbrTkQb+Zl7qTvClmIFpDG45YC+JVYz+4/gMSJChJfW+JYtyW/Am6eeIYZ1sk+VPvXgxuTA0aZLQsVHsaeTHnQ7lZzN3S0Ao5O35AGKqBITu6Mo1t4WglNJLZHZ0iFL92yfezfV7LF9JYAD/6JFVTeuBwKKHNjPupjeVBku/C7qVDbogo1Ubiowt+hMMPLVLPjxe6Xo9SUtkGj3+5ID4Z8NGHDaaF2IGVGaJkHK9+PYTYEBRDsbc1GwgzTzbds5lao6eMyepL/Kl7iUNtn3Vox/XiSymunGCmQ=-----END CERTIFICATE-----";
    private static final TppErrorMessage TPP_ERROR_MESSAGE_ACCESS = new TppErrorMessage(ERROR, ROLE_INVALID, "You don`t have the correct PSD2 role to access this service.");
    private static final TppErrorMessage TPP_ERROR_MESSAGE_EXPIRED = new TppErrorMessage(ERROR, CERTIFICATE_EXPIRED, "Certificate is expired");

    private static final String XS2A_PATH = "/v1/accounts";
    private static final String CUSTOM_PATH = "/custom-endpoint";

    @InjectMocks
    private QwacCertificateFilter qwacCertificateFilter;
    @Mock
    private TppRoleValidationService tppRoleValidationService;
    @Mock
    private TppInfoHolder tppInfoHolder;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private PrintWriter printWriter;
    @Mock
    private FilterChain chain;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private TppErrorMessageBuilder tppErrorMessageBuilder;
    @Mock
    private TppErrorMessageWriter tppErrorMessageWriter;
    @Mock
    private TppService tppService;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private Xs2aTppInfoMapper xs2aTppInfoMapper;
    @Mock
    private TppInfoRolesMapper tppInfoRolesMapper;
    @Mock
    private RequestPathResolver requestPathResolver;

    @Before
    public void setUp() {
        when(xs2aTppInfoMapper.mapToTppInfo(any(TppCertificateData.class))).thenReturn(new TppInfo());
        when(requestPathResolver.resolveRequestPath(request)).thenReturn(XS2A_PATH);
    }

    @Test
    public void doFilter_success() throws IOException, ServletException {
        //Given
        when(requestProviderService.getEncodedTppQwacCert()).thenReturn(TEST_QWAC_CERTIFICATE_VALID);

        //When
        qwacCertificateFilter.doFilter(request, response, chain);

        //Then
        verify(chain).doFilter(any(), any());
    }

    @Test
    public void doFilter_failure_expired_certificate() throws IOException, ServletException {
        //Given
        when(requestProviderService.getEncodedTppQwacCert()).thenReturn(TEST_QWAC_CERTIFICATE_EXPIRED);
        ArgumentCaptor<Integer> statusCode = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<TppErrorMessage> message = ArgumentCaptor.forClass(TppErrorMessage.class);
        when(requestProviderService.getRequestId()).thenReturn(UUID.randomUUID());
        when(tppErrorMessageBuilder.buildTppErrorMessage(ERROR, CERTIFICATE_EXPIRED)).thenReturn(TPP_ERROR_MESSAGE_EXPIRED);

        //When
        qwacCertificateFilter.doFilter(request, response, chain);

        //Then
        verify(tppErrorMessageWriter).writeError(eq(response), statusCode.capture(), message.capture());
        verify(chain, never()).doFilter(any(), any());
        assertEquals((Integer) 401, statusCode.getValue());
        assertEquals(TPP_ERROR_MESSAGE_EXPIRED, message.getValue());
    }

    @Test
    public void doFilter_success_check_tpp_roles_from_certificate() throws IOException, ServletException {
        //Given
        ArgumentCaptor<TppInfo> tppInfoArgumentCaptor = ArgumentCaptor.forClass(TppInfo.class);
        when(requestProviderService.getEncodedTppQwacCert()).thenReturn(TEST_QWAC_CERTIFICATE_VALID);
        when(requestProviderService.getTppRolesAllowedHeader()).thenReturn(null);
        when(tppRoleValidationService.hasAccess(any(), eq(request))).thenReturn(true);
        when(aspspProfileService.isCheckTppRolesFromCertificateSupported()).thenReturn(true);

        //When
        qwacCertificateFilter.doFilter(request, response, chain);

        //Then
        verify(chain).doFilter(any(), any());
        verify(tppInfoHolder).setTppInfo(tppInfoArgumentCaptor.capture());
        TppInfo tppInfo = tppInfoArgumentCaptor.getValue();
        verify(tppService, times(1)).updateTppInfo(tppInfo);
        assertTrue(tppInfo.getTppRoles().containsAll(EnumSet.of(TppRole.AISP, TppRole.PISP, TppRole.PIISP)));
    }

    @Test
    public void doFilter_success_check_tpp_roles_from_header() throws IOException, ServletException {
        //Given
        ArgumentCaptor<TppInfo> tppInfoArgumentCaptor = ArgumentCaptor.forClass(TppInfo.class);
        when(requestProviderService.getEncodedTppQwacCert()).thenReturn(TEST_QWAC_CERTIFICATE_VALID);
        when(tppRoleValidationService.hasAccess(any(), any())).thenReturn(true);
        List<TppRole> roles = Collections.singletonList(TppRole.AISP);
        when(tppInfoRolesMapper.mapToTppRoles(Arrays.asList("AISP"))).thenReturn(Arrays.asList(TppRole.AISP));
        String rolesRepresentation = roles.stream().map(TppRole::toString).collect(Collectors.joining(", "));
        when(requestProviderService.getTppRolesAllowedHeader()).thenReturn(rolesRepresentation);

        //When
        qwacCertificateFilter.doFilter(request, response, chain);

        //Then
        verify(chain).doFilter(any(), any());
        verify(tppInfoHolder).setTppInfo(tppInfoArgumentCaptor.capture());
        TppInfo tppInfo = tppInfoArgumentCaptor.getValue();
        verify(tppService, times(1)).updateTppInfo(tppInfo);
        assertEquals(roles, tppInfo.getTppRoles());
    }

    @Test
    public void doFilter_failure_wrong_tpp_roles() throws IOException, ServletException {
        //Given
        when(tppRoleValidationService.hasAccess(any(), any())).thenReturn(false);
        ArgumentCaptor<Integer> statusCode = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<TppErrorMessage> message = ArgumentCaptor.forClass(TppErrorMessage.class);
        when(requestProviderService.getEncodedTppQwacCert()).thenReturn(TEST_QWAC_CERTIFICATE_VALID);
        when(requestProviderService.getRequestId()).thenReturn(UUID.randomUUID());
        when(requestProviderService.getTppRolesAllowedHeader()).thenReturn("PIISP");
        when(tppInfoRolesMapper.mapToTppRoles(Arrays.asList("PIISP"))).thenReturn(Arrays.asList(TppRole.PIISP));
        when(tppErrorMessageBuilder.buildTppErrorMessage(ERROR, ROLE_INVALID)).thenReturn(TPP_ERROR_MESSAGE_ACCESS);

        //When
        qwacCertificateFilter.doFilter(request, response, chain);

        //Then
        verify(tppErrorMessageWriter).writeError(eq(response), statusCode.capture(), message.capture());
        verify(chain, never()).doFilter(any(), any());
        assertEquals((Integer) 401, statusCode.getValue());
        assertEquals(TPP_ERROR_MESSAGE_ACCESS, message.getValue());
    }

    @Test
    public void doFilter_success_no_check_tpp_roles() throws IOException, ServletException {
        //Given
        ArgumentCaptor<TppInfo> tppInfoArgumentCaptor = ArgumentCaptor.forClass(TppInfo.class);
        when(requestProviderService.getEncodedTppQwacCert()).thenReturn(TEST_QWAC_CERTIFICATE_VALID);
        when(aspspProfileService.isCheckTppRolesFromCertificateSupported()).thenReturn(false);
        //When
        qwacCertificateFilter.doFilter(request, response, chain);
        //Then
        verify(chain).doFilter(any(), any());
        verify(tppInfoHolder).setTppInfo(tppInfoArgumentCaptor.capture());
        TppInfo tppInfo = tppInfoArgumentCaptor.getValue();
        verify(tppService, never()).updateTppInfo(tppInfo);
        assertNull(tppInfo.getTppRoles());
    }

    @Test
    public void doFilter_onCustomEndpoint_shouldSkipFilter() throws ServletException, IOException {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        when(requestPathResolver.resolveRequestPath(mockRequest))
            .thenReturn(CUSTOM_PATH);

        // When
        qwacCertificateFilter.doFilter(mockRequest, mockResponse, chain);

        // Then
        verify(chain).doFilter(mockRequest, mockResponse);
        verifyZeroInteractions(requestProviderService, tppService, tppInfoHolder);
    }
}
