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

package de.adorsys.psd2.consent.service.migration;

import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.migration.ObsoletePiisConsentJpaRepository;
import de.adorsys.psd2.core.data.piis.PiisConsentData;
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
