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

import de.adorsys.psd2.xs2a.domain.vop.BulkVop;
import de.adorsys.psd2.xs2a.domain.vop.BulkVopInitiationResponse;
import de.adorsys.psd2.xs2a.service.context.SpiVopContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aVopResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiBulkVopMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiVopContextData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.vop.response.SpiBulkVopInitiationResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkVopSpi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service implementation for bulk VOP verification initiation.
 * This service handles the initiation of bulk Value-of-Payment (VOP) verifications,
 * following the pattern established in SingleVopInitiationService.
 */
@Slf4j
@Service
public class BulkVopInitiationService extends AbstractVopInitiationService<BulkVop, SpiBulkVopInitiationResponse> {
    private final BulkVopSpi bulkVopSpi;
    private final Xs2aToSpiBulkVopMapper xs2aToSpiBulkVopMapper;
    private final SpiToXs2aVopResponseMapper spiToXs2aVopResponseMapper;

    public BulkVopInitiationService(SpiVopContextDataProvider spiVopContextDataProvider,
                                    SpiErrorMapper spiErrorMapper,
                                    BulkVopSpi bulkVopSpi,
                                    Xs2aToSpiBulkVopMapper xs2aToSpiBulkVopMapper,
                                    SpiToXs2aVopResponseMapper spiToXs2aVopResponseMapper) {
        super(spiVopContextDataProvider, spiErrorMapper);
        this.bulkVopSpi = bulkVopSpi;
        this.xs2aToSpiBulkVopMapper = xs2aToSpiBulkVopMapper;
        this.spiToXs2aVopResponseMapper = spiToXs2aVopResponseMapper;
    }

    /**
     * Initiates SPI verification for bulk VOP requests.
     * Maps the XS2A BulkVop domain object to SPI format and delegates to the SPI layer.
     *
     * @param spiVopContextData         the SPI context data containing PSU and TPP information
     * @param bulkVop                       the bulk VOP request to be verified
     * @param partyVerificationEndpoint the endpoint URL for party verification
     * @return SPI response containing the bulk VOP initiation result
     */
    @Override
    protected SpiResponse<SpiBulkVopInitiationResponse> initiateSpiVerification(SpiVopContextData spiVopContextData,
                                                                                 BulkVop bulkVop,
                                                                                 String partyVerificationEndpoint) {
        log.debug("Initiating bulk VOP verification for VOP ID: {}", bulkVop.getVopId());

        return bulkVopSpi.performVerification(
            spiVopContextData,
            xs2aToSpiBulkVopMapper.mapToSpiBulkVop(bulkVop, partyVerificationEndpoint)
        );
    }

    /**
     * Maps the SPI bulk VOP initiation response to the XS2A domain response.
     *
     * @param spiResponse the SPI bulk VOP initiation response
     * @return the mapped XS2A bulk VOP initiation response
     */
    @Override
    protected BulkVopInitiationResponse mapToXs2aResponse(SpiBulkVopInitiationResponse spiResponse) {
        log.debug("Mapping SPI bulk VOP response to XS2A domain response");
        return spiToXs2aVopResponseMapper.mapToBulkVopInitiationResponse(spiResponse);
    }
}
