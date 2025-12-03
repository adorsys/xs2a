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

package de.adorsys.psd2.xs2a.service.vop.read;

import de.adorsys.psd2.xs2a.domain.vop.BulkVop;
import de.adorsys.psd2.xs2a.domain.vop.GetBulkVopRequestResponse;
import de.adorsys.psd2.xs2a.service.context.SpiVopContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aVopResponseMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiVopContextData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.vop.*;
import de.adorsys.psd2.xs2a.spi.service.BulkVopSpi;
import org.springframework.stereotype.Service;

/**
 * Concrete implementation of VOP read service for bulk VOP (Verification of Payee) operations.
 *
 * <p>This service handles the retrieval of bulk VOP verification results, which contain
 * multiple party verification requests processed as a single batch operation.</p>
 *
 * <p>This implementation extends {@link AbstractReadVopService} and provides specific
 * logic for:</p>
 * <ul>
 *   <li>Retrieving bulk VOP data from the SPI layer using {@link BulkVopSpi}</li>
 *   <li>Mapping SPI bulk VOP responses to XS2A domain {@link BulkVop} objects</li>
 * </ul>
 *
 * <p>The service is registered as a Spring bean with the qualifier "bulkVops" to allow
 * for service type discrimination in dependency injection scenarios.</p>
 *
 * @see AbstractReadVopService
 * @see BulkVop
 * @see BulkVopSpi
 */
@Service("bulkVops")
public class ReadBulkVopService extends AbstractReadVopService<SpiGetBulkVopResponse> {

    private final BulkVopSpi bulkVopSpi;
    private final SpiToXs2aVopResponseMapper spiToXs2aVopResponseMapper;

    /**
     * Constructs a new ReadBulkVopService with required dependencies.
     *
     * <p>Dependencies are injected via constructor injection following Spring best practices
     * for immutability and testability.</p>
     *
     * @param bulkVopSpi the SPI service for bulk VOP operations
     * @param spiVopContextDataProvider provider for creating VOP-specific SPI context data
     * @param spiErrorMapper mapper for converting SPI errors to XS2A error holders
     */
    public ReadBulkVopService(BulkVopSpi bulkVopSpi,
                              SpiVopContextDataProvider spiVopContextDataProvider,
                              SpiErrorMapper spiErrorMapper, SpiToXs2aVopResponseMapper spiToXs2aVopResponseMapper) {
        super(spiVopContextDataProvider, spiErrorMapper);
        this.bulkVopSpi = bulkVopSpi;
        this.spiToXs2aVopResponseMapper = spiToXs2aVopResponseMapper;
    }

    /**
     * Retrieves bulk VOP data from the SPI layer by VOP ID.
     *
     * <p>This method delegates to the {@link BulkVopSpi} to fetch the bulk VOP
     * verification result. The actual implementation depends on the ASPSP's
     * SPI implementation.</p>
     *
     * @param spiContextData the SPI context data containing request information,
     *                       security details, and contract ID
     * @param vopId the unique identifier of the bulk VOP verification request
     * @return SPI response containing the bulk VOP data or error information
     */
    @Override
    protected SpiResponse<SpiGetBulkVopResponse> getSpiVopById(SpiVopContextData spiContextData, String vopId) {
        return bulkVopSpi.getBulkVopById(spiContextData, vopId);
    }


    @Override
    protected GetBulkVopRequestResponse mapToXs2aGetVop(SpiGetBulkVopResponse spiResponse) {
        return spiToXs2aVopResponseMapper.mapToGetBulkVopResponse(spiResponse);
    }


}
