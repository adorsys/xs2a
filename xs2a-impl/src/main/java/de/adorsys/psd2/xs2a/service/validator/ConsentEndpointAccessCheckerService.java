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

import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsentEndpointAccessCheckerService extends EndpointAccessChecker {
    private final Xs2aAuthorisationService xs2aAuthorisationService;
    private final AspspProfileServiceWrapper aspspProfileService;

    /**
     * Checks whether endpoint is accessible for current authorisation
     *
     * @param authorisationId          ID of authorisation process
     * @param confirmationCodeReceived true if confirmationCode was received in request body
     * @return <code>true</code> if accessible. <code>false</code> otherwise.
     */
    public boolean isEndpointAccessible(String authorisationId, boolean confirmationCodeReceived) {
        boolean authorisationConfirmationRequestMandated = aspspProfileService.isAuthorisationConfirmationRequestMandated();
        if (aspspProfileService.getScaRedirectFlow() == ScaRedirectFlow.OAUTH
                && authorisationConfirmationRequestMandated) {
            return true;
        }
        boolean confirmationCodeCase = confirmationCodeReceived
                                           && authorisationConfirmationRequestMandated;

        return xs2aAuthorisationService.getAuthorisationById(authorisationId)
                   .map(a -> isAccessible(a.getChosenScaApproach(), a.getScaStatus(), confirmationCodeCase))
                   .orElse(true);
    }
}
