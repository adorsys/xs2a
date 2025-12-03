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

package de.adorsys.psd2.xs2a.spi.domain.vop;

import lombok.Data;

import java.util.UUID;

/**
 * SPI POJO class representing the response for retrieving the status of a bulk VOP (Verification of Payee) request.
 *
 * <p>This class is used in the SPI layer to represent the status information when querying
 * the current state of a bulk party verification request. It provides a lightweight response
 * containing only status-relevant data without the full verification results.</p>
 *
 * <p>This response is typically used for:</p>
 * <ul>
 *   <li>Status polling to check if a bulk verification has completed</li>
 *   <li>Monitoring the progress of asynchronous bulk verification processing</li>
 *   <li>Determining when to retrieve full verification results</li>
 * </ul>
 *
 * <p>Key differences from {@link SpiGetBulkVopResponse}:</p>
 * <ul>
 *   <li>Does not include individual party verification request/response pairs</li>
 *   <li>Focuses solely on status and metadata</li>
 *   <li>More lightweight for efficient status polling</li>
 * </ul>
 *
 * <p>This is the SPI domain representation corresponding to the XS2A domain class
 * {@code GetBulkVopStatusResponse}.</p>
 *
 * @see SpiVopBulkStatus
 * @see SpiGetBulkVopResponse
 */
@Data
public class SpiGetBulkVopStatusResponse {

    /**
     * Technical resource identification of the VOP bulk request.
     *
     * <p>This unique identifier (UUID) is used to track and retrieve the bulk VOP
     * verification request throughout its lifecycle. It remains constant from the
     * initial verification request through all status queries and result retrieval.</p>
     *
     * <p>Example: "99435c7e-ad88-49ec-a2ad-99ddcb1f5555"</p>
     *
     * <p>This field is mandatory and must not be null in valid responses.</p>
     */
    private UUID vopBulkId;

    /**
     * Current status of the bulk VOP verification process.
     *
     * <p>Indicates the current processing state of the bulk verification request:</p>
     * <ul>
     *   <li><b>RECEIVED</b> - Request has been received but processing has not started</li>
     *   <li><b>PENDING</b> - Verification is currently being processed</li>
     *   <li><b>COMPLETED</b> - All verifications have been successfully completed</li>
     *   <li><b>REJECTED</b> - The bulk request was rejected or failed</li>
     * </ul>
     *
     * <p>This status applies to the entire bulk request, not individual verification items.
     * Individual item statuses are only available when retrieving full results via
     * {@link SpiGetBulkVopResponse}.</p>
     *
     * <p>This field is mandatory and must not be null in valid responses.</p>
     */
    private SpiVopBulkStatus vopBulkStatus;

    /**
     * Reason code providing additional context for the current status.
     *
     * <p>This optional field provides a standardized reason code that explains
     * why the bulk verification is in its current status. Common use cases include:</p>
     * <ul>
     *   <li>Explaining why a request was rejected (e.g., AM04 - Insufficient funds)</li>
     *   <li>Providing details about processing delays (e.g., TKXP - Technical issue)</li>
     *   <li>Indicating specific validation failures (e.g., DS0A - Data validation error)</li>
     * </ul>
     *
     * <p>The reason codes follow industry standards as defined in the openFinance API
     * Framework Data Dictionary. See {@link SpiStatusReasonCode} for available codes
     * and their meanings.</p>
     *
     * <p>This field is optional and may be null if no specific reason code applies.</p>
     *
     * @see SpiStatusReasonCode
     */
    private SpiStatusReasonCode reasonCode;

    /**
     * Proprietary (ASPSP-specific) reason text providing additional status information.
     *
     * <p>This optional field allows ASPSPs to provide custom, human-readable text
     * explaining the current status in more detail than standardized reason codes allow.
     * This can be useful for:</p>
     * <ul>
     *   <li>Providing detailed error messages for debugging</li>
     *   <li>Explaining ASPSP-specific processing states</li>
     *   <li>Offering guidance on next steps or remediation</li>
     * </ul>
     *
     * <p>The content and format of this field are entirely at the discretion of
     * the ASPSP implementation. TPPs should not rely on parsing this field for
     * automated processing.</p>
     *
     * <p>This field is optional and may be null if no additional information is available.</p>
     */
    private String reasonProprietary;
}
