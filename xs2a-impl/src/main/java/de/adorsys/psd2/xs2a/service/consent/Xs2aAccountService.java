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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.ais.UpdateTransactionParametersRequest;
import de.adorsys.psd2.consent.api.service.AccountServiceEncrypted;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class Xs2aAccountService {

    private final AccountServiceEncrypted accountServiceEncrypted;

    public void saveTransactionParameters(String consentId, String resourceId, Xs2aTransactionParameters transactionParameters) {
        accountServiceEncrypted.saveTransactionParameters(consentId, resourceId,
                                                          new UpdateTransactionParametersRequest(transactionParameters.getNumberOfTransactions(),
                                                                                                 transactionParameters.getTotalPages(),
                                                                                                 transactionParameters.getBookingStatus()));
    }
}
