/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;

import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.RJCT;

public abstract class AbstractPaymentLink<T> extends AbstractLinkAspect<T> {
    private final AuthorisationMethodDecider authorisationMethodDecider;
    private final RedirectLinkBuilder redirectLinkBuilder;

    public AbstractPaymentLink(ScaApproachResolver scaApproachResolver, MessageService messageService, AuthorisationMethodDecider authorisationMethodDecider, RedirectLinkBuilder redirectLinkBuilder) {
        super(scaApproachResolver, messageService);
        this.authorisationMethodDecider = authorisationMethodDecider;
        this.redirectLinkBuilder = redirectLinkBuilder;
    }

    @SuppressWarnings("unchecked")
    protected ResponseObject<?> enrichLink(ResponseObject<?> result, PaymentInitiationParameters paymentRequestParameters) {
        Object body = result.getBody();
        doEnrichLink(paymentRequestParameters, (PaymentInitiationResponse) body);

        return result;
    }

    private void doEnrichLink(PaymentInitiationParameters paymentRequestParameters, PaymentInitiationResponse body) {
        body.setLinks(buildPaymentLinks(paymentRequestParameters, body));
    }

    private Links buildPaymentLinks(PaymentInitiationParameters paymentRequestParameters, PaymentInitiationResponse body) {
        if (RJCT == body.getTransactionStatus()) {
            return null;
        }
        String paymentService = paymentRequestParameters.getPaymentType().getValue();
        String paymentProduct = paymentRequestParameters.getPaymentProduct();
        String paymentId = body.getPaymentId();

        Links links = new Links();
        links.setSelf(buildPath("/v1/{payment-service}/{payment-product}/{payment-id}", paymentService, paymentProduct, paymentId));
        links.setStatus(buildPath("/v1/{payment-service}/{payment-product}/{payment-id}/status", paymentService, paymentProduct, paymentId));

        if (scaApproachResolver.resolveScaApproach() == ScaApproach.EMBEDDED) {
            return addEmbeddedRelatedLinks(links, paymentRequestParameters, body);
        } else if (scaApproachResolver.resolveScaApproach() == ScaApproach.REDIRECT) {
            return addRedirectRelatedLinks(links, paymentRequestParameters, body);
        } else if (scaApproachResolver.resolveScaApproach() == ScaApproach.OAUTH) {
            links.setScaOAuth("scaOAuth"); //TODO generate link for oauth https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/326
        }
        return links;
    }

    private Links addEmbeddedRelatedLinks(Links links, PaymentInitiationParameters paymentRequestParameters, PaymentInitiationResponse body) {
        String paymentService = paymentRequestParameters.getPaymentType().getValue();
        String paymentProduct = paymentRequestParameters.getPaymentProduct();
        String paymentId = body.getPaymentId();
        String authorizationId = body.getAuthorizationId();

        if (authorisationMethodDecider.isExplicitMethod(paymentRequestParameters.isTppExplicitAuthorisationPreferred(), body.isMultilevelScaRequired())) {
            links.setStartAuthorisation(buildPath("/v1/{payment-service}/{payment-product}/{payment-id}/authorisations", paymentService, paymentProduct, paymentId));
        } else {
            links.setScaStatus(
                buildPath("/v1/{payment-service}/{payment-product}/{payment-id}/authorisations/{authorisation-id}", paymentService, paymentProduct, paymentId, authorizationId));
            links.setStartAuthorisationWithPsuAuthentication(
                buildPath("/v1/{payment-service}/{payment-product}/{payment-id}/authorisations/{authorisation-id}", paymentService, paymentProduct, paymentId, authorizationId));
        }

        return links;
    }

    private Links addRedirectRelatedLinks(Links links, PaymentInitiationParameters paymentRequestParameters, PaymentInitiationResponse body) {
        String paymentService = paymentRequestParameters.getPaymentType().getValue();
        String paymentProduct = paymentRequestParameters.getPaymentProduct();
        String paymentId = body.getPaymentId();
        String authorisationId = body.getAuthorizationId();

        if (authorisationMethodDecider.isExplicitMethod(paymentRequestParameters.isTppExplicitAuthorisationPreferred(), body.isMultilevelScaRequired())) {
            links.setStartAuthorisation(buildPath("/v1/{payment-service}/{payment-product}/{payment-id}/authorisations", paymentService, paymentProduct, paymentId));
        } else {
            String scaRedirectLink = redirectLinkBuilder.buildPaymentScaRedirectLink(body.getPaymentId(), authorisationId);
            links.setScaRedirect(scaRedirectLink);
            links.setScaStatus(
                buildPath("/v1/{payment-service}/{payment-product}/{payment-id}/authorisations/{authorisation-id}", paymentService, paymentProduct, paymentId, authorisationId));
        }
        return links;
    }
}
