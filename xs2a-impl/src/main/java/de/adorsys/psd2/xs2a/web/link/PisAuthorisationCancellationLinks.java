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
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.aspect.UrlHolder;

import java.util.EnumSet;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.*;

public class PisAuthorisationCancellationLinks extends AbstractLinks {

    private RedirectLinkBuilder redirectLinkBuilder;
    private ScaRedirectFlow scaRedirectFlow;
    private RedirectIdService redirectIdService;

    public PisAuthorisationCancellationLinks(String httpUrl, ScaApproachResolver scaApproachResolver, RedirectLinkBuilder redirectLinkBuilder,
                                             RedirectIdService redirectIdService,
                                             String paymentService, String paymentProduct, String paymentId,
                                             String authorisationId, ScaRedirectFlow scaRedirectFlow, String internalRequestId) {
        super(httpUrl);
        this.redirectLinkBuilder = redirectLinkBuilder;
        this.scaRedirectFlow = scaRedirectFlow;
        this.redirectIdService = redirectIdService;

        setScaStatus(buildPath(UrlHolder.PIS_CANCELLATION_AUTH_LINK_URL, paymentService, paymentProduct, paymentId, authorisationId));

        ScaApproach cancellationScaApproach = scaApproachResolver.getCancellationScaApproach(authorisationId);
        if (EnumSet.of(EMBEDDED, DECOUPLED).contains(cancellationScaApproach)) {
            setUpdatePsuAuthentication(buildPath(UrlHolder.PIS_CANCELLATION_AUTH_LINK_URL, paymentService, paymentProduct, paymentId, authorisationId));
        } else if (cancellationScaApproach == REDIRECT) {
            addRedirectRelatedLinks(paymentService, paymentProduct, paymentId, authorisationId, internalRequestId);
        } else if (cancellationScaApproach == OAUTH) {
            setScaOAuth(new HrefType("scaOAuth")); //TODO generate link for oauth https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/326
        }
    }

    private void addRedirectRelatedLinks(String paymentService, String paymentProduct, String paymentId, String authorizationId, String internalRequestId) {
        String redirectId = redirectIdService.generateRedirectId(authorizationId);
        setScaRedirectOAuthLink(scaRedirectFlow, redirectLinkBuilder.buildPaymentCancellationScaRedirectLink(paymentId, redirectId, internalRequestId));

        setScaStatus(buildPath(UrlHolder.PIS_CANCELLATION_AUTH_LINK_URL, paymentService, paymentProduct, paymentId, authorizationId));
    }
}
