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

package de.adorsys.psd2.xs2a.web.validator.body.raw;

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_DESERIALIZATION_FAIL;

@Component
@Slf4j
@RequiredArgsConstructor
public class FieldExtractor {
    private static final String EXTRACT_ERROR_MESSAGE = "Couldn't extract field {} from json: {}";

    private final ErrorBuildingService errorBuildingService;
    private final Xs2aObjectMapper xs2aObjectMapper;

    public Optional<String> extractField(HttpServletRequest request, String fieldName, MessageError messageError) {
        Optional<String> fieldOptional = Optional.empty();
        try {
            fieldOptional = xs2aObjectMapper.toJsonField(request.getInputStream(), fieldName, new TypeReference<String>() {
            });
        } catch (IOException e) {
            log.info(EXTRACT_ERROR_MESSAGE, fieldName, e.getMessage());
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_DESERIALIZATION_FAIL));
        }

        return fieldOptional;
    }

    public Optional<String> extractOptionalField(HttpServletRequest request, String fieldName) {
        try {
            return xs2aObjectMapper.toJsonField(request.getInputStream(), fieldName, new TypeReference<String>() {
            });
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public List<String> extractList(HttpServletRequest request, String fieldName, MessageError messageError) {
        List<String> fieldList = new ArrayList<>();
        try {
            fieldList.addAll(xs2aObjectMapper.toJsonGetListValuesForField(request.getInputStream(), fieldName));
        } catch (IOException e) {
            log.info(EXTRACT_ERROR_MESSAGE, fieldName, e.getMessage());
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_DESERIALIZATION_FAIL));
        }
        return fieldList;
    }

    public List<String> extractOptionalList(HttpServletRequest request, String fieldName) {
        try {
            return xs2aObjectMapper.toJsonGetValuesForField(request.getInputStream(), fieldName);

        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public <T> Optional<T> mapBodyToInstance(HttpServletRequest request, MessageError messageError, Class<T> clazz) {
        try {
            return Optional.of(xs2aObjectMapper.readValue(request.getInputStream(), clazz));
        } catch (IOException e) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_DESERIALIZATION_FAIL));
        }

        return Optional.empty();
    }
}
