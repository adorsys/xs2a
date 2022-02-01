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
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationRequest;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationResponse;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.FundsConfirmationSpi;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FundsConfirmationSpiMockImpl implements FundsConfirmationSpi {
    @Override
    @NotNull
    public SpiResponse<SpiFundsConfirmationResponse> performFundsSufficientCheck(@NotNull SpiContextData contextData, @Nullable SpiPiisConsent spiPiisConsent, @NotNull SpiFundsConfirmationRequest spiFundsConfirmationRequest, @Nullable SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("FundsConfirmationSpi#performFundsSufficientCheck: contextData {}, spiPiisConsent {}, spiFundsConfirmationRequest {}, aspspConsentData {}",
                 contextData, spiPiisConsent, spiFundsConfirmationRequest, aspspConsentDataProvider != null ? aspspConsentDataProvider.loadAspspConsentData() : null);
        SpiFundsConfirmationResponse response = new SpiFundsConfirmationResponse();
        response.setFundsAvailable(true);

        return SpiResponse.<SpiFundsConfirmationResponse>builder()
                   .payload(response)
                   .build();
    }
}
