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

import java.util.EnumSet;

public class CreateConsentLinks extends AbstractLinks {

    public CreateConsentLinks(String httpUrl, ScaApproachResolver scaApproachResolver,
                              CreateConsentResponse response, RedirectLinkBuilder redirectLinkBuilder,
                              RedirectIdService redirectIdService,
                              boolean explicitMethod, boolean signingBasketModeActive,
                              ScaRedirectFlow scaRedirectFlow,
                              boolean authorisationConfirmationRequestMandated,
                              String instanceId) {
        super(httpUrl);

        String consentId = response.getConsentId();
        String authorisationId = response.getAuthorizationId();

        setSelf(buildPath(UrlHolder.CONSENT_LINK_URL, consentId));
        setStatus(buildPath(UrlHolder.CONSENT_STATUS_URL, consentId));

        ScaApproach scaApproach = authorisationId == null
                                      ? scaApproachResolver.resolveScaApproach()
                                      : scaApproachResolver.getScaApproach(authorisationId);

        if (EnumSet.of(ScaApproach.EMBEDDED, ScaApproach.DECOUPLED).contains(scaApproach)) {
            buildLinkForEmbeddedAndDecoupledScaApproach(consentId, authorisationId, explicitMethod, signingBasketModeActive);
        } else if (ScaApproach.REDIRECT == scaApproach) {
            if (explicitMethod) {
                setStartAuthorisation(buildPath(UrlHolder.CREATE_AIS_AUTHORISATION_URL, consentId));
            } else {
                String redirectId = redirectIdService.generateRedirectId(authorisationId);

                if (scaRedirectFlow == ScaRedirectFlow.OAUTH) {
                    setScaOAuth(new HrefType(redirectLinkBuilder.buildConsentScaOauthRedirectLink(consentId, redirectId, response.getInternalRequestId())));

                } else {
                    setScaRedirect(new HrefType(redirectLinkBuilder.buildConsentScaRedirectLink(consentId, redirectId, response.getInternalRequestId(), instanceId, ConsentType.AIS)));
                }

                setScaStatus(buildPath(UrlHolder.AIS_AUTHORISATION_URL, consentId, authorisationId));

                if (authorisationConfirmationRequestMandated) {
                    setConfirmation(buildPath(redirectLinkBuilder.buildConfirmationLink(consentId, redirectId, ConsentType.AIS)));
                }
            }
        }
    }

    private void buildLinkForEmbeddedAndDecoupledScaApproach(String consentId, String authorizationId,
                                                             boolean explicitMethod, boolean signingBasketModeActive) {
        if (explicitMethod) {
            if (signingBasketModeActive) {
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
}
