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

package de.adorsys.psd2.consent.web.xs2a.controller;

import de.adorsys.psd2.consent.api.CmsConsentApi;
import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.ais.ConsentStatusResponse;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.consent.api.service.ConsentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CmsConsentController implements CmsConsentApi {
    private final ConsentServiceEncrypted consentServiceEncrypted;

    @Override
    public ResponseEntity<Object> createConsent(@RequestBody CmsConsent request) {
        CmsResponse<CmsCreateConsentResponse> cmsResponse;

        try {
            cmsResponse = consentServiceEncrypted.createConsent(request);
        } catch (WrongChecksumException e) {
            return new ResponseEntity<>(CmsError.CHECKSUM_ERROR, HttpStatus.BAD_REQUEST);
        }

        if (cmsResponse.hasError()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(cmsResponse.getPayload(), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<CmsConsent> getConsentById(String encryptedConsentId) {
        CmsResponse<CmsConsent> consentById = consentServiceEncrypted.getConsentById(encryptedConsentId);

        if (consentById.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(consentById.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ConsentStatusResponse> getConsentStatusById(String encryptedConsentId) {
        CmsResponse<ConsentStatus> consentStatusById = consentServiceEncrypted.getConsentStatusById(encryptedConsentId);

        if (consentStatusById.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new ConsentStatusResponse(consentStatusById.getPayload()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> updateConsentStatus(String encryptedConsentId, String status) {
        try {
            CmsResponse<Boolean> response = consentServiceEncrypted.updateConsentStatusById(encryptedConsentId, ConsentStatus.valueOf(status));
            if (response.isSuccessful() && BooleanUtils.isTrue(response.getPayload())) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } catch (WrongChecksumException e) {
            return new ResponseEntity<>(CmsError.CHECKSUM_ERROR, HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException i) {
            log.error("Invalid consent status: [{}] for consent-ID [{}]", status, encryptedConsentId);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<Void> findAndTerminateOldConsentsByNewConsentId(String encryptedConsentId) {
        consentServiceEncrypted.findAndTerminateOldConsentsByNewConsentId(encryptedConsentId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Object> updateMultilevelScaRequired(String encryptedConsentId, boolean multilevelSca) {
        CmsResponse<Boolean> response;

        try {
            response = consentServiceEncrypted.updateMultilevelScaRequired(encryptedConsentId, multilevelSca);
        } catch (WrongChecksumException e) {
            return new ResponseEntity<>(CmsError.CHECKSUM_ERROR, HttpStatus.BAD_REQUEST);
        }

        if (response.isSuccessful() && BooleanUtils.isTrue(response.getPayload())) {
            return new ResponseEntity<>(true, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
