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

import de.adorsys.psd2.consent.api.service.AccountService;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentTransaction;
import de.adorsys.psd2.consent.repository.AisConsentJpaRepository;
import de.adorsys.psd2.consent.repository.AisConsentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceInternal implements AccountService {

    private final AisConsentTransactionRepository aisConsentTransactionRepository;
    private final AisConsentJpaRepository aisConsentJpaRepository;

    @Override
    @Transactional
    public boolean saveNumberOfTransactions(String consentId, String resourceId, int numberOfTransactions) {

        Optional<AisConsent> optionalAisConsent = aisConsentJpaRepository.findByExternalId(consentId);

        if (optionalAisConsent.isPresent()) {

            AisConsentTransaction aisConsentTransaction = new AisConsentTransaction();
            aisConsentTransaction.setConsentId(optionalAisConsent.get());
            aisConsentTransaction.setResourceId(resourceId);
            aisConsentTransaction.setNumberOfTransactions(numberOfTransactions);

            aisConsentTransactionRepository.save(aisConsentTransaction);
            return true;

        } else {
            log.info("Consent ID: [{}]. Save number of transactions failed, because consent not found", consentId);
            return false;
        }

    }
}
