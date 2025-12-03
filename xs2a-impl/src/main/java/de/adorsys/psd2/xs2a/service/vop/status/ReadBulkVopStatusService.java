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

package de.adorsys.psd2.xs2a.service.vop.status;

import de.adorsys.psd2.xs2a.domain.vop.GetBulkVopStatusResponse;
import de.adorsys.psd2.xs2a.domain.vop.GetCommonVopStatusResponse;
import de.adorsys.psd2.xs2a.service.context.SpiVopContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aVopResponseMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiVopContextData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.vop.SpiGetBulkVopResponse;
import de.adorsys.psd2.xs2a.spi.domain.vop.SpiGetBulkVopStatusResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkVopSpi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Concrete implementation of VOP status read service for bulk VOP (Verification of Payee) operations.
 *
 * <p>This service handles the retrieval of bulk VOP verification status, which provides
 * the current processing state of a bulk party verification request without the full
 * verification results. This is particularly useful for:</p>
 * <ul>
 *   <li>Polling the status of long-running bulk verification requests</li>
 *   <li>Checking if a bulk verification has completed before retrieving full results</li>
 *   <li>Monitoring the progress of asynchronous bulk verification processing</li>
 * </ul>
 *
 * <p>This implementation extends {@link AbstractReadVopStatusService} and provides specific
 * logic for:</p>
 * <ul>
 *   <li>Retrieving bulk VOP status from the SPI layer using {@link BulkVopSpi}</li>
 *   <li>Mapping SPI bulk VOP status responses to XS2A domain {@link GetBulkVopStatusResponse} objects</li>
 *   <li>Extracting only status-relevant information from the SPI response</li>
 * </ul>
 *
 * <p>The service is registered as a Spring bean and can be dependency-injected
 * into other components that need to query bulk VOP status.</p>
 *
 * <p><b>Design Notes:</b></p>
 * <ul>
 *   <li>Uses {@link SpiGetBulkVopStatusResponse} as the SPI response type, extracting only status fields</li>
 *   <li>Follows the Template Method pattern from {@link AbstractReadVopStatusService}</li>
 *   <li>Provides consistent error handling and logging via the abstract base class</li>
 * </ul>
 *
 * @see AbstractReadVopStatusService
 * @see GetBulkVopStatusResponse
 * @see BulkVopSpi
 * @see SpiGetBulkVopStatusResponse
 */
@Slf4j
@Service
public class ReadBulkVopStatusService extends AbstractReadVopStatusService<SpiGetBulkVopStatusResponse> {

    /**
     * SPI service for bulk VOP operations.
     * Used to communicate with the ASPSP's backend systems for bulk verification status retrieval.
     */
    private final BulkVopSpi bulkVopSpi;
    private final SpiToXs2aVopResponseMapper spiToXs2aVopResponseMapper;

    /**
     * Constructs a new ReadBulkVopStatusService with required dependencies.
     *
     * <p>Dependencies are injected via constructor injection following Spring best practices
     * for immutability and testability. All dependencies are required and must not be null.</p>
     *
     * @param bulkVopSpi the SPI service for bulk VOP operations
     * @param spiVopContextDataProvider provider for creating VOP-specific SPI context data
     * @param spiErrorMapper mapper for converting SPI errors to XS2A error holders
     */
    public ReadBulkVopStatusService(BulkVopSpi bulkVopSpi,
                                    SpiVopContextDataProvider spiVopContextDataProvider,
                                    SpiErrorMapper spiErrorMapper, SpiToXs2aVopResponseMapper spiToXs2aVopResponseMapper) {
        super(spiVopContextDataProvider, spiErrorMapper);
        this.bulkVopSpi = bulkVopSpi;
        this.spiToXs2aVopResponseMapper = spiToXs2aVopResponseMapper;
    }

    /**
     * Retrieves bulk VOP status data from the SPI layer by VOP ID.
     *
     * <p>This method delegates to the {@link BulkVopSpi} to fetch the bulk VOP status.
     * The ASPSP's SPI implementation should return lightweight status information without
     * the full verification results for efficient status polling.</p>
     *
     * <p>The actual implementation depends on the ASPSP's SPI implementation, which may:</p>
     * <ul>
     *   <li>Query a database for the current verification status</li>
     *   <li>Call an external service to retrieve the status</li>
     *   <li>Return cached status information</li>
     * </ul>
     *
     * <p><b>Error Handling:</b></p>
     * <p>If the SPI layer encounters errors (e.g., VOP not found, invalid ID, system errors),
     * they will be returned in the SpiResponse and handled by the abstract base class.</p>
     *
     * @param spiContextData the SPI context data containing request information,
     *                       security details, contract ID, and TPP information
     * @param vopId the unique identifier of the bulk VOP verification request
     * @return SPI response containing the bulk VOP status data or error information
     */
    @Override
    protected SpiResponse<SpiGetBulkVopStatusResponse> getSpiVopStatusById(SpiVopContextData spiContextData, String vopId) {
        log.debug("Retrieving bulk VOP status from SPI layer for VOP ID: [{}]", vopId);
        return bulkVopSpi.getBulkVopStatusById(spiContextData, vopId);
    }

    /**
     * Maps SPI bulk VOP response to XS2A bulk VOP status response.
     *
     * <p>This method extracts only the status-relevant fields from the SPI bulk VOP response
     * and maps them to the XS2A domain status object. The mapping includes:</p>
     * <ul>
     *   <li><b>vopBulkId</b> - The unique identifier of the bulk verification request</li>
     *   <li><b>vopBulkStatus</b> - The current status of the bulk verification (PENDING, COMPLETED, REJECTED, etc.)</li>
     * </ul>
     *
     * <p><b>Note:</b> Since the SPI layer currently returns the full bulk VOP response
     * ({@link SpiGetBulkVopResponse}), this method only extracts the status fields.
     * The reasonCode and reasonProprietary fields are set to null as they are not
     * available in the SPI response. A future SPI enhancement may provide these fields
     * via a dedicated status response type.</p>
     *
     * <p>Unlike the full result retrieval, this mapping does <b>not</b> include:</p>
     * <ul>
     *   <li>Individual party verification request/response pairs</li>
     *   <li>Detailed verification results</li>
     *   <li>Match scores or detailed match information</li>
     * </ul>
     *
     * <p>This lightweight mapping is intentional to support efficient status polling
     * without the overhead of transferring full verification results.</p>
     *
     * <p><b>Null Safety:</b> If the SPI response is null, this method will create
     * an empty status response object. Null fields in the SPI response will be
     * mapped as null in the XS2A response.</p>
     *
     * @param spiVopStatus the SPI bulk VOP response containing status information
     * @return {@link GetBulkVopStatusResponse} containing only status-relevant fields
     */
    @Override
    protected GetCommonVopStatusResponse mapToXs2aGetVopStatus(SpiGetBulkVopStatusResponse spiVopStatus) {
        log.debug("Mapping SPI bulk VOP status response to XS2A domain status response");

        return spiToXs2aVopResponseMapper.mapToGetBulkVopStatusResponse(spiVopStatus);
    }
}
