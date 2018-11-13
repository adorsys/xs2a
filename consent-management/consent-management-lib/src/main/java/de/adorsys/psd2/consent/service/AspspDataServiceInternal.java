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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// TODO temporary solution to switch off Hibernate dirty check. Need to understand why objects are changed here. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/364
public class AspspDataServiceInternal implements AspspDataService {
    private final SecurityDataService securityDataService;
    private final AspspConsentDataRepository aspspConsentDataRepository;

    @Override
    public @NotNull Optional<AspspConsentData> readAspspConsentData(@NotNull String encryptedConsentId) {
        return getAspspConsentDataEntity(encryptedConsentId)
                   .map(AspspConsentDataEntity::getData)
                   .flatMap(data -> securityDataService.decryptConsentData(encryptedConsentId, data))
                   .map(decryptedData -> new AspspConsentData(decryptedData.getData(), encryptedConsentId));
    }

    @Override
    @Transactional
    public boolean updateAspspConsentData(@NotNull AspspConsentData aspspConsentData) {
        Optional<String> aspspConsentDataBase64 = Optional.ofNullable(aspspConsentData.getAspspConsentData())
                                                      .map(Base64.getEncoder()::encodeToString);

        Optional<String> consentId = securityDataService.decryptId(aspspConsentData.getConsentId());

        if (!aspspConsentDataBase64.isPresent() || !consentId.isPresent()) {
            return false;
        }

        Optional<EncryptedData> encryptedData = encryptConsentData(aspspConsentData.getConsentId(), aspspConsentDataBase64.get());

        if (!encryptedData.isPresent()) {
            return false;
        }

        return updateAndSaveAspspConsentData(consentId.get(), encryptedData.get().getData());
    }

    private Optional<AspspConsentDataEntity> getAspspConsentDataEntity(String encryptedConsentId) {
        return securityDataService.decryptId(encryptedConsentId)
                   .flatMap(aspspConsentDataRepository::findByConsentId);
    }

    private Optional<EncryptedData> encryptConsentData(String encryptedConsentId, String aspspConsentDataBase64) {
        return securityDataService.encryptConsentData(encryptedConsentId, aspspConsentDataBase64);
    }

    private boolean updateAndSaveAspspConsentData(String consentId, byte[] encryptConsentData) {
        AspspConsentDataEntity aspspConsentDataEntity = aspspConsentDataRepository
                                                            .findByConsentId(consentId)
                                                            .orElseGet(() -> new AspspConsentDataEntity(consentId));
        aspspConsentDataEntity.setData(encryptConsentData);

        return aspspConsentDataRepository.save(aspspConsentDataEntity) != null;
    }
}
