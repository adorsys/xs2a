/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.service.context;

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class MdcLoggingContextService implements LoggingContextService {
    private static final String TRANSACTION_STATUS_KEY = "transactionStatus";
    private static final String SCA_STATUS_KEY = "scaStatus";

    @Override
    public void storeConsentStatus(@NotNull ConsentStatus consentStatus) {
        throw new NotImplementedException("Method hasn't been implemented yet");
    }

    @Override
    public String getConsentStatus() {
        throw new NotImplementedException("Method hasn't been implemented yet");
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
    public void clearContext() {
        MDC.clear();
    }
}
