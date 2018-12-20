/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentResponse;
import de.adorsys.psd2.consent.api.service.PisConsentService;
import de.adorsys.psd2.consent.api.service.PisConsentServiceEncrypted;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
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
public class PisConsentServiceInternalEncrypted implements PisConsentServiceEncrypted {
    private final SecurityDataService securityDataService;
    private final PisConsentService pisConsentService;

    @Override
    @Transactional
    public Optional<CreatePisConsentResponse> createPaymentConsent(PisConsentRequest request) {
        return pisConsentService.createPaymentConsent(request)
                   .map(CreatePisConsentResponse::getConsentId)
                   .flatMap(securityDataService::encryptId)
                   .map(CreatePisConsentResponse::new);
    }

    @Override
    public Optional<ConsentStatus> getConsentStatusById(String encryptedConsentId) {
        return securityDataService.decryptId(encryptedConsentId)
                   .flatMap(pisConsentService::getConsentStatusById);
    }

    @Override
    public Optional<PisConsentResponse> getConsentById(String encryptedConsentId) {
        return securityDataService.decryptId(encryptedConsentId)
                   .flatMap(pisConsentService::getConsentById);
    }

    @Override
    @Transactional
    public Optional<Boolean> updateConsentStatusById(String encryptedConsentId, ConsentStatus status) {
        return securityDataService.decryptId(encryptedConsentId)
                   .flatMap(id -> pisConsentService.updateConsentStatusById(id, status));
    }

    @Override
    public Optional<String> getDecryptedId(String encryptedId) {
        return securityDataService.decryptId(encryptedId);
    }

    @Override
    @Transactional
    public Optional<CreatePisConsentAuthorisationResponse> createAuthorization(String encryptedPaymentId,
                                                                               CmsAuthorisationType authorisationType,
                                                                               PsuIdData psuData) {
        return securityDataService.decryptId(encryptedPaymentId)
                   .flatMap(id -> pisConsentService.createAuthorization(id, authorisationType, psuData));
    }

    @Override
    @Transactional
    public Optional<CreatePisConsentAuthorisationResponse> createAuthorizationCancellation(String encryptedPaymentId,
                                                                                           CmsAuthorisationType authorisationType,
                                                                                           PsuIdData psuData) {
        return securityDataService.decryptId(encryptedPaymentId)
                   .flatMap(id -> pisConsentService.createAuthorizationCancellation(id, authorisationType, psuData));
    }

    @Override
    @Transactional
    public Optional<UpdatePisConsentPsuDataResponse> updateConsentAuthorisation(String authorisationId,
                                                                                UpdatePisConsentPsuDataRequest request) {
        return pisConsentService.updateConsentAuthorisation(authorisationId, request);
    }

    @Override
    @Transactional
    public Optional<UpdatePisConsentPsuDataResponse> updateConsentCancellationAuthorisation(String authorisationId,
                                                                                            UpdatePisConsentPsuDataRequest request) {
        return pisConsentService.updateConsentCancellationAuthorisation(authorisationId, request);
    }

    @Override
    @Transactional
    public void updatePaymentConsent(PisConsentRequest request, String encryptedConsentId) {
        securityDataService.decryptId(encryptedConsentId)
            .ifPresent(id -> pisConsentService.updatePaymentConsent(request, id));
    }

    @Override
    public Optional<GetPisConsentAuthorisationResponse> getPisConsentAuthorisationById(String authorisationId) {
        return pisConsentService.getPisConsentAuthorisationById(authorisationId);
    }

    @Override
    public Optional<GetPisConsentAuthorisationResponse> getPisConsentCancellationAuthorisationById(String cancellationId) {
        return pisConsentService.getPisConsentCancellationAuthorisationById(cancellationId);
    }

    @Override
    public Optional<List<String>> getAuthorisationsByPaymentId(String encryptedPaymentId,
                                                               CmsAuthorisationType authorisationType) {
        return securityDataService.decryptId(encryptedPaymentId)
                   .flatMap(id -> pisConsentService.getAuthorisationsByPaymentId(id, authorisationType));
    }

    @Override
    public Optional<ScaStatus> getAuthorisationScaStatus(String encryptedPaymentId, String authorisationId, CmsAuthorisationType authorisationType) {
        return securityDataService.decryptId(encryptedPaymentId)
                   .flatMap(id -> pisConsentService.getAuthorisationScaStatus(id, authorisationId, authorisationType));
    }

    @Override
    public Optional<PsuIdData> getPsuDataByPaymentId(String encryptedPaymentId) {
        return securityDataService.decryptId(encryptedPaymentId)
                   .flatMap(pisConsentService::getPsuDataByPaymentId);
    }

    @Override
    public Optional<PsuIdData> getPsuDataByConsentId(String encryptedConsentId) {
        return securityDataService.decryptId(encryptedConsentId)
                   .flatMap(pisConsentService::getPsuDataByConsentId);
    }
}
