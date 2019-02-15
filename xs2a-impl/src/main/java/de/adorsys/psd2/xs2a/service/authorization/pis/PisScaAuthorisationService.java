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
import de.adorsys.psd2.xs2a.service.authorization.ScaApproachServiceTypeProvider;

import java.util.Optional;

public interface PisScaAuthorisationService extends ScaApproachServiceTypeProvider {
    /**
     * Creates authorisation for a payment
     *
     * @param paymentId   ASPSP identifier of a payment
     * @param paymentType Type of payment
     * @param psuData     PsuIdData container of authorisation data about PSU
     * @return create payment authorisation response
     */
    Optional<Xs2aCreatePisAuthorisationResponse> createCommonPaymentAuthorisation(String paymentId, PaymentType paymentType, PsuIdData psuData);

    /**
     * Updates authorisation for the payment
     *
     * @param request Provides transporting data when updating consent psu data
     * @return update payment authorisation response
     */
    Xs2aUpdatePisCommonPaymentPsuDataResponse updateCommonPaymentPsuData(Xs2aUpdatePisCommonPaymentPsuDataRequest request);

    /**
     * Creates authorisation cancellation for the payment
     *
     * @param paymentId   ASPSP identifier of a payment
     * @param paymentType Type of payment
     * @param psuData     PsuIdData container of authorisation data about PSU
     * @return create payment cancellation authorisation response
     */
    Optional<Xs2aCreatePisCancellationAuthorisationResponse> createCommonPaymentCancellationAuthorisation(String paymentId, PaymentType paymentType, PsuIdData psuData);

    /**
     * Gets authorisation cancellation sub resources
     *
     * @param paymentId ASPSP identifier of a payment
     * @return authorisation cancellation sub resources
     */
    Optional<Xs2aPaymentCancellationAuthorisationSubResource> getCancellationAuthorisationSubResources(String paymentId);

    /**
     * Updates cancellation authorisation for the payment
     *
     * @param request Provides transporting data when updating payment psu data
     * @return update consent authorisation response
     */
    Xs2aUpdatePisCommonPaymentPsuDataResponse updateCommonPaymentCancellationPsuData(Xs2aUpdatePisCommonPaymentPsuDataRequest request);

    /**
     * Gets authorisation sub resources
     *
     * @param paymentId ASPSP identifier of a payment
     * @return authorisation sub resources
     */
    Optional<Xs2aAuthorisationSubResources> getAuthorisationSubResources(String paymentId);

    /**
     * Gets SCA status of authorisation
     *
     * @param paymentId       ASPSP identifier of the payment, associated with the authorisation
     * @param authorisationId authorisation identifier
     * @return SCA status
     */
    Optional<ScaStatus> getAuthorisationScaStatus(String paymentId, String authorisationId);

    /**
     * Gets SCA status of cancellation authorisation
     *
     * @param paymentId      ASPSP identifier of the payment, associated with the authorisation
     * @param cancellationId cancellation authorisation identifier
     * @return SCA status
     */
    Optional<ScaStatus> getCancellationAuthorisationScaStatus(String paymentId, String cancellationId);
}
