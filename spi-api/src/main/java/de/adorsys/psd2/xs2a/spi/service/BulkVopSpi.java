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

package de.adorsys.psd2.xs2a.spi.service;

import de.adorsys.psd2.xs2a.spi.domain.SpiVopContextData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.vop.SpiBulkVop;
import de.adorsys.psd2.xs2a.spi.domain.vop.SpiGetBulkVopResponse;
import de.adorsys.psd2.xs2a.spi.domain.vop.SpiGetBulkVopStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.vop.response.SpiBulkVopInitiationResponse;
import org.jetbrains.annotations.NotNull;

/**
 * Service Provider Interface (SPI) for bulk VOP (Verification of Payee) operations.
 *
 * <p>This interface defines the contract for ASPSP implementations to handle bulk VOP
 * verification requests, which process multiple party verification requests in a single
 * batch operation.</p>
 *
 * @see VopSpi
 * @see SpiBulkVop
 * @see SpiBulkVopInitiationResponse
 */
public interface BulkVopSpi extends VopSpi<SpiBulkVop, SpiBulkVopInitiationResponse> {

    /**
     * Performs bulk party verification.
     *
     * <p>This method initiates a bulk VOP verification request, processing multiple
     * party verification requests as a single batch operation.</p>
     *
     * @param vopContextData the SPI context data containing PSU and TPP information,
     *                       request metadata, and contract ID
     * @param bulkVop        the bulk VOP request containing multiple party verification requests
     * @return SPI response containing the bulk VOP initiation result with verification responses
     * @throws IllegalArgumentException if vopContextData or bulkVop is null
     */
    @NotNull
    @Override
    SpiResponse<SpiBulkVopInitiationResponse> performVerification(@NotNull SpiVopContextData vopContextData, @NotNull SpiBulkVop bulkVop);

    /**
     * Retrieves a bulk VOP verification result by its unique identifier.
     *
     * <p>This method allows fetching a previously initiated bulk VOP verification request
     * using its VOP ID. The implementation should handle cases where:</p>
     * <ul>
     *   <li>The VOP ID is invalid or not found</li>
     *   <li>The context data is null or invalid</li>
     *   <li>Database or storage access errors occur</li>
     * </ul>
     *
     * <p>Implementations should follow existing error handling patterns and return
     * appropriate error information in the SpiResponse when issues occur.</p>
     *
     * @param spiContextData the SPI context data containing request information,
     *                       security details, and contract ID
     * @param vopId          the unique identifier of the bulk VOP verification request
     * @return SPI response containing the bulk VOP data or error information if the
     * VOP ID is not found or an error occurs
     * @throws IllegalArgumentException if spiContextData or vopId is null or empty
     */
    @NotNull
    SpiResponse<SpiGetBulkVopResponse> getBulkVopById(@NotNull SpiVopContextData spiContextData, @NotNull String vopId);

    /**
     * Retrieves only the status information of a bulk VOP verification by its unique identifier.
     *
     * <p>This method is optimized for status polling, returning lightweight status information
     * without the full verification results. This is particularly useful for:</p>
     * <ul>
     *   <li>Polling the current state of long-running bulk verification requests</li>
     *   <li>Checking if a bulk verification has completed before retrieving full results</li>
     *   <li>Monitoring asynchronous bulk verification processing</li>
     * </ul>
     *
     * <p>Unlike {@link #getBulkVopById}, this method returns only status-relevant fields:</p>
     * <ul>
     *   <li><b>vopBulkId</b> - The unique identifier of the bulk verification request</li>
     *   <li><b>vopBulkStatus</b> - The current status (PENDING, COMPLETED, REJECTED, etc.)</li>
     *   <li><b>reasonCode</b> - Optional reason code for the current status</li>
     *   <li><b>reasonProprietary</b> - Optional ASPSP-specific reason text</li>
     * </ul>
     *
     * <p>The implementation should handle error cases similar to {@link #getBulkVopById}:</p>
     * <ul>
     *   <li>The VOP ID is invalid or not found</li>
     *   <li>The context data is null or invalid</li>
     *   <li>Database or storage access errors occur</li>
     * </ul>
     *
     * <p><b>Performance Note:</b> Implementations should optimize this method to retrieve
     * only status fields from storage, avoiding the overhead of loading full verification
     * results, party information, and detailed match scores.</p>
     *
     * @param spiContextData the SPI context data containing request information,
     *                       security details, and contract ID
     * @param vopId          the unique identifier of the bulk VOP verification request
     * @return SPI response containing only the bulk VOP status information or error
     * information if the VOP ID is not found or an error occurs
     * @throws IllegalArgumentException if spiContextData or vopId is null or empty
     */
    @NotNull
    SpiResponse<SpiGetBulkVopStatusResponse> getBulkVopStatusById(@NotNull SpiVopContextData spiContextData, @NotNull String vopId);

}
