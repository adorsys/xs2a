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

import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PiisConsentRepository extends CrudRepository<PiisConsentEntity, Long>, JpaSpecificationExecutor<PiisConsentEntity> {
    Optional<PiisConsentEntity> findByExternalId(String externalId);

    Optional<PiisConsentEntity> findByExternalIdAndConsentStatusIn(String externalId, Set<ConsentStatus> statuses);

    List<PiisConsentEntity> findAllByAccountsIbanAndAccountsCurrency(String iban, Currency currency);

    List<PiisConsentEntity> findAllByAccountsBbanAndAccountsCurrency(String bban, Currency currency);

    List<PiisConsentEntity> findAllByAccountsMsisdnAndAccountsCurrency(String msisdn, Currency currency);

    List<PiisConsentEntity> findAllByAccountsMaskedPanAndAccountsCurrency(String maskedPan, Currency currency);

    List<PiisConsentEntity> findAllByAccountsPanAndAccountsCurrency(String pan, Currency currency);

    List<PiisConsentEntity> findByPsuDataPsuId(String psuId);
}
