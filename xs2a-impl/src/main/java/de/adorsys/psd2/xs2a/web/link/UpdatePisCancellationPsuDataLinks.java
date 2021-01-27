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

import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;

public class UpdatePisCancellationPsuDataLinks extends AbstractLinks {

    private final ScaApproachResolver scaApproachResolver;

    public UpdatePisCancellationPsuDataLinks(String httpUrl, ScaApproachResolver scaApproachResolver,
                                             Xs2aUpdatePisCommonPaymentPsuDataRequest request, ScaStatus scaStatus,
                                             AuthenticationObject chosenScaMethod) {
        super(httpUrl);
        this.scaApproachResolver = scaApproachResolver;

        HrefType authorisationLink = buildAuthorisationLink(request);
        setScaStatus(authorisationLink);

        if (isScaStatusMethodAuthenticated(scaStatus)) {
            setSelectAuthenticationMethod(authorisationLink);
        } else if (isScaStatusMethodSelected(chosenScaMethod, scaStatus) || isDecoupledScaApproach(request)) {
            setAuthoriseTransaction(authorisationLink);
        } else if (isScaStatusMethodIdentified(scaStatus)) {
            setUpdatePsuAuthentication(authorisationLink);
        }
    }

    private HrefType buildAuthorisationLink(Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        return buildPath(UrlHolder.PIS_CANCELLATION_AUTH_LINK_URL, request.getPaymentService().getValue(),
                         request.getPaymentProduct(), request.getPaymentId(), request.getAuthorisationId());
    }

    private boolean isDecoupledScaApproach(Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        return scaApproachResolver.getScaApproach(request.getAuthorisationId()) == ScaApproach.DECOUPLED;
    }
}
