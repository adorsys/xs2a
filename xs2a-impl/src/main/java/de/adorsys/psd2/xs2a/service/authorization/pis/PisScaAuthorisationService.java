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
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.ScaApproachServiceTypeProvider;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;

import java.util.Optional;

public interface PisScaAuthorisationService extends ScaApproachServiceTypeProvider {
    /**
     * Creates authorisation for a payment
     *
     * @param createAuthorisationRequest create authorisation request
     * @param paymentType                Type of payment
     * @return create payment authorisation response
     */
    Optional<Xs2aCreatePisAuthorisationResponse> createCommonPaymentAuthorisation(Xs2aCreateAuthorisationRequest createAuthorisationRequest, PaymentType paymentType);

    /**
     * Updates authorisation for the payment
     *
     * @param request Provides transporting data when updating consent psu data
     * @return update payment authorisation response
     */
    Xs2aUpdatePisCommonPaymentPsuDataResponse updateCommonPaymentPsuData(PaymentAuthorisationParameters request);

    void updateAuthorisation(CommonAuthorisationParameters request, AuthorisationProcessorResponse response);

    void updateCancellationAuthorisation(CommonAuthorisationParameters request, AuthorisationProcessorResponse response);

    /**
     * Creates authorisation cancellation for the payment
     *
     * @param createAuthorisationRequest create Authorisation Request
     * @param paymentType                Type of payment
     * @return create payment cancellation authorisation response
     */
    Optional<Xs2aCreatePisCancellationAuthorisationResponse> createCommonPaymentCancellationAuthorisation(Xs2aCreateAuthorisationRequest createAuthorisationRequest, PaymentType paymentType);

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
    Xs2aUpdatePisCommonPaymentPsuDataResponse updateCommonPaymentCancellationPsuData(PaymentAuthorisationParameters request);

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
     * @param paymentId       ASPSP identifier of the payment, associated with the authorisation
     * @param authorisationId authorisation identifier
     * @return SCA status
     */
    Optional<ScaStatus> getCancellationAuthorisationScaStatus(String paymentId, String authorisationId);
}
