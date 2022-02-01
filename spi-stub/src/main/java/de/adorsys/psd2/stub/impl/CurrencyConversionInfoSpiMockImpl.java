/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.stub.impl;

import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCurrencyConversionInfo;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CurrencyConversionInfoSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Currency;

@Service
@Slf4j
public class CurrencyConversionInfoSpiMockImpl implements CurrencyConversionInfoSpi {
    @NotNull
    @Override
    public SpiResponse<SpiCurrencyConversionInfo> getCurrencyConversionInfo(@NotNull SpiContextData contextData, @NotNull SpiPayment payment, @NotNull String authorisationId, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("CurrencyConversionInfoSpi#getCurrencyConversionInfo: contextData {}, payment {}, authorisationId{}, aspspConsentData {}", contextData, payment, authorisationId, aspspConsentDataProvider.loadAspspConsentData());
        return SpiResponse.<SpiCurrencyConversionInfo>builder()
                       .payload(new SpiCurrencyConversionInfo(
                               new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(500)),
                               new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(300)),
                               new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(900)),
                               new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(250))
                       ))
                       .build();
    }
}
