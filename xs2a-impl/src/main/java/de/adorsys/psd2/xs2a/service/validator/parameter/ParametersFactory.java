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

package de.adorsys.psd2.xs2a.service.validator.parameter;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.xs2a.service.validator.parameter.impl.AccountRequestParameter;
import de.adorsys.psd2.xs2a.service.validator.parameter.impl.ErrorMessageParameterImpl;
import de.adorsys.psd2.xs2a.service.validator.parameter.impl.NotMatchedParameterImpl;
import de.adorsys.psd2.xs2a.web.controller.AccountController;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

// TODO: should be removed in 2.6 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/782
@Deprecated
@Service
@AllArgsConstructor
public class ParametersFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParametersFactory.class);
    private final ObjectMapper objectMapper;

    private static final Map<Class, Class> controllerClassMap = new HashMap<>();

    static {
        controllerClassMap.put(AccountController.class, AccountRequestParameter.class);
    }

    public RequestParameter getParameterImpl(Map<String, String> requestParametersMap, Class controllerClass) {
        Class<? extends RequestParameter> headerClass = controllerClassMap.get(controllerClass);

        if (headerClass == null) {
            return new NotMatchedParameterImpl();
        } else {
            try {
                return objectMapper.convertValue(requestParametersMap, headerClass);
            } catch (IllegalArgumentException exception) {
                LOGGER.error("Error request parameter conversion: " + exception.getMessage());
                return new ErrorMessageParameterImpl(exception.getMessage());
            }
        }
    }
}
