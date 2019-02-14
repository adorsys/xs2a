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

import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentService;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PisCommonPaymentServiceInternalEncrypted implements PisCommonPaymentServiceEncrypted {
    private final SecurityDataService securityDataService;
    private final PisCommonPaymentService pisCommonPaymentService;

    @Override
    @Transactional
    public Optional<CreatePisCommonPaymentResponse> createCommonPayment(PisPaymentInfo request) {
        return pisCommonPaymentService.createCommonPayment(request)
                   .map(CreatePisCommonPaymentResponse::getPaymentId)
                   .flatMap(securityDataService::encryptId)
                   .map(CreatePisCommonPaymentResponse::new);
    }

    @Override
    @Transactional
    public Optional<TransactionStatus> getPisCommonPaymentStatusById(String encryptedPaymentId) {
        return securityDataService.decryptId(encryptedPaymentId)
                   .flatMap(pisCommonPaymentService::getPisCommonPaymentStatusById);
    }

    @Override
    @Transactional
    public Optional<PisCommonPaymentResponse> getCommonPaymentById(String encryptedPaymentId) {
        return securityDataService.decryptId(encryptedPaymentId)
                   .flatMap(pisCommonPaymentService::getCommonPaymentById);
    }

    @Override
    @Transactional
    public Optional<Boolean> updateCommonPaymentStatusById(String encryptedPaymentId, TransactionStatus status) {
        return securityDataService.decryptId(encryptedPaymentId)
                   .flatMap(id -> pisCommonPaymentService.updateCommonPaymentStatusById(id, status));
    }

    @Override
    public Optional<String> getDecryptedId(String encryptedId) {
        return securityDataService.decryptId(encryptedId);
    }

    @Override
    @Transactional
    public Optional<CreatePisAuthorisationResponse> createAuthorization(String encryptedPaymentId,
                                                                        CmsAuthorisationType authorisationType,
                                                                        PsuIdData psuData) {
        return securityDataService.decryptId(encryptedPaymentId)
                   .flatMap(id -> pisCommonPaymentService.createAuthorization(id, authorisationType, psuData));
    }

    @Override
    @Transactional
    public Optional<CreatePisAuthorisationResponse> createAuthorizationCancellation(String encryptedPaymentId,
                                                                                    CmsAuthorisationType authorisationType,
                                                                                    PsuIdData psuData) {
        return securityDataService.decryptId(encryptedPaymentId)
                   .flatMap(id -> pisCommonPaymentService.createAuthorizationCancellation(id, authorisationType, psuData));
    }

    @Override
    @Transactional
    public Optional<UpdatePisCommonPaymentPsuDataResponse> updatePisAuthorisation(String authorisationId,
                                                                                  UpdatePisCommonPaymentPsuDataRequest request) {
        return pisCommonPaymentService.updatePisAuthorisation(authorisationId, request);
    }

    @Override
    @Transactional
    public Optional<UpdatePisCommonPaymentPsuDataResponse> updatePisCancellationAuthorisation(String authorisationId,
                                                                                              UpdatePisCommonPaymentPsuDataRequest request) {
        return pisCommonPaymentService.updatePisCancellationAuthorisation(authorisationId, request);
    }

    @Override
    @Transactional
    public void updateCommonPayment(PisCommonPaymentRequest request, String encryptedPaymentId) {
        securityDataService.decryptId(encryptedPaymentId)
            .ifPresent(id -> pisCommonPaymentService.updateCommonPayment(request, id));
    }

    @Override
    public Optional<GetPisAuthorisationResponse> getPisAuthorisationById(String authorisationId) {
        return pisCommonPaymentService.getPisAuthorisationById(authorisationId);
    }

    @Override
    public Optional<GetPisAuthorisationResponse> getPisCancellationAuthorisationById(String cancellationId) {
        return pisCommonPaymentService.getPisCancellationAuthorisationById(cancellationId);
    }

    @Override
    public Optional<List<String>> getAuthorisationsByPaymentId(String encryptedPaymentId,
                                                               CmsAuthorisationType authorisationType) {
        return securityDataService.decryptId(encryptedPaymentId)
                   .flatMap(id -> pisCommonPaymentService.getAuthorisationsByPaymentId(id, authorisationType));
    }

    @Override
    @Transactional
    public Optional<ScaStatus> getAuthorisationScaStatus(String encryptedPaymentId, String authorisationId, CmsAuthorisationType authorisationType) {
        return securityDataService.decryptId(encryptedPaymentId)
                   .flatMap(id -> pisCommonPaymentService.getAuthorisationScaStatus(id, authorisationId, authorisationType));
    }

    @Override
    public Optional<List<PsuIdData>> getPsuDataListByPaymentId(String encryptedPaymentId) {
        return securityDataService.decryptId(encryptedPaymentId)
                   .flatMap(pisCommonPaymentService::getPsuDataListByPaymentId);
    }
}
