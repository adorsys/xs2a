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

import de.adorsys.psd2.xs2a.service.validator.RequestValidatorService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.validation.ValidationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

// TODO: should be removed in 2.6 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/782
@Deprecated
@Aspect
@Component
@RequiredArgsConstructor
public class IpAddressValidationAspect {
    private final RequestValidatorService requestValidatorService;

    /**
     * Check for violations in initiate payment request
     *
     * @param joinPoint for initiate payment request
     * @throws ValidationException when violations exist
     */
    @Before("execution(* de.adorsys.psd2.api.PaymentApi._initiatePayment(..))")
    public void initiatePayment(JoinPoint joinPoint) throws ValidationException {

        Map<String, String> requestHeadersMap = getRequestParametersMap(joinPoint);
        Map<String, String> violationMap = requestValidatorService.getRequestViolationMapInitiatePayment(requestHeadersMap);

        if (!violationMap.isEmpty()) {
            throw new ValidationException("Wrong request header arguments");
        }
    }

    private Map<String, String> getRequestParametersMap(JoinPoint joinPoint) {
        Map<String, String> requestParameterMap = new HashMap<>();
        Object[] args = joinPoint.getArgs();

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        assert args.length == parameterAnnotations.length;
        for (int argIndex = 0; argIndex < args.length; argIndex++) {
            for (Annotation annotation : parameterAnnotations[argIndex]) {
                if (!(annotation instanceof RequestHeader)) {
                    continue;
                }
                RequestHeader headerParam = (RequestHeader) annotation;
                requestParameterMap.put(headerParam.value().toLowerCase(), String.valueOf(args[argIndex]));
            }
        }
        requestParameterMap.putIfAbsent("date", "Sun, 11 Aug 2019 15:02:37 GMT");

        return requestParameterMap;
    }
}
