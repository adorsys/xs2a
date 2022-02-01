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
