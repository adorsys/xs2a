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

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.vop.CommonVop;
import de.adorsys.psd2.xs2a.domain.vop.GetCommonVopResponse;
import de.adorsys.psd2.xs2a.service.context.SpiVopContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiVopContextData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for VOP (Verification of Payee) read operations.
 *
 * <p>This class implements the Template Method pattern to provide a common structure
 * for retrieving VOP verification results while allowing subclasses to define
 * specific implementation details for different VOP types or strategies.</p>
 *
 * <p>The template method {@link #getVopResult(String)} orchestrates the overall
 * VOP retrieval process, delegating specific steps to abstract methods that
 * subclasses must implement:</p>
 * <ul>
 *   <li>{@link #getSpiVopById(SpiVopContextData, String)} - Retrieve VOP from SPI layer</li>
 *   <li>{@link #mapToXs2aGetVop(Object)} - Map SPI response to XS2A domain object</li>
 * </ul>
 *
 * <p>This design ensures consistent error handling, logging, and context management
 * across all VOP read implementations while maintaining flexibility for different
 * VOP verification strategies.</p>
 *
 * @see ReadVopService
 * @see ResponseObject
 * @see CommonVop
 */
@Slf4j
@AllArgsConstructor
public abstract class AbstractReadVopService<R> implements ReadVopService {

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
     * Retrieves VOP verification result by VOP ID.
     *
     * <p>This template method orchestrates the VOP retrieval process:</p>
     * <ol>
     *   <li>Validates the VOP ID parameter</li>
     *   <li>Creates SPI context data for the request</li>
     *   <li>Calls the SPI layer to retrieve VOP data</li>
     *   <li>Handles errors from the SPI layer</li>
     *   <li>Maps the successful SPI response to XS2A domain object</li>
     * </ol>
     *
     * @param vopId the unique identifier of the VOP verification request.
     *              Must not be null or empty.
     * @return a {@link ResponseObject} containing either the VOP result
     * or error information if the retrieval failed
     * @throws IllegalArgumentException if vopId is null or empty
     */
    @Override
    public ResponseObject<GetCommonVopResponse> getVopResult(@NotNull String vopId) {
        // Validate VOP ID - return error response if validation fails
        if (StringUtils.isBlank(vopId)) {
            log.warn("VOP ID is null or empty - validation failed");
            return ResponseObject.<GetCommonVopResponse>builder()
                .fail(de.adorsys.psd2.xs2a.core.error.ErrorType.VOP_400,
                      de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of(de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR))
                .build();
        }

        log.info("Retrieving VOP result for VOP ID: [{}]", vopId);

        // Create SPI context data for the request
        SpiVopContextData spiContextData = spiVopContextDataProvider.provide();

        // Retrieve VOP from SPI layer
        SpiResponse<R> spiResponse = getSpiVopById(spiContextData, vopId);

        // Handle SPI errors - return error response if SPI call failed
        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, de.adorsys.psd2.xs2a.core.mapper.ServiceType.VOP);
            log.info("VOP-ID [{}]. Read Bulk VOP failed. Can't get VOP by ID at SPI level. Error msg: [{}]",
                     vopId, errorHolder);
            return ResponseObject.<GetCommonVopResponse>builder()
                .fail(errorHolder)
                .build();
        }

        // Map SPI response to XS2A domain object
        GetCommonVopResponse xs2aVop = mapToXs2aGetVop(spiResponse.getPayload());

        log.info("VOP-ID [{}]. VOP result successfully retrieved", vopId);

        // Return success response with the mapped VOP data
        return ResponseObject.<GetCommonVopResponse>builder()
            .body(xs2aVop)
            .build();
    }

    /**
     * Retrieves VOP data from the SPI (Service Provider Interface) layer.
     *
     * <p>Concrete implementations must define how to call the appropriate SPI service
     * to retrieve VOP verification results. This may vary depending on the VOP type
     * or verification strategy being used.</p>
     *
     * @param spiContextData the SPI context data containing request information,
     *                       security details, and contract ID
     * @param vopId the unique identifier of the VOP verification request
     * @return SPI response containing VOP data or error information
     */
    protected abstract SpiResponse<R> getSpiVopById(SpiVopContextData spiContextData, String vopId);

    /**
     * Maps SPI VOP response to XS2A domain object.
     *
     * <p>Concrete implementations must define how to extract and transform VOP data
     * from the SPI layer response into the appropriate XS2A domain representation.
     * This allows for different mapping strategies for various VOP types.</p>
     *
     * @param spiResponse the SPI response containing VOP data
     * @return CommonVop domain object containing the mapped VOP verification result
     */
    protected abstract GetCommonVopResponse mapToXs2aGetVop(R spiResponse);
}
