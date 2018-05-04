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

package de.adorsys.aspsp.xs2a.component;

import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.web.PaymentInitiationController;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Component
@AllArgsConstructor
public class LinkComponent {
    private final String redirectLinkToSource;

    public Links createPaymentLinks(String paymentId, PaymentProduct paymentProduct) {
        Links links = new Links();
        links.setRedirect(redirectLinkToSource);
        links.setSelf(linkTo(PaymentInitiationController.class, paymentProduct.getCode()).slash(paymentId).toString());
        links.setUpdatePsuIdentification(linkTo(PaymentInitiationController.class, paymentProduct.getCode()).slash(paymentId).toString());
        links.setUpdatePsuAuthentication(linkTo(PaymentInitiationController.class, paymentProduct.getCode()).slash(paymentId).toString());
        links.setStatus(linkTo(PaymentInitiationController.class, paymentProduct.getCode(), paymentId).slash("status").toString());
        return links;
    }
}
