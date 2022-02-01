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

package de.adorsys.psd2.consent.web.psu.controller;

import de.adorsys.psd2.consent.api.piis.v2.CmsConfirmationOfFundsConsent;
import de.adorsys.psd2.consent.api.piis.v2.CmsConfirmationOfFundsResponse;
import de.adorsys.psd2.consent.psu.api.CmsPsuConfirmationOfFundsApi;
import de.adorsys.psd2.consent.psu.api.CmsPsuConfirmationOfFundsService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CmsPsuConfirmationOfFundsController implements CmsPsuConfirmationOfFundsApi {
    private final CmsPsuConfirmationOfFundsService cmsPsuConfirmationOfFundsService;

    @Override
    public ResponseEntity<Object> updateAuthorisationStatus(String consentId, String status, String authorisationId, String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String instanceId, AuthenticationDataHolder authenticationDataHolder) {
        ScaStatus scaStatus = ScaStatus.fromValue(status);

        if (scaStatus == null) {
            log.info("Consent ID [{}], Authorisation ID [{}], Instance ID: [{}]. Bad request: SCA status [{}] incorrect.", consentId, authorisationId, instanceId, status);
            return ResponseEntity.badRequest().build();
        }

        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType, null);
        try {
            return cmsPsuConfirmationOfFundsService.updateAuthorisationStatus(psuIdData, consentId, authorisationId, scaStatus, instanceId, authenticationDataHolder)
                       ? ResponseEntity.ok().build()
                       : ResponseEntity.badRequest().build();
        } catch (AuthorisationIsExpiredException e) {
            log.debug("Consent ID [{}], Authorisation ID [{}], Instance ID: [{}]. Update authorisation status request timeout (authorisation is expired): NOK redirect url [{}]", consentId, authorisationId, instanceId, e.getNokRedirectUri());
            return new ResponseEntity<>(new CmsConfirmationOfFundsResponse(e.getNokRedirectUri()), HttpStatus.REQUEST_TIMEOUT);
        }
    }

    @Override
    public ResponseEntity<CmsConfirmationOfFundsResponse> getConsentByRedirectId(String redirectId, String instanceId) {
        Optional<CmsConfirmationOfFundsResponse> response;
        try {
            response = cmsPsuConfirmationOfFundsService.checkRedirectAndGetConsent(redirectId, instanceId);

            if (response.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            CmsConfirmationOfFundsResponse cmsConfirmationOfFundsResponse = response.get();
            return new ResponseEntity<>(cmsConfirmationOfFundsResponse, HttpStatus.OK);
        } catch (RedirectUrlIsExpiredException e) {
            log.debug("Redirect ID [{}], Instance ID: [{}]. Get consent ID by redirect ID request timeout (redirect url is expired): NOK redirect url [{}]", redirectId, instanceId, e.getNokRedirectUri());
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
            log.debug("Consent ID [{}], Authorisation ID [{}], Instance ID: [{}]. Update PSU data request timeout (authorisation is expired): NOK redirect url [{}]", consentId, authorisationId, instanceId, e.getNokRedirectUri());
            return new ResponseEntity<>(new CmsConfirmationOfFundsResponse(e.getNokRedirectUri()), HttpStatus.REQUEST_TIMEOUT);
        }
    }

    @Override
    public ResponseEntity getAuthorisationByAuthorisationId(String authorisationId, String instanceId) {
        try {
            return cmsPsuConfirmationOfFundsService.getAuthorisationByAuthorisationId(authorisationId, instanceId)
                       .map(authorisation -> new ResponseEntity<>(authorisation, HttpStatus.OK))
                       .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
        } catch (AuthorisationIsExpiredException e) {
            log.debug("Authorisation ID [{}], Instance ID: [{}]. Get authorisation by authorisation ID request timeout (authorisation is expired): NOK redirect url [{}]", authorisationId, instanceId, e.getNokRedirectUri());
            return new ResponseEntity<>(new CmsConfirmationOfFundsResponse(e.getNokRedirectUri()), HttpStatus.REQUEST_TIMEOUT);
        }
    }

    @Override
    public ResponseEntity<Void> updateConsentStatus(String consentId, String status, String instanceId) {
        ConsentStatus consentStatus;
        try {
            consentStatus = ConsentStatus.valueOf(status);
        } catch (IllegalArgumentException exception) {
            log.info("Consent ID [{}], Instance ID: [{}]. Bad request: SCA status [{}] incorrect.", consentId, instanceId, status);
            return ResponseEntity.badRequest().build();
        }

        return cmsPsuConfirmationOfFundsService.updateConsentStatus(consentId, consentStatus, instanceId)
                   ? ResponseEntity.ok().build()
                   : ResponseEntity.badRequest().build();
    }

    @Override
    public ResponseEntity<CmsConfirmationOfFundsConsent> getConsentByConsentId(String consentId, String psuId, String psuIdType,
                                                                               String psuCorporateId, String psuCorporateIdType,
                                                                               String instanceId) {
        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType, null);
        return cmsPsuConfirmationOfFundsService.getConsent(psuIdData, consentId, instanceId)
                   .map(consent -> new ResponseEntity<>(consent, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
