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

package de.adorsys.aspsp.xs2a.service.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.FORMAT_ERROR;

@Service
public class ValueValidatorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValueValidatorService.class);

    private Validator validator;

    @Autowired
    public ValueValidatorService(Validator validator) {
        this.validator = validator;
    }

    public void validate(Object objectForValidate) {
        final List<String> violations = validator.validate(objectForValidate).stream()
                                        .map(vl -> vl.getPropertyPath().toString() + " : " + vl.getMessage())
                                        .collect(Collectors.toList());

        if (violations.size() > 0) {
            LOGGER.debug(violations.toString());
            throw new ValidationException(FORMAT_ERROR.name() + ": " + violations);
        }
    }

    public void validate(Object obj, Class<?>... groups) {
        final List<String> violations = validator.validate(obj, groups).stream()
                                        .map(vl -> vl.getPropertyPath().toString() + " : " + vl.getMessage())
                                        .collect(Collectors.toList());

        if (violations.size() > 0) {
            LOGGER.debug(violations.toString());
            throw new ValidationException(FORMAT_ERROR.name() + ": " + violations);
        }
    }
}
