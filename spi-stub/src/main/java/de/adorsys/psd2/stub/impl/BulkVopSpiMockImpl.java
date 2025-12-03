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

package de.adorsys.psd2.stub.impl;

import de.adorsys.psd2.xs2a.spi.domain.SpiVopContextData;
import de.adorsys.psd2.xs2a.spi.domain.error.SpiTppMessage;
import de.adorsys.psd2.xs2a.spi.domain.error.SpiMessageErrorCode;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.vop.*;
import de.adorsys.psd2.xs2a.spi.domain.vop.response.SpiBulkVopInitiationResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkVopSpi;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class BulkVopSpiMockImpl implements BulkVopSpi {
    private static final String MATCHED_NAME = "Mocked matched name from ASPSP";
    private static final String ADDITIONAL_INFO = "Mocked verification additional information";

    @Override
    public @NotNull SpiResponse<SpiBulkVopInitiationResponse> performVerification(@NotNull SpiVopContextData vopContextData, @NotNull SpiBulkVop bulkVop) {
        log.info("BulkVopSpi#performVerification: vopContextData {}, spiBulkVop {}", vopContextData, bulkVop);

        SpiBulkVopInitiationResponse response = new SpiBulkVopInitiationResponse();
        response.setVopBulkStatus(SpiVopBulkStatus.COMPLETED);

        // Create mock party verification responses for each request
        List<SpiPartyVerificationResponse> partyVerificationResponses = new ArrayList<>();

        if (bulkVop.getPartyVerificationRequests() != null) {
            bulkVop.getPartyVerificationRequests().forEach(request -> {
                SpiPartyVerificationResponse verificationResponse = new SpiPartyVerificationResponse();
                verificationResponse.setUetr(UUID.randomUUID());
                verificationResponse.setPartyNameMatch(SpiPartyNameMatchCode.MTCH);
                verificationResponse.setPartyIdMatch(SpiPartyIdentificationMatchCode.MTCH);
                verificationResponse.setMatchedName(MATCHED_NAME);
                verificationResponse.setAdditionalInformation(ADDITIONAL_INFO);
                partyVerificationResponses.add(verificationResponse);
            });
        }

        response.setPartyVerificationResponses(partyVerificationResponses);

        return SpiResponse.<SpiBulkVopInitiationResponse>builder()
                   .payload(response)
                   .build();
    }

    /**
     * Retrieves a bulk VOP verification result by its unique identifier.
     *
     * <p>This is a mock implementation that returns simulated bulk VOP data for testing purposes.
     * In a production environment, this method would fetch the actual bulk VOP data from a
     * database or persistent storage using the provided VOP ID.</p>
     *
     * @param spiContextData the SPI context data containing request information and security details
     * @param vopId          the unique identifier of the bulk VOP verification request to retrieve
     * @return SPI response containing the bulk VOP data or error information if not found
     */
    @Override
    public @NotNull SpiResponse<SpiGetBulkVopResponse> getBulkVopById(@NotNull SpiVopContextData spiContextData, @NotNull String vopId) {
        log.info("BulkVopSpi#getVopById: spiContextData {}, vopId {}", spiContextData, vopId);

        // Create mock SpiGetBulkVopResponse
        SpiGetBulkVopResponse response = new SpiGetBulkVopResponse();
        response.setVopBulkId(UUID.fromString(vopId));
        response.setVopBulkStatus(SpiVopBulkStatus.COMPLETED);

        // Create mock party verification request/response pairs
        List<SpiVopRequestAndResponse> requestResponseList = new ArrayList<>();

        // Add a few mock verification request/response pairs
        for (int i = 0; i < 3; i++) {
            SpiVopRequestAndResponse requestAndResponse = new SpiVopRequestAndResponse();

            // Set request data
            requestAndResponse.setUetr(UUID.randomUUID());

            SpiAccountReference partyAccount = new SpiAccountReference();
            partyAccount.setIban("DE89370400440532013000");
            requestAndResponse.setPartyAccount(partyAccount);

            SpiAgentDescription partyAgent = new SpiAgentDescription();
            SpiFinancialInstitutionIdentification financialInstitution = new SpiFinancialInstitutionIdentification();
            financialInstitution.setBicfi("DEUTDEFF");
            partyAgent.setFinancialInstitutionId(financialInstitution);
            requestAndResponse.setPartyAgent(partyAgent);

            SpiPartyDescription4 party = new SpiPartyDescription4();
            party.setName("Mock Party Name " + (i + 1));
            requestAndResponse.setParty(party);

            // Set response data
            requestAndResponse.setPartyNameMatch(SpiPartyNameMatchCode.MTCH);
            requestAndResponse.setPartyIdMatch(SpiPartyIdentificationMatchCode.MTCH);
            requestAndResponse.setMatchedName(MATCHED_NAME + " " + (i + 1));
            requestAndResponse.setAdditionalInformation(ADDITIONAL_INFO + " for request " + (i + 1));

            requestResponseList.add(requestAndResponse);
        }

        response.setPartyVerificationRequestResponses(requestResponseList);

        log.info("BulkVopSpi#getVopById: returning mock bulk VOP response with {} verification pairs",
                 requestResponseList.size());

        return SpiResponse.<SpiGetBulkVopResponse>builder()
                   .payload(response)
                   .build();
    }

    /**
     * Retrieves only the status information of a bulk VOP verification by its unique identifier.
     *
     * <p>This is a mock implementation that returns simulated status data for testing purposes.
     * In a production environment, this method would fetch only the status fields from a
     * database or persistent storage, optimized for efficient status polling without loading
     * full verification results.</p>
     *
     * <p>This mock implementation simulates a lightweight status query that returns only:</p>
     * <ul>
     *   <li>VOP bulk ID - The unique identifier</li>
     *   <li>VOP bulk status - The current processing state (COMPLETED in this mock)</li>
     *   <li>Reason code - Optional status reason (null in this mock)</li>
     *   <li>Reason proprietary - Optional ASPSP-specific reason text (null in this mock)</li>
     * </ul>
     *
     * @param spiContextData the SPI context data containing request information and security details
     * @param vopId          the unique identifier of the bulk VOP verification request
     * @return SPI response containing only the bulk VOP status information or error if not found
     */
    @Override
    public @NotNull SpiResponse<SpiGetBulkVopStatusResponse> getBulkVopStatusById(@NotNull SpiVopContextData spiContextData, @NotNull String vopId) {
        log.info("BulkVopSpi#getBulkVopStatusById: spiContextData {}, vopId {}", spiContextData, vopId);

        UUID parsedVopId;
        try {
            parsedVopId = UUID.fromString(vopId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid VOP ID format: {}", vopId, e);
            return SpiResponse.<SpiGetBulkVopStatusResponse>builder()
                       .error(new SpiTppMessage(
                           SpiMessageErrorCode.FORMAT_ERROR,
                           "Invalid VOP ID format. Expected UUID format."
                       ))
                       .build();
        }

        // Create mock SpiGetBulkVopStatusResponse with only status fields
        // In a real implementation, this would be a database query fetching only status columns
        SpiGetBulkVopStatusResponse statusResponse = new SpiGetBulkVopStatusResponse();
        statusResponse.setVopBulkId(parsedVopId);
        statusResponse.setVopBulkStatus(SpiVopBulkStatus.COMPLETED);

        // Mock: In a real implementation, reasonCode would be populated based on actual verification results
        // Example scenarios where reasonCode would be set:
        // - BEXX: Backend error during verification processing
        // - DS0_A: Data validation error in party information
        // - AM21: Invalid party identification details
        statusResponse.setReasonCode(SpiStatusReasonCode.AC05);

        // Mock: reasonProprietary would contain ASPSP-specific details in a real implementation
        // Example: "All 15 party verifications completed successfully at 2025-11-06T10:30:00Z"
        statusResponse.setReasonProprietary("Mock reason proprietary text");

        log.info("BulkVopSpi#getBulkVopStatusById: returning mock status - ID: {}, Status: {}",
                 statusResponse.getVopBulkId(), statusResponse.getVopBulkStatus());

        return SpiResponse.<SpiGetBulkVopStatusResponse>builder()
                   .payload(statusResponse)
                   .build();
    }
}
