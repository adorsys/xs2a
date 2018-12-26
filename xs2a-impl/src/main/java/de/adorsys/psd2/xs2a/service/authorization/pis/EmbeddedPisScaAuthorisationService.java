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
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthorisationSubResources;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPaymentCancellationAuthorisationSubResource;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class EmbeddedPisScaAuthorisationService implements PisScaAuthorisationService {
    private final PisAuthorisationService authorisationService;
    private final Xs2aPisCommonPaymentMapper pisCommonPaymentMapper;

    /**
     * Creates authorization for pis consent
     *
     * @param paymentId   ASPSP identifier of a payment
     * @param paymentType Type of payment
     * @param psuData     PsuIdData container of authorisation data about PSU
     * @return create consent authorization response, which contains authorization id, sca status, payment type and links
     */
    @Override
    public Optional<Xs2aCreatePisAuthorisationResponse> createCommonPaymentAuthorisation(String paymentId, PaymentType paymentType, PsuIdData psuData) {
        return pisCommonPaymentMapper.mapToXsa2CreatePisAuthorizationResponse(authorisationService.createPisAuthorisation(paymentId, psuData), paymentType);
    }

    /**
     * Updates authorization for pis consent
     *
     * @param request Provides transporting data when updating consent psu data
     * @return update consent authorization response, which contains payment id, authorization id, sca status, psu message and links
     */
    @Override
    public Xs2aUpdatePisCommonPaymentPsuDataResponse updateCommonPaymentPsuData(Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        return authorisationService.updatePisAuthorisation(request);
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
    public Optional<Xs2aCreatePisCancellationAuthorisationResponse> createCommonPaymentCancellationAuthorisation(String paymentId, PaymentType paymentType, PsuIdData psuData) {
        return pisCommonPaymentMapper.mapToXs2aCreatePisCancellationAuthorisationResponse(authorisationService.createPisAuthorisationCancellation(paymentId, psuData), paymentType);
    }

    /**
     * Gets authorization cancellation sub resources
     *
     * @param paymentId ASPSP identifier of a payment
     * @return authorization cancellation sub resources
     */
    @Override
    public Optional<Xs2aPaymentCancellationAuthorisationSubResource> getCancellationAuthorisationSubResources(String paymentId) {
        return authorisationService.getCancellationAuthorisationSubResources(paymentId)
                   .map(Xs2aPaymentCancellationAuthorisationSubResource::new);
    }

    /**
     * Updates cancellation authorisation for pis consent
     *
     * @param request Provides transporting data when updating consent psu data
     * @return update consent authorisation response, which contains payment id, authorisation id, sca status, psu message and links
     */
    @Override
    public Xs2aUpdatePisCommonPaymentPsuDataResponse updateCommonPaymentCancellationPsuData(Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        return authorisationService.updatePisCancellationAuthorisation(request);
    }

    /**
     * Gets authorisation sub resources
     *
     * @param paymentId ASPSP identifier of a payment
     * @return authorization sub resources
     */
    @Override
    public Optional<Xs2aAuthorisationSubResources> getAuthorisationSubResources(String paymentId) {
        return authorisationService.getAuthorisationSubResources(paymentId)
                   .map(Xs2aAuthorisationSubResources::new);
    }

    /**
     * Gets SCA status of authorisation
     *
     * @param paymentId       ASPSP identifier of the payment, associated with the authorisation
     * @param authorisationId authorisation identifier
     * @return SCA status
     */
    @Override
    public Optional<ScaStatus> getAuthorisationScaStatus(String paymentId, String authorisationId) {
        return authorisationService.getAuthorisationScaStatus(paymentId, authorisationId);
    }

    /**
     * Gets SCA status of cancellation authorisation
     *
     * @param paymentId      ASPSP identifier of the payment, associated with the authorisation
     * @param cancellationId cancellation authorisation identifier
     * @return SCA status
     */
    @Override
    public Optional<ScaStatus> getCancellationAuthorisationScaStatus(String paymentId, String cancellationId) {
        return authorisationService.getCancellationAuthorisationScaStatus(paymentId, cancellationId);
    }
}
