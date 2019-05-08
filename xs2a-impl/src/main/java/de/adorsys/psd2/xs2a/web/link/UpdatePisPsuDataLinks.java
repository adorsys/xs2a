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
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.aspect.UrlHolder;

public class UpdatePisPsuDataLinks extends AbstractLinks {

    private ScaApproachResolver scaApproachResolver;

    public UpdatePisPsuDataLinks(String httpUrl, ScaApproachResolver scaApproachResolver,
                                 Xs2aUpdatePisCommonPaymentPsuDataRequest request, ScaStatus scaStatus,
                                 Xs2aAuthenticationObject chosenScaMethod) {
        super(httpUrl);
        this.scaApproachResolver = scaApproachResolver;

        setSelf(buildPath(UrlHolder.PAYMENT_LINK_URL, request.getPaymentService(), request.getPaymentProduct(), request.getPaymentId()));
        setStatus(buildPath(UrlHolder.PAYMENT_STATUS_URL, request.getPaymentService(), request.getPaymentProduct(), request.getPaymentId()));

        if (isScaStatusMethodAuthenticated(scaStatus)) {
            setSelectAuthenticationMethod(buildAuthorisationLink(request));

            // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/722
        } else if (isScaStatusMethodSelected(chosenScaMethod, scaStatus) && isEmbeddedScaApproach(request.getAuthorisationId())) {
            setAuthoriseTransaction(buildAuthorisationLink(request));
        } else if (isScaStatusFinalised(scaStatus)) {
            setScaStatus(buildAuthorisationLink(request));
        } else if (isScaStatusMethodIdentified(scaStatus)) {
            setUpdatePsuAuthentication(buildAuthorisationLink(request));
        }
    }

    private String buildAuthorisationLink(Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        return buildPath(UrlHolder.PIS_AUTHORISATION_LINK_URL, request.getPaymentService(), request.getPaymentProduct(),
                         request.getPaymentId(), request.getAuthorisationId());
    }

    private boolean isEmbeddedScaApproach(String authorisationId) {
        return scaApproachResolver.getInitiationScaApproach(authorisationId) == ScaApproach.EMBEDDED;
    }

    private boolean isScaStatusFinalised(ScaStatus scaStatus) {
        return scaStatus == ScaStatus.FINALISED;
    }

    private boolean isScaStatusMethodSelected(Xs2aAuthenticationObject chosenScaMethod, ScaStatus scaStatus) {
        return chosenScaMethod != null
                   && scaStatus == ScaStatus.SCAMETHODSELECTED;
    }

    private boolean isScaStatusMethodAuthenticated(ScaStatus scaStatus) {
        return scaStatus == ScaStatus.PSUAUTHENTICATED;
    }

    private boolean isScaStatusMethodIdentified(ScaStatus scaStatus) {
        return scaStatus == ScaStatus.PSUIDENTIFIED;
    }
}
