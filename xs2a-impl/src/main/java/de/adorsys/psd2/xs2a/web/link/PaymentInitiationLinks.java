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
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.link.holder.LinkParameters;

import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.RJCT;

public class PaymentInitiationLinks extends AbstractLinks {//NOSONAR

    private final ScaApproachResolver scaApproachResolver;
    private final RedirectLinkBuilder redirectLinkBuilder;
    private final RedirectIdService redirectIdService;
    private final ScaRedirectFlow scaRedirectFlow;
    private final LinkParameters linkParameters;

    public PaymentInitiationLinks(LinkParameters linkParameters, ScaApproachResolver scaApproachResolver, RedirectLinkBuilder redirectLinkBuilder,
                                  RedirectIdService redirectIdService,
                                  PaymentInitiationParameters paymentRequestParameters, PaymentInitiationResponse body,
                                  ScaRedirectFlow scaRedirectFlow) {
        super(linkParameters.getHttpUrl());
        this.scaApproachResolver = scaApproachResolver;
        this.redirectLinkBuilder = redirectLinkBuilder;
        this.redirectIdService = redirectIdService;
        this.linkParameters = linkParameters;
        this.scaRedirectFlow = scaRedirectFlow;

        buildPaymentLinks(paymentRequestParameters, body, linkParameters.isSigningBasketModeActive());
    }

    private void buildPaymentLinks(PaymentInitiationParameters paymentRequestParameters, PaymentInitiationResponse body, boolean signingBasketModeActive) {
        if (RJCT == body.getTransactionStatus()) {
            return;
        }
        String paymentService = paymentRequestParameters.getPaymentType().getValue();
        String paymentProduct = paymentRequestParameters.getPaymentProduct();
        String paymentId = body.getPaymentId();
        String authorisationId = body.getAuthorizationId();
        String internalRequestId = body.getInternalRequestId();

        setSelf(buildPath(UrlHolder.PAYMENT_LINK_URL, paymentService, paymentProduct, paymentId));
        setStatus(buildPath(UrlHolder.PAYMENT_STATUS_URL, paymentService, paymentProduct, paymentId));

        ScaApproach scaApproach = authorisationId == null ?
                                      scaApproachResolver.resolveScaApproach()
                                      : scaApproachResolver.getScaApproach(authorisationId);

        if (scaApproach == ScaApproach.EMBEDDED) {
            addEmbeddedRelatedLinks(paymentService, paymentProduct, paymentId, authorisationId, signingBasketModeActive);
        } else if (scaApproach == ScaApproach.REDIRECT) {
            addRedirectRelatedLinks(paymentService, paymentProduct, paymentId, authorisationId, internalRequestId);
        } else if (scaApproach == ScaApproach.DECOUPLED){
            addDecoupledRelatedLinks(paymentService, paymentProduct, paymentId, authorisationId);
        }
    }

    private void addDecoupledRelatedLinks(String paymentService, String paymentProduct, String paymentId,
                                          String authorisationId) {
        if (linkParameters.isExplicitMethod()) {
            setStartAuthorisation(buildPath(UrlHolder.START_PIS_AUTHORISATION_URL, paymentService, paymentProduct, paymentId));
        } else {
            setScaStatus(buildPath(UrlHolder.PIS_AUTHORISATION_LINK_URL, paymentService, paymentProduct, paymentId, authorisationId));
        }
    }

    private void addEmbeddedRelatedLinks(String paymentService, String paymentProduct, String paymentId,
                                         String authorisationId, boolean signingBasketModeActive) {
        if (linkParameters.isExplicitMethod()) {
            if (signingBasketModeActive) { // no more data needs to be updated
                setStartAuthorisation(buildPath(UrlHolder.START_PIS_AUTHORISATION_URL, paymentService, paymentProduct, paymentId));
            } else {
                setStartAuthorisationWithPsuAuthentication(buildPath(UrlHolder.START_PIS_AUTHORISATION_URL, paymentService, paymentProduct, paymentId));
            }
        } else {
            setScaStatus(
                buildPath(UrlHolder.PIS_AUTHORISATION_LINK_URL, paymentService, paymentProduct, paymentId, authorisationId));
            setUpdatePsuAuthentication(
                buildPath(UrlHolder.PIS_AUTHORISATION_LINK_URL, paymentService, paymentProduct, paymentId, authorisationId));
        }
    }

    private void addRedirectRelatedLinks(String paymentService, String paymentProduct, String paymentId, String authorisationId, String internalRequestId) {
        if (linkParameters.isExplicitMethod()) {
            setStartAuthorisation(buildPath(UrlHolder.START_PIS_AUTHORISATION_URL, paymentService, paymentProduct, paymentId));
        } else {
            String redirectId = redirectIdService.generateRedirectId(authorisationId);

            String paymentOauthLink = scaRedirectFlow == ScaRedirectFlow.OAUTH
                                          ? redirectLinkBuilder.buildPaymentScaOauthRedirectLink(paymentId, redirectId, internalRequestId)
                                          : redirectLinkBuilder.buildPaymentScaRedirectLink(paymentId, redirectId, internalRequestId, linkParameters.getInstanceId());

            setScaRedirect(new HrefType(paymentOauthLink));
            setScaStatus(
                buildPath(UrlHolder.PIS_AUTHORISATION_LINK_URL, paymentService, paymentProduct, paymentId, authorisationId));

            if (linkParameters.isAuthorisationConfirmationRequestMandated()) {
                setConfirmation(buildPath(redirectLinkBuilder.buildPisConfirmationLink(paymentService, paymentProduct, paymentId, redirectId)));
            }
        }
    }
}
