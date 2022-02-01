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

package de.adorsys.psd2.xs2a.service.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR;

@Slf4j
@Service
public class ValueValidatorService {

    private Validator validator;

    @Autowired
    public ValueValidatorService(Validator validator) {
        this.validator = validator;
    }

    public void validateAccountIdTransactionId(String accountId, String transactionId) {
        ValidationGroup fieldValidator = new ValidationGroup();
        fieldValidator.setAccountId(accountId);
        fieldValidator.setTransactionId(transactionId);

        validate(fieldValidator, ValidationGroup.AccountIdAndTransactionIdIsValid.class);
    }

    public void validate(Object obj, Class<?>... groups) {
        final List<String> violations = validator.validate(obj, groups).stream()
                                            .map(vl -> vl.getPropertyPath().toString() + " : " + vl.getMessage())
                                            .collect(Collectors.toList());

        if (!violations.isEmpty()) {
            log.debug("Value validation failed: {}", violations.toString());
            throw new ValidationException(FORMAT_ERROR.name() + ": " + violations);
        }
    }
}
