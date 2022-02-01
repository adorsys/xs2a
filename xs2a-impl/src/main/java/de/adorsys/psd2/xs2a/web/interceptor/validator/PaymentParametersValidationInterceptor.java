/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.interceptor.validator;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.PathParameterExtractor;
import de.adorsys.psd2.xs2a.web.Psd2PaymentMethodNameConstant;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.path.PaymentTypeAndProductValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PARAMETER_NOT_SUPPORTED_WRONG_PAYMENT_TYPE;

/**
 * This interceptor is used for incoming requests validation accessed for XS2A PIS controllers. This interceptor should be
 * registered (and launched) before the main request validation interceptor, because ASPSP profile configuration is autowired here
 * and some documentation-specific HTTP codes must be returned from it.
 * <p>
 * Also, binding by PSD2 controller method names is used here to cover all XS2A PIS controller endpoints (initiate
 * payment, start its SCA, cancel payment etc).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentParametersValidationInterceptor extends HandlerInterceptorAdapter {

    private static final String PAYMENT_SERVICE = "payment-service";
    private static final String PAYMENT_PRODUCT = "payment-product";

    private final ErrorBuildingService errorBuildingService;
    private final PathParameterExtractor pathParameterExtractor;
    private final PaymentTypeAndProductValidator paymentTypeAndProductValidator;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        return isRequestValid(request, response, handler);
    }

    private boolean isRequestValid(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            String methodName = handlerMethod.getMethod().getName();

            // Filter requests, which are addressed to payment API according to PSD2 documentation.
            boolean paymentRequest = Arrays.stream(Psd2PaymentMethodNameConstant.values())
                                         .anyMatch(mn -> mn.getValue().equals(methodName));

            if (paymentRequest) {
                return isPaymentRequestValid(request, response);
            }
        }

        return true;
    }

    private boolean isPaymentRequestValid(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> pathParameters = pathParameterExtractor.extractParameters(request);

        String paymentTypeString = pathParameters.get(PAYMENT_SERVICE);
        Optional<PaymentType> paymentTypeOptional = Optional.ofNullable(paymentTypeString)
                                                        .flatMap(PaymentType::getByValue);

        if (paymentTypeOptional.isEmpty()) {
            MessageError messageError = new MessageError(ErrorType.PIS_400, TppMessageInformation.of(PARAMETER_NOT_SUPPORTED_WRONG_PAYMENT_TYPE, paymentTypeString));
            errorBuildingService.buildPaymentErrorResponse(response, messageError);
            return false;
        }

        String paymentProduct = pathParameters.get(PAYMENT_PRODUCT);

        if (paymentProduct != null) {
            ValidationResult validationResult = paymentTypeAndProductValidator.validateTypeAndProduct(paymentTypeOptional.get(), paymentProduct);

            if (validationResult.isNotValid()) {
                errorBuildingService.buildPaymentErrorResponse(response, validationResult.getMessageError());
                return false;
            }
        }

        return true;
    }
}
