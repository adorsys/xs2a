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

import de.adorsys.psd2.consent.api.pis.CmsPayment;
import de.adorsys.psd2.consent.api.pis.CmsPaymentResponse;
import de.adorsys.psd2.consent.psu.api.CmsPsuAuthorisation;
import de.adorsys.psd2.consent.psu.api.CmsPsuPisApi;
import de.adorsys.psd2.consent.psu.api.CmsPsuPisService;
import de.adorsys.psd2.consent.psu.api.pis.CmsPisPsuDataAuthorisation;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CmsPsuPisController implements CmsPsuPisApi {
    private final CmsPsuPisService cmsPsuPisService;

    @Override
    public ResponseEntity<Object> updatePsuInPayment(String authorisationId, String instanceId, PsuIdData psuIdData) {
        try {
            return cmsPsuPisService.updatePsuInPayment(psuIdData, authorisationId, instanceId)
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
        } catch (AuthorisationIsExpiredException e) {
            log.debug("Authorisation ID [{}], Instance ID: [{}]. Update PSU data request timeout (authorisation is expired): NOK redirect url [{}]", authorisationId, instanceId, e.getNokRedirectUri());
            return new ResponseEntity<>(new CmsPaymentResponse(e.getNokRedirectUri()), HttpStatus.REQUEST_TIMEOUT);
        }
    }

    @Override
    public ResponseEntity<Object> getPaymentIdByRedirectId(String redirectId, String instanceId) {
        Optional<CmsPaymentResponse> response;
        try {
            response = cmsPsuPisService.checkRedirectAndGetPayment(redirectId, instanceId);

            if (response.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            CmsPaymentResponse cmsPaymentResponse = response.get();
            return new ResponseEntity<>(cmsPaymentResponse, HttpStatus.OK);
        } catch (RedirectUrlIsExpiredException e) {
            log.debug("Redirect ID [{}], Instance ID: [{}]. Get payment ID by redirect ID request timeout (redirect url is expired): NOK redirect url [{}]", redirectId, instanceId, e.getNokRedirectUri());
            return new ResponseEntity<>(new CmsPaymentResponse(e.getNokRedirectUri()), HttpStatus.REQUEST_TIMEOUT);
        }
    }

    @Override
    public ResponseEntity<CmsPayment> getPaymentByPaymentId(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String paymentId, String instanceId) {
        return getPaymentById(psuId, psuIdType, psuCorporateId, psuCorporateIdType, paymentId, instanceId);
    }

    @Override
    public ResponseEntity<Object> getPaymentIdByRedirectIdForCancellation(String redirectId, String instanceId) {
        Optional<CmsPaymentResponse> response;
        try {
            response = cmsPsuPisService.checkRedirectAndGetPaymentForCancellation(redirectId, instanceId);

            if (response.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            CmsPaymentResponse cmsPaymentResponse = response.get();
            return new ResponseEntity<>(cmsPaymentResponse, HttpStatus.OK);
        } catch (RedirectUrlIsExpiredException e) {
            log.debug("Redirect ID [{}], Instance ID: [{}]. Get payment ID for cancellation by redirect ID request timeout (redirect url is expired): NOK redirect url [{}]", redirectId, instanceId, e.getNokRedirectUri());
            return new ResponseEntity<>(new CmsPaymentResponse(e.getNokRedirectUri()), HttpStatus.REQUEST_TIMEOUT);
        }
    }

    @Override
    public ResponseEntity<CmsPayment> getPaymentByPaymentIdForCancellation(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String paymentId, String instanceId) {
        return getPaymentById(psuId, psuIdType, psuCorporateId, psuCorporateIdType, paymentId, instanceId);
    }

    @Override
    public ResponseEntity<CmsPsuAuthorisation> getAuthorisationByAuthorisationId(String authorisationId, String instanceId) {
        return cmsPsuPisService.getAuthorisationByAuthorisationId(authorisationId, instanceId)
            .map(payment -> new ResponseEntity<>(payment, HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @Override
    public ResponseEntity<Object> updateAuthorisationStatus(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String paymentId, String authorisationId, String status, String instanceId, AuthenticationDataHolder authenticationDataHolder) {
        ScaStatus scaStatus = ScaStatus.fromValue(status);

        if (scaStatus == null) {
            log.info("Payment ID [{}], Authorisation ID [{}], Instance ID: [{}]. Bad request: SCA status [{}] incorrect.", paymentId, authorisationId, instanceId, status);
            return ResponseEntity.badRequest().build();
        }

        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType, null);
        try {
            return cmsPsuPisService.updateAuthorisationStatus(psuIdData, paymentId, authorisationId, scaStatus, instanceId, authenticationDataHolder)
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
        } catch (AuthorisationIsExpiredException e) {
            return new ResponseEntity<>(new CmsPaymentResponse(e.getNokRedirectUri()), HttpStatus.REQUEST_TIMEOUT);
        }
    }

    @Override
    public ResponseEntity<Void> updatePaymentStatus(String paymentId, String status, String instanceId) {
        return cmsPsuPisService.updatePaymentStatus(paymentId, TransactionStatus.valueOf(status), instanceId)
            ? ResponseEntity.ok().build()
            : ResponseEntity.badRequest().build();
    }

    @Override
    public ResponseEntity<List<CmsPisPsuDataAuthorisation>> psuAuthorisationStatuses(String paymentId, String instanceId, Integer pageIndex, Integer itemsPerPage) {
        return cmsPsuPisService.getPsuDataAuthorisations(paymentId, instanceId, pageIndex, itemsPerPage)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @NotNull
    private ResponseEntity<CmsPayment> getPaymentById(String psuId, String psuIdType,
                                                      String psuCorporateId, String psuCorporateIdType,
                                                      String paymentId, String instanceId) {
        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType, null);
        return cmsPsuPisService.getPayment(psuIdData, paymentId, instanceId)
            .map(payment -> new ResponseEntity<>(payment, HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }
}
