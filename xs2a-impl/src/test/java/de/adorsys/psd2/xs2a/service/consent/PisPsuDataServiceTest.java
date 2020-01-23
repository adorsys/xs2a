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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PisPsuDataServiceTest {
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String WRONG_ID = "wrong id";
    private static final List<PsuIdData> LIST_PSU_DATA = getListPisPayment();

    @InjectMocks
    private PisPsuDataService pisPsuDataService;

    @Mock
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    @Mock
    private RequestProviderService requestProviderService;

    @Test
    void getPsuDataByPaymentId_success() {
        //Given
        when(pisCommonPaymentServiceEncrypted.getPsuDataListByPaymentId(PAYMENT_ID))
            .thenReturn(Optional.of(LIST_PSU_DATA));

        //When
        List<PsuIdData> actualResponse = pisPsuDataService.getPsuDataByPaymentId(PAYMENT_ID);

        //Then
        assertThat(actualResponse).isEqualTo(LIST_PSU_DATA);
    }

    @Test
    void getPsuDataByPaymentId_failed() {
        //Given
        when(pisCommonPaymentServiceEncrypted.getPsuDataListByPaymentId(WRONG_ID))
            .thenReturn(Optional.empty());

        //When
        List<PsuIdData> actualResponse = pisPsuDataService.getPsuDataByPaymentId(WRONG_ID);

        //Then
        assertThat(actualResponse).isEqualTo(Collections.EMPTY_LIST);
    }

    private static List<PsuIdData> getListPisPayment() {
        return Collections.singletonList(new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType"));
    }
}
