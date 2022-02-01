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

package de.adorsys.psd2.xs2a.web.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MethodValidatorController {

    private Map<String, MethodValidator> methodValidatorContext = new HashMap<>();
    private List<MethodValidator> methodValidators;
    private DefaultMethodValidatorImpl defaultMethodValidator;

    @Autowired
    public MethodValidatorController(List<MethodValidator> methodValidators, DefaultMethodValidatorImpl defaultMethodValidator) {
        this.methodValidators = methodValidators;
        this.defaultMethodValidator = defaultMethodValidator;

        createMethodValidationContext();
    }

    /**
     * Returns particular {@link MethodValidator} according to method name
     * (i.e. "_createConsent", "_initiatePayment"...)
     *
     * @param methodName from request
     * @return {@link MethodValidator}
     */
    public MethodValidator getMethod(String methodName) {
        return Optional.ofNullable(methodValidatorContext.get(methodName)).orElse(defaultMethodValidator);
    }

    private void createMethodValidationContext() {
        methodValidators.forEach(m -> methodValidatorContext.put(m.getMethodName(), m));
    }
}
