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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.Amount;
import de.adorsys.psd2.model.ConfirmationOfFunds;
import de.adorsys.psd2.model.InlineResponse2003;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
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
        // Given
        ConfirmationOfFunds confirmationOfFunds = jsonReader.getObjectFromFile("json/service/mapper/funds-confirmation-model-mapper/confirmation-of-funds.json", ConfirmationOfFunds.class);
        FundsConfirmationRequest expected = jsonReader.getObjectFromFile("json/service/mapper/funds-confirmation-model-mapper/funds-confirmation-request.json", FundsConfirmationRequest.class);
        when(xs2aObjectMapper.convertValue(any(Object.class), eq(AccountReference.class)))
            .thenReturn(expected.getPsuAccount());
        when(amountModelMapper.mapToXs2aAmount(any(Amount.class)))
            .thenReturn(expected.getInstructedAmount());
        // When
        FundsConfirmationRequest actual = fundsConfirmationModelMapper.mapToFundsConfirmationRequest(confirmationOfFunds, CONSENT_ID);
        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToInitialResponse2003() {
        // Given
        FundsConfirmationResponse fundsConfirmationResponse = new FundsConfirmationResponse(true);

        // When
        InlineResponse2003 actual = fundsConfirmationModelMapper.mapToInlineResponse2003(fundsConfirmationResponse);

        InlineResponse2003 expected = new InlineResponse2003().fundsAvailable(true);

        // Then
        assertThat(actual).isEqualTo(expected);
    }
}
