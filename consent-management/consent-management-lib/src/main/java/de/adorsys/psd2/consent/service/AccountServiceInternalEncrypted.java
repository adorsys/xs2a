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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.ais.UpdateTransactionParametersRequest;
import de.adorsys.psd2.consent.api.service.AccountService;
import de.adorsys.psd2.consent.api.service.AccountServiceEncrypted;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceInternalEncrypted implements AccountServiceEncrypted {

    private final SecurityDataService securityDataService;
    private final AccountService accountService;

    @Override
    public boolean saveTransactionParameters(String encryptedConsentId, String resourceId, UpdateTransactionParametersRequest transactionParameters) {

        Optional<String> optionalId = securityDataService.decryptId(encryptedConsentId);

        if (optionalId.isPresent()) {
            return accountService.saveTransactionParameters(optionalId.get(), resourceId, transactionParameters);
        } else {
            log.info("Encrypted Consent ID: [{}]. Save number of transactions failed, couldn't decrypt consent ID",
                     encryptedConsentId);
            return false;
        }

    }
}
