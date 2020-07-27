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

package de.adorsys.psd2.consent.web.psu.controller;

import de.adorsys.psd2.consent.api.ais.CmsAisConsentResponse;
import de.adorsys.psd2.consent.api.piis.v2.CmsConfirmationOfFundsResponse;
import de.adorsys.psd2.consent.psu.api.CmsPsuConfirmationOfFundsApi;
import de.adorsys.psd2.consent.psu.api.CmsPsuConfirmationOfFundsService;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class CmsPsuConfirmationOfFundsController implements CmsPsuConfirmationOfFundsApi {
    private final CmsPsuConfirmationOfFundsService cmsPsuConfirmationOfFundsService;

    @Override
    public ResponseEntity<Object> updateAuthorisationStatus(String consentId, String status, String authorisationId, String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String instanceId, AuthenticationDataHolder authenticationDataHolder) {
        ScaStatus scaStatus = ScaStatus.fromValue(status);

        if (scaStatus == null) {
            return ResponseEntity.badRequest().build();
        }

        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType, null);
        try {
            return cmsPsuConfirmationOfFundsService.updateAuthorisationStatus(psuIdData, consentId, authorisationId, scaStatus, instanceId, authenticationDataHolder)
                       ? ResponseEntity.ok().build()
                       : ResponseEntity.badRequest().build();
        } catch (AuthorisationIsExpiredException e) {
            return new ResponseEntity<>(new CmsConfirmationOfFundsResponse(e.getNokRedirectUri()), HttpStatus.REQUEST_TIMEOUT);
        }
    }

    @Override
    public ResponseEntity<CmsConfirmationOfFundsResponse> getConsentIdByRedirectId(String redirectId, String instanceId) {
        Optional<CmsConfirmationOfFundsResponse> response;
        try {
            response = cmsPsuConfirmationOfFundsService.checkRedirectAndGetConsent(redirectId, instanceId);

            if (response.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            CmsConfirmationOfFundsResponse cmsConfirmationOfFundsResponse = response.get();
            return new ResponseEntity<>(cmsConfirmationOfFundsResponse, HttpStatus.OK);
        } catch (RedirectUrlIsExpiredException e) {
            return new ResponseEntity<>(new CmsConfirmationOfFundsResponse(e.getNokRedirectUri()), HttpStatus.REQUEST_TIMEOUT);
        }
    }

    @Override
    public ResponseEntity<Object> updatePsuDataInConsent(String consentId, String authorisationId, String instanceId, PsuIdData psuIdData) {
        try {
            return cmsPsuConfirmationOfFundsService.updatePsuDataInConsent(psuIdData, authorisationId, instanceId)
                       ? ResponseEntity.ok().build()
                       : ResponseEntity.badRequest().build();
        } catch (AuthorisationIsExpiredException e) {
            return new ResponseEntity<>(new CmsAisConsentResponse(e.getNokRedirectUri()), HttpStatus.REQUEST_TIMEOUT);
        }
    }

    @Override
    public ResponseEntity getAuthorisationByAuthorisationId(String authorisationId, String instanceId) {
        try {
        return cmsPsuConfirmationOfFundsService.getAuthorisationByAuthorisationId(authorisationId, instanceId)
                   .map(authorisation -> new ResponseEntity<>(authorisation, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
        } catch (AuthorisationIsExpiredException e) {
            return new ResponseEntity<>(new CmsConfirmationOfFundsResponse(e.getNokRedirectUri()), HttpStatus.REQUEST_TIMEOUT);
        }
    }
}
