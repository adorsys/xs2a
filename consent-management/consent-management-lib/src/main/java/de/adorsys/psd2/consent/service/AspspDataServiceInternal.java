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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.AspspDataService;
import de.adorsys.psd2.consent.domain.AspspConsentDataEntity;
import de.adorsys.psd2.consent.repository.AspspConsentDataRepository;
import de.adorsys.psd2.consent.service.security.EncryptedData;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
        if(aspspConsentData.isEmptyConsentData()){
            log.info("Update Aspsp consent data failed, because aspsp Consent data is empty.");
            return false;
        }

        byte[] data = aspspConsentData.getAspspConsentDataBytes();
        if (Objects.isNull(data)) {
            return deleteAspspConsentData(aspspConsentData.getConsentId());
        }

        String encryptedConsentId = aspspConsentData.getConsentId();
        if (!securityDataService.isConsentIdEncrypted(encryptedConsentId)) {
            return updateAndSaveAspspConsentData(encryptedConsentId, data);
        }

        Optional<String> decryptConsentId = securityDataService.decryptId(encryptedConsentId);
        if (decryptConsentId.isEmpty()) {
            log.info("Consent ID: [{}]. Update Aspsp consent data failed, because consent id cannot be decrypted.", encryptedConsentId);
            return false;
        }

        Optional<EncryptedData> encryptedData = securityDataService.encryptConsentData(encryptedConsentId, data);
        if (encryptedData.isEmpty()) {
            log.info("Consent ID: [{}]. Update Aspsp consent data failed, because aspsp consent data cannot be encrypted.", encryptedConsentId);
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
                   .orElseGet(() -> {
                       log.info("External Consent ID: [{}]. Delete aspsp consent data failed, couldn't decrypt consent id",
                                externalId);
                       return false;
                   });
    }

    private boolean deleteAspspConsentDataIfExist(@NotNull String consentId) {
        if (aspspConsentDataRepository.existsById(consentId)) {
            aspspConsentDataRepository.deleteById(consentId);
            return true;
        }
        log.info("Consent ID: [{}]. Delete Aspsp consent data failed, because aspsp consent data for this consent id does not exist.", consentId);
        return false;
    }

    private Optional<AspspConsentDataEntity> getAspspConsentDataEntity(String externalId) {
        return securityDataService.decryptId(externalId)
                   .flatMap(aspspConsentDataRepository::findByConsentId);
    }

    private boolean updateAndSaveAspspConsentData(String consentId, byte[] encryptConsentData) {
        AspspConsentDataEntity aspspConsentDataEntity = aspspConsentDataRepository
                                                            .findByConsentId(consentId)
                                                            .orElseGet(() -> new AspspConsentDataEntity(consentId));
        aspspConsentDataEntity.setData(encryptConsentData);

        return aspspConsentDataRepository.save(aspspConsentDataEntity) != null;
    }
}
