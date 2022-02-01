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

package de.adorsys.psd2.xs2a.web.filter.holder;

import de.adorsys.psd2.consent.api.service.TppService;
import de.adorsys.psd2.validator.certificate.util.TppCertificateData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppInfoHolder;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppRoleValidationService;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageWriter;
import de.adorsys.psd2.xs2a.web.filter.TppErrorMessage;
import de.adorsys.psd2.xs2a.web.mapper.Xs2aTppInfoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.domain.MessageCategory.ERROR;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CERTIFICATE_EXPIRED;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.ROLE_INVALID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QwacCertificateServiceTest {
    private static final String TEST_QWAC_CERTIFICATE_VALID = "-----BEGIN CERTIFICATE-----MIIFNjCCAx6gAwIBAgIERd3y8TANBgkqhkiG9w0BAQsFADB4MQswCQYDVQQGEwJERTEQMA4GA1UECAwHQkFWQVJJQTESMBAGA1UEBwwJTnVyZW1iZXJnMSIwIAYDVQQKDBlUcnVzdCBTZXJ2aWNlIFByb3ZpZGVyIEFHMR8wHQYDVQQLDBZJbmZvcm1hdGlvbiBUZWNobm9sb2d5MB4XDTIwMDMwNTEzMzk1MFoXDTMwMDMwMzAwMDAwMFowgcExITAfBgNVBAoMGEZpY3Rpb25hbCBDb3Jwb3JhdGlvbiBBRzElMCMGCgmSJomT8ixkARkWFXB1YmxpYy5jb3Jwb3JhdGlvbi5kZTEfMB0GA1UECwwWSW5mb3JtYXRpb24gVGVjaG5vbG9neTEQMA4GA1UEBhMHR2VybWFueTEPMA0GA1UECAwGQmF5ZXJuMRIwEAYDVQQHDAlOdXJlbWJlcmcxHTAbBgNVBGEMFFBTRERFLUZBS0VOQ0EtODdCMkFDMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsHAdLWn7pEAlD5daEjKv7hE4FW+vMJRrA/Bw2M/Zsu8VFfW1ARmbTgTy7rGLFBK/Y2SToEj60+5GEkCgCvi+vI/Bdykk8XqjpVsJjTW67np1b2Av8F61zvCnn2UOxBtXBHCzR1j2yz2om1IMYieGu/cDTWLNkbuoGSnj0dq4CbHp2f8ch++goffqLRXr642j8cVlqZYsapB8y+Z8IydbtNBd/XAmRTAprmdRv9B4PC7P+lIYX8QbXw77f+9/2Kty7oVHtjle+GnTR8wH5nCiMQsA9V564/34lKwuEkzuryV1HzitQ/X7FSZoiSQRTxbxjVO+xdzI3hjF2FZjVvkqywIDAQABo34wfDB6BggrBgEFBQcBAwRuMGwGBgQAgZgnAjBiMDkwEQYHBACBmCcBAwwGUFNQX0FJMBEGBwQAgZgnAQIMBlBTUF9QSTARBgcEAIGYJwEEDAZQU1BfSUMMGVRydXN0IFNlcnZpY2UgUHJvdmlkZXIgQUcMCkRFLUZBS0VOQ0EwDQYJKoZIhvcNAQELBQADggIBACKUQc3O3TOFG8tWk4sQd3f9SGlOcBOMekSXCxRgskcYkjhWW4+EN1FYzlGuXPfq1yngKaM3ss9yCDVep0MFa4hDJ/hzSSD5upExzwWDkUa97AHCjZd39W6kLaCMAc5vTbR9r7zBvMKBcAmhZ9mWCvrvbHUOURv5yBfrrEk4AM1Vakf5l+fWP4JhA779+7JlwpQRpy5dgqROwKQ2L634d2osgXUV4CkqhSUQ5LcYI4uBFyKnM0pyGaNYdKhBC95J0y5GYa7NpKJNZXf+clTbe33gCt2SFSOMa7CV5NYpnohS201uNd/ffWLzGtFBnHLNpX8qTfFc16mtIcJo6Iiof2CYgfYAyJByBC1gZHf1wAtfQzAn6JcEaJzmehXKKl9x7X62aaGan7l+MblUT65Gd+Yed+rXLF6svefbrcIbZwt/W+v1fbfnip9QEFPV3VLjg0vk9Y30ftZCcFRSHLD3mdxcVEtmVxDDxyzDUwXF7J/mi4RQhZBb3OtwwEIWC2zUaycNMZWJRI+RqfLvanlDFFMoYeSZKTFf8jS/PPcfpKOAiTGu21iuuv+gYxh/rgjW419w26ya+Q3jabaz3E9Im/opSU5sQ9W92ALA14J9VZs6v8BVmqKTB5APKfeTYoXg9MjP9fjVM/hP26kIgQVs5Bz15ov8uQlQC+OTO+2y5ozs-----END CERTIFICATE-----";
    private static final String TEST_QWAC_CERTIFICATE_EXPIRED = "-----BEGIN CERTIFICATE-----MIIEBjCCAu6gAwIBAgIEAmCHWTANBgkqhkiG9w0BAQsFADCBlDELMAkGA1UEBhMCREUxDzANBgNVBAgMBkhlc3NlbjESMBAGA1UEBwwJRnJhbmtmdXJ0MRUwEwYDVQQKDAxBdXRob3JpdHkgQ0ExCzAJBgNVBAsMAklUMSEwHwYDVQQDDBhBdXRob3JpdHkgQ0EgRG9tYWluIE5hbWUxGTAXBgkqhkiG9w0BCQEWCmNhQHRlc3QuZGUwHhcNMTgwODE3MDcxNzAyWhcNMTgwOTAzMDc1NzMxWjB6MRMwEQYDVQQDDApUUFAgU2FtcGxlMQwwCgYDVQQKDANvcmcxCzAJBgNVBAsMAm91MRAwDgYDVQQGEwdHZXJtYW55MQ8wDQYDVQQIDAZCYXllcm4xEjAQBgNVBAcMCU51cmVtYmVyZzERMA8GA1UEYQwIMTIzNDU5ODcwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCMMnLvNLvqxkHbxdcWRcyUrZ4oy++R/7hWMiWH4U+5kLTLICnlFofN3EgIuP5hZz9Zm8aPoJkr8Y1xEyP8X4a5YTFtMmrXwAOgW6BVTaBeO7eV6Me1yc2NawzWMNp0Zz/Lsnrmj2h7/dRYaYofFHjWPFRW+gjVwv95NFhcD9+H5rr+fMwoci0ERFvy70TYnLfuRrG1BpYOwEV+wVFRIciXE3CKjEh2wbz1Yr4DhD+6FtOElU8VPkWqGRZmr1n54apuLrxL9vIbt7qsaQirsUp5ez2SFGFTydUv+WqZaPGzONVptAymOfTcIsgcxDWx/liKlpdqwyXpJaOIrrXcEnQ1AgMBAAGjeTB3MHUGCCsGAQUFBwEDBGkwZwYGBACBmCcCMF0wTDARBgcEAIGYJwEBDAZQU1BfQVMwEQYHBACBmCcBAgwGUFNQX1BJMBEGBwQAgZgnAQMMBlBTUF9BSTARBgcEAIGYJwEEDAZQU1BfSUMMBEF1dGgMBzEyMTkwODgwDQYJKoZIhvcNAQELBQADggEBAKrHWMriNquiC1vfNKkJFPINi2T2J5FmRQfamrkzS3AI5zPPXx32MzbrTkQb+Zl7qTvClmIFpDG45YC+JVYz+4/gMSJChJfW+JYtyW/Am6eeIYZ1sk+VPvXgxuTA0aZLQsVHsaeTHnQ7lZzN3S0Ao5O35AGKqBITu6Mo1t4WglNJLZHZ0iFL92yfezfV7LF9JYAD/6JFVTeuBwKKHNjPupjeVBku/C7qVDbogo1Ubiowt+hMMPLVLPjxe6Xo9SUtkGj3+5ID4Z8NGHDaaF2IGVGaJkHK9+PYTYEBRDsbc1GwgzTzbds5lao6eMyepL/Kl7iUNtn3Vox/XiSymunGCmQ=-----END CERTIFICATE-----";
    private static final TppErrorMessage TPP_ERROR_MESSAGE_ACCESS = new TppErrorMessage(ERROR, ROLE_INVALID);
    private static final TppErrorMessage TPP_ERROR_MESSAGE_EXPIRED = new TppErrorMessage(ERROR, CERTIFICATE_EXPIRED);

    @InjectMocks
    private QwacCertificateService qwacCertificateService;
    @Mock
    private TppInfoHolder tppInfoHolder;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private PrintWriter printWriter;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private TppErrorMessageWriter tppErrorMessageWriter;
    @Mock
    private Xs2aTppInfoMapper xs2aTppInfoMapper;
    @Mock
    private TppService tppService;
    @Mock
    private TppRoleValidationService tppRoleValidationService;

    @Test
    void doFilter_success() throws Exception {
        //Given
        when(xs2aTppInfoMapper.mapToTppInfo(any(TppCertificateData.class))).thenReturn(new TppInfo());

        //When
        qwacCertificateService.isApplicable(request, response, TEST_QWAC_CERTIFICATE_VALID);

        //Then
        verify(tppInfoHolder).setTppInfo(any());
    }

    @Test
    void doFilter_failure_expired_certificate() throws Exception {
        //Given
        ArgumentCaptor<TppErrorMessage> message = ArgumentCaptor.forClass(TppErrorMessage.class);

        //When
        qwacCertificateService.isApplicable(request, response, TEST_QWAC_CERTIFICATE_EXPIRED);

        //Then
        verify(tppErrorMessageWriter).writeError(eq(response), message.capture());
        verify(tppInfoHolder, never()).setTppInfo(any());
        assertEquals(TPP_ERROR_MESSAGE_EXPIRED, message.getValue());
    }

    @Test
    void doFilter_success_check_tpp_roles_from_certificate() throws Exception {
        //Given
        when(xs2aTppInfoMapper.mapToTppInfo(any(TppCertificateData.class))).thenReturn(new TppInfo());
        ArgumentCaptor<TppInfo> tppInfoArgumentCaptor = ArgumentCaptor.forClass(TppInfo.class);
        when(requestProviderService.getTppRolesAllowedHeader()).thenReturn(null);
        when(tppRoleValidationService.hasAccess(any(), eq(request))).thenReturn(true);
        when(aspspProfileService.isCheckTppRolesFromCertificateSupported()).thenReturn(true);

        //When
        qwacCertificateService.isApplicable(request, response, TEST_QWAC_CERTIFICATE_VALID);

        //Then
        verify(tppInfoHolder).setTppInfo(tppInfoArgumentCaptor.capture());
        TppInfo tppInfo = tppInfoArgumentCaptor.getValue();
        verify(tppService, times(1)).updateTppInfo(tppInfo);
        assertTrue(tppInfo.getTppRoles().containsAll(EnumSet.of(TppRole.AISP, TppRole.PISP, TppRole.PIISP)));
    }

    @Test
    void doFilter_success_check_tpp_roles_from_header() throws Exception {
        //Given
        when(xs2aTppInfoMapper.mapToTppInfo(any(TppCertificateData.class))).thenReturn(new TppInfo());
        ArgumentCaptor<TppInfo> tppInfoArgumentCaptor = ArgumentCaptor.forClass(TppInfo.class);
        when(tppRoleValidationService.hasAccess(any(), any())).thenReturn(true);
        List<TppRole> roles = Collections.singletonList(TppRole.AISP);
        when(xs2aTppInfoMapper.mapToTppRoles(Collections.singletonList("AISP"))).thenReturn(Collections.singletonList(TppRole.AISP));
        String rolesRepresentation = roles.stream().map(TppRole::toString).collect(Collectors.joining(", "));
        when(requestProviderService.getTppRolesAllowedHeader()).thenReturn(rolesRepresentation);

        //When
        qwacCertificateService.isApplicable(request, response, TEST_QWAC_CERTIFICATE_VALID);

        //Then
        verify(tppInfoHolder).setTppInfo(tppInfoArgumentCaptor.capture());
        TppInfo tppInfo = tppInfoArgumentCaptor.getValue();
        verify(tppService, times(1)).updateTppInfo(tppInfo);
        assertEquals(roles, tppInfo.getTppRoles());
    }

    @Test
    void doFilter_failure_wrong_tpp_roles() throws Exception {
        //Given
        when(xs2aTppInfoMapper.mapToTppInfo(any(TppCertificateData.class))).thenReturn(new TppInfo());
        when(tppRoleValidationService.hasAccess(any(), any())).thenReturn(false);
        ArgumentCaptor<TppErrorMessage> message = ArgumentCaptor.forClass(TppErrorMessage.class);
        when(requestProviderService.getTppRolesAllowedHeader()).thenReturn("PIISP");
        when(xs2aTppInfoMapper.mapToTppRoles(Collections.singletonList("PIISP"))).thenReturn(Collections.singletonList(TppRole.PIISP));

        //When
        qwacCertificateService.isApplicable(request, response, TEST_QWAC_CERTIFICATE_VALID);

        //Then
        verify(tppErrorMessageWriter).writeError(eq(response), message.capture());
        assertEquals(TPP_ERROR_MESSAGE_ACCESS, message.getValue());
    }

    @Test
    void doFilter_success_no_check_tpp_roles() throws Exception {
        //Given
        when(xs2aTppInfoMapper.mapToTppInfo(any(TppCertificateData.class))).thenReturn(new TppInfo());
        ArgumentCaptor<TppInfo> tppInfoArgumentCaptor = ArgumentCaptor.forClass(TppInfo.class);
        when(aspspProfileService.isCheckTppRolesFromCertificateSupported()).thenReturn(false);

        //When
        qwacCertificateService.isApplicable(request, response, TEST_QWAC_CERTIFICATE_VALID);

        //Then
        verify(tppInfoHolder).setTppInfo(tppInfoArgumentCaptor.capture());
        TppInfo tppInfo = tppInfoArgumentCaptor.getValue();
        verify(tppService, never()).updateTppInfo(tppInfo);
        assertNull(tppInfo.getTppRoles());
    }
}
