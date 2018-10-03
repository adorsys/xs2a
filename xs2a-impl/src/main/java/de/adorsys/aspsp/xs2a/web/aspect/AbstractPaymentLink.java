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

import de.adorsys.aspsp.xs2a.component.JsonConverter;
import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentRequestParameters;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentType;
import de.adorsys.aspsp.xs2a.service.authorization.AuthorisationMethodService;
import de.adorsys.aspsp.xs2a.service.message.MessageService;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.aspsp.profile.domain.ScaApproach;

import java.util.Base64;
import java.util.EnumSet;
import java.util.List;

import static de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus.RJCT;
import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.PERIODIC;
import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.SINGLE;

public abstract class AbstractPaymentLink<T> extends AbstractLinkAspect<T> {
    private boolean tppExplicitAuthorisationPreferred = false;
    private AuthorisationMethodService authorisationMethodService;

    public AbstractPaymentLink(int maxNumberOfCharInTransactionJson, AspspProfileServiceWrapper aspspProfileService, JsonConverter jsonConverter, MessageService messageService, AuthorisationMethodService authorisationMethodService) {
        super(maxNumberOfCharInTransactionJson, aspspProfileService, jsonConverter, messageService);
        this.authorisationMethodService = authorisationMethodService;
    }

    @SuppressWarnings("unchecked")
    protected ResponseObject<?> enrichLink(ResponseObject<?> result, PaymentRequestParameters paymentRequestParameters, String psuId) {
        Object body = result.getBody();

        setTppExplicitAuthorisationPreferred(paymentRequestParameters.isTppExplicitAuthorisationPreferred());
        PaymentType paymentType = paymentRequestParameters.getPaymentType();

        if (EnumSet.of(SINGLE, PERIODIC).contains(paymentType)) {
            doEnrichLink(paymentType, (PaymentInitialisationResponse) body, psuId);
        } else {
            ((List<PaymentInitialisationResponse>) body)
                .forEach(r -> doEnrichLink(paymentType, r, psuId));
        }
        return result;
    }

    private void doEnrichLink(PaymentType paymentType, PaymentInitialisationResponse body, String psuId) {
        body.setLinks(buildPaymentLinks(body, paymentType.getValue(), psuId));
    }

    private Links buildPaymentLinks(PaymentInitialisationResponse body, String paymentService, String psuId) {
        if (RJCT == body.getTransactionStatus()) {
            return null;
        }
        String encodedPaymentId = Base64.getEncoder()
                                      .encodeToString(body.getPaymentId().getBytes());
        Links links = new Links();
        links.setSelf(buildPath("/v1/{paymentService}/{paymentId}", paymentService, encodedPaymentId));
        links.setStatus(buildPath("/v1/{paymentService}/{paymentId}/status", paymentService, encodedPaymentId));
        if (aspspProfileService.getScaApproach() == ScaApproach.EMBEDDED) {
            return addEmbeddedRelatedLinks(links, paymentService, encodedPaymentId, body.getAuthorizationId());
        } else if (aspspProfileService.getScaApproach() == ScaApproach.REDIRECT) {
            addRedirectRelatedLinks(links, paymentService, encodedPaymentId, body.getPisConsentId(), psuId, body.getAuthorizationId());
        } else if (aspspProfileService.getScaApproach() == ScaApproach.OAUTH) {
            links.setScaOAuth("scaOAuth"); //TODO generate link for oauth https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/326
        }
        return links;
    }

    private Links addEmbeddedRelatedLinks(Links links, String paymentService, String paymentId, String authorisationId) {
        if (authorisationMethodService.isExplicitMethod(tppExplicitAuthorisationPreferred)) {
            links.setStartAuthorisation(buildPath("/v1/{payment-service}/{payment-id}/authorisations", paymentService, paymentId));
        } else {
            links.setScaStatus(
                buildPath("/v1/{paymentService}/{paymentId}/authorisations/{authorisation-id}", paymentService, paymentId, authorisationId));
            links.setStartAuthorisationWithPsuAuthentication(
                buildPath("/v1/{paymentService}/{paymentId}/authorisations/{authorisation-id}", paymentService, paymentId, authorisationId));
        }

        return links;
    }

    private Links addRedirectRelatedLinks(Links links, String paymentService, String paymentId, String consentId, String psuId, String authorisationId) {
        if (authorisationMethodService.isExplicitMethod(tppExplicitAuthorisationPreferred)) {
            links.setStartAuthorisation(buildPath("/v1/{payment-service}/{payment-id}/authorisations", paymentService, paymentId));
        } else {
            links.setScaRedirect(aspspProfileService.getPisRedirectUrlToAspsp() + consentId + "/" + paymentId + "/" + psuId);
            links.setScaStatus(
                buildPath("/v1/{payment-service}/{payment-id}/authorisations/{authorisation-id}", paymentService, paymentId, authorisationId));
        }

        return links;
    }

    private void setTppExplicitAuthorisationPreferred(boolean tppExplicitPreferred) {
        this.tppExplicitAuthorisationPreferred = tppExplicitPreferred;
    }
}
