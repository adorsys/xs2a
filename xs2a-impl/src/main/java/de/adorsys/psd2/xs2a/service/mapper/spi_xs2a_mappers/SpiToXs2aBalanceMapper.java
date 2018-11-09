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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.domain.BalanceType;
import de.adorsys.psd2.xs2a.domain.Xs2aBalance;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountBalance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SpiToXs2aBalanceMapper {
    private final SpiToXs2aAmountMapper amountMapper;

    public Xs2aBalance mapToXs2aBalance(SpiAccountBalance spiAccountBalance) {
        return Optional.ofNullable(spiAccountBalance)
                   .map(b -> {
                       Xs2aBalance balance = new Xs2aBalance();
                       balance.setBalanceAmount(amountMapper.mapToXs2aAmount(spiAccountBalance.getSpiBalanceAmount()));
                       balance.setBalanceType(BalanceType.valueOf(spiAccountBalance.getSpiBalanceType().name()));
                       balance.setLastChangeDateTime(spiAccountBalance.getLastChangeDateTime());
                       balance.setReferenceDate(spiAccountBalance.getReferenceDate());
                       balance.setLastCommittedTransaction(spiAccountBalance.getLastCommittedTransaction());
                       return balance;
                   })
                   .orElse(null);
    }

    public List<Xs2aBalance> mapToXs2aBalanceList(List<SpiAccountBalance> spiBalances) {
        if (CollectionUtils.isEmpty(spiBalances)) {
            return new ArrayList<>();
        }
        return spiBalances.stream()
                   .map(this::mapToXs2aBalance)
                   .collect(Collectors.toList());
    }
}
