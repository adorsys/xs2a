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
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentType;

import java.util.Base64;
import java.util.List;

import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.PERIODIC;
import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.SINGLE;
import static java.util.EnumSet.of;

public abstract class AbstractPaymentLink<T> extends AbstractLinkAspect<T> {

    @SuppressWarnings("unchecked")
    protected ResponseObject<?> enrichLink(ResponseObject<?> result, PaymentType paymentType) {
        Object body = result.getBody();
        if (of(SINGLE, PERIODIC).contains(paymentType)) {
            doEnrichLink(paymentType, (PaymentInitialisationResponse) body);
        } else {
            ((List<PaymentInitialisationResponse>) body)
                .forEach(r -> doEnrichLink(paymentType, r));
        }
        return result;
    }

    private void doEnrichLink(PaymentType paymentType, PaymentInitialisationResponse body) {
        body.setLinks(buildPaymentLinks(body, paymentType.getValue()));
    }

    private Links buildPaymentLinks(PaymentInitialisationResponse body, String paymentService) {
        String encodedPaymentId = Base64.getEncoder()
                                      .encodeToString(body.getPaymentId().getBytes());
        Links links = new Links();
        links.setScaRedirect(aspspProfileService.getPisRedirectUrlToAspsp() + body.getPisConsentId() + "/" + encodedPaymentId);
        links.setSelf(buildPath("/v1/{paymentService}/{paymentId}", paymentService, encodedPaymentId));
        links.setStatus(buildPath("/v1/{paymentService}/{paymentId}/status", paymentService, encodedPaymentId));
        return links;
    }
}


