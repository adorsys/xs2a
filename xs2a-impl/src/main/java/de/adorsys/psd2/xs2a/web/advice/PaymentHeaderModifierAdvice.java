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

package de.adorsys.psd2.xs2a.web.advice;

import de.adorsys.psd2.model.PaymentInitationRequestResponse201;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.LinkExtractor;
import de.adorsys.psd2.xs2a.web.controller.PaymentController;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.List;

@ControllerAdvice(assignableTypes = {PaymentController.class})
public class PaymentHeaderModifierAdvice extends CommonHeaderModifierAdvice {
    private static final String SELF_LINK_NAME = "self";

    private final LinkExtractor linkExtractor;

    public PaymentHeaderModifierAdvice(ScaApproachResolver scaApproachResolver, LinkExtractor linkExtractor) {
        super(scaApproachResolver);
        this.linkExtractor = linkExtractor;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        String methodName = returnType.getMethod().getName();

        if ("_startPaymentAuthorisation".equals(methodName)
                || "_startPaymentInitiationCancellationAuthorisation".equals(methodName)
                || "_updatePaymentCancellationPsuData".equals(methodName)
                || "_updatePaymentPsuData".equals(methodName)) {
            response.getHeaders().add("Aspsp-Sca-Approach", scaApproachResolver.resolveScaApproach().name());
        } else if ("_initiatePayment".equals(methodName)) {
            response.getHeaders().add("Aspsp-Sca-Approach", scaApproachResolver.resolveScaApproach().name());
            response.getHeaders().add("Location", getLocationHeaderForInitiatePayment(body));
        }

        return body;
    }

    private String getLocationHeaderForInitiatePayment(Object body) {
        String location = null;
        if (body instanceof List) {
            List responseList = (List) body;
            if (CollectionUtils.isNotEmpty(responseList)) {
                location = getSelfLink(responseList.get(0));
            }
        } else {
            location = getSelfLink(body);
        }
        return location;
    }

    private String getSelfLink(Object response) {
        String selfLink = null;
        if (response instanceof PaymentInitationRequestResponse201) {
            PaymentInitationRequestResponse201 paymentResponse = (PaymentInitationRequestResponse201) response;

            selfLink = linkExtractor.extract(paymentResponse.getLinks(), SELF_LINK_NAME)
                           .orElse(null);
        }
        return selfLink;
    }
}
