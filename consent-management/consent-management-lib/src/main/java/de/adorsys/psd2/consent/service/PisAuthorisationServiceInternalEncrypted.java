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
import de.adorsys.psd2.consent.api.pis.authorisation.*;
import de.adorsys.psd2.consent.api.service.PisAuthorisationService;
import de.adorsys.psd2.consent.api.service.PisAuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
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
public class PisAuthorisationServiceInternalEncrypted implements PisAuthorisationServiceEncrypted {
    private final SecurityDataService securityDataService;
    private final PisAuthorisationService pisAuthorisationService;

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

        return pisAuthorisationService.createAuthorization(decryptIdOptional.get(), request);
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

        return pisAuthorisationService.createAuthorizationCancellation(decryptIdOptional.get(), request);
    }

    @Override
    @Transactional
    public CmsResponse<UpdatePisCommonPaymentPsuDataResponse> updatePisAuthorisation(String authorisationId,
                                                                                     UpdatePisCommonPaymentPsuDataRequest request) {
        return pisAuthorisationService.updatePisAuthorisation(authorisationId, request);
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updatePisAuthorisationStatus(String authorisationId, ScaStatus scaStatus) {
        return pisAuthorisationService.updatePisAuthorisationStatus(authorisationId, scaStatus);
    }

    @Override
    @Transactional
    public CmsResponse<UpdatePisCommonPaymentPsuDataResponse> updatePisCancellationAuthorisation(String authorisationId,
                                                                                                 UpdatePisCommonPaymentPsuDataRequest request) {
        return pisAuthorisationService.updatePisCancellationAuthorisation(authorisationId, request);
    }

    @Override
    @Transactional
    public CmsResponse<GetPisAuthorisationResponse> getPisAuthorisationById(String authorisationId) {
        return pisAuthorisationService.getPisAuthorisationById(authorisationId);
    }

    @Override
    public CmsResponse<GetPisAuthorisationResponse> getPisCancellationAuthorisationById(String cancellationId) {
        return pisAuthorisationService.getPisCancellationAuthorisationById(cancellationId);
    }

    @Override
    public CmsResponse<List<String>> getAuthorisationsByPaymentId(String encryptedPaymentId,
                                                                  PaymentAuthorisationType authorisationType) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedPaymentId);

        if (!decryptIdOptional.isPresent()) {
            log.info("Encrypted Payment ID: [{}]. Get payment authorisation list failed, couldn't decrypt consent id",
                     encryptedPaymentId);
            return CmsResponse.<List<String>>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return pisAuthorisationService.getAuthorisationsByPaymentId(decryptIdOptional.get(), authorisationType);
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

        return pisAuthorisationService.getAuthorisationScaStatus(decryptIdOptional.get(), authorisationId, authorisationType);

    }

    @Override
    public CmsResponse<Boolean> isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        return pisAuthorisationService.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods) {
        return pisAuthorisationService.saveAuthenticationMethods(authorisationId, methods);
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        return pisAuthorisationService.updateScaApproach(authorisationId, scaApproach);
    }

    @Override
    public CmsResponse<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId, PaymentAuthorisationType authorisationType) {
        return pisAuthorisationService.getAuthorisationScaApproach(authorisationId, authorisationType);
    }
}
