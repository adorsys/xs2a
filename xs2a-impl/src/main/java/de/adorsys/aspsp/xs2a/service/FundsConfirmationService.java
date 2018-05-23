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

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class FundsConfirmationService {
    private final AccountService accountService;

    public ResponseObject<FundsConfirmationResponse> fundsConfirmation(FundsConfirmationRequest request) {
        Boolean fundsAvailable = Optional.ofNullable(request)
                                     .map(req -> isFundsAvailable(req.getPsuAccount(), req.getInstructedAmount()))
                                     .orElse(false);

        return ResponseObject.<FundsConfirmationResponse>builder()
                   .body(new FundsConfirmationResponse(fundsAvailable)).build();
    }

    private boolean isFundsAvailable(AccountReference accountReference, Amount requiredAmount) {
        List<Balances> balances = accountService.getAccountBalancesByAccountReference(accountReference);

        return balances.stream()
                   .findFirst()
                   .map(Balances::getInterimAvailable)
                   .map(SingleBalance::getAmount)
                   .map(am -> isRequiredAmountEnough(requiredAmount, am))
                   .orElse(false);
    }

    private boolean isRequiredAmountEnough(Amount requiredAmount, Amount availableAmount) {
        return getDoubleContent(availableAmount.getContent()) >= getDoubleContent(requiredAmount.getContent()) &&
                   availableAmount.getCurrency() == requiredAmount.getCurrency();
    }

    private double getDoubleContent(String content) {
        return Optional.of(content)
                   .map(Double::parseDouble)
                   .orElse(0.0d);
    }
}
