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
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;

public class UpdatePisPsuDataLinks extends AbstractLinks {//NOSONAR

    private final ScaApproachResolver scaApproachResolver;

    public UpdatePisPsuDataLinks(String httpUrl, ScaApproachResolver scaApproachResolver,
                                 PaymentAuthorisationParameters request, ScaStatus scaStatus,
                                 AuthenticationObject chosenScaMethod) {
        super(httpUrl);
        this.scaApproachResolver = scaApproachResolver;

        HrefType authorisationLink = buildAuthorisationLink(request);
        setScaStatus(authorisationLink);

        if (isScaStatusMethodAuthenticated(scaStatus)) {
            setSelectAuthenticationMethod(authorisationLink);
        } else if (isScaStatusMethodSelected(chosenScaMethod, scaStatus) && isEmbeddedScaApproach(request.getAuthorisationId())) {
            setAuthoriseTransaction(authorisationLink);
        } else if (isScaStatusFinalised(scaStatus)) {
            setScaStatus(authorisationLink);
        } else if (isScaStatusMethodIdentified(scaStatus)) {
            setUpdatePsuAuthentication(authorisationLink);
        }
    }

    private HrefType buildAuthorisationLink(PaymentAuthorisationParameters request) {
        return buildPath(UrlHolder.PIS_AUTHORISATION_LINK_URL, request.getPaymentService().getValue(), request.getPaymentProduct(),
                         request.getPaymentId(), request.getAuthorisationId());
    }

    private boolean isEmbeddedScaApproach(String authorisationId) {
        return scaApproachResolver.getScaApproach(authorisationId) == ScaApproach.EMBEDDED;
    }

    private boolean isScaStatusFinalised(ScaStatus scaStatus) {
        return scaStatus == ScaStatus.FINALISED;
    }
}
