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
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// TODO temporary solution to switch off Hibernate dirty check. Need to understand why objects are changed here. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/364
public class AspspDataServiceInternal implements AspspDataService {
    private final SecurityDataService securityDataService;
    private final AspspConsentDataRepository aspspConsentDataRepository;
    private final Pattern uuidPattern = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

    @Override
    public @NotNull Optional<AspspConsentData> readAspspConsentData(@NotNull String consentId) {
        boolean isConsentIdEncrypted = isConsentIdEncrypted(consentId);
        Optional<AspspConsentDataEntity> aspspConsentDataEntity = getAspspConsentDataEntity(consentId, isConsentIdEncrypted);
        if (!aspspConsentDataEntity.isPresent()) {
            return Optional.empty();
        }

        byte[] data = aspspConsentDataEntity.get().getData();

        if (isConsentIdEncrypted) {
            Optional<DecryptedData> decryptedData = securityDataService.decryptConsentData(consentId, data);
            if (!decryptedData.isPresent()) {
                return Optional.empty();
            }
            data = decryptedData.get().getData();
        }

        AspspConsentData aspspConsentData = new AspspConsentData(data, consentId);
        return Optional.of(aspspConsentData);
    }

    @Override
    @Transactional
    public boolean updateAspspConsentData(@NotNull AspspConsentData aspspConsentData) {
        byte[] data = aspspConsentData.getAspspConsentData();
        if (Objects.isNull(data)) {
            return false;
        }

        String consentId = aspspConsentData.getConsentId();
        boolean isConsentIdEncrypted = isConsentIdEncrypted(consentId);

        if (isConsentIdEncrypted) {
            Optional<String> decryptConsentId = securityDataService.decryptId(consentId);

            if (!decryptConsentId.isPresent()) {
                return false;
            }

            Optional<EncryptedData> encryptedData = encryptConsentData(consentId, Base64.getEncoder().encodeToString(data));

            if (!encryptedData.isPresent()) {
                return false;
            }

            data = encryptedData.get().getData();
            consentId = decryptConsentId.get();
        }

        return updateAndSaveAspspConsentData(consentId, data);
    }

    private Optional<AspspConsentDataEntity> getAspspConsentDataEntity(String consentId, boolean isConsentIdEncrypted) {
        return isConsentIdEncrypted
                   ? securityDataService.decryptId(consentId).flatMap(aspspConsentDataRepository::findByConsentId)
                   : aspspConsentDataRepository.findByConsentId(consentId);
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

    private boolean isConsentIdEncrypted(String s) {
        return !uuidPattern.matcher(s).matches();
    }
}
