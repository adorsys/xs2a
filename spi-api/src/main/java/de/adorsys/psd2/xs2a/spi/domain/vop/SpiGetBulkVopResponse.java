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

import java.util.List;
import java.util.UUID;

/**
 * SPI POJO class representing the response for retrieving a bulk VOP (Verification of Payee) verification result.
 *
 * <p>This class is used in the SPI layer to represent the response data when retrieving
 * the status and results of a bulk party verification request. It contains:</p>
 * <ul>
 *   <li>The unique identifier of the bulk VOP request</li>
 *   <li>The current status of the bulk verification process</li>
 *   <li>A list of party verification requests paired with their verification results</li>
 * </ul>
 *
 * <p>This is the SPI domain representation corresponding to the XS2A domain class
 * {@code GetBulkVopResponse}.</p>
 *
 * @see SpiVopBulkStatus
 * @see SpiVopRequestAndResponse
 */
@Data
public class SpiGetBulkVopResponse {

    /**
     * Technical resource identification of the VOP bulk request.
     *
     * <p>This unique identifier (UUID) is used to track and retrieve the bulk VOP
     * verification request throughout its lifecycle.</p>
     *
     * <p>Example: "99435c7e-ad88-49ec-a2ad-99ddcb1f5555"</p>
     */
    private UUID vopBulkId;

    /**
     * Current status of the bulk VOP verification process.
     *
     * <p>Indicates whether the bulk verification is pending, completed, or failed.
     * This status applies to the entire bulk request, not individual verification items.</p>
     */
    private SpiVopBulkStatus vopBulkStatus;

    /**
     * Non-empty array of party verification request and response pairs.
     *
     * <p>Each element in this list contains the original verification request paired with
     * its corresponding verification result. The list must contain at least one element.</p>
     *
     * <p>Each verification request/response pair includes:</p>
     * <ul>
     *   <li>Request data: UETR, party account, party agent, and party description</li>
     *   <li>Response data: Party name match results, party ID match results, matched name, and additional information</li>
     * </ul>
     */
    private List<SpiVopRequestAndResponse> partyVerificationRequestResponses;

}
