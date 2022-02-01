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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

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

    @Test
    void getPsuDataByPaymentId_success() {
        //Given
        when(pisCommonPaymentServiceEncrypted.getPsuDataListByPaymentId(PAYMENT_ID))
            .thenReturn(CmsResponse.<List<PsuIdData>>builder().payload(LIST_PSU_DATA).build());

        //When
        List<PsuIdData> actualResponse = pisPsuDataService.getPsuDataByPaymentId(PAYMENT_ID);

        //Then
        assertThat(actualResponse).isEqualTo(LIST_PSU_DATA);
    }

    @Test
    void getPsuDataByPaymentId_failed() {
        //Given
        when(pisCommonPaymentServiceEncrypted.getPsuDataListByPaymentId(WRONG_ID))
            .thenReturn(CmsResponse.<List<PsuIdData>>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When
        List<PsuIdData> actualResponse = pisPsuDataService.getPsuDataByPaymentId(WRONG_ID);

        //Then
        assertThat(actualResponse).isEqualTo(Collections.EMPTY_LIST);
    }

    private static List<PsuIdData> getListPisPayment() {
        return Collections.singletonList(new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress"));
    }
}
