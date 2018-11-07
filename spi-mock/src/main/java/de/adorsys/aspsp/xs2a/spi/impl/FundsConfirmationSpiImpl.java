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

import de.adorsys.aspsp.xs2a.exception.RestException;
import de.adorsys.aspsp.xs2a.spi.config.rest.AspspRemoteUrls;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiBalanceType;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationConsent;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.FundsConfirmationSpi;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FundsConfirmationSpiImpl implements FundsConfirmationSpi {
    private static final String TEST_ASPSP_DATA = "Test aspsp data";

    @Qualifier("aspspRestTemplate")
    private final RestTemplate aspspRestTemplate;
    private final AspspRemoteUrls remoteSpiUrls;

    @Override
    @NotNull
    public SpiResponse<Boolean> performFundsSufficientCheck(@NotNull SpiPsuData psuData, @Nullable SpiFundsConfirmationConsent consent, @NotNull SpiAccountReference reference, @NotNull SpiAmount amount, @NotNull AspspConsentData aspspConsentData) {
        try {
            //TODO Account data reads should be performed through specially created endpoint https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/383
            List<SpiAccountDetails> accounts = Optional.ofNullable(
                aspspRestTemplate.exchange(
                    remoteSpiUrls.getAccountDetailsByIban(),
                    HttpMethod.GET,
                    new HttpEntity<>(null), new ParameterizedTypeReference<List<SpiAccountDetails>>() {
                    }, reference.getIban())
                    .getBody())
                                                   .orElseGet(Collections::emptyList);
            List<SpiAccountBalance> balances = extractAccountBalancesByCurrency(accounts, reference.getCurrency());

            return SpiResponse.<Boolean>builder()
                       .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .payload(isBalancesSufficient(balances, amount))
                       .success();

        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<Boolean>builder()
                           .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }
            return SpiResponse.<Boolean>builder()
                       .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
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
