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

package de.adorsys.psd2.consent.repository;

import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentUsage;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;

public interface AisConsentUsageRepository extends Xs2aCrudRepository<AisConsentUsage, Long> {
    @Lock(value = LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    Optional<AisConsentUsage> findWriteByConsentAndUsageDate(AisConsent aisConsent, LocalDate usageDate);

    @Lock(value = LockModeType.OPTIMISTIC)
    Optional<AisConsentUsage> findReadByConsentAndUsageDate(AisConsent aisConsent, LocalDate usageDate);
}
