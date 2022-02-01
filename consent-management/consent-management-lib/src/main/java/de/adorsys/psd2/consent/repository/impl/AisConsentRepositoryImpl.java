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

package de.adorsys.psd2.consent.repository.impl;

import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.AisConsentVerifyingRepository;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.sha.ChecksumCalculatingFactory;
import de.adorsys.psd2.consent.service.sha.ChecksumCalculatingService;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AisConsentRepositoryImpl implements AisConsentVerifyingRepository {
    private final ConsentJpaRepository aisConsentRepository;
    private final ChecksumCalculatingFactory calculatingFactory;
    private final AisConsentMapper aisConsentMapper;
    private final AuthorisationRepository authorisationRepository;

    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public ConsentEntity verifyAndSave(ConsentEntity entity) throws WrongChecksumException {
        return verifyAndSaveInternal(entity);
    }

    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public ConsentEntity verifyAndUpdate(ConsentEntity entity) throws WrongChecksumException {
        return verifyAndUpdateInternal(entity);
    }

    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public List<ConsentEntity> verifyAndSaveAll(List<ConsentEntity> consents) throws WrongChecksumException {
        List<ConsentEntity> consentList = new ArrayList<>();

        for (ConsentEntity entity : consents) {
            ConsentEntity consent = verifyAndSaveInternal(entity);
            consentList.add(consent);
        }
        return consentList;
    }

    @Override
    @Transactional
    public Optional<ConsentEntity> getActualAisConsent(String consentId) {
        return aisConsentRepository.findByExternalId(consentId)
                   .filter(c -> !c.getConsentStatus().isFinalisedStatus());
    }

    private ConsentEntity verifyAndSaveInternal(ConsentEntity consentEntity) throws WrongChecksumException {
        Optional<ChecksumCalculatingService> calculatingServiceOptional = calculatingFactory.getServiceByChecksum(consentEntity.getChecksum(), ConsentType.AIS);

        if (calculatingServiceOptional.isPresent()) {
            ChecksumCalculatingService calculatingService = calculatingServiceOptional.get();

            if (!isAisConsentChecksumCorrect(consentEntity, calculatingService)) {
                throw new WrongChecksumException();
            }

            if (wasStatusSwitchedToValid(consentEntity)) {
                byte[] newChecksum = calculatingService.calculateChecksumForConsent(mapToAisConsent(consentEntity));
                consentEntity.setChecksum(newChecksum);
            }
        }

        return aisConsentRepository.save(consentEntity);
    }

    private ConsentEntity verifyAndUpdateInternal(ConsentEntity entity) throws WrongChecksumException {
        Optional<ChecksumCalculatingService> calculatingServiceOptional = calculatingFactory.getServiceByChecksum(entity.getChecksum(), ConsentType.AIS);

        if (calculatingServiceOptional.isPresent()) {
            ChecksumCalculatingService calculatingService = calculatingServiceOptional.get();

            if (!isAisConsentChecksumCorrect(entity, calculatingService)) {
                throw new WrongChecksumException();
            }

            if (entity.getConsentStatus() == VALID) {
                byte[] newChecksum = calculatingService.calculateChecksumForConsent(mapToAisConsent(entity));
                entity.setChecksum(newChecksum);
            }
        }

        return aisConsentRepository.save(entity);
    }

    private boolean isAisConsentChecksumCorrect(ConsentEntity entity, ChecksumCalculatingService calculatingService) {
        byte[] checksumFromDb = entity.getChecksum();

        if (checksumFromDb != null
                && wasStatusHoldBefore(entity)
                && !calculatingService.verifyConsentWithChecksum(mapToAisConsent(entity), checksumFromDb)) {
            log.warn("AIS consent checksum verification failed! AIS consent ID: [{}]. Contact ASPSP for details.", entity.getExternalId());
            return false;
        }
        return true;
    }

    private boolean wasStatusSwitchedToValid(ConsentEntity entity) {
        return entity.getConsentStatus() == VALID
                   && EnumSet.of(RECEIVED, PARTIALLY_AUTHORISED).contains(getPreviousConsentStatus(entity));
    }

    private boolean wasStatusHoldBefore(ConsentEntity entity) {
        return getPreviousConsentStatus(entity) == VALID
                   || isFinalisedStatus(entity);
    }

    private AisConsent mapToAisConsent(ConsentEntity entity) {
        List<AuthorisationEntity> authorisationEntityList =
            authorisationRepository.findAllByParentExternalIdAndType(entity.getExternalId(), AuthorisationType.CONSENT);

        return aisConsentMapper.mapToAisConsent(entity, authorisationEntityList);
    }

    private ConsentStatus getPreviousConsentStatus(ConsentEntity entity) {
        Optional<ConsentEntity> optionalConsentStatus = aisConsentRepository.findByExternalId(entity.getExternalId());

        return optionalConsentStatus
                   .map(ConsentEntity::getConsentStatus)
                   .orElse(null);

    }

    private boolean isFinalisedStatus(ConsentEntity entity) {
        ConsentStatus previousConsentStatus = getPreviousConsentStatus(entity);
        return previousConsentStatus != null && previousConsentStatus.isFinalisedStatus();
    }
}
