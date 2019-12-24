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

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.service.PisAuthorisationServiceEncrypted;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PisEndpointAccessCheckerService extends EndpointAccessChecker {
    private final PisAuthorisationServiceEncrypted pisAuthorisationServiceEncrypted;
    private final AspspProfileServiceWrapper aspspProfileService;
    /**
     * Checks whether endpoint is accessible for current authorisation
     *
     * @param authorisationId   ID of authorisation process
     * @param authorisationType payment initiation or cancellation
     * @param confirmationCodeReceived   true if confirmationCode was received in request body
     * @return <code>true</code> if accessible. <code>false</code> otherwise.
     */
    public boolean isEndpointAccessible(String authorisationId, PaymentAuthorisationType authorisationType, boolean confirmationCodeReceived) {
        boolean confirmationCodeCase = confirmationCodeReceived
                                           && aspspProfileService.isAuthorisationConfirmationRequestMandated();

        CmsResponse<GetPisAuthorisationResponse> authorisationResponse = null;
        if (authorisationType == PaymentAuthorisationType.CREATED) {
            authorisationResponse = pisAuthorisationServiceEncrypted.getPisAuthorisationById(authorisationId);
        } else if (authorisationType == PaymentAuthorisationType.CANCELLED) {
            authorisationResponse = pisAuthorisationServiceEncrypted.getPisCancellationAuthorisationById(authorisationId);
        }

        return Optional.ofNullable(authorisationResponse)
                   .map(CmsResponse::getPayload)
                   .map(p -> isAccessible(p.getChosenScaApproach(), p.getScaStatus(), confirmationCodeCase))
                   .orElse(true);
    }
}
