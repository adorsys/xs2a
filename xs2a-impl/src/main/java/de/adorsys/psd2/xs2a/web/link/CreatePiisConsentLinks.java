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
import de.adorsys.psd2.xs2a.domain.consent.Xs2aConfirmationOfFundsResponse;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.link.holder.LinkParameters;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.*;

public class CreatePiisConsentLinks extends AbstractLinks {
    private final ScaApproachResolver scaApproachResolver;
    private final RedirectLinkBuilder redirectLinkBuilder;
    private final RedirectIdService redirectIdService;
    private final ScaRedirectFlow scaRedirectFlow;
    private final LinkParameters linkParameters;

    public CreatePiisConsentLinks(LinkParameters linkParameters, ScaApproachResolver scaApproachResolver,
                                  Xs2aConfirmationOfFundsResponse response, RedirectLinkBuilder redirectLinkBuilder,
                                  RedirectIdService redirectIdService, ScaRedirectFlow scaRedirectFlow) {
        super(linkParameters.getHttpUrl());
        this.scaApproachResolver = scaApproachResolver;
        this.redirectLinkBuilder = redirectLinkBuilder;
        this.redirectIdService = redirectIdService;
        this.scaRedirectFlow = scaRedirectFlow;
        this.linkParameters = linkParameters;

        buildConsentLinks(response);
    }

    private void buildConsentLinks(Xs2aConfirmationOfFundsResponse response) {

        String consentId = response.getConsentId();

        setSelf(buildPath(UrlHolder.PIIS_CONSENT_LINK_URL, consentId));
        setStatus(buildPath(UrlHolder.PIIS_CONSENT_STATUS_URL, consentId));

        String authorisationId = response.getAuthorizationId();
        ScaApproach scaApproach = authorisationId == null
                                      ? scaApproachResolver.resolveScaApproach()
                                      : scaApproachResolver.getScaApproach(authorisationId);

        if (scaApproach == EMBEDDED) {
            addEmbeddedRelatedLinks(consentId, authorisationId);
        } else if (scaApproach == REDIRECT) {
            addRedirectRelatedLinks(response, consentId, authorisationId);
        } else if (scaApproach == DECOUPLED) {
            addDecoupledRelatedLinks(consentId, authorisationId);
        }
    }

    private void addRedirectRelatedLinks(Xs2aConfirmationOfFundsResponse response, String consentId, String authorisationId) {
        if (linkParameters.isExplicitMethod()) {
            setStartAuthorisation(buildPath(UrlHolder.CREATE_PIIS_AUTHORISATION_URL, consentId));
        } else {
            String redirectId = redirectIdService.generateRedirectId(authorisationId);
            String consentOauthLink = scaRedirectFlow == ScaRedirectFlow.OAUTH
                                          ? redirectLinkBuilder.buildConsentScaOauthRedirectLink(consentId, redirectId, response.getInternalRequestId())
                                          : redirectLinkBuilder.buildConsentScaRedirectLink(consentId, redirectId, response.getInternalRequestId(), linkParameters.getInstanceId(), ConsentType.PIIS_TPP);

            setScaRedirect(new HrefType(consentOauthLink));
            setScaStatus(buildPath(UrlHolder.PIIS_AUTHORISATION_URL, consentId, authorisationId));

            if (linkParameters.isAuthorisationConfirmationRequestMandated()) {
                setConfirmation(buildPath(redirectLinkBuilder.buildConfirmationLink(consentId, redirectId, ConsentType.PIIS_TPP)));
            }
        }
    }

    private void addDecoupledRelatedLinks(String consentId, String authorisationId) {
        if (linkParameters.isExplicitMethod()) {
            setStartAuthorisation(buildPath(UrlHolder.CREATE_PIIS_AUTHORISATION_URL, consentId));
        } else {
            setScaStatus(buildPath(UrlHolder.PIIS_AUTHORISATION_URL, consentId, authorisationId));
        }
    }

    private void addEmbeddedRelatedLinks(String consentId, String authorizationId) {
        if (linkParameters.isExplicitMethod()) {
            if (linkParameters.isSigningBasketModeActive()) {
                setStartAuthorisation(buildPath(UrlHolder.CREATE_PIIS_AUTHORISATION_URL, consentId));
            } else {
                setStartAuthorisationWithPsuAuthentication(buildPath(UrlHolder.CREATE_PIIS_AUTHORISATION_URL, consentId));
            }
        } else {
            setScaStatus(buildPath(UrlHolder.PIIS_AUTHORISATION_URL, consentId, authorizationId));
            setUpdatePsuAuthentication(buildPath(UrlHolder.PIIS_AUTHORISATION_URL, consentId, authorizationId));
        }
    }
}
