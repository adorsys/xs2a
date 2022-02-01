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
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.link.holder.LinkParameters;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.EMBEDDED;
import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.REDIRECT;

public class PisAuthorisationCancellationLinks extends AbstractLinks {//NOSONAR

    private final RedirectLinkBuilder redirectLinkBuilder;
    private final ScaRedirectFlow scaRedirectFlow;
    private final RedirectIdService redirectIdService;
    private final LinkParameters linkParameters;

    public PisAuthorisationCancellationLinks(LinkParameters linkParameters,
                                             ScaApproachResolver scaApproachResolver,
                                             RedirectLinkBuilder redirectLinkBuilder,
                                             RedirectIdService redirectIdService,
                                             ScaRedirectFlow scaRedirectFlow) {
        super(linkParameters.getHttpUrl());
        this.redirectLinkBuilder = redirectLinkBuilder;
        this.scaRedirectFlow = scaRedirectFlow;
        this.redirectIdService = redirectIdService;
        this.linkParameters = linkParameters;

        setScaStatus(buildPath(UrlHolder.PIS_CANCELLATION_AUTH_LINK_URL, linkParameters.getPaymentService(),
            linkParameters.getPaymentProduct(), linkParameters.getPaymentId(), linkParameters.getAuthorisationId()));

        ScaApproach cancellationScaApproach = scaApproachResolver.getScaApproach(linkParameters.getAuthorisationId());
        if (cancellationScaApproach == EMBEDDED) {
            setUpdatePsuAuthentication(buildPath(UrlHolder.PIS_CANCELLATION_AUTH_LINK_URL,
                linkParameters.getPaymentService(), linkParameters.getPaymentProduct(),
                linkParameters.getPaymentId(), linkParameters.getAuthorisationId()));
        } else if (cancellationScaApproach == REDIRECT) {
            addRedirectRelatedLinks(linkParameters.getPaymentId(), linkParameters.getAuthorisationId(), linkParameters.getInternalRequestId());
        }
    }

    private void addRedirectRelatedLinks(String paymentId, String authorizationId, String internalRequestId) {
        String redirectId = redirectIdService.generateRedirectId(authorizationId);

        String paymentCancellationOauthLink = scaRedirectFlow == ScaRedirectFlow.OAUTH
                                                  ? redirectLinkBuilder.buildPaymentCancellationScaOauthRedirectLink(paymentId, redirectId, internalRequestId)
                                                  : redirectLinkBuilder.buildPaymentCancellationScaRedirectLink(paymentId, redirectId, internalRequestId, linkParameters.getInstanceId());

        setScaRedirect(new HrefType(paymentCancellationOauthLink));

    }
}
