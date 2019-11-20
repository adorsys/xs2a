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


package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.config.PisCommonPaymentRemoteUrls;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisCommonPaymentServiceRemoteTest {

    private static final String UPDATE_MULTILEVEL_SCA_URL = "http://base.url/pis/common-payments/{payment-id}/multilevel-sca?multilevel-sca={multilevel-sca}";
    private static final String PAYMENT_ID = "paymentId";

    @InjectMocks
    private PisCommonPaymentServiceRemote service;

    @Mock
    private RestTemplate consentRestTemplate;
    @Mock
    private PisCommonPaymentRemoteUrls remotePisCommonPaymentUrls;

    @Test
    public void updateMultilevelSca() {
        when(remotePisCommonPaymentUrls.updateMultilevelScaRequired()).thenReturn(UPDATE_MULTILEVEL_SCA_URL);
        when(consentRestTemplate.exchange(UPDATE_MULTILEVEL_SCA_URL, HttpMethod.PUT, null, Boolean.class, PAYMENT_ID, true)).thenReturn(ResponseEntity.ok(true));

        CmsResponse<Boolean> actualResponse = service.updateMultilevelSca(PAYMENT_ID, true);

        assertTrue(actualResponse.isSuccessful());
        assertTrue(actualResponse.getPayload());
    }
}
