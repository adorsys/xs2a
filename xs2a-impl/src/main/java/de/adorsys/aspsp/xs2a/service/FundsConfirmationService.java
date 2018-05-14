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

import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.aspsp.xs2a.service.mapper.AccountMapper;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class FundsConfirmationService {
    private final AccountService accountService;
    private final AccountMapper accountMapper;

    public ResponseObject<FundsConfirmationResponse> fundsConfirmation(FundsConfirmationRequest request) {
        Boolean fundsAvailable = Optional.ofNullable(request)
                                     .map(this::isFundsAvailable)
                                     .orElse(false);

        return ResponseObject.builder()
                   .body(new FundsConfirmationResponse(fundsAvailable)).build();
    }

    private boolean isFundsAvailable(FundsConfirmationRequest request) {
        SpiAmount requiredAmount = accountMapper.mapToSpiAmount(request.getInstructedAmount());

        return Optional.ofNullable(request.getPsuAccount())
                   .flatMap(accountService::getSpiAccountDetailsByAccountReference)
                   .flatMap(SpiAccountDetails::getFirstBalance)
                   .map(SpiBalances::getInterimAvailable)
                   .map(SpiAccountBalance::getSpiAmount)
                   .map(spiAm -> spiAm.getDoubleContent() >= requiredAmount.getDoubleContent() &&
                                     spiAm.getCurrency() == requiredAmount.getCurrency())
                   .orElse(false);
    }
}
