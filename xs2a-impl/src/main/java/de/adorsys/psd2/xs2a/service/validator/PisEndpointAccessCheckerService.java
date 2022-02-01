/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PisEndpointAccessCheckerService extends EndpointAccessChecker {
    private final AuthorisationServiceEncrypted authorisationServiceEncrypted;
    private final AspspProfileServiceWrapper aspspProfileService;
    /**
     * Checks whether endpoint is accessible for current authorisation
     *
     * @param authorisationId   ID of authorisation process
     * @param confirmationCodeReceived   true if confirmationCode was received in request body
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

        CmsResponse<Authorisation> authorisationResponse = authorisationServiceEncrypted.getAuthorisationById(authorisationId);

        return Optional.ofNullable(authorisationResponse)
                   .map(CmsResponse::getPayload)
                   .map(p -> isAccessible(p.getChosenScaApproach(), p.getScaStatus(), confirmationCodeCase))
                   .orElse(true);
    }
}
