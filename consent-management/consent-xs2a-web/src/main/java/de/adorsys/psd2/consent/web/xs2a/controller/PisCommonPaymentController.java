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

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.PisCommonPaymentApi;
import de.adorsys.psd2.consent.api.authorisation.*;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentDataStatusResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PisCommonPaymentController implements PisCommonPaymentApi {
    private final PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    private final AuthorisationServiceEncrypted authorisationServiceEncrypted;

    @Override
    public ResponseEntity<CreatePisCommonPaymentResponse> createCommonPayment(PisPaymentInfo request) {
        CmsResponse<CreatePisCommonPaymentResponse> response = pisCommonPaymentServiceEncrypted.createCommonPayment(request);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<PisCommonPaymentDataStatusResponse> getPisCommonPaymentStatusById(String paymentId) {
        CmsResponse<TransactionStatus> response = pisCommonPaymentServiceEncrypted.getPisCommonPaymentStatusById(paymentId);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(new PisCommonPaymentDataStatusResponse(response.getPayload()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PisCommonPaymentResponse> getCommonPaymentById(String paymentId) {
        CmsResponse<PisCommonPaymentResponse> response = pisCommonPaymentServiceEncrypted.getCommonPaymentById(paymentId);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateCommonPaymentStatus(String paymentId, String status) {
        TransactionStatus transactionStatus = TransactionStatus.getByValue(status);
        if (transactionStatus == null) {
            log.error("Invalid transaction status: [{}] for payment-ID [{}]", status, paymentId);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        CmsResponse<Boolean> response = pisCommonPaymentServiceEncrypted.updateCommonPaymentStatusById(paymentId, transactionStatus);
        if (response.isSuccessful() && BooleanUtils.isTrue(response.getPayload())) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<CreateAuthorisationResponse> createAuthorisation(String paymentId, CreateAuthorisationRequest request) {
        CmsResponse<CreateAuthorisationResponse> response = authorisationServiceEncrypted.createAuthorisation(new PisAuthorisationParentHolder(paymentId), request);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<CreateAuthorisationResponse> createAuthorisationCancellation(String paymentId, CreateAuthorisationRequest request) {
        CmsResponse<CreateAuthorisationResponse> response = authorisationServiceEncrypted.createAuthorisation(new PisCancellationAuthorisationParentHolder(paymentId), request);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Authorisation> updateAuthorisation(String authorisationId, UpdateAuthorisationRequest request) {
        CmsResponse<Authorisation> response = authorisationServiceEncrypted.updateAuthorisation(authorisationId, request);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateAuthorisationStatus(String authorisationId, String authorisationStatus) {
        CmsResponse<Boolean> response = authorisationServiceEncrypted.updateAuthorisationStatus(authorisationId, ScaStatus.fromValue(authorisationStatus));

        if (response.isSuccessful() && BooleanUtils.isTrue(response.getPayload())) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<Authorisation> getAuthorisation(String authorisationId) {
        CmsResponse<Authorisation> response = authorisationServiceEncrypted.getAuthorisationById(authorisationId);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ScaStatus> getAuthorisationScaStatus(String paymentId, String authorisationId) {
        CmsResponse<ScaStatus> response = authorisationServiceEncrypted.getAuthorisationScaStatus(authorisationId, new PisAuthorisationParentHolder(paymentId));

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Authorisation> updateCancellationAuthorisation(String authorisationId, UpdateAuthorisationRequest request) {
        CmsResponse<Authorisation> response = authorisationServiceEncrypted.updateAuthorisation(authorisationId, request);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<String>> getAuthorisationCancellation(String authorisationId) {
        CmsResponse<List<String>> response = authorisationServiceEncrypted.getAuthorisationsByParentId(new PisCancellationAuthorisationParentHolder(authorisationId));

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<String>> getAuthorisationsCancellation(String paymentId) {
        CmsResponse<List<String>> response = authorisationServiceEncrypted.getAuthorisationsByParentId(new PisCancellationAuthorisationParentHolder(paymentId));

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ScaStatus> getCancellationAuthorisationScaStatus(String paymentId, String authorisationId) {
        CmsResponse<ScaStatus> response = authorisationServiceEncrypted.getAuthorisationScaStatus(authorisationId, new PisCancellationAuthorisationParentHolder(paymentId));

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<String>> getAuthorisations(String paymentId) {
        CmsResponse<List<String>> response = authorisationServiceEncrypted.getAuthorisationsByParentId(new PisAuthorisationParentHolder(paymentId));

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        CmsResponse<Boolean> response = authorisationServiceEncrypted.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
        return new ResponseEntity<>(response.isSuccessful() && response.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods) {
        CmsResponse<Boolean> response = authorisationServiceEncrypted.saveAuthenticationMethods(authorisationId, methods);

        if (response.isSuccessful() && BooleanUtils.isTrue(response.getPayload())) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<Boolean> updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        CmsResponse<Boolean> response = authorisationServiceEncrypted.updateScaApproach(authorisationId, scaApproach);

        if (response.isSuccessful() && BooleanUtils.isTrue(response.getPayload())) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId) {
        CmsResponse<AuthorisationScaApproachResponse> response = authorisationServiceEncrypted.getAuthorisationScaApproach(authorisationId);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AuthorisationScaApproachResponse> getCancellationAuthorisationScaApproach(String authorisationId) {
        CmsResponse<AuthorisationScaApproachResponse> response = authorisationServiceEncrypted.getAuthorisationScaApproach(authorisationId);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> updateMultilevelScaRequired(String paymentId, boolean multilevelSca) {
        CmsResponse<Boolean> response = pisCommonPaymentServiceEncrypted.updateMultilevelSca(paymentId, multilevelSca);

        if (response.isSuccessful() && BooleanUtils.isTrue(response.getPayload())) {
            return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);

        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
