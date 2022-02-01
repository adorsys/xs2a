/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
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
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.logger.context;

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class MdcLoggingContextService implements LoggingContextService {
    private static final String TRANSACTION_STATUS_KEY = "transactionStatus";
    private static final String CONSENT_STATUS_KEY = "consentStatus";
    private static final String SCA_STATUS_KEY = "scaStatus";
    private static final String INTERNAL_REQUEST_ID_KEY = "internal-request-id";
    private static final String X_REQUEST_ID_KEY = "x-request-id";
    private static final String INSTANCE_ID_KEY = "instance-id";

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
    public void clearContext() {
        MDC.clear();
    }
}
