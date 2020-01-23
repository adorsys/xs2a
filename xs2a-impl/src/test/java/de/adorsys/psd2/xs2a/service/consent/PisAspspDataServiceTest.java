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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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
            .thenReturn(Optional.of(PAYMENT_ID));

        //When
        String actualResponse = pisAspspDataService.getInternalPaymentIdByEncryptedString(ENCRYPTED_ID);

        //Then
        assertThat(actualResponse).isEqualTo(PAYMENT_ID);
    }

    @Test
    void getInternalPaymentIdByEncryptedString_failed() {
        //Given
        when(pisCommonPaymentServiceEncrypted.getDecryptedId(WRONG_ID))
            .thenReturn(Optional.empty());

        //When
        String actualResponse = pisAspspDataService.getInternalPaymentIdByEncryptedString(WRONG_ID);

        //Then
        assertThat(actualResponse).isNull();
    }
}
