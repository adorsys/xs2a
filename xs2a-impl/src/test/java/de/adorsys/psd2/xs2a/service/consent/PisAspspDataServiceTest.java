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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PisAspspDataServiceTest {
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String ENCRYPTED_ID = "3278921mxl-n2131-13nw";
    private static final String WRONG_ID = "wrong id";

    @InjectMocks
    private PisAspspDataService pisAspspDataService;

    @Mock
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;

    @Test
    void getInternalPaymentIdByEncryptedString_success() {
        //Given
        when(pisCommonPaymentServiceEncrypted.getDecryptedId(ENCRYPTED_ID))
            .thenReturn(CmsResponse.<String>builder().payload(PAYMENT_ID).build());

        //When
        String actualResponse = pisAspspDataService.getInternalPaymentIdByEncryptedString(ENCRYPTED_ID);

        //Then
        assertThat(actualResponse).isEqualTo(PAYMENT_ID);
    }

    @Test
    void getInternalPaymentIdByEncryptedString_failed() {
        //Given
        when(pisCommonPaymentServiceEncrypted.getDecryptedId(WRONG_ID))
            .thenReturn(CmsResponse.<String>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When
        String actualResponse = pisAspspDataService.getInternalPaymentIdByEncryptedString(WRONG_ID);

        //Then
        assertThat(actualResponse).isNull();
    }
}
