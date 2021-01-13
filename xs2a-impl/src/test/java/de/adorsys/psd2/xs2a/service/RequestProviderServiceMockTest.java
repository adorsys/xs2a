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
        when(request.getHeader(requestProviderService.getTppQwacCertificateHeaderName())).thenReturn(QWAC_CERTIFICATE);

        String encodedTppQwacCert = requestProviderService.getEncodedTppQwacCert();
        assertEquals(QWAC_CERTIFICATE, encodedTppQwacCert);
    }

    @Test
    void getEncodedTppQwacCert_mockCertificate() {
        ReflectionTestUtils.setField(requestProviderService, "qwacCertificateMock", MOCK_QWAC_CERTIFICATE);
        when(request.getHeader(requestProviderService.getTppQwacCertificateHeaderName())).thenReturn(null);

        String encodedTppQwacCert = requestProviderService.getEncodedTppQwacCert();
        assertEquals(MOCK_QWAC_CERTIFICATE, encodedTppQwacCert);
    }
}
