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
import de.adorsys.aspsp.xs2a.service.mapper.FundMapper;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.fund.SpiFundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.spi.service.FundsConfirmationSpi;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class FundsConfirmationService {
    private final FundsConfirmationSpi fundsConfirmationSpi;
    private final FundMapper fundMapper;

    public ResponseObject<FundsConfirmationResponse> fundsConfirmation(FundsConfirmationRequest request) {
        SpiFundsConfirmationRequest spiRequest = fundMapper.mapToSpiFundsConfirmationRequest(request);
        Boolean response = Optional.ofNullable(fundsConfirmationSpi.getRequestedAccountDetails(spiRequest))
            .map(acc -> checkBalance(acc, spiRequest))
            .orElse(false);

        return ResponseObject.builder()
            .body(new FundsConfirmationResponse(response)).build();
    }

    private boolean checkBalance(SpiAccountDetails accountDetails, SpiFundsConfirmationRequest request) {
        return accountDetails.getFirstBalance()
            .map(bal -> compareAmounts(bal, request))
            .orElse(false);
    }

    private boolean compareAmounts(SpiBalances balance, SpiFundsConfirmationRequest request) {
        return getAvailableAccountBalance(balance)
            .map(am -> am.getDoubleContent() >= getRequestedAmount(request))
            .orElse(false);
    }

    private double getRequestedAmount(SpiFundsConfirmationRequest request) {
        return Optional.ofNullable(request.getInstructedAmount())
            .map(am -> am.getDoubleContent())
            .orElse(0.0d);
    }

    private Optional<SpiAmount> getAvailableAccountBalance(SpiBalances balance) {
        return Optional.ofNullable(balance.getInterimAvailable())
            .map(bal -> Optional.of(bal.getSpiAmount()))
            .orElse(Optional.empty());
    }
}
