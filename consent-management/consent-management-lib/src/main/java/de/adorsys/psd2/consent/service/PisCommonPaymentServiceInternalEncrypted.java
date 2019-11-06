/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.*;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentService;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PisCommonPaymentServiceInternalEncrypted implements PisCommonPaymentServiceEncrypted {
    private final SecurityDataService securityDataService;
    private final PisCommonPaymentService pisCommonPaymentService;

    @Override
    @Transactional
    public CmsResponse<CreatePisCommonPaymentResponse> createCommonPayment(PisPaymentInfo request) {
        CmsResponse<CreatePisCommonPaymentResponse> paymentResponse = pisCommonPaymentService.createCommonPayment(request);

        if (paymentResponse.hasError()) {
            return paymentResponse;
        }

        CreatePisCommonPaymentResponse payment = paymentResponse.getPayload();
        Optional<String> encryptIdOptional = securityDataService.encryptId(payment.getPaymentId());

        if (!encryptIdOptional.isPresent()) {
            log.info("Payment ID: [{}]. Create common payment failed, couldn't encrypt payment id", payment.getPaymentId());
            return CmsResponse.<CreatePisCommonPaymentResponse>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return CmsResponse.<CreatePisCommonPaymentResponse>builder()
                   .payload(new CreatePisCommonPaymentResponse(encryptIdOptional.get()))
                   .build();
    }

    @Override
    @Transactional
    public CmsResponse<TransactionStatus> getPisCommonPaymentStatusById(String encryptedPaymentId) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedPaymentId);

        if (!decryptIdOptional.isPresent()) {
            log.info("Encrypted Payment ID: [{}]. Get common payment status by ID failed, couldn't decrypt consent id",
                     encryptedPaymentId);
            return CmsResponse.<TransactionStatus>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return pisCommonPaymentService.getPisCommonPaymentStatusById(decryptIdOptional.get());
    }

    @Override
    @Transactional
    public CmsResponse<PisCommonPaymentResponse> getCommonPaymentById(String encryptedPaymentId) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedPaymentId);

        if (!decryptIdOptional.isPresent()) {
            log.info("Encrypted Payment ID: [{}]. Get common payment by ID failed, couldn't decrypt consent id",
                     encryptedPaymentId);
            return CmsResponse.<PisCommonPaymentResponse>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return pisCommonPaymentService.getCommonPaymentById(decryptIdOptional.get());
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updateCommonPaymentStatusById(String encryptedPaymentId, TransactionStatus status) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedPaymentId);

        if (!decryptIdOptional.isPresent()) {
            log.info("Encrypted Payment ID: [{}]. Get common payment status by ID failed, couldn't decrypt consent id",
                     encryptedPaymentId);
            return CmsResponse.<Boolean>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return pisCommonPaymentService.updateCommonPaymentStatusById(decryptIdOptional.get(), status);
    }

    @Override
    public CmsResponse<String> getDecryptedId(String encryptedId) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedId);

        if (!decryptIdOptional.isPresent()) {
            log.info("Encrypted Payment ID: [{}]. Couldn't decrypt consent id", encryptedId);
            return CmsResponse.<String>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }
        return CmsResponse.<String>builder()
                   .payload(decryptIdOptional.get())
                   .build();
    }

    @Override
    @Transactional
    public CmsResponse<CreatePisAuthorisationResponse> createAuthorization(String encryptedPaymentId, CreatePisAuthorisationRequest request) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedPaymentId);

        if (!decryptIdOptional.isPresent()) {
            log.info("Encrypted Payment ID: [{}].Create authorisation failed, couldn't decrypt consent id",
                     encryptedPaymentId);
            return CmsResponse.<CreatePisAuthorisationResponse>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return pisCommonPaymentService.createAuthorization(decryptIdOptional.get(), request);
    }

    @Override
    @Transactional
    public CmsResponse<CreatePisAuthorisationResponse> createAuthorizationCancellation(String encryptedPaymentId,
                                                                                       CreatePisAuthorisationRequest request) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedPaymentId);

        if (!decryptIdOptional.isPresent()) {
            log.info("Encrypted Payment ID: [{}].Create authorisation cancellation failed, couldn't decrypt consent id",
                     encryptedPaymentId);
            return CmsResponse.<CreatePisAuthorisationResponse>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return pisCommonPaymentService.createAuthorizationCancellation(decryptIdOptional.get(), request);
    }

    @Override
    @Transactional
    public CmsResponse<UpdatePisCommonPaymentPsuDataResponse> updatePisAuthorisation(String authorisationId,
                                                                                     UpdatePisCommonPaymentPsuDataRequest request) {
        return pisCommonPaymentService.updatePisAuthorisation(authorisationId, request);
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updatePisAuthorisationStatus(String authorisationId, ScaStatus scaStatus) {
        return pisCommonPaymentService.updatePisAuthorisationStatus(authorisationId, scaStatus);
    }

    @Override
    @Transactional
    public CmsResponse<UpdatePisCommonPaymentPsuDataResponse> updatePisCancellationAuthorisation(String authorisationId,
                                                                                                 UpdatePisCommonPaymentPsuDataRequest request) {
        return pisCommonPaymentService.updatePisCancellationAuthorisation(authorisationId, request);
    }

    @Override
    @Transactional
    public CmsResponse<CmsResponse.VoidResponse> updateCommonPayment(PisCommonPaymentRequest request, String encryptedPaymentId) {
        securityDataService.decryptId(encryptedPaymentId)
            .ifPresent(id -> pisCommonPaymentService.updateCommonPayment(request, id));

        return CmsResponse.<CmsResponse.VoidResponse>builder()
                   .payload(CmsResponse.voidResponse())
                   .build();
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updateMultilevelSca(String encryptedPaymentId, boolean multilevelScaRequired) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedPaymentId);

        if (!decryptIdOptional.isPresent()) {
            log.info("Encrypted Payment ID: [{}]. Update payment multilevel SCA failed, couldn't decrypt consent id",
                     encryptedPaymentId);
            return CmsResponse.<Boolean>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return pisCommonPaymentService.updateMultilevelSca(decryptIdOptional.get(), multilevelScaRequired);
    }

    @Override
    public CmsResponse<GetPisAuthorisationResponse> getPisAuthorisationById(String authorisationId) {
        return pisCommonPaymentService.getPisAuthorisationById(authorisationId);
    }

    @Override
    public CmsResponse<GetPisAuthorisationResponse> getPisCancellationAuthorisationById(String cancellationId) {
        return pisCommonPaymentService.getPisCancellationAuthorisationById(cancellationId);
    }

    @Override
    public CmsResponse<List<String>> getAuthorisationsByPaymentId(String encryptedPaymentId,
                                                                  PaymentAuthorisationType authorisationType) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedPaymentId);

        if (!decryptIdOptional.isPresent()) {
            log.info("Encrypted Payment ID: [{}]. Get payment authrisation list failed, couldn't decrypt consent id",
                     encryptedPaymentId);
            return CmsResponse.<List<String>>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return pisCommonPaymentService.getAuthorisationsByPaymentId(decryptIdOptional.get(), authorisationType);
    }

    @Override
    @Transactional
    public CmsResponse<ScaStatus> getAuthorisationScaStatus(String encryptedPaymentId, String authorisationId, PaymentAuthorisationType authorisationType) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedPaymentId);

        if (!decryptIdOptional.isPresent()) {
            log.info("Encrypted Payment ID: [{}]. Get common payment authorisation SCA status failed, couldn't decrypt consent id",
                     encryptedPaymentId);
            return CmsResponse.<ScaStatus>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return pisCommonPaymentService.getAuthorisationScaStatus(decryptIdOptional.get(), authorisationId, authorisationType);

    }

    @Override
    public CmsResponse<List<PsuIdData>> getPsuDataListByPaymentId(String encryptedPaymentId) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedPaymentId);

        if (!decryptIdOptional.isPresent()) {
            log.info("Encrypted Payment ID: [{}]. Get PSU data list by payment ID failed, couldn't decrypt consent id",
                     encryptedPaymentId);
            return CmsResponse.<List<PsuIdData>>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return pisCommonPaymentService.getPsuDataListByPaymentId(decryptIdOptional.get());
    }

    @Override
    public CmsResponse<Boolean> isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        return pisCommonPaymentService.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods) {
        return pisCommonPaymentService.saveAuthenticationMethods(authorisationId, methods);
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        return pisCommonPaymentService.updateScaApproach(authorisationId, scaApproach);
    }

    @Override
    public CmsResponse<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId, PaymentAuthorisationType authorisationType) {
        return pisCommonPaymentService.getAuthorisationScaApproach(authorisationId, authorisationType);
    }
}
