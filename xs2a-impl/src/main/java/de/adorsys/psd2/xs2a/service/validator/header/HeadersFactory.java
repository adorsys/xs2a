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

package de.adorsys.psd2.xs2a.service.validator.header;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.xs2a.service.validator.header.impl.*;
import de.adorsys.psd2.xs2a.web.controller.AccountController;
import de.adorsys.psd2.xs2a.web.controller.ConsentController;
import de.adorsys.psd2.xs2a.web.controller.FundsConfirmationController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

// TODO: should be removed in 2.6 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/782
@Deprecated
public class HeadersFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeadersFactory.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Map<Class, Class> controllerClassMap = new HashMap<>();

    static {
        controllerClassMap.put(AccountController.class, AccountRequestHeader.class);
        controllerClassMap.put(ConsentController.class, ConsentRequestHeader.class);
        controllerClassMap.put(FundsConfirmationController.class, FundsConfirmationRequestHeader.class);
    }

    /**
     * Gets request header instance filled by request headers and controller class
     *
     * @param requestHeadersMap headers and its values
     * @param controllerClass controller class that gives us appropriate header class
     * @return request header instance filled by headers and controller class
     */
    public static RequestHeader getHeadersImpl(Map<String, String> requestHeadersMap, Class controllerClass) {
        return getHeadersImplByRequestHeaderClass(requestHeadersMap, controllerClassMap.get(controllerClass));
    }

    /**
     * Gets request header instance filled by request headers and request header class
     *
     * @param requestHeadersMap headers and its values
     * @param headerClass header class that will be construct with requestHeadersMap
     * @return request header instance filled by headers and request header class
     */
    public static RequestHeader getHeadersImplByRequestHeaderClass(Map<String, String> requestHeadersMap, Class<? extends RequestHeader> headerClass) {

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
