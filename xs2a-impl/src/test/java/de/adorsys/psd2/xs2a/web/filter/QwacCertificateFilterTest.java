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

import de.adorsys.psd2.xs2a.service.validator.tpp.TppInfoHolder;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppRoleValidationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QwacCertificateFilterTest {
    private static final String HEADER_QWAC = "tpp-qwac-certificate";
    private static final String EXPIRED_CERTIFICATE_MESSAGE = "Signature/corporate seal certificate is expired.";
    private static final String TEST_QWAC_CERTIFICATE_VALID = "-----BEGIN CERTIFICATE-----MIIFQTCCAymgAwIBAgIESLvdaTANBgkqhkiG9w0BAQsFADB4MQswCQYDVQQGEwJERTEQMA4GA1UECAwHQkFWQVJJQTESMBAGA1UEBwwJTnVyZW1iZXJnMSIwIAYDVQQKDBlUcnVzdCBTZXJ2aWNlIFByb3ZpZGVyIEFHMR8wHQYDVQQLDBZJbmZvcm1hdGlvbiBUZWNobm9sb2d5MB4XDTE5MDMwNTE1MTIwN1oXDTIwMDMwNDAwMDAwMFowgcwxITAfBgNVBAoMGEZpY3Rpb25hbCBDb3Jwb3JhdGlvbiBBRzEJMAcGA1UEAwwAMSUwIwYKCZImiZPyLGQBGRYVcHVibGljLmNvcnBvcmF0aW9uLmRlMR8wHQYDVQQLDBZJbmZvcm1hdGlvbiBUZWNobm9sb2d5MRAwDgYDVQQGEwdHZXJtYW55MQ8wDQYDVQQIDAZCYXllcm4xEjAQBgNVBAcMCU51cmVtYmVyZzEdMBsGA1UEYQwUUFNEREUtRkFLRU5DQS04N0IyQUMwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCeDcYlVutZeGFtOkonIMGHwway2ASZl8p7/v7USIxeMo/5ppbAa6Ei7i7jH9ORBoHV6qxAwNFkdd8JDneNiNn0NSvoYTemr5mqyXYhwpzLueXth1oBgjLYvcaLFXXQGS0dd6sDcaCTbw9xdDmap+6xYDRzIrdviyiph1ewpUXlrEHNu5Oomk7R5Dpv4gM9uRwYiskRigZdnArfyQ3ZYW4VZvMlFW3t1IVvSiOWvruF24w+j1g3BOHNM7tIAOlOUYQpHV1G1ChcFt5/ICArtsGAd4/ZUzlmujktdO+hNA70fDHUxkG6vQRFQhSnszzJ/C/g632nMTJbAaGtO2OvdL9DAgMBAAGjfjB8MHoGCCsGAQUFBwEDBG4wbAYGBACBmCcCMGIwOTARBgcEAIGYJwEDDAZQU1BfQUkwEQYHBACBmCcBAgwGUFNQX1BJMBEGBwQAgZgnAQQMBlBTUF9JQwwZVHJ1c3QgU2VydmljZSBQcm92aWRlciBBRwwKREUtRkFLRU5DQTANBgkqhkiG9w0BAQsFAAOCAgEAK07yQviS7/zKm1EqQyyGkEbf/1sHb9FLPBr/BicYxc3IQGd4xG1SJ1uLudX37Yq/o6exjixZ8ywib27jNLCpsF1dEQabHNXgS4enojf7CVTyKjDkKqE1mwqPmGeoWWwaWOUsWQ2/Ja/UTW5Bn5iA+nHCXVrkcjFVnRvi+dSsRm4J3E0EdAAwBkSEqHGDZO1ZiAh20YkNExx8MKKiHAVZ0ZFCXzYcaWzaK6yeCarvyPNCb+BAsc1wf3/88tLT9Nof/Ctzv2L9OjGHcalCLf/g/qTr6/50J4IMVdBwoVkg27yRE5EC3RKJE5BFx6TNWeNGs7r8HpAhO/6hLKzVHjrsA8/SAwTWNQNWdP/azSV42DuVMjDi5o5Ax9RkHXRvjsuwTR19AKvIc6nv/8XUtwORjHW+FTXTGa28PqCD1ZACiHytIBXrETevmLIlFuh6ZaWKBYPUc3DmJbFSZkhRFybh1SEtl/WzeQjIKqkRw0MGzDIRwD0sYqeE8ENkJbXJG+Cy4c42mZmEwG6E7HQQtiT9Irt1cnUiFDRe6g+h4GaxhOC5Pluxhij4DaNHCIZm30IHcyA4vZOyj7rXcvvfGMwPgbSdqSdEeNB25FEmFmJnavESxyJKYNx3JONm//0yRpacfWos/MjmbLWynYz8Bv8EK7mCS84bmSlUrUgHoNvDeBc=-----END CERTIFICATE-----";
    private static final String TEST_QWAC_CERTIFICATE_EXPIRED = "-----BEGIN CERTIFICATE-----MIIEBjCCAu6gAwIBAgIEAmCHWTANBgkqhkiG9w0BAQsFADCBlDELMAkGA1UEBhMCREUxDzANBgNVBAgMBkhlc3NlbjESMBAGA1UEBwwJRnJhbmtmdXJ0MRUwEwYDVQQKDAxBdXRob3JpdHkgQ0ExCzAJBgNVBAsMAklUMSEwHwYDVQQDDBhBdXRob3JpdHkgQ0EgRG9tYWluIE5hbWUxGTAXBgkqhkiG9w0BCQEWCmNhQHRlc3QuZGUwHhcNMTgwODE3MDcxNzAyWhcNMTgwOTAzMDc1NzMxWjB6MRMwEQYDVQQDDApUUFAgU2FtcGxlMQwwCgYDVQQKDANvcmcxCzAJBgNVBAsMAm91MRAwDgYDVQQGEwdHZXJtYW55MQ8wDQYDVQQIDAZCYXllcm4xEjAQBgNVBAcMCU51cmVtYmVyZzERMA8GA1UEYQwIMTIzNDU5ODcwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCMMnLvNLvqxkHbxdcWRcyUrZ4oy++R/7hWMiWH4U+5kLTLICnlFofN3EgIuP5hZz9Zm8aPoJkr8Y1xEyP8X4a5YTFtMmrXwAOgW6BVTaBeO7eV6Me1yc2NawzWMNp0Zz/Lsnrmj2h7/dRYaYofFHjWPFRW+gjVwv95NFhcD9+H5rr+fMwoci0ERFvy70TYnLfuRrG1BpYOwEV+wVFRIciXE3CKjEh2wbz1Yr4DhD+6FtOElU8VPkWqGRZmr1n54apuLrxL9vIbt7qsaQirsUp5ez2SFGFTydUv+WqZaPGzONVptAymOfTcIsgcxDWx/liKlpdqwyXpJaOIrrXcEnQ1AgMBAAGjeTB3MHUGCCsGAQUFBwEDBGkwZwYGBACBmCcCMF0wTDARBgcEAIGYJwEBDAZQU1BfQVMwEQYHBACBmCcBAgwGUFNQX1BJMBEGBwQAgZgnAQMMBlBTUF9BSTARBgcEAIGYJwEEDAZQU1BfSUMMBEF1dGgMBzEyMTkwODgwDQYJKoZIhvcNAQELBQADggEBAKrHWMriNquiC1vfNKkJFPINi2T2J5FmRQfamrkzS3AI5zPPXx32MzbrTkQb+Zl7qTvClmIFpDG45YC+JVYz+4/gMSJChJfW+JYtyW/Am6eeIYZ1sk+VPvXgxuTA0aZLQsVHsaeTHnQ7lZzN3S0Ao5O35AGKqBITu6Mo1t4WglNJLZHZ0iFL92yfezfV7LF9JYAD/6JFVTeuBwKKHNjPupjeVBku/C7qVDbogo1Ubiowt+hMMPLVLPjxe6Xo9SUtkGj3+5ID4Z8NGHDaaF2IGVGaJkHK9+PYTYEBRDsbc1GwgzTzbds5lao6eMyepL/Kl7iUNtn3Vox/XiSymunGCmQ=-----END CERTIFICATE-----";

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
    private FilterChain chain;

    @Test
    public void doFilterInternal_success() throws IOException, ServletException {
        //Given
        when(request.getHeader(HEADER_QWAC)).thenReturn(TEST_QWAC_CERTIFICATE_VALID);
        when(tppRoleValidationService.hasAccess(any(), any())).thenReturn(true);

        //When
        qwacCertificateFilter.doFilterInternal(request, response, chain);

        //Then
        verify(chain).doFilter(any(), any());
    }

    @Test
    public void doFilterInternal_failure_expired_certificate() throws IOException, ServletException {
        //Given
        ArgumentCaptor<Integer> statusCode = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        when(request.getHeader(HEADER_QWAC)).thenReturn(TEST_QWAC_CERTIFICATE_EXPIRED);

        //When
        qwacCertificateFilter.doFilterInternal(request, response, chain);

        //Then
        verify(response).sendError(statusCode.capture(), message.capture());
        verify(chain, never()).doFilter(any(), any());
        assertEquals((Integer) 401, statusCode.getValue());
        assertEquals(EXPIRED_CERTIFICATE_MESSAGE, message.getValue());
    }
}
