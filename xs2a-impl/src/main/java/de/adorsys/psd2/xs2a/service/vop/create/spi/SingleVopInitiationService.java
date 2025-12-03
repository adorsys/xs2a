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

package de.adorsys.psd2.xs2a.service.vop.create.spi;

import de.adorsys.psd2.xs2a.domain.vop.SingleVop;
import de.adorsys.psd2.xs2a.domain.vop.SingleVopInitiationReponse;
import de.adorsys.psd2.xs2a.service.context.SpiVopContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aVopResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiSingleVopMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiVopContextData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.vop.response.SpiSingleVopInitiationResponse;
import de.adorsys.psd2.xs2a.spi.service.SingleVopSpi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service implementation for single VOP verification initiation
 */
@Slf4j
@Service
public class SingleVopInitiationService extends AbstractVopInitiationService<SingleVop, SpiSingleVopInitiationResponse> {
    private final SingleVopSpi singleVopSpi;
    private final Xs2aToSpiSingleVopMapper xs2AToSpiSingleVopMapper;
    private final SpiToXs2aVopResponseMapper spiToXs2aVopResponseMapper;

    public SingleVopInitiationService(SpiVopContextDataProvider spiVopContextDataProvider,
                                      SpiErrorMapper spiErrorMapper,
                                      SingleVopSpi singleVopSpi,
                                      Xs2aToSpiSingleVopMapper xs2AToSpiSingleVopMapper,
                                      SpiToXs2aVopResponseMapper spiToXs2aVopResponseMapper) {
        super(spiVopContextDataProvider, spiErrorMapper);
        this.singleVopSpi = singleVopSpi;
        this.xs2AToSpiSingleVopMapper = xs2AToSpiSingleVopMapper;
        this.spiToXs2aVopResponseMapper = spiToXs2aVopResponseMapper;
    }

    @Override
    protected SpiResponse<SpiSingleVopInitiationResponse> initiateSpiVerification(SpiVopContextData spiVopContextData, SingleVop vop,
                                                                                  String partyVerificationEndpoint) {

        return singleVopSpi.performVerification(spiVopContextData,xs2AToSpiSingleVopMapper.mapToSpiSingleVop(vop, partyVerificationEndpoint));
    }

    /**
     * @param spiResponse
     * @return
     */
    @Override
    protected SingleVopInitiationReponse mapToXs2aResponse(SpiSingleVopInitiationResponse spiResponse) {
        return spiToXs2aVopResponseMapper.mapToVopInitiationResponse(spiResponse);
    }

}
