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

package de.adorsys.psd2.consent.service.psu;

import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.migration.AisConsentLazyMigrationService;
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
    private final AuthorisationRepository authorisationRepository;
    private final AisConsentLazyMigrationService aisConsentLazyMigrationService;

    boolean updatePsuData(AuthorisationEntity authorisation, PsuIdData psuIdData, ConsentType consentType) {
        PsuData newPsuData = psuDataMapper.mapToPsuData(psuIdData, authorisation.getInstanceId());

        if (newPsuData == null || StringUtils.isBlank(newPsuData.getPsuId())) {
            log.info("Authorisation ID : [{}]. Update PSU data in consent failed in updatePsuData method, because newPsuData or psuId in newPsuData is empty or null.",
                     authorisation.getExternalId());
            return false;
        }

        Optional<PsuData> optionalPsuData = Optional.ofNullable(authorisation.getPsuData());
        boolean isPsuDataPresentInAuthorisation = optionalPsuData.isPresent();
        if (isPsuDataPresentInAuthorisation) {
            PsuData psuDataInAuthorisation = optionalPsuData.get();
            updatePsuFromAuthorisation(newPsuData, psuDataInAuthorisation);
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
        if (isPsuDataPresentInAuthorisation) { // TODO remove this if block with proper solution of issue https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/-/issues/1309
            authorisationRepository.save(authorisation);
        }
        return true;
    }

    private void updatePsuFromAuthorisation(PsuData newPsuData, PsuData psuDataInAuthorisation) {
        newPsuData.setId(psuDataInAuthorisation.getId());
        if (psuDataInAuthorisation.getAdditionalPsuData() != null && newPsuData.getAdditionalPsuData() != null) {
            newPsuData.getAdditionalPsuData().setId(psuDataInAuthorisation.getAdditionalPsuData().getId());
        }
    }
}
