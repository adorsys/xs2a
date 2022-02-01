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
import de.adorsys.psd2.consent.domain.account.AisConsentTransaction;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.AisConsentTransactionRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
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
    private final ConsentJpaRepository consentJpaRepository;

    @Override
    @Transactional
    public boolean saveTransactionParameters(String consentId, String resourceId, UpdateTransactionParametersRequest transactionParameters) {

        Optional<ConsentEntity> optionalConsent = consentJpaRepository.findByExternalId(consentId);

        if (optionalConsent.isPresent()) {

            AisConsentTransaction aisConsentTransaction = new AisConsentTransaction();
            aisConsentTransaction.setConsentId(optionalConsent.get());
            aisConsentTransaction.setResourceId(resourceId);
            aisConsentTransaction.setNumberOfTransactions(transactionParameters.getNumberOfTransactions());
            aisConsentTransaction.setTotalPages(transactionParameters.getTotalPages());
            aisConsentTransaction.setBookingStatus(transactionParameters.getBookingStatus());

            aisConsentTransactionRepository.save(aisConsentTransaction);
            return true;

        } else {
            log.info("Consent ID: [{}]. Save number of transactions failed, because consent not found", consentId);
            return false;
        }

    }
}
