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

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;

public class PisCommonPaymentRemoteUrlsTest {
    private static final String BASE_URL = "http://base.url";

    private PisCommonPaymentRemoteUrls pisCommonPaymentRemoteUrls;

    @Before
    public void setUp() {
        pisCommonPaymentRemoteUrls = new PisCommonPaymentRemoteUrls();
        ReflectionTestUtils.setField(pisCommonPaymentRemoteUrls, "commonPaymentServiceBaseUrl", BASE_URL);
    }

    @Test
    public void createPisCommonPayment() {
        assertEquals("http://base.url/pis/common-payments/",
                     pisCommonPaymentRemoteUrls.createPisCommonPayment());
    }

    @Test
    public void updatePisCommonPaymentStatus() {
        assertEquals("http://base.url/pis/common-payments/{paymentId}/status/{status}",
                     pisCommonPaymentRemoteUrls.updatePisCommonPaymentStatus());
    }

    @Test
    public void getPisCommonPaymentStatusById() {
        assertEquals("http://base.url/pis/common-payments/{paymentId}/status",
                     pisCommonPaymentRemoteUrls.getPisCommonPaymentStatusById());
    }

    @Test
    public void getPisCommonPaymentById() {
        assertEquals("http://base.url/pis/common-payments/{paymentId}",
                     pisCommonPaymentRemoteUrls.getPisCommonPaymentById());
    }

    @Test
    public void createPisAuthorisation() {
        assertEquals("http://base.url/pis/common-payments/{payment-id}/authorizations",
                     pisCommonPaymentRemoteUrls.createPisAuthorisation());
    }

    @Test
    public void createPisAuthorisationCancellation() {
        assertEquals("http://base.url/pis/common-payments/{payment-id}/cancellation-authorisations",
                     pisCommonPaymentRemoteUrls.createPisAuthorisationCancellation());
    }

    @Test
    public void getCancellationAuthorisationSubResources() {
        assertEquals("http://base.url/pis/common-payments/{payment-id}/cancellation-authorisations",
                     pisCommonPaymentRemoteUrls.getCancellationAuthorisationSubResources());
    }

    @Test
    public void updatePisAuthorisation() {
        assertEquals("http://base.url/pis/common-payments/authorizations/{authorization-id}",
                     pisCommonPaymentRemoteUrls.updatePisAuthorisation());
    }

    @Test
    public void updatePisCancellationAuthorisation() {
        assertEquals("http://base.url/pis/common-payments/cancellation-authorisations/{cancellation-id}",
                     pisCommonPaymentRemoteUrls.updatePisCancellationAuthorisation());
    }

    @Test
    public void getPisAuthorisationById() {
        assertEquals("http://base.url/pis/common-payments/authorizations/{authorization-id}",
                     pisCommonPaymentRemoteUrls.getPisAuthorisationById());
    }

    @Test
    public void getPisCancellationAuthorisationById() {
        assertEquals("http://base.url/pis/common-payments/cancellation-authorisations/{cancellation-id}",
                     pisCommonPaymentRemoteUrls.getPisCancellationAuthorisationById());
    }

    @Test
    public void getPaymentIdByEncryptedString() {
        assertEquals("http://base.url/pis/payment/{payment-id}",
                     pisCommonPaymentRemoteUrls.getPaymentIdByEncryptedString());
    }

    @Test
    public void updatePisCommonPayment() {
        assertEquals("http://base.url/pis/common-payments/{consent-id}/payment",
                     pisCommonPaymentRemoteUrls.updatePisCommonPayment());
    }

    @Test
    public void getPsuDataByCommonPaymentId() {
        assertEquals("http://base.url/pis/common-payments/{consent-id}/psu-data",
                     pisCommonPaymentRemoteUrls.getPsuDataByCommonPaymentId());
    }

    @Test
    public void getPsuDataByPaymentId() {
        assertEquals("http://base.url/pis/payment/{payment-id}/psu-data",
                     pisCommonPaymentRemoteUrls.getPsuDataByPaymentId());
    }

    @Test
    public void getAuthorisationSubResources() {
        assertEquals("http://base.url/pis/common-payments/{payment-id}/authorisations",
                     pisCommonPaymentRemoteUrls.getAuthorisationSubResources());
    }

    @Test
    public void getAuthorisationScaStatus() {
        assertEquals("http://base.url/pis/common-payments/{payment-id}/authorisations/{authorisation-id}/status",
                     pisCommonPaymentRemoteUrls.getAuthorisationScaStatus());
    }

    @Test
    public void getCancellationAuthorisationScaStatus() {
        assertEquals("http://base.url/pis/common-payments/{payment-id}/cancellation-authorisations/{cancellation-id}/status",
                     pisCommonPaymentRemoteUrls.getCancellationAuthorisationScaStatus());
    }

    @Test
    public void isAuthenticationMethodDecoupled() {
        assertEquals("http://base.url/pis/common-payments/authorisations/{authorisation-id}/authentication-methods/{authentication-method-id}",
                     pisCommonPaymentRemoteUrls.isAuthenticationMethodDecoupled());
    }

    @Test
    public void saveAuthenticationMethods() {
        assertEquals("http://base.url/pis/common-payments/authorisations/{authorisation-id}/authentication-methods",
                     pisCommonPaymentRemoteUrls.saveAuthenticationMethods());
    }

    @Test
    public void updateScaApproach() {
        assertEquals("http://base.url/pis/common-payments/authorisations/{authorisation-id}/sca-approach/{sca-approach}",
                     pisCommonPaymentRemoteUrls.updateScaApproach());
    }

    @Test
    public void getAuthorisationScaApproach() {
        assertEquals("http://base.url/pis/common-payments/authorisations/{authorisation-id}/sca-approach",
                     pisCommonPaymentRemoteUrls.getAuthorisationScaApproach());
    }

    @Test
    public void getCancellationAuthorisationScaApproach() {
        assertEquals("http://base.url/pis/common-payments/cancellation-authorisations/{authorisation-id}/sca-approach",
                     pisCommonPaymentRemoteUrls.getCancellationAuthorisationScaApproach());
    }
}
