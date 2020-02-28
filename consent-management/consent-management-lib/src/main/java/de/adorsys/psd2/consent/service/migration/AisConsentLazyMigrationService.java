/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.service.migration;

import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.migration.ObsoleteAisConsentJpaRepository;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AisConsentLazyMigrationService {

    private final ObsoleteAisConsentJpaRepository obsoleteAisConsentJpaRepository;
    private final ConsentJpaRepository consentJpaRepository;
    private final ConsentDataMapper consentDataMapper;


    public ConsentEntity migrateIfNeeded(ConsentEntity consentEntity) {
        if (consentEntity.getData() == null) {
            Optional<AisConsent> obsoleteAisConsentOptional = obsoleteAisConsentJpaRepository.findByExternalId(consentEntity.getExternalId());
            if (obsoleteAisConsentOptional.isPresent()) {
                byte[] consentData = getConsentData(obsoleteAisConsentOptional.get());
                consentEntity.setData(consentData);
                consentJpaRepository.save(consentEntity);
            }
        }
        return consentEntity;
    }

    private byte[] getConsentData(AisConsent aisConsent) {
        AisConsentData aisConsentData = new AisConsentData(aisConsent.getAvailableAccounts(), aisConsent.getAllPsd2(), aisConsent.getAvailableAccountsWithBalance(),
                                                           aisConsent.isCombinedServiceIndicator());


        return consentDataMapper.getBytesFromConsentData(aisConsentData);
    }
}
