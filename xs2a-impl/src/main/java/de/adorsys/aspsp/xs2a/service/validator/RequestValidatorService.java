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


import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.aspsp.xs2a.service.validator.header.HeadersFactory;
import de.adorsys.aspsp.xs2a.service.validator.header.RequestHeader;
import de.adorsys.aspsp.xs2a.service.validator.header.impl.ErrorMessageHeaderImpl;
import de.adorsys.aspsp.xs2a.service.validator.header.impl.PaymentInitiationRequestHeader;
import de.adorsys.aspsp.xs2a.service.validator.parameter.ParametersFactory;
import de.adorsys.aspsp.xs2a.service.validator.parameter.RequestParameter;
import de.adorsys.aspsp.xs2a.service.validator.parameter.impl.ErrorMessageParameterImpl;
import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.cors.CorsConfigurationSource;
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
    private AspspProfileServiceWrapper aspspProfileService;

    private static final String PAYMENT_PRODUCT_PATH_VAR = "payment-product";
    private static final String PAYMENT_SERVICE_PATH_VAR = "payment-service";

    /**
     * Gets violations of incoming request, checking headers, parameters and path variables
     *
     * @param request incoming request
     * @param handler of method
     * @return Map with violations for incoming request
     */
    public Map<String, String> getRequestViolationMap(HttpServletRequest request, Object handler) {
        Map<String, String> violationMap = new HashMap<>();

        if (handler instanceof CorsConfigurationSource) { // TODO delete after creation original 'Tpp Demo app' https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/156
            return violationMap;
        }

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            violationMap.putAll(getRequestHeaderViolationMap(request, handlerMethod));
            violationMap.putAll(getRequestParametersViolationMap(request, handlerMethod));
            violationMap.putAll(getRequestPathVariablesViolationMap(request));
        }

        return violationMap;
    }

    /**
     * Gets violations for initiate  payment request base on headers and its values
     *
     * @param requestHeadersMap map with headers and its values
     * @return Map with violations for initiate payment request
     */
    public Map<String, String> getRequestViolationMapInitiatePayment(Map<String, String> requestHeadersMap) {

        RequestHeader headerImpl = HeadersFactory.getHeadersImplByRequestHeaderClass(requestHeadersMap, PaymentInitiationRequestHeader.class);

        if (headerImpl instanceof ErrorMessageHeaderImpl) {
            return Collections.singletonMap("Wrong header arguments: ",
                ((ErrorMessageHeaderImpl) headerImpl).getErrorMessage()
            );
        }

        return getViolationMessagesMap(validator.validate(headerImpl));
    }

    /**
     * Gets violations for initiate  payment request base on request
     *
     * @param request initiate payment request
     * @return Map with violations for initiate payment request
     */
    public Map<String, String> getRequestViolationMapInitiatePayment(HttpServletRequest request) {
        Map<String, String> requestHeadersMap = getRequestHeadersMap(request);
        return getRequestViolationMapInitiatePayment(requestHeadersMap);
    }

    private Map<String, String> getRequestParametersViolationMap(HttpServletRequest request, HandlerMethod handler) {

        Map<String, String> requestParameterMap = getRequestParametersMap(request);

        RequestParameter parameterImpl = parametersFactory.getParameterImpl(requestParameterMap, handler.getBeanType());

        if (parameterImpl instanceof ErrorMessageParameterImpl) {
            return Collections.singletonMap("Wrong parameters : ", ((ErrorMessageParameterImpl) parameterImpl).getErrorMessage());
        }

        return getViolationMessagesMap(validator.validate(parameterImpl));
    }

    private Map<String, String> getRequestPathVariablesViolationMap(HttpServletRequest request) {
        Map<String, String> requestPathViolationMap = new HashMap<>();
        requestPathViolationMap.putAll(checkPaymentProductByRequest(request));
        requestPathViolationMap.putAll(getPaymentTypeViolationMap(request));

        return requestPathViolationMap;
    }

    private Map<String, String> getPaymentTypeViolationMap(HttpServletRequest request) {
        Map<String, String> pathVariableMap = getPathVariableMap(request);
        return Optional.ofNullable(pathVariableMap)
                   .map(m -> m.get(PAYMENT_SERVICE_PATH_VAR))
                   .flatMap(PaymentType::getByValue)
                   .map(this::getViolationMapForPaymentType)
                   .orElseGet(Collections::emptyMap);
    }

    private Map<String, String> getRequestHeaderViolationMap(HttpServletRequest request, HandlerMethod handler) {

        Map<String, String> requestHeadersMap = getRequestHeadersMap(request);

        RequestHeader headerImpl = HeadersFactory.getHeadersImpl(requestHeadersMap, handler.getBeanType());

        if (headerImpl instanceof ErrorMessageHeaderImpl) {
            return Collections.singletonMap("Wrong header arguments: ",
                ((ErrorMessageHeaderImpl) headerImpl).getErrorMessage()
            );
        }

        return getViolationMessagesMap(validator.validate(headerImpl));
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

        requestHeaderMap.putIfAbsent("date", "Sun, 11 Aug 2019 15:02:37 GMT");
        return requestHeaderMap;
    }

    private Map<String, String> getRequestParametersMap(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                   .collect(Collectors.toMap(
                       Map.Entry::getKey,
                       e -> String.join(",", e.getValue())));
    }

    private Map<String, String> checkPaymentProductByRequest(HttpServletRequest request) {
        //noinspection unchecked
        Map<String, String> pathVariableMap = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        return Optional.ofNullable(pathVariableMap)
                   .map(mp -> mp.get(PAYMENT_PRODUCT_PATH_VAR))
                   .map(this::checkPaymentProductSupportAndGetViolationMap)
                   .orElseGet(Collections::emptyMap);
    }

    private Map<String, String> checkPaymentProductSupportAndGetViolationMap(String paymentProduct) {
        return Optional.ofNullable(paymentProduct)
                   .flatMap(PaymentProduct::getByValue)
                   .map(this::getViolationMapForPaymentProduct)
                   .orElseGet(() -> Collections.singletonMap(MessageErrorCode.PRODUCT_UNKNOWN.getName(), "Wrong payment product: " + paymentProduct));
    }

    private Map<String, String> getViolationMapForPaymentProduct(PaymentProduct paymentProduct) {
        return isPaymentProductAvailable(paymentProduct)
                   ? Collections.emptyMap()
                   : Collections.singletonMap(MessageErrorCode.PRODUCT_UNKNOWN.getName(), "Wrong payment product: " + paymentProduct.getValue());
    }

    private Map<String, String> getViolationMapForPaymentType(PaymentType paymentType) {
        PaymentType consentPaymentType = PaymentType.valueOf(paymentType.name());
        return isPaymentTypeAvailable(consentPaymentType)
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

    private <T> Map<String, String> getViolationMessagesMap(Set<ConstraintViolation<T>> collection) {
        return collection.stream()
                   .collect(Collectors.toMap(
                       violation -> violation.getPropertyPath().toString(),
                       violation -> "'" + violation.getPropertyPath().toString() + "' " + violation.getMessage()));
    }

    private Map<String, String> getPathVariableMap(HttpServletRequest request) {
        //noinspection unchecked
        return (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    }
}
