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

package de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.aspsp.xs2a.domain.CashAccountType;
import de.adorsys.aspsp.xs2a.domain.account.AccountStatus;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aUsageType;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountStatus;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountType;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiUsageType;
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

    public Xs2aAccountDetails mapToXs2aAccountDetails(SpiAccountDetails accountDetails) {
        return Optional.ofNullable(accountDetails)
                   .map(ad -> new Xs2aAccountDetails(
                           ad.getId(),
                           ad.getIban(),
                           ad.getBban(),
                           ad.getPan(),
                           ad.getMaskedPan(),
                           ad.getMsisdn(),
                           ad.getCurrency(),
                           ad.getName(),
                           ad.getProduct(),
                           mapToAccountType(ad.getCashSpiAccountType()),
                           mapToAccountStatus(ad.getSpiAccountStatus()),
                           ad.getBic(),
                           ad.getLinkedAccounts(),
                           mapToXs2aUsageType(ad.getUsageType()),
                           ad.getDetails(),
                           balanceMapper.mapToXs2aBalanceList(ad.getBalances())
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
