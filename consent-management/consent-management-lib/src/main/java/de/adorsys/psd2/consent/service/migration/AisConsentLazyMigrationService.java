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
