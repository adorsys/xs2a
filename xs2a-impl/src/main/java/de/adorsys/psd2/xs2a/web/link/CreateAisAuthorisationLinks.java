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

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.link.holder.LinkParameters;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.EMBEDDED;
import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.REDIRECT;

public class CreateAisAuthorisationLinks extends AbstractLinks {
    public CreateAisAuthorisationLinks(LinkParameters linkParameters, CreateConsentAuthorizationResponse response,
                                       ScaApproachResolver scaApproachResolver, RedirectLinkBuilder redirectLinkBuilder,
                                       RedirectIdService redirectIdService, ScaRedirectFlow scaRedirectFlow) {
        super(linkParameters.getHttpUrl());

        String consentId = response.getConsentId();
        String authorisationId = response.getAuthorisationId();

        setScaStatus(buildPath(UrlHolder.AIS_AUTHORISATION_URL, consentId, authorisationId));

        ScaApproach initiationScaApproach = scaApproachResolver.getScaApproach(authorisationId);
        if (initiationScaApproach == REDIRECT) {
            String redirectId = redirectIdService.generateRedirectId(authorisationId);

            String consentOauthLink = scaRedirectFlow == ScaRedirectFlow.OAUTH
                                          ? redirectLinkBuilder.buildConsentScaOauthRedirectLink(consentId, redirectId, response.getInternalRequestId())
                                          : redirectLinkBuilder.buildConsentScaRedirectLink(consentId, redirectId, response.getInternalRequestId(), linkParameters.getInstanceId(), ConsentType.AIS);

            setScaRedirect(new HrefType(consentOauthLink));
            if (linkParameters.isAuthorisationConfirmationRequestMandated()) {
                setConfirmation(buildPath(redirectLinkBuilder.buildConfirmationLink(consentId, redirectId, ConsentType.AIS)));
            }

        } else if (initiationScaApproach == EMBEDDED) {
            setUpdatePsuAuthentication(buildPath(UrlHolder.AIS_AUTHORISATION_URL, consentId, authorisationId));
        }
    }
}
