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

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.aspect.UrlHolder;

import java.util.EnumSet;

public class CreateConsentLinks extends AbstractLinks {

    public CreateConsentLinks(String httpUrl, ScaApproachResolver scaApproachResolver,
                              CreateConsentResponse response, RedirectLinkBuilder redirectLinkBuilder,
                              boolean isExplicitMethod, PsuIdData psuData) {
        super(httpUrl);

        String consentId = response.getConsentId();
        String authorizationId = response.getAuthorizationId();

        setSelf(buildPath(UrlHolder.CONSENT_LINK_URL, consentId));
        setStatus(buildPath(UrlHolder.CONSENT_STATUS_URL, consentId));

        String authorisationId = response.getAuthorizationId();
        ScaApproach scaApproach = authorisationId == null
                                      ? scaApproachResolver.resolveScaApproach()
                                      : scaApproachResolver.getInitiationScaApproach(authorisationId);

        if (EnumSet.of(ScaApproach.EMBEDDED, ScaApproach.DECOUPLED).contains(scaApproach)) {
            buildLinkForEmbeddedAndDecoupledScaApproach(response, psuData, consentId, authorizationId, isExplicitMethod);
        } else if (ScaApproach.REDIRECT == scaApproach) {
            if (isExplicitMethod) {
                setStartAuthorisation(buildPath(UrlHolder.CREATE_AIS_AUTHORISATION_URL, consentId));
            } else {
                setScaRedirect(redirectLinkBuilder.buildConsentScaRedirectLink(consentId, authorizationId));
                setScaStatus(buildPath(UrlHolder.AIS_AUTHORISATION_URL, consentId, authorizationId));
            }
        }
    }

    private void buildLinkForEmbeddedAndDecoupledScaApproach(CreateConsentResponse response, PsuIdData psuData,
                                                             String consentId, String authorizationId,
                                                             boolean isExplicitMethod) {
        if (isExplicitMethod) {
            // TODO refactor isSigningBasketSupported https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/811
            boolean isSigningBasketSupported = !response.isMultilevelScaRequired();

            if (isSigningBasketSupported) { // no more data needs to be updated
                setStartAuthorisation(buildPath(UrlHolder.CREATE_AIS_AUTHORISATION_URL, consentId));
            } else if (psuData.isEmpty()) {
                setStartAuthorisationWithPsuIdentification(buildPath(UrlHolder.CREATE_AIS_AUTHORISATION_URL, consentId));
            } else {
                setStartAuthorisationWithPsuAuthentication(buildPath(UrlHolder.CREATE_AIS_AUTHORISATION_URL, consentId));
            }
        } else {
            setScaStatus(buildPath(UrlHolder.AIS_AUTHORISATION_URL, consentId, authorizationId));
            if (psuData.isEmpty()) {
                setUpdatePsuIdentification(
                    buildPath(UrlHolder.AIS_AUTHORISATION_URL, consentId, authorizationId));
            } else {
                setUpdatePsuAuthentication(
                    buildPath(UrlHolder.AIS_AUTHORISATION_URL, consentId, authorizationId));
            }
        }
    }
}
