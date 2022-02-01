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

import de.adorsys.psd2.consent.api.pis.CmsBasePaymentResponse;
import de.adorsys.psd2.consent.api.pis.CmsPaymentResponse;
import de.adorsys.psd2.consent.api.pis.UpdatePaymentRequest;
import de.adorsys.psd2.consent.psu.api.CmsPsuAuthorisation;
import de.adorsys.psd2.consent.psu.api.CmsPsuPisApi;
import de.adorsys.psd2.consent.psu.api.CmsPsuPisService;
import de.adorsys.psd2.consent.psu.api.pis.CmsPisPsuDataAuthorisation;
import de.adorsys.psd2.consent.web.psu.mapper.PaymentModelMapperCmsPsu;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@AllArgsConstructor
public class CmsPsuPisController implements CmsPsuPisApi {
    private final CmsPsuPisService cmsPsuPisService;
    private final PaymentModelMapperCmsPsu paymentModelMapperCms;

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
    public ResponseEntity<Object> updatePayment(String paymentId, String paymentService, String paymentProduct, String instanceId, Object body) {
        if (PaymentType.getByValue(paymentService).isEmpty()) {
            log.info("Payment ID [{}], Payment Service [{}], Payment Product: [{}], Instance ID: [{}]. Bad request: Payment Service incorrect.", paymentId, paymentService, paymentProduct, instanceId);
            return ResponseEntity.badRequest().build();
        }

        byte[] payment = paymentModelMapperCms.mapToXs2aPayment();
        UpdatePaymentRequest updatePaymentRequest = new UpdatePaymentRequest(payment, instanceId, paymentId, paymentProduct, paymentService);
        return cmsPsuPisService.updatePayment(updatePaymentRequest)
                   ? ResponseEntity.ok().build()
                   : ResponseEntity.badRequest().build();
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
    public ResponseEntity<CmsBasePaymentResponse> getPaymentByPaymentId(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String paymentId, String instanceId) {
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
    public ResponseEntity<CmsBasePaymentResponse> getPaymentByPaymentIdForCancellation(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String paymentId, String instanceId) {
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
    private ResponseEntity<CmsBasePaymentResponse> getPaymentById(String psuId, String psuIdType,
                                                                  String psuCorporateId, String psuCorporateIdType,
                                                                  String paymentId, String instanceId) {
        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType, null);
        return cmsPsuPisService.getPayment(psuIdData, paymentId, instanceId)
                   .map(payment -> new ResponseEntity<>(payment, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }
}
