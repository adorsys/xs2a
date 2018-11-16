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
import de.adorsys.psd2.consent.api.CmsAspspConsentDataBase64;
import de.adorsys.psd2.consent.api.ConsentType;
import de.adorsys.psd2.consent.api.service.CommonConsentService;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// TODO temporary solution to switch off Hibernate dirty check. Need to understand why objects are changed here. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/364
public class CommonConsentServiceInternal implements CommonConsentService {
    private final AspspDataService aspspDataService;
    private final ConsentServiceFactory consentServiceFactory;

    @Override
    public Optional<CmsAspspConsentDataBase64> getAspspConsentDataByConsentId(String encryptedConsentId, ConsentType consentType) {
        return isConsentExist(encryptedConsentId, consentType)
                   ? Optional.of(getCmsAspspConsentDataBase64(encryptedConsentId))
                   : Optional.empty();
    }

    @Override
    public Optional<CmsAspspConsentDataBase64> getAspspConsentDataByPaymentId(String encryptedPaymentId) {
        return isConsentExist(encryptedPaymentId, ConsentType.PIS)
                   ? Optional.of(getCmsAspspConsentDataBase64(encryptedPaymentId))
                   : Optional.empty();
    }

    @Override
    @Transactional
    public Optional<String> saveAspspConsentData(String encryptedConsentId, CmsAspspConsentDataBase64 request, ConsentType consentType) {
        boolean consentExist = isConsentExist(encryptedConsentId, consentType);

        if (!consentExist) {
            return Optional.empty();
        }

        Optional<AspspConsentData> aspspConsentData = Optional.ofNullable(request.getAspspConsentDataBase64())
                                                          .map(Base64.getDecoder()::decode)
                                                          .map(dta -> new AspspConsentData(dta, encryptedConsentId));

        if (!aspspConsentData.isPresent()) {
            return Optional.empty();
        }

        return aspspDataService.updateAspspConsentData(aspspConsentData.get())
                   ? Optional.of(encryptedConsentId)
                   : Optional.empty();
    }

    @Override
    @Transactional
    public boolean deleteAspspConsentDataByConsentId(String encryptedConsentId, ConsentType consentType) {
        if (!isConsentExist(encryptedConsentId, consentType)) {
            return false;
        }
        return aspspDataService.deleteAspspConsentData(encryptedConsentId);
    }

    private boolean isConsentExist(String encryptedConsentId, ConsentType consentType) {
        return consentServiceFactory.getConsentServiceByConsentType(consentType).isConsentExist(encryptedConsentId);
    }

    private CmsAspspConsentDataBase64 getCmsAspspConsentDataBase64(String encryptedConsentId) {
        Optional<String> aspspConsentDataBase64 = aspspDataService.readAspspConsentData(encryptedConsentId)
                                                      .map(AspspConsentData::getAspspConsentData)
                                                      .map(Base64.getEncoder()::encodeToString);

        return new CmsAspspConsentDataBase64(encryptedConsentId, aspspConsentDataBase64.orElse(null));
    }
}
