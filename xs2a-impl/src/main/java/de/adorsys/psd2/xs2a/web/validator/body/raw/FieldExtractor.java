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
