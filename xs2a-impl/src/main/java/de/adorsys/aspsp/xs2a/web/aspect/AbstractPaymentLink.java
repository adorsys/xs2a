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
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;

import java.util.Base64;

public abstract class AbstractPaymentLink<T> extends AbstractLinkAspect<T> {

    protected Links buildPaymentLinks(PaymentInitialisationResponse body, String paymentService) {
        String encodedPaymentId = Base64.getEncoder().encodeToString(body.getPaymentId().getBytes());

        Links links = new Links();
        links.setScaRedirect(aspspProfileService.getPisRedirectUrlToAspsp() + body.getPisConsentId() + "/" + encodedPaymentId);
        links.setSelf(buildLink("/v1/{paymentService}/{paymentId}", paymentService, encodedPaymentId));
        links.setStatus(buildLink("/v1/{paymentService}/{paymentId}/status", paymentService, encodedPaymentId));
        return links;
    }
}


