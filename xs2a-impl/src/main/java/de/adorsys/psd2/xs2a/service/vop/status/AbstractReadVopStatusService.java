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

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.vop.GetCommonVopStatusResponse;
import de.adorsys.psd2.xs2a.service.context.SpiVopContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiVopContextData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for VOP (Verification of Payee) status read operations.
 *
 * <p>This class implements the Template Method pattern to provide a common structure
 * for retrieving VOP verification status while allowing subclasses to define
 * specific implementation details for different VOP types or strategies.</p>
 *
 * <p>The template method {@link #getVopStatus(String)} orchestrates the overall
 * VOP status retrieval process, delegating specific steps to abstract methods that
 * subclasses must implement:</p>
 * <ul>
 *   <li>{@link #getSpiVopStatusById(SpiVopContextData, String)} - Retrieve VOP status from SPI layer</li>
 *   <li>{@link #mapToXs2aGetVopStatus(Object)} - Map SPI response to XS2A domain object</li>
 * </ul>
 *
 * <p>This design ensures consistent error handling, logging, validation, and context management
 * across all VOP status read implementations while maintaining flexibility for different
 * VOP verification strategies (single, bulk, etc.).</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Input validation with detailed error responses</li>
 *   <li>Standardized logging for debugging and audit trails</li>
 *   <li>Consistent SPI error mapping and handling</li>
 *   <li>Generic type parameter for flexible SPI response types</li>
 * </ul>
 *
 * @param <R> the SPI response type returned by {@link #getSpiVopStatusById(SpiVopContextData, String)}
 *
 * @see ReadVopStatusService
 * @see ResponseObject
 * @see GetCommonVopStatusResponse
 */
@Slf4j
@AllArgsConstructor
public abstract class AbstractReadVopStatusService<R> implements ReadVopStatusService {

    /**
     * Provider for creating SPI context data specific to VOP operations.
     * Used to build the appropriate context including security and contract information.
     */
    protected final SpiVopContextDataProvider spiVopContextDataProvider;

    /**
     * Mapper for converting SPI error responses to XS2A error holders.
     * Ensures consistent error handling across the application.
     */
    private final SpiErrorMapper spiErrorMapper;

    /**
     * Retrieves VOP verification status by VOP ID.
     *
     * <p>This template method orchestrates the VOP status retrieval process:</p>
     * <ol>
     *   <li>Validates the VOP ID parameter (null/empty check)</li>
     *   <li>Logs the status retrieval request</li>
     *   <li>Creates SPI context data for the request</li>
     *   <li>Calls the SPI layer to retrieve VOP status data</li>
     *   <li>Handles and maps errors from the SPI layer</li>
     *   <li>Maps the successful SPI response to XS2A domain object</li>
     *   <li>Returns the result wrapped in a ResponseObject</li>
     * </ol>
     *
     * @param vopId the unique identifier of the VOP verification request.
     *              Must not be null or empty.
     * @return a {@link ResponseObject} containing either:
     *         <ul>
     *           <li>Success: {@link GetCommonVopStatusResponse} with current status</li>
     *           <li>Failure: error information (validation failure, VOP not found, SPI errors, etc.)</li>
     *         </ul>
     * @throws IllegalArgumentException if vopId is null or empty
     */
    @Override
    public ResponseObject<GetCommonVopStatusResponse> getVopStatus(@NotNull String vopId) {
        // Validate VOP ID - return error response if validation fails
        if (StringUtils.isBlank(vopId)) {
            log.warn("VOP ID is null or empty - validation failed");
            return ResponseObject.<GetCommonVopStatusResponse>builder()
                .fail(ErrorHolder.builder(ErrorType.VOP_400)
                    .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR, "VOP ID must not be null or empty"))
                    .build())
                .build();
        }

        log.info("Retrieving VOP status for VOP ID: [{}]", vopId);

        // Create SPI context data for the request
        SpiVopContextData spiContextData = spiVopContextDataProvider.provide();

        // Retrieve VOP status from SPI layer
        SpiResponse<R> spiResponse = getSpiVopStatusById(spiContextData, vopId);

        // Handle SPI errors - return error response if SPI call failed
        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.VOP);
            log.info("VOP-ID [{}]. Read VOP Status failed. Can't get VOP status by ID at SPI level. Error msg: [{}]",
                     vopId, errorHolder);
            return ResponseObject.<GetCommonVopStatusResponse>builder()
                .fail(errorHolder)
                .build();
        }

        // Map SPI response to XS2A domain object
        GetCommonVopStatusResponse xs2aVopStatus = mapToXs2aGetVopStatus(spiResponse.getPayload());

        log.info("VOP-ID [{}]. VOP status successfully retrieved", vopId);

        // Return success response with the mapped VOP status data
        return ResponseObject.<GetCommonVopStatusResponse>builder()
            .body(xs2aVopStatus)
            .build();
    }

    /**
     * Retrieves VOP status data from the SPI (Service Provider Interface) layer.
     *
     * <p>Concrete implementations must define how to call the appropriate SPI service
     * to retrieve VOP verification status. This may vary depending on the VOP type
     * (single verification, bulk verification) or verification strategy being used.</p>
     *
     * <p>The SPI layer is responsible for:</p>
     * <ul>
     *   <li>Communicating with the bank's backend systems</li>
     *   <li>Retrieving current verification status</li>
     *   <li>Handling any SPI-level errors or exceptions</li>
     * </ul>
     *
     * @param spiContextData the SPI context data containing request information,
     *                       security details, contract ID, and TPP information
     * @param vopId the unique identifier of the VOP verification request
     * @return SPI response containing VOP status data (type R) or error information
     */
    protected abstract SpiResponse<R> getSpiVopStatusById(SpiVopContextData spiContextData, String vopId);

    /**
     * Maps SPI VOP status response to XS2A domain object.
     *
     * <p>Concrete implementations must define how to extract and transform VOP status data
     * from the SPI layer response into the appropriate XS2A domain representation.
     * This allows for different mapping strategies for various VOP types and status structures.</p>
     *
     * <p>The mapping should include:</p>
     * <ul>
     *   <li>VOP identification information (ID, bulk ID if applicable)</li>
     *   <li>Current verification status</li>
     *   <li>Timestamps and metadata</li>
     *   <li>Any additional status-specific information</li>
     * </ul>
     *
     * @param spiVopStatus the SPI response payload containing VOP status data
     * @return {@link GetCommonVopStatusResponse} domain object containing the mapped VOP status
     */
    protected abstract GetCommonVopStatusResponse mapToXs2aGetVopStatus(R spiVopStatus);
}
