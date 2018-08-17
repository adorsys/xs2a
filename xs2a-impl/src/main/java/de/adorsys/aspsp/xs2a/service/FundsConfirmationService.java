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

import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.Balance;
import de.adorsys.aspsp.xs2a.domain.BalanceType;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.account.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class FundsConfirmationService {
    private final AccountService accountService;

    /**
     * Checks if the account balance is sufficient for requested operation
     *
     * @param request Contains the requested balanceAmount in order to comparing with available balanceAmount on account
     * @return Response with result 'true' if there are enough funds on the account, 'false' if not
     */
    public ResponseObject<FundsConfirmationResponse> fundsConfirmation(FundsConfirmationRequest request) {
        Boolean fundsAvailable = Optional.ofNullable(request)
                                     .map(req -> isFundsAvailable(req.getPsuAccount(), req.getInstructedAmount()))
                                     .orElse(false);

        return ResponseObject.<FundsConfirmationResponse>builder()
                   .body(new FundsConfirmationResponse(fundsAvailable)).build();
    }

    private boolean isFundsAvailable(AccountReference accountReference, Amount requiredAmount) {
        List<Balance> balances = getAccountBalancesByAccountReference(accountReference);

        return balances.stream()
                   .filter(bal -> BalanceType.INTERIM_AVAILABLE == bal.getBalanceType())
                   .findFirst()
                   .map(Balance::getBalanceAmount)
                   .map(am -> isRequiredAmountEnough(requiredAmount, am))
                   .orElse(false);
    }

    private boolean isRequiredAmountEnough(Amount requiredAmount, Amount availableAmount) {
        return convertToBigDecimal(availableAmount.getContent()).compareTo(convertToBigDecimal(requiredAmount.getContent())) >= 0 &&
                   availableAmount.getCurrency() == requiredAmount.getCurrency();
    }

    private BigDecimal convertToBigDecimal(String content) {
        return Optional.ofNullable(content)
                   .map(BigDecimal::new)
                   .orElse(BigDecimal.ZERO);
    }

    private List<Balance> getAccountBalancesByAccountReference(AccountReference reference) {
        return Optional.ofNullable(reference)
                   .map(accountService::getAccountDetailsByAccountReference)
                   .filter(Optional::isPresent)
                   .map(Optional::get)
                   .map(AccountDetails::getBalances)
                   .orElseGet(Collections::emptyList);
    }
}
