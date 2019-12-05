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

package de.adorsys.psd2.consent.repository.impl;

import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.repository.AisConsentJpaRepository;
import de.adorsys.psd2.consent.repository.AisConsentVerifyingRepository;
import de.adorsys.psd2.consent.service.sha.ChecksumCalculatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.*;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class AisConsentRepositoryImpl implements AisConsentVerifyingRepository {
    private final AisConsentJpaRepository aisConsentRepository;
    private final ChecksumCalculatingFactory calculatingFactory;

    @Override
    public AisConsent verifyAndSave(AisConsent entity) {
        return verifyAndSaveInternal(entity);
    }

    @Override
    public AisConsent verifyAndUpdate(AisConsent entity) {
        return verifyAndUpdateInternal(entity);
    }

    @Override
    public List<AisConsent> verifyAndSaveAll(List<AisConsent> entity) {
        return entity.stream()
                   .map(this::verifyAndSaveInternal)
                   .collect(Collectors.toList());
    }

    private AisConsent verifyAndSaveInternal(AisConsent entity) {
        Optional<ChecksumCalculatingService> calculatingServiceOptional = calculatingFactory.getServiceByChecksum(entity.getChecksum());

        if (calculatingServiceOptional.isPresent()) {
            ChecksumCalculatingService calculatingService = calculatingServiceOptional.get();

            if (!isAisConsentChecksumCorrect(entity, calculatingService)) {
                // TODO: https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1119
                // Discuss and simplify this.
                return entity;
            }

            if (wasStatusSwitchedToValid(entity)) {
                byte[] newChecksum = calculatingService.calculateChecksumForConsent(entity);
                entity.setChecksum(newChecksum);
            }
        }

        return aisConsentRepository.save(entity);
    }

    private AisConsent verifyAndUpdateInternal(AisConsent entity) {
        Optional<ChecksumCalculatingService> calculatingServiceOptional = calculatingFactory.getServiceByChecksum(entity.getChecksum());

        if (calculatingServiceOptional.isPresent()) {
            ChecksumCalculatingService calculatingService = calculatingServiceOptional.get();

            if (!isAisConsentChecksumCorrect(entity, calculatingService)) {
                // TODO: https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1119
                // Discuss and simplify this.
                return entity;
            }

            if (entity.getConsentStatus() == VALID) {
                byte[] newChecksum = calculatingService.calculateChecksumForConsent(entity);
                entity.setChecksum(newChecksum);
            }
        }

        return aisConsentRepository.save(entity);
    }

    private boolean isAisConsentChecksumCorrect(AisConsent entity, ChecksumCalculatingService calculatingService) {
        byte[] checksumFromDb = entity.getChecksum();

        if (checksumFromDb != null
                && wasStatusValidBefore(entity)
                && !calculatingService.verifyConsentWithChecksum(entity, checksumFromDb)) {
            log.warn("Checksum verification failed!");
            return false;
        }
        return true;
    }

    private boolean wasStatusSwitchedToValid(AisConsent entity) {
        return entity.getConsentStatus() == VALID
                   && EnumSet.of(RECEIVED, PARTIALLY_AUTHORISED).contains(entity.getPreviousConsentStatus());
    }

    private boolean wasStatusValidBefore(AisConsent entity) {
        return entity.getPreviousConsentStatus() == VALID;
    }
}
