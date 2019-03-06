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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.domain.pis.PaymentAuthorisationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PisEndpointAccessCheckerService extends EndpointAccessChecker {
    private final PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;

    /**
     * Checks whether endpoint is accessible for current authorisation
     *
     * @param authorisationId ID of authorisation process
     * @param authorisationType payment initiation or cancellation
     * @return <code>true</code> if accessible. <code>false</code> otherwise.
     */
    public boolean isEndpointAccessible(String authorisationId, PaymentAuthorisationType authorisationType) {
        Optional<GetPisAuthorisationResponse> authorisationResponse = Optional.empty();
        if (authorisationType == PaymentAuthorisationType.INITIATION) {
            authorisationResponse = pisCommonPaymentServiceEncrypted.getPisAuthorisationById(authorisationId);
        } else if (authorisationType == PaymentAuthorisationType.CANCELLATION) {
            authorisationResponse = pisCommonPaymentServiceEncrypted.getPisCancellationAuthorisationById(authorisationId);
        }

        return authorisationResponse
                   .map(p -> isAccessible(p.getChosenScaApproach(), p.getScaStatus()))
                   .orElse(true);
    }
}
