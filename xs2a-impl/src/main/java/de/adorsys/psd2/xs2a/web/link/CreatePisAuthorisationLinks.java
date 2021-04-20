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
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.link.holder.LinkParameters;

import java.util.EnumSet;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.*;

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
        if (EnumSet.of(EMBEDDED, DECOUPLED).contains(initiationScaApproach)) {
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
