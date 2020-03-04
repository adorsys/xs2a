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

package de.adorsys.psd2.xs2a.web.filter;

import de.adorsys.psd2.consent.api.service.TppService;
import de.adorsys.psd2.validator.certificate.util.TppCertificateData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppInfoHolder;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppRoleValidationService;
import de.adorsys.psd2.xs2a.web.Xs2aEndpointChecker;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageWriter;
import de.adorsys.psd2.xs2a.web.mapper.TppInfoRolesMapper;
import de.adorsys.psd2.xs2a.web.mapper.Xs2aTppInfoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.domain.MessageCategory.ERROR;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CERTIFICATE_EXPIRED;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.ROLE_INVALID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QwacCertificateFilterTest {
    private static final String TEST_QWAC_CERTIFICATE_VALID = "-----BEGIN CERTIFICATE-----MIIFNjCCAx6gAwIBAgIEcQJvYzANBgkqhkiG9w0BAQsFADB4MQswCQYDVQQGEwJERTEQMA4GA1UECAwHQkFWQVJJQTESMBAGA1UEBwwJTnVyZW1iZXJnMSIwIAYDVQQKDBlUcnVzdCBTZXJ2aWNlIFByb3ZpZGVyIEFHMR8wHQYDVQQLDBZJbmZvcm1hdGlvbiBUZWNobm9sb2d5MB4XDTIwMDMwNDA5MTYxM1oXDTIxMDMwNDAwMDAwMFowgcExITAfBgNVBAoMGEZpY3Rpb25hbCBDb3Jwb3JhdGlvbiBBRzElMCMGCgmSJomT8ixkARkWFXB1YmxpYy5jb3Jwb3JhdGlvbi5kZTEfMB0GA1UECwwWSW5mb3JtYXRpb24gVGVjaG5vbG9neTEQMA4GA1UEBhMHR2VybWFueTEPMA0GA1UECAwGQmF5ZXJuMRIwEAYDVQQHDAlOdXJlbWJlcmcxHTAbBgNVBGEMFFBTRERFLUZBS0VOQ0EtODdCMkFDMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApLM+4noVMsJVHmdYpeLfAoT6ASyVedSTnhDc+uO9ZHBCW9CX2WdkL1M2ObG/qLJcI0kCiHE4wMmllgT+6Vec3Uce398406yGNLh95N9bEKjx+lIs7iM1KEmsrX2RjciStigMPu8nrTyZi8w8rlC7lCT0wQ63+u7W/Sr9guBUZCBmxWiWDOf4fQOsKhK0RvrP7Q4FeTsQEGdmwkQzQIzk0sNyDrVUqqs5qLqQJESTjQ1u9b2zQe0f14PPiNVFUIy0m/vxXl1O5pPiUlT8N+IAi9cHqUQxBdBtkvaAT6uFRJAPevGGtmQ66iEhl1pCfK5RpxnsS5GwS7vz81PYApPFIQIDAQABo34wfDB6BggrBgEFBQcBAwRuMGwGBgQAgZgnAjBiMDkwEQYHBACBmCcBAwwGUFNQX0FJMBEGBwQAgZgnAQIMBlBTUF9QSTARBgcEAIGYJwEEDAZQU1BfSUMMGVRydXN0IFNlcnZpY2UgUHJvdmlkZXIgQUcMCkRFLUZBS0VOQ0EwDQYJKoZIhvcNAQELBQADggIBABnAZg21fFw7qZ4ksOqj6l/Su61WmHSIxiDyoVQx3I1uaf4CM7vPn7anCduxCLmbyrqDgzV6gF00QkTZLhPeHbRf9g39wlxoOoOkuuIeo+Tq+xoIqJ7Nh+dDX4NJgbmNRlLFfpzftAQks+Xu9He+uze6twXMYYVLmPSJsfa1+aYYh1LroiXc/wcGVQeQ9mNmWR93ok4xryjKK0G1RfDM2gyRl36wBMDAf8SVCfEXlvwsMMC4Iue+CIlGXxgU7hKslRR3CPOpdi/nmXvuI+O8NXnOS4A3x8iM9erwOQgJ7g75p+kP/tMmWxU0LVKWqsQY+xBtp/UJ6LKXSS0ipK+Wc3OtIm+NSsFAo9oWwMI9wYRoyWzkfiklNLadBBJLN0RHuira87B0nNwWo7H3QomVuDkpwO2rfOgKlI+HltMYNSIUhMBxyGg18GoXZC3si0bNZx/NQFNM6oQjRVVgmnE81inc6g3KY7K/QQqYQW0d4vtR2LYEYqDYh0yiLhEDY+ztuEMa4OqgXpnmWx4Z8SE8WSRZwnBzBs6klJVDhg3SzfdX4wDh1SA3AqBx8Rl/FKkAdB/2gpMRHuTHHYJTPnJGwQOTWFO2I6raCY+yojcLpex2eIFJGJfP9IP9TulXYKzjCMBXLbUCRx+drTxd+1YAwv2rWrEyki2C5AqZQNKyXdMo-----END CERTIFICATE-----";
    private static final String TEST_QWAC_CERTIFICATE_EXPIRED = "-----BEGIN CERTIFICATE-----MIIEBjCCAu6gAwIBAgIEAmCHWTANBgkqhkiG9w0BAQsFADCBlDELMAkGA1UEBhMCREUxDzANBgNVBAgMBkhlc3NlbjESMBAGA1UEBwwJRnJhbmtmdXJ0MRUwEwYDVQQKDAxBdXRob3JpdHkgQ0ExCzAJBgNVBAsMAklUMSEwHwYDVQQDDBhBdXRob3JpdHkgQ0EgRG9tYWluIE5hbWUxGTAXBgkqhkiG9w0BCQEWCmNhQHRlc3QuZGUwHhcNMTgwODE3MDcxNzAyWhcNMTgwOTAzMDc1NzMxWjB6MRMwEQYDVQQDDApUUFAgU2FtcGxlMQwwCgYDVQQKDANvcmcxCzAJBgNVBAsMAm91MRAwDgYDVQQGEwdHZXJtYW55MQ8wDQYDVQQIDAZCYXllcm4xEjAQBgNVBAcMCU51cmVtYmVyZzERMA8GA1UEYQwIMTIzNDU5ODcwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCMMnLvNLvqxkHbxdcWRcyUrZ4oy++R/7hWMiWH4U+5kLTLICnlFofN3EgIuP5hZz9Zm8aPoJkr8Y1xEyP8X4a5YTFtMmrXwAOgW6BVTaBeO7eV6Me1yc2NawzWMNp0Zz/Lsnrmj2h7/dRYaYofFHjWPFRW+gjVwv95NFhcD9+H5rr+fMwoci0ERFvy70TYnLfuRrG1BpYOwEV+wVFRIciXE3CKjEh2wbz1Yr4DhD+6FtOElU8VPkWqGRZmr1n54apuLrxL9vIbt7qsaQirsUp5ez2SFGFTydUv+WqZaPGzONVptAymOfTcIsgcxDWx/liKlpdqwyXpJaOIrrXcEnQ1AgMBAAGjeTB3MHUGCCsGAQUFBwEDBGkwZwYGBACBmCcCMF0wTDARBgcEAIGYJwEBDAZQU1BfQVMwEQYHBACBmCcBAgwGUFNQX1BJMBEGBwQAgZgnAQMMBlBTUF9BSTARBgcEAIGYJwEEDAZQU1BfSUMMBEF1dGgMBzEyMTkwODgwDQYJKoZIhvcNAQELBQADggEBAKrHWMriNquiC1vfNKkJFPINi2T2J5FmRQfamrkzS3AI5zPPXx32MzbrTkQb+Zl7qTvClmIFpDG45YC+JVYz+4/gMSJChJfW+JYtyW/Am6eeIYZ1sk+VPvXgxuTA0aZLQsVHsaeTHnQ7lZzN3S0Ao5O35AGKqBITu6Mo1t4WglNJLZHZ0iFL92yfezfV7LF9JYAD/6JFVTeuBwKKHNjPupjeVBku/C7qVDbogo1Ubiowt+hMMPLVLPjxe6Xo9SUtkGj3+5ID4Z8NGHDaaF2IGVGaJkHK9+PYTYEBRDsbc1GwgzTzbds5lao6eMyepL/Kl7iUNtn3Vox/XiSymunGCmQ=-----END CERTIFICATE-----";
    private static final TppErrorMessage TPP_ERROR_MESSAGE_ACCESS = new TppErrorMessage(ERROR, ROLE_INVALID);
    private static final TppErrorMessage TPP_ERROR_MESSAGE_EXPIRED = new TppErrorMessage(ERROR, CERTIFICATE_EXPIRED);

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
    private Xs2aEndpointChecker xs2aEndpointChecker;
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

    @Test
    void doFilter_success() throws IOException, ServletException {
        //Given
        when(xs2aEndpointChecker.isXs2aEndpoint(request)).thenReturn(true);
        when(xs2aTppInfoMapper.mapToTppInfo(any(TppCertificateData.class))).thenReturn(new TppInfo());

        when(requestProviderService.getEncodedTppQwacCert()).thenReturn(TEST_QWAC_CERTIFICATE_VALID);

        //When
        qwacCertificateFilter.doFilter(request, response, chain);

        //Then
        verify(chain).doFilter(any(), any());
    }

    @Test
    void doFilter_failure_expired_certificate() throws IOException, ServletException {
        //Given
        when(xs2aEndpointChecker.isXs2aEndpoint(request)).thenReturn(true);

        when(requestProviderService.getEncodedTppQwacCert()).thenReturn(TEST_QWAC_CERTIFICATE_EXPIRED);
        ArgumentCaptor<Integer> statusCode = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<TppErrorMessage> message = ArgumentCaptor.forClass(TppErrorMessage.class);

        //When
        qwacCertificateFilter.doFilter(request, response, chain);

        //Then
        verify(tppErrorMessageWriter).writeError(eq(response), statusCode.capture(), message.capture());
        verify(chain, never()).doFilter(any(), any());
        assertEquals((Integer) 401, statusCode.getValue());
        assertEquals(TPP_ERROR_MESSAGE_EXPIRED, message.getValue());
    }

    @Test
    void doFilter_success_check_tpp_roles_from_certificate() throws IOException, ServletException {
        //Given
        when(xs2aEndpointChecker.isXs2aEndpoint(request)).thenReturn(true);
        when(xs2aTppInfoMapper.mapToTppInfo(any(TppCertificateData.class))).thenReturn(new TppInfo());

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
    void doFilter_success_check_tpp_roles_from_header() throws IOException, ServletException {
        //Given
        when(xs2aEndpointChecker.isXs2aEndpoint(request)).thenReturn(true);
        when(xs2aTppInfoMapper.mapToTppInfo(any(TppCertificateData.class))).thenReturn(new TppInfo());

        ArgumentCaptor<TppInfo> tppInfoArgumentCaptor = ArgumentCaptor.forClass(TppInfo.class);
        when(requestProviderService.getEncodedTppQwacCert()).thenReturn(TEST_QWAC_CERTIFICATE_VALID);
        when(tppRoleValidationService.hasAccess(any(), any())).thenReturn(true);
        List<TppRole> roles = Collections.singletonList(TppRole.AISP);
        when(tppInfoRolesMapper.mapToTppRoles(Collections.singletonList("AISP"))).thenReturn(Collections.singletonList(TppRole.AISP));
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
    void doFilter_failure_wrong_tpp_roles() throws IOException, ServletException {
        //Given
        when(xs2aEndpointChecker.isXs2aEndpoint(request)).thenReturn(true);
        when(xs2aTppInfoMapper.mapToTppInfo(any(TppCertificateData.class))).thenReturn(new TppInfo());

        when(tppRoleValidationService.hasAccess(any(), any())).thenReturn(false);
        ArgumentCaptor<Integer> statusCode = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<TppErrorMessage> message = ArgumentCaptor.forClass(TppErrorMessage.class);
        when(requestProviderService.getEncodedTppQwacCert()).thenReturn(TEST_QWAC_CERTIFICATE_VALID);
        when(requestProviderService.getTppRolesAllowedHeader()).thenReturn("PIISP");
        when(tppInfoRolesMapper.mapToTppRoles(Collections.singletonList("PIISP"))).thenReturn(Collections.singletonList(TppRole.PIISP));

        //When
        qwacCertificateFilter.doFilter(request, response, chain);

        //Then
        verify(tppErrorMessageWriter).writeError(eq(response), statusCode.capture(), message.capture());
        verify(chain, never()).doFilter(any(), any());
        assertEquals((Integer) 401, statusCode.getValue());
        assertEquals(TPP_ERROR_MESSAGE_ACCESS, message.getValue());
    }

    @Test
    void doFilter_success_no_check_tpp_roles() throws IOException, ServletException {
        //Given
        when(xs2aEndpointChecker.isXs2aEndpoint(request)).thenReturn(true);
        when(xs2aTppInfoMapper.mapToTppInfo(any(TppCertificateData.class))).thenReturn(new TppInfo());

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
    void doFilter_onCustomEndpoint_shouldSkipFilter() throws ServletException, IOException {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        // When
        qwacCertificateFilter.doFilter(mockRequest, mockResponse, chain);

        // Then
        verify(chain).doFilter(mockRequest, mockResponse);
        verifyNoMoreInteractions(requestProviderService, tppService, tppInfoHolder);
    }
}
