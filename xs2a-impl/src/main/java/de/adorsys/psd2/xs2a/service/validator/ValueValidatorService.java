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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.service.RequestProviderService;
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
    private final RequestProviderService requestProviderService;

    private Validator validator;

    @Autowired
    public ValueValidatorService(RequestProviderService requestProviderService, Validator validator) {
        this.validator = validator;
        this.requestProviderService = requestProviderService;
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
            log.debug("InR-ID: [{}], X-Request-ID: [{}]. Value validation failed: {}",
                      requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), violations.toString());
            throw new ValidationException(FORMAT_ERROR.name() + ": " + violations);
        }
    }
}
