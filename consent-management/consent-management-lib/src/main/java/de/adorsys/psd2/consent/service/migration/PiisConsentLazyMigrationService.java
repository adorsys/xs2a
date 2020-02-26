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

import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.migration.ObsoletePiisConsentJpaRepository;
import de.adorsys.psd2.core.data.piis.v1.PiisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PiisConsentLazyMigrationService {
    private final ObsoletePiisConsentJpaRepository obsoletePiisConsentJpaRepository;
    private final ConsentJpaRepository consentJpaRepository;
    private final ConsentDataMapper consentDataMapper;

    public ConsentEntity migrateIfNeeded(ConsentEntity consentEntity) {
        if (consentEntity.getData() == null) {
            Optional<PiisConsentEntity> obsoletePiisConsentOptional = obsoletePiisConsentJpaRepository.findByExternalId(consentEntity.getExternalId());
            if (obsoletePiisConsentOptional.isPresent()) {
                byte[] consentData = getConsentData(obsoletePiisConsentOptional.get());
                consentEntity.setData(consentData);
                consentJpaRepository.save(consentEntity);
            }
        }
        return consentEntity;
    }

    public List<ConsentEntity> migrateIfNeeded(List<ConsentEntity> consentEntities) {
        consentEntities.forEach(this::migrateIfNeeded);
        return consentEntities;
    }

    private byte[] getConsentData(PiisConsentEntity piisConsent) {
        PiisConsentData piisConsentData = new PiisConsentData(piisConsent.getCardNumber(), piisConsent.getCardExpiryDate(),
                                                              piisConsent.getCardInformation(), piisConsent.getRegistrationInformation());


        return consentDataMapper.getBytesFromConsentData(piisConsentData);
    }
}
