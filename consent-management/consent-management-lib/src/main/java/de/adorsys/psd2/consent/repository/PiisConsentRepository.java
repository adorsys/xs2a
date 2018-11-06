/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.repository;

import de.adorsys.psd2.consent.domain.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PiisConsentRepository extends CrudRepository<PiisConsent, Long> {
    Optional<PiisConsent> findByExternalId(String externalId);

    Optional<PiisConsent> findByExternalIdAndConsentStatusIn(String externalId, Set<ConsentStatus> statuses);

    List<PiisConsent> findAllByAccounts_IbanAndAccounts_Currency(String iban, Currency currency);

    List<PiisConsent> findAllByAccounts_BbanAndAccounts_Currency(String bban, Currency currency);

    List<PiisConsent> findAllByAccounts_MsisdnAndAccounts_Currency(String msisdn, Currency currency);

    List<PiisConsent> findAllByAccounts_MaskedPanAndAccounts_Currency(String maskedPan, Currency currency);

    List<PiisConsent> findAllByAccounts_PanAndAccounts_Currency(String pan, Currency currency);
}
