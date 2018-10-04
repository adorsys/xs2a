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

package de.adorsys.aspsp.xs2a.repository;

import de.adorsys.aspsp.xs2a.domain.AisConsent;
import de.adorsys.psd2.consent.api.CmsConsentStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AisConsentRepository extends CrudRepository<AisConsent, Long> {
    List<AisConsent> findByConsentStatusIn(Set<CmsConsentStatus> statuses);

    Optional<AisConsent> findByExternalId(String externalId);

    Optional<AisConsent> findByExternalIdAndConsentStatusIn(String externalId, Set<CmsConsentStatus> statuses);
}
