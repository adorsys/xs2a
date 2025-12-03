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

package de.adorsys.psd2.logger.context;

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.vop.PartyIdentificationMatchCode;
import de.adorsys.psd2.xs2a.core.vop.PartyNameMatchCode;
import de.adorsys.psd2.xs2a.core.vop.PartyVerificationResponse;
import de.adorsys.psd2.xs2a.core.vop.VopBulkStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MdcLoggingContextService implements LoggingContextService {
    private static final String TRANSACTION_STATUS_KEY = "transactionStatus";
    private static final String CONSENT_STATUS_KEY = "consentStatus";
    private static final String SCA_STATUS_KEY = "scaStatus";
    private static final String INTERNAL_REQUEST_ID_KEY = "internal-request-id";
    private static final String X_REQUEST_ID_KEY = "x-request-id";
    private static final String INSTANCE_ID_KEY = "instance-id";
    private static final String VOP_BULK_STATUS_KEY = "vopBulkStatus";
    private static final String VOP_BULK_ID_KEY = "vopBulkId";
    private static final String VOP_PARTY_ID_MATCH_KEY = "vopPartyIdMatch";
    private static final String VOP_PARTY_NAME_MATCH_KEY = "vopPartyNameMatch";

    @Override
    public void storeConsentStatus(@NotNull ConsentStatus consentStatus) {
        MDC.put(CONSENT_STATUS_KEY, consentStatus.getValue());
    }

    @Override
    public String getConsentStatus() {
        return MDC.get(CONSENT_STATUS_KEY);
    }

    @Override
    public void storeTransactionStatus(@NotNull TransactionStatus transactionStatus) {
        MDC.put(TRANSACTION_STATUS_KEY, transactionStatus.getTransactionStatus());
    }

    @Override
    public String getTransactionStatus() {
        return MDC.get(TRANSACTION_STATUS_KEY);
    }

    @Override
    public void storeScaStatus(@NotNull ScaStatus scaStatus) {
        MDC.put(SCA_STATUS_KEY, scaStatus.getValue());
    }

    @Override
    public void storeTransactionAndScaStatus(@NotNull TransactionStatus transactionStatus, @Nullable ScaStatus scaStatus) {
        storeTransactionStatus(transactionStatus);
        if (scaStatus != null) {
            storeScaStatus(scaStatus);
        }
    }

    @Override
    public String getScaStatus() {
        return MDC.get(SCA_STATUS_KEY);
    }

    @Override
    public void storeRequestInformation(RequestInfo requestInfo) {
        if (requestInfo == null) {
            return;
        }

        MDC.put(INTERNAL_REQUEST_ID_KEY, requestInfo.getInternalRequestId());
        MDC.put(X_REQUEST_ID_KEY, requestInfo.getXRequestId());
        MDC.put(INSTANCE_ID_KEY, requestInfo.getInstanceId());
    }

    @Override
    public RequestInfo getRequestInformation() {
        String internalRequestId = MDC.get(INTERNAL_REQUEST_ID_KEY);
        String xRequestId = MDC.get(X_REQUEST_ID_KEY);
        String instanceId = MDC.get(INSTANCE_ID_KEY);

        return new RequestInfo(internalRequestId, xRequestId, instanceId);
    }

    @Override
    public void storeVopBulkStatus(@NotNull String vopBulkStatus) {
        MDC.put(VOP_BULK_STATUS_KEY, vopBulkStatus);
    }

    @Override
    public String getVopBulkStatus() {
        return MDC.get(VOP_BULK_STATUS_KEY);
    }

    @Override
    public void storeVopBulkId(@NotNull String vopBulkId) {
        MDC.put(VOP_BULK_ID_KEY, vopBulkId);
    }

    @Override
    public String getVopBulkId() {
        return MDC.get(VOP_BULK_ID_KEY);
    }

    @Override
    public void storeVerificationStatus(PartyIdentificationMatchCode partyIdMatch, PartyNameMatchCode partyNameMatch) {
        if (partyIdMatch != null) {
            MDC.put(VOP_PARTY_ID_MATCH_KEY, partyIdMatch.toString());
        }
        if (partyNameMatch != null) {
            MDC.put(VOP_PARTY_NAME_MATCH_KEY, partyNameMatch.toString());
        }
    }

    @Override
    public void storeBulkVerificationStatus(@NotNull VopBulkStatus vopBulkStatus, 
                                             @Nullable List<PartyVerificationResponse> partyVerificationResponses) {
        // Store the bulk status
        storeVopBulkStatus(vopBulkStatus.getValue());
        
        // Store summary information about the verification responses
        if (partyVerificationResponses != null && !partyVerificationResponses.isEmpty()) {
            // Count the different match results
            long successfulMatches = partyVerificationResponses.stream()
                .filter(response -> response.getPartyIdMatch() == PartyIdentificationMatchCode.MTCH 
                             && response.getPartyNameMatch() == PartyNameMatchCode.MTCH)
                .count();
            
            long partialMatches = partyVerificationResponses.stream()
                .filter(response -> (response.getPartyIdMatch() == PartyIdentificationMatchCode.MTCH 
                             || response.getPartyNameMatch() == PartyNameMatchCode.MTCH)
                             && !(response.getPartyIdMatch() == PartyIdentificationMatchCode.MTCH 
                             && response.getPartyNameMatch() == PartyNameMatchCode.MTCH))
                .count();
            
            long noMatches = partyVerificationResponses.size() - successfulMatches - partialMatches;
            
            // Store the statistics in MDC
            MDC.put("vopBulkTotalCount", String.valueOf(partyVerificationResponses.size()));
            MDC.put("vopBulkSuccessfulMatches", String.valueOf(successfulMatches));
            MDC.put("vopBulkPartialMatches", String.valueOf(partialMatches));
            MDC.put("vopBulkNoMatches", String.valueOf(noMatches));
        }
    }

    @Override
    public void storeGetBulkVopIdAndStatus(@NotNull java.util.UUID vopBulkId, @NotNull VopBulkStatus vopBulkStatus) {
        // Store the bulk ID as a string
        storeVopBulkId(vopBulkId.toString());
        
        // Store the bulk status
        storeVopBulkStatus(vopBulkStatus.getValue());
    }

    @Override
    public void storeGetBulkVopStatusIdAndStatus(@NotNull java.util.UUID vopBulkId, @NotNull VopBulkStatus vopBulkStatus) {
        // Store the bulk ID as a string for status retrieval operations
        storeVopBulkId(vopBulkId.toString());
        
        // Store the bulk status
        storeVopBulkStatus(vopBulkStatus.getValue());
        
        // Add a marker to distinguish status retrieval from result retrieval
        MDC.put("vopOperationType", "STATUS_CHECK");
    }

    @Override
    public void clearContext() {
        MDC.clear();
    }
}
