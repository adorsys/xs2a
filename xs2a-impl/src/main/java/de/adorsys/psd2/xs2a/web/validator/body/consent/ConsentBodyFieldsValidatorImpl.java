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

package de.adorsys.psd2.xs2a.web.validator.body.consent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.model.Consents;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.AbstractBodyValidatorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Component
public class ConsentBodyFieldsValidatorImpl extends AbstractBodyValidatorImpl implements ConsentBodyValidator {

    @Autowired
    public ConsentBodyFieldsValidatorImpl(ErrorBuildingService errorBuildingService, ObjectMapper objectMapper) {
        super(errorBuildingService, objectMapper);
    }

    @Override
    public void validate(HttpServletRequest request, MessageError messageError) {

        Optional<Consents> consentsOptional = mapBodyToInstance(request, messageError, Consents.class);

        // In case of wrong JSON - we don't proceed the inner fields validation.
        if (!consentsOptional.isPresent()) {
            return;
        }

        Consents consents = consentsOptional.get();

        if (Objects.isNull(consents.getRecurringIndicator())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'recurringIndicator' should not be null");
        }

        if (Objects.isNull(consents.getValidUntil())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'validUntil' should not be null");
        } else {
            validateValidUntil(consents.getValidUntil(), messageError);
        }

        if (Objects.isNull(consents.getFrequencyPerDay())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'frequencyPerDay' should not be null");
        } else {
            validateFrequencyPerDay(consents.getFrequencyPerDay(), messageError);
        }
    }

    private void validateValidUntil(LocalDate validUntil, MessageError messageError) {
        if (validUntil.isBefore(LocalDate.now())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'validUntil' should not be in the past");
        }
    }

    private void validateFrequencyPerDay(Integer frequencyPerDay, MessageError messageError) {
        if (frequencyPerDay < 1) {
            errorBuildingService.enrichMessageError(messageError, "Value 'frequencyPerDay' should not be lower than 1");
        }
    }
}
