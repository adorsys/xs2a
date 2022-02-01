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
        if (decryptedConsentId.isEmpty()) {
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

        if (decryptIdOptional.isEmpty()) {
            log.info("Encrypted Consent ID: [{}]. Update aspsp account access with response failed, couldn't decrypt consent id",
                     encryptedConsentId);
            return CmsResponse.<CmsConsent>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return aisConsentService.updateAspspAccountAccess(decryptIdOptional.get(), request);
    }
}
