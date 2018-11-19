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
import de.adorsys.psd2.consent.service.security.DecryptedData;
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
        boolean isConsentIdEncrypted = isConsentIdEncrypted(externalId);
        Optional<AspspConsentDataEntity> aspspConsentDataEntity = getAspspConsentDataEntity(externalId, isConsentIdEncrypted);
        if (!aspspConsentDataEntity.isPresent()) {
            return Optional.empty();
        }

        byte[] data = aspspConsentDataEntity.get()
                          .getData();

        if (isConsentIdEncrypted) {
            Optional<DecryptedData> decryptedData = securityDataService.decryptConsentData(externalId, data);
            if (!decryptedData.isPresent()) {
                return Optional.empty();
            }

            AspspConsentData aspspConsentData = new AspspConsentData(decryptedData.get()
                                                                         .getData(), externalId);
            return Optional.of(aspspConsentData);
        }

        AspspConsentData aspspConsentData = new AspspConsentData(data, externalId);
        return Optional.of(aspspConsentData);
    }

    @Override
    @Transactional
    public boolean updateAspspConsentData(@NotNull AspspConsentData aspspConsentData) {
        byte[] data = aspspConsentData.getAspspConsentData();
        if (Objects.isNull(data)) {
            return deleteAspspConsentData(aspspConsentData.getConsentId());
        }

        String encryptedConsentId = aspspConsentData.getConsentId();
        String consentId = encryptedConsentId;
        if (isConsentIdEncrypted(encryptedConsentId)) {
            Optional<String> decryptConsentId = securityDataService.decryptId(encryptedConsentId);

            if (!decryptConsentId.isPresent()) {
                return false;
            }

            Optional<EncryptedData> encryptedData = encryptConsentData(encryptedConsentId, Base64.getEncoder().encodeToString(data));

            if (!encryptedData.isPresent()) {
                return false;
            }

            data = encryptedData.get().getData();
            consentId = decryptConsentId.get();
        }

        return updateAndSaveAspspConsentData(consentId, data);
    }

    @Override
    @Transactional
    public boolean deleteAspspConsentData(@NotNull String externalId) {
        String consentId = externalId;
        if (isConsentIdEncrypted(externalId)) {
            Optional<String> decryptConsentId = securityDataService.decryptId(externalId);

            if (!decryptConsentId.isPresent()) {
                return false;
            }

            consentId = decryptConsentId.get();
        }

        if (aspspConsentDataRepository.exists(consentId)) {
            aspspConsentDataRepository.delete(consentId);
            return true;
        }

        return false;
    }

    private Optional<AspspConsentDataEntity> getAspspConsentDataEntity(String externalId, boolean isConsentIdEncrypted) {
        if (isConsentIdEncrypted) {
            return securityDataService.decryptId(externalId)
                       .flatMap(aspspConsentDataRepository::findByConsentId);
        }

        return aspspConsentDataRepository.findByConsentId(externalId);
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

    private boolean isConsentIdEncrypted(String consentId) {
        return consentId.contains("_=_");
    }
}
