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
    void updateMultilevelScaRequired() {
        assertEquals("http://base.url/pis/common-payments/{payment-id}/multilevel-sca?multilevel-sca={multilevel-sca}",
                     pisCommonPaymentRemoteUrls.updateMultilevelScaRequired());
    }
}
