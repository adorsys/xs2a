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

import de.adorsys.psd2.xs2a.domain.Xs2aExchangeRate;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiExchangeRate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SpiToXs2aExchangeRateMapper {

    public Xs2aExchangeRate mapToExchangeRate(SpiExchangeRate spiExchangeRate) {
        return Optional.ofNullable(spiExchangeRate)
                   .map(e -> {
                       Xs2aExchangeRate exchangeRate = new Xs2aExchangeRate();
                       exchangeRate.setRateContract(spiExchangeRate.getRateContract());
                       exchangeRate.setUnitCurrency(spiExchangeRate.getUnitCurrency());
                       exchangeRate.setRate(spiExchangeRate.getRate());
                       exchangeRate.setSourceCurrency(spiExchangeRate.getSourceCurrency());
                       exchangeRate.setTargetCurrency(spiExchangeRate.getTargetCurrency());
                       exchangeRate.setRateDate(spiExchangeRate.getRateDate());

                       return exchangeRate;
                   })
                   .orElse(null);
    }

    public List<Xs2aExchangeRate> mapToExchangeRateList(List<SpiExchangeRate> spiExchangeRates) {
        if (CollectionUtils.isEmpty(spiExchangeRates)) {
            return new ArrayList<>();
        }

        return spiExchangeRates.stream()
                   .map(this::mapToExchangeRate)
                   .collect(Collectors.toList());
    }
}
