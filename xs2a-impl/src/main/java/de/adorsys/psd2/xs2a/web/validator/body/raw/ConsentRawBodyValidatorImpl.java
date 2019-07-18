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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.xs2a.component.JsonConverter;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.converter.LocalDateConverter;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aRequestBodyDateFields.AIS_CONSENT_DATE_FIELDS;

@Component
public class ConsentRawBodyValidatorImpl extends AbstractRawBodyValidatorImpl implements ConsentRawBodyValidator {

    protected ConsentRawBodyValidatorImpl(ErrorBuildingService errorBuildingService, ObjectMapper objectMapper, JsonConverter jsonConverter, LocalDateConverter localDateConverter) {
        super(errorBuildingService, objectMapper, jsonConverter, localDateConverter);
    }

    @Override
    public void validate(HttpServletRequest request, MessageError messageError) {
        validateRawDataDates(request, AIS_CONSENT_DATE_FIELDS.getDateFields(), messageError);
    }
}
