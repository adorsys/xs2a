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
