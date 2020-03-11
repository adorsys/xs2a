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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.core.data.AccountAccess;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AisConsentServiceInternalEncrypted implements AisConsentServiceEncrypted {
    private final SecurityDataService securityDataService;
    private final AisConsentService aisConsentService;

    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public CmsResponse<CmsResponse.VoidResponse> checkConsentAndSaveActionLog(AisConsentActionRequest encryptedRequest) throws WrongChecksumException {
        String consentId = encryptedRequest.getConsentId();
        Optional<String> decryptedConsentId = securityDataService.decryptId(consentId);
        if (!decryptedConsentId.isPresent()) {
            log.info("Encrypted Consent ID: [{}]. Check consent and save action log failed, couldn't decrypt consent id",
                     consentId);
            return CmsResponse.<CmsResponse.VoidResponse>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        AisConsentActionRequest decryptedRequest = new AisConsentActionRequest(encryptedRequest.getTppId(),
                                                                               decryptedConsentId.get(),
                                                                               encryptedRequest.getActionStatus(),
                                                                               encryptedRequest.getRequestUri(),
                                                                               encryptedRequest.isUpdateUsage(),
                                                                               encryptedRequest.getResourceId(),
                                                                               encryptedRequest.getTransactionId());
        return aisConsentService.checkConsentAndSaveActionLog(decryptedRequest);
    }

    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public CmsResponse<CmsConsent> updateAspspAccountAccess(String encryptedConsentId, AccountAccess request) throws WrongChecksumException {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedConsentId);

        if (!decryptIdOptional.isPresent()) {
            log.info("Encrypted Consent ID: [{}]. Update aspsp account access with response failed, couldn't decrypt consent id",
                     encryptedConsentId);
            return CmsResponse.<CmsConsent>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return aisConsentService.updateAspspAccountAccess(decryptIdOptional.get(), request);
    }
}
