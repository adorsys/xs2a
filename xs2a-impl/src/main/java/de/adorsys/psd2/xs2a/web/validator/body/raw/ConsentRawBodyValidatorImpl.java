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

import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.body.DateFieldValidator;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aRequestBodyDateFields.AIS_CONSENT_DATE_FIELDS;

@Component
public class ConsentRawBodyValidatorImpl implements ConsentRawBodyValidator {
    private DateFieldValidator dateFieldValidator;

    protected ConsentRawBodyValidatorImpl(DateFieldValidator dateFieldValidator) {
        this.dateFieldValidator = dateFieldValidator;
    }

    @Override
    public MessageError validate(HttpServletRequest request, MessageError messageError) {
        return dateFieldValidator.validateRawDataDates(request, AIS_CONSENT_DATE_FIELDS.getDateFields(), messageError);
    }
}
