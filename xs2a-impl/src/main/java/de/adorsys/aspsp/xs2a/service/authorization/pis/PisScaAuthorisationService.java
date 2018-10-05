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

package de.adorsys.aspsp.xs2a.service.authorization.pis;

import de.adorsys.aspsp.xs2a.domain.consent.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.domain.consent.Xsa2CreatePisConsentAuthorisationResponse;
import de.adorsys.aspsp.xs2a.domain.consent.Xsa2CreatePisConsentCancellationAuthorisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentType;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;

import java.util.Optional;

public interface PisScaAuthorisationService {
    Optional<Xsa2CreatePisConsentAuthorisationResponse> createConsentAuthorisation(String paymentId, PaymentType paymentType);

    Optional<Xs2aUpdatePisConsentPsuDataResponse> updateConsentPsuData(UpdatePisConsentPsuDataRequest request);

    default Optional<Xsa2CreatePisConsentCancellationAuthorisationResponse> createConsentCancellationAuthorisation(String paymentId, PaymentType paymentType) {
        Optional<Xsa2CreatePisConsentAuthorisationResponse> consentAuthorisation = createConsentAuthorisation(paymentId, paymentType);

        if (!consentAuthorisation.isPresent()) {
            return Optional.empty();
        }

        Xsa2CreatePisConsentAuthorisationResponse csar = consentAuthorisation.get();
        Xsa2CreatePisConsentCancellationAuthorisationResponse cscar = new Xsa2CreatePisConsentCancellationAuthorisationResponse(csar.getAuthorizationId(), csar.getScaStatus(), csar.getPaymentType(), csar.getLinks());

        return Optional.of(cscar);
    }
}
