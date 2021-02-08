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

import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.CmsAisAccountConsent;
import de.adorsys.psd2.consent.api.ais.CmsAisConsentResponse;
import de.adorsys.psd2.consent.psu.api.CmsPsuAisApi;
import de.adorsys.psd2.consent.psu.api.CmsPsuAisService;
import de.adorsys.psd2.consent.psu.api.CmsPsuAuthorisation;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisConsentAccessRequest;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisPsuDataAuthorisation;
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

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CmsPsuAisController implements CmsPsuAisApi {
    private final CmsPsuAisService cmsPsuAisService;

    @Override
    public ResponseEntity<Object> updatePsuDataInConsent(String consentId, String authorisationId, String instanceId, PsuIdData psuIdData) {
        try {
            return cmsPsuAisService.updatePsuDataInConsent(psuIdData, authorisationId, instanceId)
                       ? ResponseEntity.ok().build()
                       : ResponseEntity.badRequest().build();
        } catch (AuthorisationIsExpiredException e) {
            return new ResponseEntity<>(new CmsAisConsentResponse(e.getNokRedirectUri()), HttpStatus.REQUEST_TIMEOUT);
        }
    }

    @Override
    public ResponseEntity<Object> updateAuthorisationStatus(String consentId, String status, String authorisationId, String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String instanceId, AuthenticationDataHolder authenticationDataHolder) {
        ScaStatus scaStatus = ScaStatus.fromValue(status);

        if (scaStatus == null) {
            log.info("Consent ID [{}], Authorisation ID [{}], Instance ID: [{}]. Bad request: SCA status [{}] incorrect.", consentId, authorisationId, instanceId, status);
            return ResponseEntity.badRequest().build();
        }

        PsuIdData psuIdData = getPsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);

        try {
            return cmsPsuAisService.updateAuthorisationStatus(psuIdData, consentId, authorisationId, scaStatus, instanceId, authenticationDataHolder)
                       ? ResponseEntity.ok().build()
                       : ResponseEntity.badRequest().build();
        } catch (AuthorisationIsExpiredException e) {
            log.debug("Consent ID [{}], Authorisation ID [{}], Instance ID: [{}]. Update authorisation status request timeout (authorisation is expired): NOK redirect url [{}]", consentId, authorisationId, instanceId, e.getNokRedirectUri());
            return new ResponseEntity<>(new CmsAisConsentResponse(e.getNokRedirectUri()), HttpStatus.REQUEST_TIMEOUT);
        }
    }

    @Override
    public ResponseEntity<Boolean> confirmConsent(String consentId, String instanceId) {
        try {
            return new ResponseEntity<>(cmsPsuAisService.confirmConsent(consentId, instanceId), HttpStatus.OK);
        } catch (WrongChecksumException e) {
            log.info("Consent ID [{}], Instance ID: [{}]. Confirm AIS consent failed due to wrong checksum.", consentId, instanceId);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<Boolean> rejectConsent(String consentId, String instanceId) {
        try {
            return new ResponseEntity<>(cmsPsuAisService.rejectConsent(consentId, instanceId), HttpStatus.OK);
        } catch (WrongChecksumException e) {
            log.info("Consent ID [{}], Instance ID: [{}]. Reject AIS consent failed due to wrong checksum.", consentId, instanceId);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<List<CmsAisAccountConsent>> getConsentsForPsu(String psuId, String psuIdType,
                                                                        String psuCorporateId, String psuCorporateIdType,
                                                                        String instanceId, Integer pageIndex,
                                                                        Integer itemsPerPage, String additionalTppInfo) {
        PsuIdData psuIdData = getPsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);
        return new ResponseEntity<>(cmsPsuAisService.getConsentsForPsuAndAdditionalTppInfo(psuIdData, instanceId, pageIndex, itemsPerPage, additionalTppInfo), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> revokeConsent(String consentId, String instanceId) {
        try {
            return new ResponseEntity<>(cmsPsuAisService.revokeConsent(consentId, instanceId), HttpStatus.OK);
        } catch (WrongChecksumException e) {
            log.info("Consent ID [{}], Instance ID: [{}]. Revoke AIS consent failed due to wrong checksum.", consentId, instanceId);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<Boolean> authorisePartiallyConsent(String consentId, String instanceId) {
        try {
            return new ResponseEntity<>(cmsPsuAisService.authorisePartiallyConsent(consentId, instanceId), HttpStatus.OK);
        } catch (WrongChecksumException e) {
            log.info("Consent ID [{}], Instance ID: [{}]. Authorise partially AIS consent failed due to wrong checksum.", consentId, instanceId);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<CmsAisConsentResponse> getConsentIdByRedirectId(String redirectId, String instanceId) {
        Optional<CmsAisConsentResponse> response;
        try {
            response = cmsPsuAisService.checkRedirectAndGetConsent(redirectId, instanceId);

            if (response.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            CmsAisConsentResponse cmsAisConsentResponse = response.get();
            return new ResponseEntity<>(cmsAisConsentResponse, HttpStatus.OK);
        } catch (RedirectUrlIsExpiredException e) {
            log.debug("Redirect ID [{}], Instance ID: [{}]. Get consent ID by redirect ID request timeout (redirect url is expired): NOK redirect url [{}]", redirectId, instanceId, e.getNokRedirectUri());
            return new ResponseEntity<>(new CmsAisConsentResponse(e.getNokRedirectUri()), HttpStatus.REQUEST_TIMEOUT);
        }
    }

    @Override
    public ResponseEntity<CmsAisAccountConsent> getConsentByConsentId(String consentId, String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String instanceId) {
        PsuIdData psuIdData = getPsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);
        return cmsPsuAisService.getConsent(psuIdData, consentId, instanceId)
                   .map(aisAccountConsent -> new ResponseEntity<>(aisAccountConsent, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Override
    public ResponseEntity<CmsPsuAuthorisation> getAuthorisationByAuthorisationId(String authorisationId, String instanceId) {
        return cmsPsuAisService.getAuthorisationByAuthorisationId(authorisationId, instanceId)
                   .map(payment -> new ResponseEntity<>(payment, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @Override
    public ResponseEntity<Void> putAccountAccessInConsent(String consentId, CmsAisConsentAccessRequest accountAccessRequest, String instanceId) {
        boolean accessSaved = cmsPsuAisService.updateAccountAccessInConsent(consentId, accountAccessRequest, instanceId);

        if (accessSaved) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<List<CmsAisPsuDataAuthorisation>> psuDataAuthorisations(String consentId, String instanceId, Integer pageIndex, Integer itemsPerPage) {
        return cmsPsuAisService.getPsuDataAuthorisations(consentId, instanceId, pageIndex, itemsPerPage)
                   .map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    private PsuIdData getPsuIdData(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType) {
        return new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType, null);
    }
}
