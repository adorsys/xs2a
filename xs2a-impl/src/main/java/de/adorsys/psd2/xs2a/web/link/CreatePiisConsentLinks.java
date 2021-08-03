/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
