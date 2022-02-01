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

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.link.holder.LinkParameters;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.EMBEDDED;
import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.REDIRECT;

public class CreatePisAuthorisationLinks extends AbstractLinks {
    public CreatePisAuthorisationLinks(LinkParameters linkParameters, ScaApproachResolver scaApproachResolver, RedirectLinkBuilder redirectLinkBuilder,
                                       RedirectIdService redirectIdService,
                                       Xs2aCreatePisAuthorisationRequest createRequest, ScaRedirectFlow scaRedirectFlow) {
        super(linkParameters.getHttpUrl());

        String paymentId = createRequest.getPaymentId();
        String paymentService = createRequest.getPaymentService().getValue();
        String paymentProduct = createRequest.getPaymentProduct();

        setScaStatus(buildPath(UrlHolder.PIS_AUTHORISATION_LINK_URL, paymentService, paymentProduct, paymentId, linkParameters.getAuthorisationId()));

        ScaApproach initiationScaApproach = scaApproachResolver.getScaApproach(linkParameters.getAuthorisationId());
        if (initiationScaApproach == EMBEDDED) {
            String path = UrlHolder.PIS_AUTHORISATION_LINK_URL;
            setUpdatePsuAuthentication(buildPath(path, paymentService, paymentProduct, paymentId, linkParameters.getAuthorisationId()));
        } else if (initiationScaApproach == REDIRECT) {
            String redirectId = redirectIdService.generateRedirectId(linkParameters.getAuthorisationId());

            String paymentOauthLink = scaRedirectFlow == ScaRedirectFlow.OAUTH
                                          ? redirectLinkBuilder.buildPaymentScaOauthRedirectLink(paymentId, redirectId,
                linkParameters.getInternalRequestId())
                                          : redirectLinkBuilder.buildPaymentScaRedirectLink(paymentId, redirectId,
                linkParameters.getInternalRequestId(), linkParameters.getInstanceId());

            setScaRedirect(new HrefType(paymentOauthLink));

            if (linkParameters.isAuthorisationConfirmationRequestMandated()) {
                setConfirmation(buildPath(redirectLinkBuilder.buildPisConfirmationLink(paymentService, paymentProduct, paymentId, redirectId)));
            }
        }
    }
}
