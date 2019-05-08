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
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.aspect.UrlHolder;

import java.util.EnumSet;

import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.RJCT;
import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.*;

public class PaymentInitiationLinks extends AbstractLinks {

    private ScaApproachResolver scaApproachResolver;
    private RedirectLinkBuilder redirectLinkBuilder;
    private boolean isExplicitMethod;

    public PaymentInitiationLinks(String httpUrl, ScaApproachResolver scaApproachResolver, RedirectLinkBuilder redirectLinkBuilder,
                                  PaymentInitiationParameters paymentRequestParameters, PaymentInitiationResponse body,
                                  boolean isExplicitMethod) {
        super(httpUrl);
        this.scaApproachResolver = scaApproachResolver;
        this.redirectLinkBuilder = redirectLinkBuilder;
        this.isExplicitMethod = isExplicitMethod;

        buildPaymentLinks(paymentRequestParameters, body);
    }

    private void buildPaymentLinks(PaymentInitiationParameters paymentRequestParameters, PaymentInitiationResponse body) {
        if (RJCT == body.getTransactionStatus()) {
            return;
        }
        String paymentService = paymentRequestParameters.getPaymentType().getValue();
        String paymentProduct = paymentRequestParameters.getPaymentProduct();
        String paymentId = body.getPaymentId();

        setSelf(buildPath(UrlHolder.PAYMENT_LINK_URL, paymentService, paymentProduct, paymentId));
        setStatus(buildPath(UrlHolder.PAYMENT_STATUS_URL, paymentService, paymentProduct, paymentId));

        ScaApproach scaApproach = scaApproachResolver.resolveScaApproach();
        if (EnumSet.of(EMBEDDED, DECOUPLED).contains(scaApproach)) {
            addEmbeddedDecoupledRelatedLinks(paymentRequestParameters, body);
        } else if (scaApproach == REDIRECT) {
            addRedirectRelatedLinks(paymentRequestParameters, body);
        } else if (scaApproach == OAUTH) {
            setScaOAuth("scaOAuth"); //TODO generate link for oauth https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/326
        }
    }

    private void addEmbeddedDecoupledRelatedLinks(PaymentInitiationParameters paymentRequestParameters, PaymentInitiationResponse body) {
        String paymentService = paymentRequestParameters.getPaymentType().getValue();
        String paymentProduct = paymentRequestParameters.getPaymentProduct();
        String paymentId = body.getPaymentId();
        String authorizationId = body.getAuthorizationId();

        if (isExplicitMethod) {
            // TODO refactor isSigningBasketSupported https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/811
            boolean isSigningBasketSupported = !body.isMultilevelScaRequired();

            if (isSigningBasketSupported) { // no more data needs to be updated
                setStartAuthorisation(buildPath(UrlHolder.START_PIS_AUTHORISATION_URL, paymentService, paymentProduct, paymentId));

            } else if (paymentRequestParameters.getPsuData().isEmpty()) {
                setStartAuthorisationWithPsuIdentification(buildPath(UrlHolder.START_PIS_AUTHORISATION_URL, paymentService, paymentProduct, paymentId));
            } else {
                setStartAuthorisationWithPsuAuthentication(buildPath(UrlHolder.START_PIS_AUTHORISATION_URL, paymentService, paymentProduct, paymentId));
            }
        } else {
            setScaStatus(
                buildPath(UrlHolder.PIS_AUTHORISATION_LINK_URL, paymentService, paymentProduct, paymentId, authorizationId));
            if (paymentRequestParameters.getPsuData().isEmpty()) {
                setUpdatePsuIdentification(
                    buildPath(UrlHolder.PIS_AUTHORISATION_LINK_URL, paymentService, paymentProduct, paymentId, authorizationId));
            } else {
                setUpdatePsuAuthentication(
                    buildPath(UrlHolder.PIS_AUTHORISATION_LINK_URL, paymentService, paymentProduct, paymentId, authorizationId));
            }
        }
    }

    private void addRedirectRelatedLinks(PaymentInitiationParameters paymentRequestParameters, PaymentInitiationResponse body) {
        String paymentService = paymentRequestParameters.getPaymentType().getValue();
        String paymentProduct = paymentRequestParameters.getPaymentProduct();
        String paymentId = body.getPaymentId();
        String authorisationId = body.getAuthorizationId();

        if (isExplicitMethod) {
            setStartAuthorisation(buildPath(UrlHolder.START_PIS_AUTHORISATION_URL, paymentService, paymentProduct, paymentId));
        } else {
            String scaRedirectLink = redirectLinkBuilder.buildPaymentScaRedirectLink(body.getPaymentId(), authorisationId);
            setScaRedirect(scaRedirectLink);
            setScaStatus(
                buildPath(UrlHolder.PIS_AUTHORISATION_LINK_URL, paymentService, paymentProduct, paymentId, authorisationId));
        }
    }
}
