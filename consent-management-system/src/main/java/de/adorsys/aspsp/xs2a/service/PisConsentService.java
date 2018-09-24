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

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.consent.api.CmsConsentStatus;
import de.adorsys.aspsp.xs2a.consent.api.CmsScaMethod;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.CreatePisConsentAuthorisationResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PisConsent;
import de.adorsys.aspsp.xs2a.domain.pis.PisConsentAuthorization;
import de.adorsys.aspsp.xs2a.repository.PisConsentAuthorizationRepository;
import de.adorsys.aspsp.xs2a.repository.PisConsentRepository;
import de.adorsys.aspsp.xs2a.repository.PisPaymentDataRepository;
import de.adorsys.aspsp.xs2a.service.mapper.PisConsentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

import static de.adorsys.aspsp.xs2a.consent.api.CmsConsentStatus.RECEIVED;
import static de.adorsys.aspsp.xs2a.consent.api.CmsConsentStatus.VALID;
import static de.adorsys.aspsp.xs2a.consent.api.CmsScaStatus.SCAMETHODSELECTED;
import static de.adorsys.aspsp.xs2a.consent.api.CmsScaStatus.STARTED;

@Service
@RequiredArgsConstructor
public class PisConsentService {
    private final PisConsentRepository pisConsentRepository;
    private final PisConsentMapper pisConsentMapper;
    private final PisConsentAuthorizationRepository pisConsentAuthorizationRepository;
    private final PisPaymentDataRepository pisPaymentDataRepository;

    /**
     * Creates new pis consent with full information about payment
     *
     * @param request Consists information about payments.
     * @return Response containing identifier of consent
     */
    public Optional<CreatePisConsentResponse> createPaymentConsent(PisConsentRequest request) {
        return pisConsentMapper.mapToPisConsent(request)
                   .map(pisConsentRepository::save)
                   .map(r -> new CreatePisConsentResponse(r.getExternalId(), r.getPayments().iterator().next().getPaymentId()));
    }

    /**
     * Retrieves consent status from pis consent by consent identifier
     *
     * @param consentId String representation of pis consent identifier
     * @return Information about the status of a consent
     */
    public Optional<CmsConsentStatus> getConsentStatusById(String consentId) {
        return getPisConsentById(consentId)
                   .map(PisConsent::getConsentStatus);
    }

    /**
     * Reads full information of pis consent by consent identifier
     *
     * @param consentId String representation of pis consent identifier
     * @return Response containing full information about pis consent
     */
    public Optional<PisConsentResponse> getConsentById(String consentId) {
        return getPisConsentById(consentId)
                   .flatMap(pisConsentMapper::mapToPisConsentResponse);
    }

    /**
     * Updates pis consent status by consent identifier
     *
     * @param consentId String representation of pis consent identifier
     * @param status    new consent status
     * @return Response containing result of status changing
     */
    public Optional<Boolean> updateConsentStatusById(String consentId, CmsConsentStatus status) {
        return getActualPisConsent(consentId)
                   .map(con -> setStatusAndSaveConsent(con, status))
                   .map(con -> con.getConsentStatus() == status);
    }

    private Optional<PisConsent> getPisConsentById(String consentId) {
        return Optional.ofNullable(consentId)
                   .flatMap(pisConsentRepository::findByExternalId);
    }

    private PisConsent setStatusAndSaveConsent(PisConsent consent, CmsConsentStatus status) {
        consent.setConsentStatus(status);
        return pisConsentRepository.save(consent);
    }

    private Optional<PisConsent> getActualPisConsent(String consentId) {
        return Optional.ofNullable(consentId)
                   .flatMap(c -> pisConsentRepository.findByExternalIdAndConsentStatusIn(consentId, EnumSet.of(RECEIVED, VALID)));
    }

    /**
     * Create consent authorization
     *
     * @param paymentId
     * @return String authorization id
     */
    @Transactional
    public Optional<CreatePisConsentAuthorisationResponse> createAuthorization(String paymentId) {
        return pisPaymentDataRepository.findByPaymentIdAndConsent_ConsentStatus(paymentId, RECEIVED)
                   .map(pisConsent -> saveNewAuthorization(pisConsent.getConsent()))
                   .map(c -> new CreatePisConsentAuthorisationResponse(c.getExternalId()));
    }

    private PisConsentAuthorization saveNewAuthorization(PisConsent pisConsent) {
        PisConsentAuthorization consentAuthorization = new PisConsentAuthorization();
        consentAuthorization.setExternalId(UUID.randomUUID().toString());
        consentAuthorization.setConsent(pisConsent);
        consentAuthorization.setScaStatus(STARTED);
        return pisConsentAuthorizationRepository.save(consentAuthorization);
    }

    public Optional<UpdatePisConsentPsuDataResponse> updateConsentAuthorization(String authorizationId, UpdatePisConsentPsuDataRequest request) {
        return pisConsentAuthorizationRepository.findByExternalId(authorizationId)
                   .map(p -> {
                       if (STARTED == p.getScaStatus()) {
                           p.getConsent()
                               .setAspspConsentData(request.getCmsAspspConsentData().getBody());
                       }
                       if (SCAMETHODSELECTED == request.getScaStatus()) {
                           String chosenMethod = request.getAuthenticationMethodId();
                           if (StringUtils.isEmpty(chosenMethod)) {
                               return null;
                           }
                           p.setChosenScaMethod(CmsScaMethod.valueOf(request.getAuthenticationMethodId()));
                       }
                       p.setScaStatus(request.getScaStatus());
                       return pisConsentAuthorizationRepository.save(p);
                   }).map(pisConsentMapper::mapToUpdatePisConsentPsuDataResponse);
    }

    public Optional<GetPisConsentAuthorisationResponse> getPisConsentAuthorizationById(String authorizationId) {
        return pisConsentAuthorizationRepository.findByExternalId(authorizationId)
                   .map(pisConsentMapper::mapToGetPisConsentAuthorizationResponse);
    }
}
