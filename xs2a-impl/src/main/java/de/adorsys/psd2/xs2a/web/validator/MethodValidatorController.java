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

    @Autowired
    public MethodValidatorController(List<MethodValidator> methodValidators) {
        this.methodValidators = methodValidators;

        createMethodValidationContext();
    }

    /**
     * Returns particular {@link MethodValidator} according to method name
     * (i.e. "_createConsent", "_initiatePayment"...)
     *
     * @param methodName from request
     * @return {@link MethodValidator}
     */
    public Optional<MethodValidator> getMethod(String methodName) {
        return Optional.ofNullable(methodValidatorContext.get(methodName));
    }

    private void createMethodValidationContext() {
        methodValidators.forEach(m -> methodValidatorContext.put(m.getMethodName(), m));
    }
}
