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

import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.EMBEDDED;

public class UpdatePisAuthorisationLinks extends AbstractLinks {

    public UpdatePisAuthorisationLinks(String httpUrl, ScaApproachResolver scaApproachResolver,
                                       Xs2aUpdatePisCommonPaymentPsuDataResponse response,
                                       Xs2aCreatePisAuthorisationRequest createRequest) {
        super(httpUrl);

        ScaStatus scaStatus = response.getScaStatus();

        HrefType authorisationLink = buildAuthorisationLink(response, createRequest);
        setScaStatus(authorisationLink);

        if (isScaStatusMethodAuthenticated(scaStatus)) {
            setSelectAuthenticationMethod(authorisationLink);
        } else if (isScaStatusMethodSelected(response.getChosenScaMethod(), scaStatus) &&
                       scaApproachResolver.getScaApproach(response.getAuthorisationId()) == EMBEDDED) {
            setAuthoriseTransaction(authorisationLink);
        } else if (isScaStatusMethodIdentified(scaStatus)) {
            setUpdatePsuAuthentication(authorisationLink);
        }
    }

    private HrefType buildAuthorisationLink(Xs2aUpdatePisCommonPaymentPsuDataResponse response, Xs2aCreatePisAuthorisationRequest createRequest) {
        return buildPath(UrlHolder.PIS_AUTHORISATION_LINK_URL, createRequest.getPaymentService().getValue(),
                         createRequest.getPaymentProduct(), createRequest.getPaymentId(), response.getAuthorisationId());
    }
}
