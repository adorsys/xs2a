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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.Amount;
import de.adorsys.psd2.model.ConfirmationOfFunds;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FundsConfirmationModelMapperTest {
    private static final String CONSENT_ID = "233108c8-8f67-4866-b4b7-66a0df044342";

    @InjectMocks
    private FundsConfirmationModelMapper fundsConfirmationModelMapper;
    @Mock
    private Xs2aObjectMapper xs2aObjectMapper;
    @Mock
    private AmountModelMapper amountModelMapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToFundsConfirmationRequest() {
        //Given
        ConfirmationOfFunds confirmationOfFunds = jsonReader.getObjectFromFile("json/service/mapper/funds-confirmation-model-mapper/confirmation-of-funds.json", ConfirmationOfFunds.class);
        FundsConfirmationRequest expected = jsonReader.getObjectFromFile("json/service/mapper/funds-confirmation-model-mapper/funds-confirmation-request.json", FundsConfirmationRequest.class);
        when(xs2aObjectMapper.convertValue(any(Object.class), eq(AccountReference.class)))
            .thenReturn(expected.getPsuAccount());
        when(amountModelMapper.mapToXs2aAmount(any(Amount.class)))
            .thenReturn(expected.getInstructedAmount());
        //When
        FundsConfirmationRequest actual = fundsConfirmationModelMapper.mapToFundsConfirmationRequest(confirmationOfFunds, CONSENT_ID);
        //Then
        assertEquals(expected, actual);
    }
}
