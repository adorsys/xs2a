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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PisAuthorisationServiceInternalEncrypted implements PisAuthorisationServiceEncrypted {
    private final SecurityDataService securityDataService;
    private final PisAuthorisationService pisAuthorisationService;

    @Override
    @Transactional
    public Optional<CreatePisAuthorisationResponse> createAuthorization(String encryptedPaymentId, CreatePisAuthorisationRequest request) {
        return securityDataService.decryptId(encryptedPaymentId)
                   .flatMap(id -> pisAuthorisationService.createAuthorization(id, request));
    }

    @Override
    @Transactional
    public Optional<CreatePisAuthorisationResponse> createAuthorizationCancellation(String encryptedPaymentId,
                                                                                    CreatePisAuthorisationRequest request) {
        return securityDataService.decryptId(encryptedPaymentId)
                   .flatMap(id -> pisAuthorisationService.createAuthorizationCancellation(id, request));
    }

    @Override
    @Transactional
    public Optional<UpdatePisCommonPaymentPsuDataResponse> updatePisAuthorisation(String authorisationId,
                                                                                  UpdatePisCommonPaymentPsuDataRequest request) {
        return pisAuthorisationService.updatePisAuthorisation(authorisationId, request);
    }

    @Override
    @Transactional
    public boolean updatePisAuthorisationStatus(String authorisationId, ScaStatus scaStatus) {
        return pisAuthorisationService.updatePisAuthorisationStatus(authorisationId, scaStatus);
    }

    @Override
    @Transactional
    public Optional<UpdatePisCommonPaymentPsuDataResponse> updatePisCancellationAuthorisation(String authorisationId,
                                                                                              UpdatePisCommonPaymentPsuDataRequest request) {
        return pisAuthorisationService.updatePisCancellationAuthorisation(authorisationId, request);
    }

    @Override
    public Optional<GetPisAuthorisationResponse> getPisAuthorisationById(String authorisationId) {
        return pisAuthorisationService.getPisAuthorisationById(authorisationId);
    }

    @Override
    public Optional<GetPisAuthorisationResponse> getPisCancellationAuthorisationById(String cancellationId) {
        return pisAuthorisationService.getPisCancellationAuthorisationById(cancellationId);
    }

    @Override
    public Optional<List<String>> getAuthorisationsByPaymentId(String encryptedPaymentId,
                                                               PaymentAuthorisationType authorisationType) {
        return securityDataService.decryptId(encryptedPaymentId)
                   .flatMap(id -> pisAuthorisationService.getAuthorisationsByPaymentId(id, authorisationType));
    }

    @Override
    @Transactional
    public Optional<ScaStatus> getAuthorisationScaStatus(String encryptedPaymentId, String authorisationId, PaymentAuthorisationType authorisationType) {
        return securityDataService.decryptId(encryptedPaymentId)
                   .flatMap(id -> pisAuthorisationService.getAuthorisationScaStatus(id, authorisationId, authorisationType));
    }

    @Override
    public boolean isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        return pisAuthorisationService.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
    }

    @Override
    @Transactional
    public boolean saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods) {
        return pisAuthorisationService.saveAuthenticationMethods(authorisationId, methods);
    }

    @Override
    @Transactional
    public boolean updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        return pisAuthorisationService.updateScaApproach(authorisationId, scaApproach);
    }

    @Override
    public Optional<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId, PaymentAuthorisationType authorisationType) {
        return pisAuthorisationService.getAuthorisationScaApproach(authorisationId, authorisationType);
    }
}
