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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.AspspDataService;
import de.adorsys.psd2.consent.domain.AspspConsentDataEntity;
import de.adorsys.psd2.consent.repository.AspspConsentDataRepository;
import de.adorsys.psd2.consent.service.security.EncryptedData;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// TODO temporary solution to switch off Hibernate dirty check. Need to understand why objects are changed here. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/364
public class AspspDataServiceInternal implements AspspDataService {
    private final SecurityDataService securityDataService;
    private final AspspConsentDataRepository aspspConsentDataRepository;

    @Override
    public @NotNull Optional<AspspConsentData> readAspspConsentData(@NotNull String externalId) {
        if (!securityDataService.isConsentIdEncrypted(externalId)) {
            return aspspConsentDataRepository.findByConsentId(externalId)
                       .map(aspspConsentDataEntity -> new AspspConsentData(aspspConsentDataEntity.getData(), externalId));
        }

        return getAspspConsentDataEntity(externalId)
                   .map(AspspConsentDataEntity::getData)
                   .flatMap(data -> securityDataService.decryptConsentData(externalId, data))
                   .map(dta -> new AspspConsentData(dta.getData(), externalId));
    }

    @Override
    @Transactional
    public boolean updateAspspConsentData(@NotNull AspspConsentData aspspConsentData) {
        byte[] data = aspspConsentData.getAspspConsentData();
        if (Objects.isNull(data)) {
            return deleteAspspConsentData(aspspConsentData.getConsentId());
        }

        String encryptedConsentId = aspspConsentData.getConsentId();
        if (!securityDataService.isConsentIdEncrypted(encryptedConsentId)) {
            return updateAndSaveAspspConsentData(encryptedConsentId, data);
        }

        Optional<String> decryptConsentId = securityDataService.decryptId(encryptedConsentId);
        if (!decryptConsentId.isPresent()) {
            return false;
        }

        Optional<EncryptedData> encryptedData = encryptConsentData(encryptedConsentId, Base64.getEncoder().encodeToString(data));
        if (!encryptedData.isPresent()) {
            return false;
        }

        return updateAndSaveAspspConsentData(decryptConsentId.get(), encryptedData.get().getData());
    }

    @Override
    @Transactional
    public boolean deleteAspspConsentData(@NotNull String externalId) {
        if (!securityDataService.isConsentIdEncrypted(externalId)) {
            return deleteAspspConsentDataIfExist(externalId);
        }

        return securityDataService.decryptId(externalId)
                   .map(this::deleteAspspConsentDataIfExist)
                   .orElse(false);
    }

    private boolean deleteAspspConsentDataIfExist(@NotNull String consentId) {
        if (aspspConsentDataRepository.exists(consentId)) {
            aspspConsentDataRepository.delete(consentId);
            return true;
        }
        return false;
    }

    private Optional<AspspConsentDataEntity> getAspspConsentDataEntity(String externalId) {
        return securityDataService.decryptId(externalId)
                   .flatMap(aspspConsentDataRepository::findByConsentId);
    }

    private Optional<EncryptedData> encryptConsentData(String externalId, String aspspConsentDataBase64) {
        return securityDataService.encryptConsentData(externalId, aspspConsentDataBase64);
    }

    private boolean updateAndSaveAspspConsentData(String consentId, byte[] encryptConsentData) {
        AspspConsentDataEntity aspspConsentDataEntity = aspspConsentDataRepository
                                                            .findByConsentId(consentId)
                                                            .orElseGet(() -> new AspspConsentDataEntity(consentId));
        aspspConsentDataEntity.setData(encryptConsentData);

        return aspspConsentDataRepository.save(aspspConsentDataEntity) != null;
    }
}
