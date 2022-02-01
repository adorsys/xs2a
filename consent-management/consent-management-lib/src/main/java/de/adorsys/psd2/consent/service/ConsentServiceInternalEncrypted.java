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
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.consent.api.service.ConsentService;
import de.adorsys.psd2.consent.api.service.ConsentServiceEncrypted;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.TerminateOldConsentsRequest;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsentServiceInternalEncrypted implements ConsentServiceEncrypted {
    private final SecurityDataService securityDataService;
    private final ConsentService consentService;

    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public CmsResponse<CmsCreateConsentResponse> createConsent(CmsConsent request) throws WrongChecksumException {
        CmsResponse<CmsCreateConsentResponse> serviceResponse = consentService.createConsent(request);

        if (serviceResponse.hasError()) {
            return serviceResponse;
        }

        CmsCreateConsentResponse cmsCreateConsentResponse = serviceResponse.getPayload();
        Optional<String> encryptIdOptional = securityDataService.encryptId(cmsCreateConsentResponse.getConsentId());

        if (encryptIdOptional.isEmpty()) {
            log.info("Consent ID: [{}]. Create consent failed, couldn't encrypt consent id", cmsCreateConsentResponse.getConsentId());
            return CmsResponse.<CmsCreateConsentResponse>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return CmsResponse.<CmsCreateConsentResponse>builder()
                   .payload(new CmsCreateConsentResponse(encryptIdOptional.get(), cmsCreateConsentResponse.getCmsConsent()))
                   .build();
    }

    @Override
    @Transactional
    public CmsResponse<ConsentStatus> getConsentStatusById(String encryptedConsentId) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedConsentId);

        if (decryptIdOptional.isEmpty()) {
            log.info("Encrypted Consent ID: [{}]. Get consent status by id failed, couldn't decrypt consent id", encryptedConsentId);
            return CmsResponse.<ConsentStatus>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return consentService.getConsentStatusById(decryptIdOptional.get());
    }

    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public CmsResponse<Boolean> updateConsentStatusById(String encryptedConsentId, ConsentStatus status) throws WrongChecksumException {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedConsentId);

        if (decryptIdOptional.isEmpty()) {
            log.info("Encrypted Consent ID: [{}]. Update consent by id failed, couldn't decrypt consent id", encryptedConsentId);
            return CmsResponse.<Boolean>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return consentService.updateConsentStatusById(decryptIdOptional.get(), status);
    }

    @Override
    @Transactional
    public CmsResponse<CmsConsent> getConsentById(String encryptedConsentId) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedConsentId);

        if (decryptIdOptional.isEmpty()) {
            log.info("Encrypted Consent ID: [{}]. Get consent by id failed, couldn't decrypt consent id", encryptedConsentId);
            return CmsResponse.<CmsConsent>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return consentService.getConsentById(decryptIdOptional.get());
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> findAndTerminateOldConsentsByNewConsentId(String encryptedNewConsentId) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedNewConsentId);

        if (decryptIdOptional.isEmpty()) {
            log.info("Encrypted Consent ID: [{}]. Terminate consent by id failed, couldn't decrypt consent id", encryptedNewConsentId);
            return CmsResponse.<Boolean>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return consentService.findAndTerminateOldConsentsByNewConsentId(decryptIdOptional.get());
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> findAndTerminateOldConsents(String encryptedNewConsentId,
                                                            TerminateOldConsentsRequest request) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedNewConsentId);

        if (decryptIdOptional.isEmpty()) {
            log.info("Encrypted Consent ID: [{}]. Terminate consent by id failed, couldn't decrypt consent id", encryptedNewConsentId);
            return CmsResponse.<Boolean>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return consentService.findAndTerminateOldConsents(decryptIdOptional.get(), request);
    }

    @Override
    public CmsResponse<List<PsuIdData>> getPsuDataByConsentId(String encryptedConsentId) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedConsentId);

        if (decryptIdOptional.isEmpty()) {
            log.info("Encrypted Consent ID: [{}]. Get psu data by consent id failed, couldn't decrypt consent id",
                     encryptedConsentId);
            return CmsResponse.<List<PsuIdData>>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return consentService.getPsuDataByConsentId(decryptIdOptional.get());
    }

    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public CmsResponse<Boolean> updateMultilevelScaRequired(String encryptedConsentId, boolean multilevelScaRequired) throws WrongChecksumException {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedConsentId);

        if (decryptIdOptional.isEmpty()) {
            log.info("Encrypted Consent ID: [{}]. Update MultilevelScaRequired failed, couldn't decrypt consent id",
                     encryptedConsentId);
            return CmsResponse.<Boolean>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return consentService.updateMultilevelScaRequired(decryptIdOptional.get(), multilevelScaRequired);
    }
}
