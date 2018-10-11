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

package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiBalanceType;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationConsent;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.FundsConfirmationSpi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Currency;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FundsConfirmationSpiImpl implements FundsConfirmationSpi {
    private final AccountSpi accountSpi;

    @Override
    public SpiResponse<Boolean> peformFundsSufficientCheck(SpiFundsConfirmationConsent consent, SpiAccountReference reference, SpiAmount amount, AspspConsentData aspspConsentData) {
        //TODO Account data reads should be performed through specially created endpoint https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/383
        List<SpiAccountDetails> accounts = accountSpi.readAccountDetailsByIban(reference.getIban(), aspspConsentData).getPayload();
        List<SpiAccountBalance> balances = extractAccountBalancesByCurrency(accounts, reference.getCurrency());

        return new SpiResponse<>(isBalancesSufficient(balances, amount), aspspConsentData);
    }

    private boolean isBalancesSufficient(List<SpiAccountBalance> balances, SpiAmount amount) {
        return balances.stream()
                   .filter(bal -> SpiBalanceType.INTERIM_AVAILABLE == bal.getSpiBalanceType())
                   .findFirst()
                   .map(SpiAccountBalance::getSpiBalanceAmount)
                   .map(am -> isRequiredAmountEnough(amount, am))
                   .orElse(false);
    }

    private boolean isRequiredAmountEnough(SpiAmount requiredAmount, SpiAmount availableAmount) {
        return availableAmount.getAmount().compareTo(requiredAmount.getAmount()) >= 0 &&
                   availableAmount.getCurrency() == requiredAmount.getCurrency();
    }

    private List<SpiAccountBalance> extractAccountBalancesByCurrency(List<SpiAccountDetails> accounts, Currency currency) {
        return accounts.stream()
                   .filter(spiAcc -> spiAcc.getCurrency() == currency)
                   .findFirst()
                   .map(SpiAccountDetails::getBalances)
                   .orElseGet(Collections::emptyList);
    }
}
