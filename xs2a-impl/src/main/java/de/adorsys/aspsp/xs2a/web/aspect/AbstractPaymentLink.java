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

package de.adorsys.aspsp.xs2a.web.aspect;

import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.service.authorization.AuthorisationMethodService;
import de.adorsys.aspsp.xs2a.service.message.MessageService;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.aspsp.profile.domain.ScaApproach;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.List;

import static de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus.RJCT;
import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.PERIODIC;
import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.SINGLE;

public abstract class AbstractPaymentLink<T> extends AbstractLinkAspect<T> {
    private AuthorisationMethodService authorisationMethodService;

    public AbstractPaymentLink(AspspProfileServiceWrapper aspspProfileService, MessageService messageService, AuthorisationMethodService authorisationMethodService) {
        super(aspspProfileService, messageService);
        this.authorisationMethodService = authorisationMethodService;
    }

    @SuppressWarnings("unchecked")
    protected ResponseObject<?> enrichLink(ResponseObject<?> result, PaymentInitiationParameters paymentRequestParameters) {
        Object body = result.getBody();

        if (EnumSet.of(SINGLE, PERIODIC).contains(paymentRequestParameters.getPaymentType())) {
            doEnrichLink(paymentRequestParameters, (PaymentInitiationResponse) body);
        } else {
            ((List<PaymentInitiationResponse>) body)
                .forEach(r -> doEnrichLink(paymentRequestParameters, r));
        }
        return result;
    }

    private void doEnrichLink(PaymentInitiationParameters paymentRequestParameters, PaymentInitiationResponse body) {
        body.setLinks(buildPaymentLinks(paymentRequestParameters, body));
    }

    //TODO encode payment id with base64 encoding and add decoders to every endpoint links lead https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/382
    private Links buildPaymentLinks(PaymentInitiationParameters paymentRequestParameters, PaymentInitiationResponse body) {
        if (RJCT == body.getTransactionStatus()) {
            return null;
        }
        String paymentService = paymentRequestParameters.getPaymentType().getValue();
        String paymentId = body.getPaymentId();

        Links links = new Links();
        links.setSelf(buildPath("/v1/{payment-service}/{payment-id}", paymentService, paymentId));
        links.setStatus(buildPath("/v1/{payment-service}/{payment-id}/status", paymentService, paymentId));

        if (aspspProfileService.getScaApproach() == ScaApproach.EMBEDDED) {
            return addEmbeddedRelatedLinks(links, paymentRequestParameters, body);
        } else if (aspspProfileService.getScaApproach() == ScaApproach.REDIRECT) {
            return addRedirectRelatedLinks(links, paymentRequestParameters, body);
        } else if (aspspProfileService.getScaApproach() == ScaApproach.OAUTH) {
            links.setScaOAuth("scaOAuth"); //TODO generate link for oauth https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/326
        }
        return links;
    }

    private Links addEmbeddedRelatedLinks(Links links, PaymentInitiationParameters paymentRequestParameters, PaymentInitiationResponse body) {
        String paymentService = paymentRequestParameters.getPaymentType().getValue();
        String paymentId = body.getPaymentId();
        String authorizationId = body.getAuthorizationId();

        if (authorisationMethodService.isExplicitMethod(paymentRequestParameters.isTppExplicitAuthorisationPreferred())) {
            links.setStartAuthorisation(buildPath("/v1/{payment-service}/{payment-id}/authorisations", paymentService, paymentId));
        } else {
            links.setScaStatus(
                buildPath("/v1/{payment-service}/{payment-id}/authorisations/{authorisation-id}", paymentService, paymentId, authorizationId));
            links.setStartAuthorisationWithPsuAuthentication(
                buildPath("/v1/{payment-service}/{payment-id}/authorisations/{authorisation-id}", paymentService, paymentId, authorizationId));
        }

        return links;
    }

    private Links addRedirectRelatedLinks(Links links, PaymentInitiationParameters paymentRequestParameters, PaymentInitiationResponse body) {
        String paymentService = paymentRequestParameters.getPaymentType().getValue();
        String paymentId = body.getPaymentId();
        String authorizationId = body.getAuthorizationId();
        String psuId = paymentRequestParameters.getPsuId();

        if (authorisationMethodService.isExplicitMethod(paymentRequestParameters.isTppExplicitAuthorisationPreferred())) {
            links.setStartAuthorisation(buildPath("/v1/{payment-service}/{payment-id}/authorisations", paymentService, paymentId));
        } else {
            String link = aspspProfileService.getPisRedirectUrlToAspsp() + body.getPisConsentId() + "/" + paymentId;
            if (StringUtils.isNotBlank(psuId)) {
                link = link + "/" + psuId;
            }
            links.setScaRedirect(link);
            links.setScaStatus(
                buildPath("/v1/{payment-service}/{payment-id}/authorisations/{authorisation-id}", paymentService, paymentId, authorizationId));
        }
        return links;
    }
}
