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


package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.config.PisCommonPaymentRemoteUrls;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PisCommonPaymentServiceRemoteTest {

    private static final String UPDATE_MULTILEVEL_SCA_URL = "http://base.url/pis/common-payments/{payment-id}/multilevel-sca?multilevel-sca={multilevel-sca}";
    private static final String PAYMENT_ID = "paymentId";

    @InjectMocks
    private PisCommonPaymentServiceRemote service;

    @Mock
    private RestTemplate consentRestTemplate;
    @Mock
    private PisCommonPaymentRemoteUrls remotePisCommonPaymentUrls;

    @Test
    void updateMultilevelSca() {
        when(remotePisCommonPaymentUrls.updateMultilevelScaRequired()).thenReturn(UPDATE_MULTILEVEL_SCA_URL);
        when(consentRestTemplate.exchange(UPDATE_MULTILEVEL_SCA_URL, HttpMethod.PUT, null, Boolean.class, PAYMENT_ID, true)).thenReturn(ResponseEntity.ok(true));

        CmsResponse<Boolean> actualResponse = service.updateMultilevelSca(PAYMENT_ID, true);

        assertTrue(actualResponse.isSuccessful());
        assertTrue(actualResponse.getPayload());
    }
}
