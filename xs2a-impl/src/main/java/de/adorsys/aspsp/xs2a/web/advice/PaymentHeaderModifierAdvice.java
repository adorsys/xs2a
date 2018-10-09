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

package de.adorsys.aspsp.xs2a.web.advice;

import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.aspsp.xs2a.web.PaymentController;
import de.adorsys.psd2.model.PaymentInitationRequestResponse201;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.List;
import java.util.Optional;

@ControllerAdvice(assignableTypes = {PaymentController.class})
public class PaymentHeaderModifierAdvice extends CommonHeaderModifierAdvice {

    public PaymentHeaderModifierAdvice(AspspProfileServiceWrapper aspspProfileServiceWrapper) {
        super(aspspProfileServiceWrapper);
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        String methodName = returnType.getMethod().getName();

        if ("_startPaymentAuthorisation".equals(methodName) || "_startPaymentInitiationCancellationAuthorisation".equals(methodName)
                || "_updatePaymentCancellationPsuData".equals(methodName) || "_updatePaymentPsuData".equals(methodName)) {
            response.getHeaders().add("Aspsp-Sca-Approach", getScaApproach().name());
        } else if ("_initiatePayment".equals(methodName)) {
            response.getHeaders().add("Aspsp-Sca-Approach", getScaApproach().name());
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

            selfLink = Optional.ofNullable(paymentResponse.getLinks())
                           .map(links -> links.get("self").toString())
                           .orElse(null);
        }
        return selfLink;
    }
}
