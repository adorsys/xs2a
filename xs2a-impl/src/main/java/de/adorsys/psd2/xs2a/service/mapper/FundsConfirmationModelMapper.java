/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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
