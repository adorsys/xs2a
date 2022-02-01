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
