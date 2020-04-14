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

package de.adorsys.psd2.consent.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PisCommonPaymentRemoteUrlsTest {
    private static final String BASE_URL = "http://base.url";

    private PisCommonPaymentRemoteUrls pisCommonPaymentRemoteUrls;

    @BeforeEach
    void setUp() {
        pisCommonPaymentRemoteUrls = new PisCommonPaymentRemoteUrls();
        ReflectionTestUtils.setField(pisCommonPaymentRemoteUrls, "commonPaymentServiceBaseUrl", BASE_URL);
    }

    @Test
    void createPisCommonPayment() {
        assertEquals("http://base.url/pis/common-payments/",
                     pisCommonPaymentRemoteUrls.createPisCommonPayment());
    }

    @Test
    void updatePisCommonPaymentStatus() {
        assertEquals("http://base.url/pis/common-payments/{paymentId}/status/{status}",
                     pisCommonPaymentRemoteUrls.updatePisCommonPaymentStatus());
    }

    @Test
    void getPisCommonPaymentStatusById() {
        assertEquals("http://base.url/pis/common-payments/{paymentId}/status",
                     pisCommonPaymentRemoteUrls.getPisCommonPaymentStatusById());
    }

    @Test
    void getPisCommonPaymentById() {
        assertEquals("http://base.url/pis/common-payments/{paymentId}",
                     pisCommonPaymentRemoteUrls.getPisCommonPaymentById());
    }

    @Test
    void createPisAuthorisation() {
        assertEquals("http://base.url/pis/common-payments/{payment-id}/authorisations",
                     pisCommonPaymentRemoteUrls.createPisAuthorisation());
    }

    @Test
    void createPisAuthorisationCancellation() {
        assertEquals("http://base.url/pis/common-payments/{payment-id}/cancellation-authorisations",
                     pisCommonPaymentRemoteUrls.createPisAuthorisationCancellation());
    }

    @Test
    void getCancellationAuthorisationSubResources() {
        assertEquals("http://base.url/pis/common-payments/{payment-id}/cancellation-authorisations",
                     pisCommonPaymentRemoteUrls.getCancellationAuthorisationSubResources());
    }

    @Test
    void updatePisAuthorisation() {
        assertEquals("http://base.url/pis/common-payments/authorisations/{authorisation-id}",
                     pisCommonPaymentRemoteUrls.updatePisAuthorisation());
    }

    @Test
    void updatePisCancellationAuthorisation() {
        assertEquals("http://base.url/pis/common-payments/cancellation-authorisations/{authorisation-id}",
                     pisCommonPaymentRemoteUrls.updatePisCancellationAuthorisation());
    }

    @Test
    void getPisAuthorisationById() {
        assertEquals("http://base.url/pis/common-payments/authorisations/{authorisation-id}",
                     pisCommonPaymentRemoteUrls.getPisAuthorisationById());
    }

    @Test
    void getPisCancellationAuthorisationById() {
        assertEquals("http://base.url/pis/common-payments/cancellation-authorisations/{authorisation-id}",
                     pisCommonPaymentRemoteUrls.getPisCancellationAuthorisationById());
    }

    @Test
    void getPaymentIdByEncryptedString() {
        assertEquals("http://base.url/pis/payment/{payment-id}",
                     pisCommonPaymentRemoteUrls.getPaymentIdByEncryptedString());
    }

    @Test
    void updatePisCommonPayment() {
        assertEquals("http://base.url/pis/common-payments/{consent-id}/payment",
                     pisCommonPaymentRemoteUrls.updatePisCommonPayment());
    }

    @Test
    void getPsuDataByCommonPaymentId() {
        assertEquals("http://base.url/pis/common-payments/{consent-id}/psu-data",
                     pisCommonPaymentRemoteUrls.getPsuDataByCommonPaymentId());
    }

    @Test
    void getPsuDataByPaymentId() {
        assertEquals("http://base.url/pis/payment/{payment-id}/psu-data",
                     pisCommonPaymentRemoteUrls.getPsuDataByPaymentId());
    }

    @Test
    void getAuthorisationSubResources() {
        assertEquals("http://base.url/pis/common-payments/{payment-id}/authorisations",
                     pisCommonPaymentRemoteUrls.getAuthorisationSubResources());
    }

    @Test
    void getAuthorisationScaStatus() {
        assertEquals("http://base.url/pis/common-payments/{payment-id}/authorisations/{authorisation-id}/status",
                     pisCommonPaymentRemoteUrls.getAuthorisationScaStatus());
    }

    @Test
    void getCancellationAuthorisationScaStatus() {
        assertEquals("http://base.url/pis/common-payments/{payment-id}/cancellation-authorisations/{authorisation-id}/status",
                     pisCommonPaymentRemoteUrls.getCancellationAuthorisationScaStatus());
    }

    @Test
    void isAuthenticationMethodDecoupled() {
        assertEquals("http://base.url/pis/common-payments/authorisations/{authorisation-id}/authentication-methods/{authentication-method-id}",
                     pisCommonPaymentRemoteUrls.isAuthenticationMethodDecoupled());
    }

    @Test
    void saveAuthenticationMethods() {
        assertEquals("http://base.url/pis/common-payments/authorisations/{authorisation-id}/authentication-methods",
                     pisCommonPaymentRemoteUrls.saveAuthenticationMethods());
    }

    @Test
    void updateScaApproach() {
        assertEquals("http://base.url/pis/common-payments/authorisations/{authorisation-id}/sca-approach/{sca-approach}",
                     pisCommonPaymentRemoteUrls.updateScaApproach());
    }

    @Test
    void getAuthorisationScaApproach() {
        assertEquals("http://base.url/pis/common-payments/authorisations/{authorisation-id}/sca-approach",
                     pisCommonPaymentRemoteUrls.getAuthorisationScaApproach());
    }

    @Test
    void getCancellationAuthorisationScaApproach() {
        assertEquals("http://base.url/pis/common-payments/cancellation-authorisations/{authorisation-id}/sca-approach",
                     pisCommonPaymentRemoteUrls.getCancellationAuthorisationScaApproach());
    }

    @Test
    void updateMultilevelScaRequired() {
        assertEquals("http://base.url/pis/common-payments/{payment-id}/multilevel-sca?multilevel-sca={multilevel-sca}",
                     pisCommonPaymentRemoteUrls.updateMultilevelScaRequired());
    }
}
