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

package de.adorsys.psd2.consent.service.psu;

import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.migration.AisConsentLazyMigrationService;
import de.adorsys.psd2.consent.service.psu.util.PsuDataUpdater;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
class CmsPsuConsentServiceInternal {
    private final PsuDataMapper psuDataMapper;
    private final ConsentJpaRepository consentJpaRepository;
    private final CmsPsuService cmsPsuService;
    private final AisConsentLazyMigrationService aisConsentLazyMigrationService;
    private final PsuDataUpdater psuDataUpdater;

    boolean updatePsuData(AuthorisationEntity authorisation, PsuIdData psuIdData, ConsentType consentType) {
        PsuData newPsuData = psuDataMapper.mapToPsuData(psuIdData, authorisation.getInstanceId());

        if (newPsuData == null || StringUtils.isBlank(newPsuData.getPsuId())) {
            log.info("Authorisation ID : [{}]. Update PSU data in consent failed in updatePsuData method, because newPsuData or psuId in newPsuData is empty or null.",
                     authorisation.getExternalId());
            return false;
        }

        Optional<PsuData> optionalPsuData = Optional.ofNullable(authorisation.getPsuData());
        if (optionalPsuData.isPresent()) {
            newPsuData = psuDataUpdater.updatePsuDataEntity(optionalPsuData.get(), newPsuData);
        } else {
            log.info("Authorisation ID [{}]. No PSU data available in the authorisation.", authorisation.getExternalId());

            Optional<ConsentEntity> consentOptional = consentJpaRepository.findByExternalId(authorisation.getParentExternalId());
            if (consentOptional.isEmpty()) {
                log.info("Authorisation ID [{}]. Update PSU data in consent failed, couldn't find consent by the parent ID in the authorisation.",
                         authorisation.getExternalId());
                return false;
            }

            ConsentEntity consentEntity = consentOptional.get();
            if (consentType == ConsentType.AIS) {
                consentEntity = aisConsentLazyMigrationService.migrateIfNeeded(consentEntity);
            }

            List<PsuData> psuDataList = consentEntity.getPsuDataList();
            Optional<PsuData> psuDataOptional = cmsPsuService.definePsuDataForAuthorisation(newPsuData, psuDataList);
            if (psuDataOptional.isPresent()) {
                newPsuData = psuDataOptional.get();
                consentEntity.setPsuDataList(cmsPsuService.enrichPsuData(newPsuData, psuDataList));
            }
        }
        authorisation.setPsuData(newPsuData);
        return true;
    }
}
