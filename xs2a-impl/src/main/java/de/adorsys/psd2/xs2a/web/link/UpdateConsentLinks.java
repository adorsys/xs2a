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
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.aspect.UrlHolder;

public class UpdateConsentLinks extends AbstractLinks {

    public UpdateConsentLinks(String httpUrl, ScaApproachResolver scaApproachResolver, UpdateConsentPsuDataReq request) {
        super(httpUrl);

        String consentId = request.getConsentId();
        String authorizationId = request.getAuthorizationId();
        ScaStatus scaStatus = request.getScaStatus();

        setSelf(buildPath(UrlHolder.CONSENT_LINK_URL, consentId));
        setStatus(buildPath(UrlHolder.CONSENT_STATUS_URL, consentId));

        if (scaStatus == ScaStatus.PSUAUTHENTICATED) {
            setSelectAuthenticationMethod(buildPath(UrlHolder.AIS_AUTHORISATION_URL, consentId, authorizationId));
        } else if (scaStatus == ScaStatus.SCAMETHODSELECTED) {
            ScaApproach scaApproach = scaApproachResolver.getInitiationScaApproach(authorizationId);
            if (scaApproach == ScaApproach.DECOUPLED) {
                setScaStatus(buildPath(UrlHolder.AIS_AUTHORISATION_URL, consentId, authorizationId));
            } else {
                setAuthoriseTransaction(buildPath(UrlHolder.AIS_AUTHORISATION_URL, consentId, authorizationId));
            }
        } else if (scaStatus == ScaStatus.FINALISED) {
            setScaStatus(buildPath(UrlHolder.AIS_AUTHORISATION_URL, consentId, authorizationId));
        } else if (scaStatus == ScaStatus.PSUIDENTIFIED) {
            setStartAuthorisationWithPsuAuthentication(buildPath(UrlHolder.AIS_AUTHORISATION_URL, consentId, authorizationId));
        }
    }
}
