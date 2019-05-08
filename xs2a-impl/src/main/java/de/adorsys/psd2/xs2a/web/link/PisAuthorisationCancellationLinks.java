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
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.aspect.UrlHolder;

import java.util.EnumSet;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.*;

public class PisAuthorisationCancellationLinks extends AbstractLinks {

    private RedirectLinkBuilder redirectLinkBuilder;

    public PisAuthorisationCancellationLinks(String httpUrl, ScaApproachResolver scaApproachResolver, RedirectLinkBuilder redirectLinkBuilder,
                                             String paymentService, String paymentProduct, String paymentId, String authorizationId, PsuIdData psuData) {
        super(httpUrl);
        this.redirectLinkBuilder = redirectLinkBuilder;

        setSelf(buildPath(UrlHolder.PAYMENT_LINK_URL, paymentService, paymentProduct, paymentId));
        setStatus(buildPath(UrlHolder.PAYMENT_STATUS_URL, paymentService, paymentProduct, paymentId));

        ScaApproach cancellationScaApproach = scaApproachResolver.getCancellationScaApproach(authorizationId);
        if (EnumSet.of(EMBEDDED, DECOUPLED).contains(cancellationScaApproach)) {
            addEmbeddedDecoupledRelatedLinks(paymentService, paymentProduct, paymentId, authorizationId, psuData);
        } else if (cancellationScaApproach == REDIRECT) {
            addRedirectRelatedLinks(paymentService, paymentProduct, paymentId, authorizationId);
        } else if (cancellationScaApproach == OAUTH) {
            setScaOAuth("scaOAuth"); //TODO generate link for oauth https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/326
        }
    }

    private void addEmbeddedDecoupledRelatedLinks(String paymentService, String paymentProduct, String paymentId, String authorizationId, PsuIdData psuData) {
        String path = UrlHolder.PIS_CANCELLATION_AUTH_LINK_URL;
        if (psuData.isEmpty()) {
            setUpdatePsuIdentification(buildPath(path, paymentService, paymentProduct, paymentId, authorizationId));
        } else {
            setUpdatePsuAuthentication(buildPath(path, paymentService, paymentProduct, paymentId, authorizationId));
        }
    }

    private void addRedirectRelatedLinks(String paymentService, String paymentProduct, String paymentId, String authorizationId) {
        String link = redirectLinkBuilder.buildPaymentCancellationScaRedirectLink(paymentId, authorizationId);
        setScaRedirect(link);
        setScaStatus(buildPath(UrlHolder.PIS_CANCELLATION_AUTH_LINK_URL, paymentService, paymentProduct, paymentId, authorizationId));
    }
}
