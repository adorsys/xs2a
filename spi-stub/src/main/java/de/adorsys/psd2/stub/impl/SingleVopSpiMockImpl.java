/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.stub.impl;

import de.adorsys.psd2.model.PartyIdentificationMatchCode;
import de.adorsys.psd2.model.PartyNameMatchCode;
import de.adorsys.psd2.xs2a.spi.domain.SpiVopContextData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.vop.SpiSingleVop;
import de.adorsys.psd2.xs2a.spi.domain.vop.response.SpiSingleVopInitiationResponse;
import de.adorsys.psd2.xs2a.spi.service.SingleVopSpi;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SingleVopSpiMockImpl implements SingleVopSpi {
    private static final String MATCHED_NAME = "Mocked matched name from ASPSP";
    private static final String ADDITIONAL_INFO = "Mocked verification additional information";

    @Override
    public @NotNull SpiResponse<SpiSingleVopInitiationResponse> performVerification(@NotNull SpiVopContextData vopContextData, @NotNull SpiSingleVop singleVop) {
        log.info("SingleVopSpi#performVerification: vopContextData {}, spiSingleVop {}", vopContextData, singleVop);
        
        SpiSingleVopInitiationResponse response = new SpiSingleVopInitiationResponse();
        response.setPartyNameMatch(PartyNameMatchCode.MTCH);
        response.setPartyIdMatch(PartyIdentificationMatchCode.MTCH);
        response.setMatchedName(MATCHED_NAME);
        response.setAdditionalInformation(ADDITIONAL_INFO);

        return SpiResponse.<SpiSingleVopInitiationResponse>builder()
                   .payload(response)
                   .build();
    }
}
