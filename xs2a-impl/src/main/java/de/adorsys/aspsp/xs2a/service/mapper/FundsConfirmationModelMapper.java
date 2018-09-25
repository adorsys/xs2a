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

package de.adorsys.aspsp.xs2a.service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.psd2.model.ConfirmationOfFunds;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FundsConfirmationModelMapper {

    private final ObjectMapper objectMapper;

    public FundsConfirmationRequest mapToFundsConfirmationRequest(ConfirmationOfFunds confirmationOfFunds) {
        return Optional.ofNullable(confirmationOfFunds)
                   .map(conf -> {
                       FundsConfirmationRequest fundsConfirmationRequest = new FundsConfirmationRequest();
                       fundsConfirmationRequest.setCardNumber(conf.getCardNumber());
                       fundsConfirmationRequest.setPayee(conf.getPayee());
                       fundsConfirmationRequest.setPsuAccount(mapToXs2aAccountReferenceInner(conf.getAccount()));
                       fundsConfirmationRequest.setInstructedAmount(AmountModelMapper.mapToXs2aAmount(conf.getInstructedAmount()));
                       return fundsConfirmationRequest;
                   })
                   .orElse(null);
    }

    private Xs2aAccountReference mapToXs2aAccountReferenceInner(Object reference) {
        return objectMapper.convertValue(reference, Xs2aAccountReference.class);
    }
}
