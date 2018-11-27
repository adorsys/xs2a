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

package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisConsentCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPaymentAuthorisationSubResource;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPaymentCancellationAuthorisationSubResource;
import de.adorsys.psd2.xs2a.domain.consent.Xsa2CreatePisConsentAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class RedirectPisScaAuthorisationService implements PisScaAuthorisationService {
    private final PisAuthorisationService authorisationService;
    private final Xs2aPisConsentMapper pisConsentMapper;

    /**
     * Creates authorization for pis consent
     *
     * @param paymentId   ASPSP identifier of a payment
     * @param paymentType Type of payment
     * @param psuData     PsuIdData container of authorisation data about PSU
     * @return create consent authorization response, which contains authorization id, sca status, payment type and links
     */
    @Override
    public Optional<Xsa2CreatePisConsentAuthorisationResponse> createConsentAuthorisation(String paymentId, PaymentType paymentType, PsuIdData psuData) {
        return pisConsentMapper.mapToXsa2CreatePisConsentAuthorizationResponse(authorisationService.createPisConsentAuthorisation(paymentId, psuData), paymentType);
    }

    /**
     * Updates authorization for pis consent
     *
     * @param request Provides transporting data when updating consent psu data
     * @return update consent authorization response, which contains payment id, authorization id, sca status, psu message and links
     */
    @Override
    public Xs2aUpdatePisConsentPsuDataResponse updateConsentPsuData(Xs2aUpdatePisConsentPsuDataRequest request) {
        return authorisationService.updatePisConsentAuthorisation(request);
    }

    /**
     * Creates authorization cancellation for pis consent
     *
     * @param paymentId   ASPSP identifier of a payment
     * @param paymentType Type of payment
     * @param psuData     PsuIdData container of authorisation data about PSU
     * @return
     */
    @Override
    public Optional<Xs2aCreatePisConsentCancellationAuthorisationResponse> createConsentCancellationAuthorisation(String paymentId, PaymentType paymentType, PsuIdData psuData) {
        return pisConsentMapper.mapToXs2aCreatePisConsentCancellationAuthorisationResponse(authorisationService.createPisConsentAuthorisationCancellation(paymentId, psuData), paymentType);
    }

    /**
     * Gets authorization cancellation sub resources
     *
     * @param paymentId ASPSP identifier of a payment
     * @return authorization cancellation sub resources
     */
    @Override
    public Optional<Xs2aPaymentCancellationAuthorisationSubResource> getCancellationAuthorisationSubResources(String paymentId) {
        return pisConsentMapper.mapToXs2aPaymentCancellationAuthorisationSubResource(authorisationService.getCancellationAuthorisationSubResources(paymentId));
    }

    /**
     * Updates cancellation authorisation for pis consent
     *
     * @param request Provides transporting data when updating consent psu data
     * @return update consent authorisation response, which contains payment id, authorisation id, sca status, psu message and links
     */
    @Override
    public Xs2aUpdatePisConsentPsuDataResponse updateConsentCancellationPsuData(Xs2aUpdatePisConsentPsuDataRequest request) {
        return null;
    }

    /**
     * Gets authorisation sub resources
     *
     * @param paymentId ASPSP identifier of a payment
     * @return authorisation sub resources
     */
    @Override
    public Optional<Xs2aPaymentAuthorisationSubResource> getAuthorisationSubResources(String paymentId) {
        String authorisationSubResources = authorisationService.getAuthorisationSubResources(paymentId);
        Xs2aPaymentAuthorisationSubResource xs2aPaymentAuthorisationSubResource = pisConsentMapper.mapToXs2aPaymentAuthorisationSubResource(authorisationSubResources);
        return Optional.of(xs2aPaymentAuthorisationSubResource);
    }
}
