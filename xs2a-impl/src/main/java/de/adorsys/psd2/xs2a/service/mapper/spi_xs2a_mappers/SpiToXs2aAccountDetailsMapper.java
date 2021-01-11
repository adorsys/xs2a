/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.domain.CashAccountType;
import de.adorsys.psd2.xs2a.domain.account.AccountStatus;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aUsageType;
import de.adorsys.psd2.xs2a.spi.domain.account.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SpiToXs2aAccountDetailsMapper {
    private final SpiToXs2aBalanceMapper balanceMapper;
    private final SpiToXs2aAddressMapper spiToXs2aAddressMapper;
    private final SpiToXs2aAmountMapper spiToXs2aAmountMapper;

    public Xs2aAccountDetails mapToXs2aAccountDetails(SpiAccountDetails accountDetails) {
        return Optional.ofNullable(accountDetails)
                   .map(ad -> new Xs2aAccountDetails(
                            ad.getAspspAccountId(),
                            ad.getResourceId(),
                            ad.getIban(),
                            ad.getBban(),
                            ad.getPan(),
                            ad.getMaskedPan(),
                            ad.getMsisdn(),
                            ad.getCurrency(),
                            ad.getName(),
                            ad.getDisplayName(),
                            ad.getProduct(),
                            mapToAccountType(ad.getCashSpiAccountType()),
                            mapToAccountStatus(ad.getSpiAccountStatus()),
                            ad.getBic(),
                            ad.getLinkedAccounts(),
                            mapToXs2aUsageType(ad.getUsageType()),
                            ad.getDetails(),
                            balanceMapper.mapToXs2aBalanceList(ad.getBalances()),
                            accountDetails.getOwnerName(),
                            Optional.ofNullable(accountDetails.getOwnerAddress()).map(spiToXs2aAddressMapper::mapToAddress).orElse(null)
                        )
                   )
                   .orElse(null);
    }

    public Xs2aCardAccountDetails mapToXs2aCardAccountDetails(SpiCardAccountDetails cardAccountDetails) {
        return Optional.ofNullable(cardAccountDetails)
                   .map(ad -> new Xs2aCardAccountDetails(
                            ad.getAspspAccountId(),
                            ad.getResourceId(),
                            ad.getMaskedPan(),
                            ad.getCurrency(),
                            ad.getName(),
                            ad.getDisplayName(),
                            ad.getProduct(),
                            mapToAccountType(ad.getCashSpiAccountType()),
                            mapToAccountStatus(ad.getSpiAccountStatus()),
                            mapToXs2aUsageType(ad.getUsageType()),
                            ad.getDetails(),
                            balanceMapper.mapToXs2aBalanceList(ad.getBalances()),
                            Optional.ofNullable(ad.getCreditLimit()).map(spiToXs2aAmountMapper::mapToXs2aAmount).orElse(null),
                            ad.getOwnerName(),
                            ad.getDebitAccounting()
                        )
                   )
                   .orElse(null);

    }

    public List<Xs2aAccountDetails> mapToXs2aAccountDetailsList(List<SpiAccountDetails> accountDetails) {
        if (CollectionUtils.isEmpty(accountDetails)) {
            return new ArrayList<>();
        }

        return accountDetails.stream()
                   .map(this::mapToXs2aAccountDetails)
                   .collect(Collectors.toList());
    }

    public List<Xs2aCardAccountDetails> mapToXs2aCardAccountDetailsList(List<SpiCardAccountDetails> cardAccountDetails) {
        if (CollectionUtils.isEmpty(cardAccountDetails)) {
            return new ArrayList<>();
        }

        return cardAccountDetails.stream()
                   .map(this::mapToXs2aCardAccountDetails)
                   .collect(Collectors.toList());
    }

    private CashAccountType mapToAccountType(SpiAccountType spiAccountType) {
        return Optional.ofNullable(spiAccountType)
                   .map(type -> CashAccountType.valueOf(type.name()))
                   .orElse(null);
    }

    private AccountStatus mapToAccountStatus(SpiAccountStatus spiAccountStatus) {
        return Optional.ofNullable(spiAccountStatus)
                   .map(status -> AccountStatus.valueOf(status.name()))
                   .orElse(null);
    }

    private Xs2aUsageType mapToXs2aUsageType(SpiUsageType spiUsageType) {
        return Optional.ofNullable(spiUsageType)
                   .map(usage -> Xs2aUsageType.valueOf(usage.name()))
                   .orElse(null);
    }
}
