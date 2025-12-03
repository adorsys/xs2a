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

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.domain.vop.CommonVop;
import de.adorsys.psd2.xs2a.domain.vop.ErrorVopInitiationResponse;
import de.adorsys.psd2.xs2a.domain.vop.VopInitiationResponse;
import de.adorsys.psd2.xs2a.service.context.SpiVopContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiVopContextData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract service for initiating VOP verifications in SPI
 *
 * @param <T> type of VOP handled by this service
 * @param <R> type of SPI response on VOP initiation
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractVopInitiationService<T extends CommonVop, R> implements VopInitiationService<T> {
    private final SpiVopContextDataProvider spiVopContextDataProvider;
    private final SpiErrorMapper spiErrorMapper;

    @Override
    public VopInitiationResponse performVerification(T vop, String partyVerificationEndpoint) {
        SpiVopContextData spiVopContextData = spiVopContextDataProvider.provide();

        SpiResponse<R> spiResponse = initiateSpiVerification(spiVopContextData, vop, partyVerificationEndpoint);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.VOP);
            log.info("SPI VOP verification failed with error: {}", errorHolder);
            return new ErrorVopInitiationResponse(errorHolder);
        }

        return mapToXs2aResponse(spiResponse.getPayload());
    }

    /**
     * Initiates SPI verification for VOP.
     *
     * @param spivOPContextData the SPI VOP context data
     * @param vop the VOP request object
     * @param partyVerificationEndpoint the party verification endpoint
     * @return SPI response containing the verification result
     */
    protected abstract SpiResponse<R> initiateSpiVerification(SpiVopContextData spivOPContextData, T vop, String partyVerificationEndpoint);

    /**
     * Maps the SPI response to XS2A VOP initiation response.
     *
     * @param spiResponse the SPI response
     * @return the XS2A VOP initiation response
     */
    protected abstract VopInitiationResponse mapToXs2aResponse(R spiResponse);
}
