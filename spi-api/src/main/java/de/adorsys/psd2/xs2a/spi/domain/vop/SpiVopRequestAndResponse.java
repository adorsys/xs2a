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
 * SPI POJO class representing a party verification request paired with its verification response.
 *
 * <p>This class is used in the SPI layer to represent both the original verification
 * request data and the resulting verification response data for a single party. It contains:</p>
 * <ul>
 *   <li>Request data: UETR, party account, party agent (optional), and party description</li>
 *   <li>Response data: Party name match code, party ID match code, matched name (optional),
 *       and additional information (optional)</li>
 * </ul>
 *
 * <p>This is the SPI domain representation corresponding to the XS2A domain class
 * {@code VopRequestAndResponse}. It combines the request and response
 * in a single object for convenient processing and mapping.</p>
 *
 * @see SpiPartyNameMatchCode
 * @see SpiPartyIdentificationMatchCode
 * @see SpiPartyDescription4
 * @see SpiAccountReference
 * @see SpiAgentDescription
 */
@Data
public class SpiVopRequestAndResponse {

    /**
     * Unique identifier for this party verification request.
     *
     * <p>This UUID (Universally Unique Transaction Reference) is used to track
     * and correlate the verification request with its response.</p>
     *
     * <p>Example: "99435c7e-ad88-49ec-a2ad-99ddcb1f5555"</p>
     */
    private UUID uetr;

    /**
     * Account reference of the party to be verified.
     *
     * <p>The data type might be restricted by the ASPSP to specific formats
     * (e.g., "iban" use only). Contains the account identification information
     * for the party being verified.</p>
     */
    private SpiAccountReference partyAccount;

    /**
     * Optional agent description for the party.
     *
     * <p>Contains information about the financial institution or agent
     * associated with the party being verified.</p>
     */
    private SpiAgentDescription partyAgent;

    /**
     * Description of the party to be verified.
     *
     * <p>This structure contains the party name and/or a party identifier.
     * Related API Access Schemes might define further requirements.</p>
     */
    private SpiPartyDescription4 party;

    /**
     * Match result for the party name.
     *
     * <p>Only provided if a party name has been provided in the request.
     * Indicates whether the party name matches, partially matches, or does not match
     * the records held by the ASPSP.</p>
     *
     * <p>Possible values: MTCH (match), NMAT (no match), PART (partial match)</p>
     */
    private SpiPartyNameMatchCode partyNameMatch;

    /**
     * Match result for the party identification.
     *
     * <p>Only provided if a party identification has been provided in the request.
     * Indicates whether the party identifier matches, partially matches, or does not match
     * the records held by the ASPSP.</p>
     *
     * <p>Possible values: MTCH (match), NMAT (no match), PART (partial match)</p>
     */
    private SpiPartyIdentificationMatchCode partyIdMatch;

    /**
     * Name of the party as matched by the ASPSP.
     *
     * <p>This attribute might be provided in case of a near match and contains
     * the name as provided by the addressed party agent. Maximum 140 characters.</p>
     *
     * <p>Example: "John Smith" or "ACME Corporation Ltd"</p>
     */
    private String matchedName;

    /**
     * Additional information about the verification.
     *
     * <p>Should be human-readable text providing additional context or details
     * about the verification result. Maximum 500 characters.</p>
     *
     * <p>Example: "Name matches but address differs" or "Account verified successfully"</p>
     */
    private String additionalInformation;
}
