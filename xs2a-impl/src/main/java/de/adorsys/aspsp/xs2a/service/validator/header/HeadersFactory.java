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

package de.adorsys.aspsp.xs2a.service.validator.header;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.service.validator.header.impl.*;
import de.adorsys.aspsp.xs2a.web.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HeadersFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeadersFactory.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Map<Class, Class> controllerClassMap = new HashMap<>();

    static {
        controllerClassMap.put(AccountController.class, AccountRequestHeader.class);
        controllerClassMap.put(ConsentInformationController.class, ConsentRequestHeader.class);
        controllerClassMap.put(PaymentInitiationController.class, PaymentRequestHeader.class);
        controllerClassMap.put(BulkPaymentInitiationController.class, PaymentInitiationRequestHeader.class);
        controllerClassMap.put(PeriodicPaymentsController.class, PaymentInitiationRequestHeader.class);
        controllerClassMap.put(FundsConfirmationController.class, FundsConfirmationRequestHeader.class);
        controllerClassMap.put(PaymentController.class, PaymentInitiationRequestHeader.class);
    }

    public static RequestHeader getHeadersImpl(Map<String, String> requestHeadersMap, Class controllerClass) {
        Class<? extends RequestHeader> headerClass = controllerClassMap.get(controllerClass);

        if (headerClass == null) {
            return new NotMatchedHeaderImpl();
        } else {
            try {
                return MAPPER.convertValue(requestHeadersMap, headerClass);
            } catch (IllegalArgumentException exception) {
                LOGGER.error("Error request headers conversion: " + exception.getMessage());
                return new ErrorMessageHeaderImpl(exception.getMessage());
            }
        }
    }
}
