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
import de.adorsys.psd2.model.ConfirmationOfFunds;
import de.adorsys.psd2.model.InlineResponse2003;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FundsConfirmationModelMapper {
    private final Xs2aObjectMapper xs2aObjectMapper;
    private final AmountModelMapper amountModelMapper;

    public FundsConfirmationRequest mapToFundsConfirmationRequest(ConfirmationOfFunds confirmationOfFunds, String consentId) {
        return Optional.ofNullable(confirmationOfFunds)
                   .map(conf -> {
                       FundsConfirmationRequest fundsConfirmationRequest = new FundsConfirmationRequest();
                       fundsConfirmationRequest.setCardNumber(conf.getCardNumber());
                       fundsConfirmationRequest.setPayee(conf.getPayee());
                       fundsConfirmationRequest.setPsuAccount(mapToAccountReferenceInner(conf.getAccount()));
                       fundsConfirmationRequest.setInstructedAmount(amountModelMapper.mapToXs2aAmount(conf.getInstructedAmount()));
                       fundsConfirmationRequest.setConsentId(consentId);
                       return fundsConfirmationRequest;
                   })
                   .orElse(null);
    }

    /**
     * Maps internal FundsConfirmationResponse into InlineResponse200.
     *
     * @param fundsConfirmationResponse response from funds confirmation service.
     * @return InlineResponse200 for controller
     */
    public InlineResponse2003 mapToInlineResponse2003(FundsConfirmationResponse fundsConfirmationResponse) {
        return new InlineResponse2003().fundsAvailable(fundsConfirmationResponse.isFundsAvailable());
    }

    private AccountReference mapToAccountReferenceInner(Object reference) {
        return xs2aObjectMapper.convertValue(reference, AccountReference.class);
    }
}
