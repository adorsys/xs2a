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

package de.adorsys.aspsp.xs2a.service.validator;


import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.service.AspspProfileService;
import de.adorsys.aspsp.xs2a.service.validator.header.HeadersFactory;
import de.adorsys.aspsp.xs2a.service.validator.header.RequestHeader;
import de.adorsys.aspsp.xs2a.service.validator.header.impl.ErrorMessageHeaderImpl;
import de.adorsys.aspsp.xs2a.service.validator.parameter.ParametersFactory;
import de.adorsys.aspsp.xs2a.service.validator.parameter.RequestParameter;
import de.adorsys.aspsp.xs2a.service.validator.parameter.impl.ErrorMessageParameterImpl;
import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.spi.domain.consent.pis.PaymentType;
import de.adorsys.aspsp.xs2a.web.BulkPaymentInitiationController;
import de.adorsys.aspsp.xs2a.web.PaymentInitiationController;
import de.adorsys.aspsp.xs2a.web.PeriodicPaymentsController;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.*;
import java.util.stream.Collectors;

@Log4j
@Service
public class RequestValidatorService {
    @Autowired
    private ParametersFactory parametersFactory;
    @Autowired
    private Validator validator;
    @Autowired
    private AspspProfileService aspspProfileService;

    final String PAYMENT_PRODUCT_PATH_VAR = "payment-product";
    static HashMap<Object, PaymentType> classMap;

    static {
        classMap = new HashMap<>();
        classMap.put(PaymentInitiationController.class, PaymentType.FUTURE_DATED);
        classMap.put(BulkPaymentInitiationController.class, PaymentType.BULK);
        classMap.put(PeriodicPaymentsController.class, PaymentType.PERIODIC);
    }

    public Map<String, String> getRequestViolationMap(HttpServletRequest request, Object handler) {
        Map<String, String> violationMap = new HashMap<>();
        violationMap.putAll(getRequestHeaderViolationMap(request, handler));
        violationMap.putAll(getRequestParametersViolationMap(request, handler));
        violationMap.putAll(getRequestPathVariablesViolationMap(request, handler));

        return violationMap;
    }

    public Map<String, String> getRequestParametersViolationMap(HttpServletRequest request, Object handler) {

        Map<String, String> requestParameterMap = getRequestParametersMap(request);

        RequestParameter parameterImpl = parametersFactory.getParameterImpl(requestParameterMap, ((HandlerMethod) handler).getBeanType());

        if (parameterImpl instanceof ErrorMessageParameterImpl) {
            return Collections.singletonMap("Wrong parameters : ", ((ErrorMessageParameterImpl) parameterImpl).getErrorMessage());
        }

        Map<String, String> requestParameterViolationsMap = validator.validate(parameterImpl).stream()
                                                                .collect(Collectors.toMap(violation -> violation.getPropertyPath().toString(), ConstraintViolation::getMessage));

        return requestParameterViolationsMap;
    }

    public Map<String, String> getRequestPathVariablesViolationMap(HttpServletRequest request, Object handler) {
        Map<String, String> requestPathViolationMap = new HashMap<>();
        requestPathViolationMap.putAll(checkPaymentProductByRequest(request));
        requestPathViolationMap.putAll(getPaymentTypeViolationMap(handler));

        return requestPathViolationMap;
    }

    public Map<String, String> getPaymentTypeViolationMap(Object handler) {
        return Optional.ofNullable(classMap.get(((HandlerMethod) handler).getBeanType()))
                   .map(this::getViolationMapForPaymentType)
                   .orElse(Collections.emptyMap());
    }

    public Map<String, String> getRequestHeaderViolationMap(HttpServletRequest request, Object handler) {

        Map<String, String> requestHeadersMap = getRequestHeadersMap(request);

        RequestHeader headerImpl = HeadersFactory.getHeadersImpl(requestHeadersMap, ((HandlerMethod) handler).getBeanType());

        if (headerImpl instanceof ErrorMessageHeaderImpl) {
            return Collections.singletonMap("Wrong header arguments: ", ((ErrorMessageHeaderImpl) headerImpl).getErrorMessage());
        }

        Map<String, String> requestHeaderViolationsMap = validator.validate(headerImpl).stream()
                                                             .collect(Collectors.toMap(violation -> violation.getPropertyPath().toString(), ConstraintViolation::getMessage));

        return requestHeaderViolationsMap;
    }

    private Map<String, String> getRequestHeadersMap(HttpServletRequest request) {

        Map<String, String> requestHeaderMap = new HashMap<>();
        if (request == null) {
            return requestHeaderMap;
        }

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            requestHeaderMap.put(key, value);
        }

        return requestHeaderMap;
    }

    private Map<String, String> getRequestParametersMap(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                   .collect(Collectors.toMap(
                       Map.Entry::getKey,
                       e -> String.join(",", e.getValue())));
    }

    private Map<String, String> checkPaymentProductByRequest(HttpServletRequest request) {
        Map<String, String> pathVariableMap = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        return Optional.ofNullable(pathVariableMap)
                   .map(mp -> mp.get(PAYMENT_PRODUCT_PATH_VAR))
                   .map(this::checkPaymentProductSupportAndGetViolationMap)
                   .orElse(Collections.emptyMap());
    }

    private Map<String, String> checkPaymentProductSupportAndGetViolationMap(String paymentProduct) {
        return Optional.ofNullable(paymentProduct)
                   .flatMap(PaymentProduct::getByCode)
                   .map(this::getViolationMapForPaymentProduct)
                   .orElse(Collections.singletonMap(MessageErrorCode.PRODUCT_UNKNOWN.getName(), "Wrong payment product: " + paymentProduct));
    }

    private Map<String, String> getViolationMapForPaymentProduct(PaymentProduct paymentProduct) {
        return isPaymentProductAvailable(paymentProduct)
                   ? Collections.emptyMap()
                   : Collections.singletonMap(MessageErrorCode.PRODUCT_UNKNOWN.getName(), "Wrong payment product: " + paymentProduct.getCode());
    }


    private Map<String, String> getViolationMapForPaymentType(PaymentType paymentType) {
        return isPaymentTypeAvailable(paymentType)
                   ? Collections.emptyMap()
                   : Collections.singletonMap(MessageErrorCode.PARAMETER_NOT_SUPPORTED.getName(), "Wrong payment type: " + paymentType.getValue());
    }

    private boolean isPaymentProductAvailable(PaymentProduct paymentProduct) {
        List<PaymentProduct> paymentProducts = aspspProfileService.getAvailablePaymentProducts();
        return paymentProducts.contains(paymentProduct);
    }

    private boolean isPaymentTypeAvailable(PaymentType paymentType) {
        List<PaymentType> paymentTypes = aspspProfileService.getAvailablePaymentTypes();
        return paymentTypes.contains(paymentType);
    }
}
