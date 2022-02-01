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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestProviderServiceMockTest {

    private static final String QWAC_CERTIFICATE = "certificate";
    private static final String MOCK_QWAC_CERTIFICATE = "mock-certificate";

    @InjectMocks
    private RequestProviderServiceMock requestProviderService;

    @Mock
    private HttpServletRequest request;

    @Test
    void getEncodedTppQwacCert_headerCertificate() {
        when(request.getHeader(RequestProviderService.TPP_QWAC_CERTIFICATE_HEADER)).thenReturn(QWAC_CERTIFICATE);

        String encodedTppQwacCert = requestProviderService.getEncodedTppQwacCert();
        assertEquals(QWAC_CERTIFICATE, encodedTppQwacCert);
    }

    @Test
    void getEncodedTppQwacCert_mockCertificate() {
        ReflectionTestUtils.setField(requestProviderService, "qwacCertificateMock", MOCK_QWAC_CERTIFICATE);
        when(request.getHeader(RequestProviderService.TPP_QWAC_CERTIFICATE_HEADER)).thenReturn(null);

        String encodedTppQwacCert = requestProviderService.getEncodedTppQwacCert();
        assertEquals(MOCK_QWAC_CERTIFICATE, encodedTppQwacCert);
    }
}
