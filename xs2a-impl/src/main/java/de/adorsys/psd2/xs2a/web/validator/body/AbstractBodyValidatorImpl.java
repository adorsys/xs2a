/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.validator.body;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

/**
 * Class with common functionality (AIS and PIS) for bodies validating.
 */
public class AbstractBodyValidatorImpl {

    protected ErrorBuildingService errorBuildingService;
    protected ObjectMapper objectMapper;

    protected AbstractBodyValidatorImpl(ErrorBuildingService errorBuildingService, ObjectMapper objectMapper) {
        this.errorBuildingService = errorBuildingService;
        this.objectMapper = objectMapper;
    }

    protected void checkFieldForMaxLength(String fieldToCheck, String fieldName, int maxLength, MessageError messageError) {
        if (fieldToCheck.length() > maxLength) {
            String text = String.format("Value '%s' should not be more than %s symbols", fieldName, maxLength);
            errorBuildingService.enrichMessageError(messageError, text);
        }
    }

    protected void checkForMaxLengthIfNotNull(String field, String fieldName, int maxLength, MessageError messageError) {
        if (StringUtils.isNotBlank(field)) {
            checkFieldForMaxLength(field, fieldName, maxLength, messageError);
        }
    }

    protected <T> Optional<T> mapBodyToInstance(HttpServletRequest request, MessageError messageError, Class<T> clazz) {
        try {
            return Optional.of(objectMapper.readValue(request.getInputStream(), clazz));
        } catch (IOException e) {
            errorBuildingService.enrichMessageError(messageError, "Cannot deserialize the request body");
        }

        return Optional.empty();
    }
}
