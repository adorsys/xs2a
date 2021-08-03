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

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentResponse;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.link.holder.LinkParameters;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.*;

public class CreateConsentLinks extends AbstractLinks {
    private final ScaApproachResolver scaApproachResolver;
    private final RedirectLinkBuilder redirectLinkBuilder;
    private final RedirectIdService redirectIdService;
    private final ScaRedirectFlow scaRedirectFlow;
    private final LinkParameters linkParameters;

    public CreateConsentLinks(LinkParameters linkParameters, ScaApproachResolver scaApproachResolver,
                              CreateConsentResponse response, RedirectLinkBuilder redirectLinkBuilder,
                              RedirectIdService redirectIdService,
                              ScaRedirectFlow scaRedirectFlow) {
        super(linkParameters.getHttpUrl());
        this.scaApproachResolver = scaApproachResolver;
        this.redirectLinkBuilder = redirectLinkBuilder;
        this.redirectIdService = redirectIdService;
        this.linkParameters = linkParameters;
        this.scaRedirectFlow = scaRedirectFlow;

        buildConsentLinks(response);
    }

    private void buildConsentLinks(CreateConsentResponse response) {

        String consentId = response.getConsentId();
        String authorisationId = response.getAuthorizationId();

        setSelf(buildPath(UrlHolder.CONSENT_LINK_URL, consentId));
        setStatus(buildPath(UrlHolder.CONSENT_STATUS_URL, consentId));

        ScaApproach scaApproach = authorisationId == null
                                      ? scaApproachResolver.resolveScaApproach()
                                      : scaApproachResolver.getScaApproach(authorisationId);

        if (scaApproach == EMBEDDED) {
            buildLinkForEmbeddedScaApproach(consentId, authorisationId);
        } else if (scaApproach == REDIRECT ) {
            buildLinkForRedirectScaApproach(consentId, authorisationId, response);
        } else if (scaApproach == DECOUPLED) {
            buildLinkForDecoupledScaApproach(consentId, authorisationId);
        }
    }

    private void buildLinkForEmbeddedScaApproach(String consentId, String authorizationId) {
        if (linkParameters.isExplicitMethod()) {
            if (linkParameters.isSigningBasketModeActive()) {
                setStartAuthorisation(buildPath(UrlHolder.CREATE_AIS_AUTHORISATION_URL, consentId));
            } else {
                setStartAuthorisationWithPsuAuthentication(buildPath(UrlHolder.CREATE_AIS_AUTHORISATION_URL, consentId));
            }
        } else {
            setScaStatus(buildPath(UrlHolder.AIS_AUTHORISATION_URL, consentId, authorizationId));
            setUpdatePsuAuthentication(
                buildPath(UrlHolder.AIS_AUTHORISATION_URL, consentId, authorizationId));
        }
    }

    private void buildLinkForRedirectScaApproach(String consentId, String authorisationId, CreateConsentResponse response) {
        if (linkParameters.isExplicitMethod()) {
            setStartAuthorisation(buildPath(UrlHolder.CREATE_AIS_AUTHORISATION_URL, consentId));
        } else {
            String redirectId = redirectIdService.generateRedirectId(authorisationId);

            String consentOauthLink = scaRedirectFlow == ScaRedirectFlow.OAUTH
                                          ? redirectLinkBuilder.buildConsentScaOauthRedirectLink(consentId, redirectId, response.getInternalRequestId())
                                          : redirectLinkBuilder.buildConsentScaRedirectLink(consentId, redirectId, response.getInternalRequestId(), linkParameters.getInstanceId(), ConsentType.AIS);

            setScaRedirect(new HrefType(consentOauthLink));
            setScaStatus(buildPath(UrlHolder.AIS_AUTHORISATION_URL, consentId, authorisationId));

            if (linkParameters.isAuthorisationConfirmationRequestMandated()) {
                setConfirmation(buildPath(redirectLinkBuilder.buildConfirmationLink(consentId, redirectId, ConsentType.AIS)));
            }
        }
    }

    private void buildLinkForDecoupledScaApproach(String consentId, String authorisationId) {
        if (linkParameters.isExplicitMethod()) {
            setStartAuthorisation(buildPath(UrlHolder.CREATE_AIS_AUTHORISATION_URL, consentId));
        } else {
            setScaStatus(buildPath(UrlHolder.AIS_AUTHORISATION_URL, consentId, authorisationId));
        }
    }
}
