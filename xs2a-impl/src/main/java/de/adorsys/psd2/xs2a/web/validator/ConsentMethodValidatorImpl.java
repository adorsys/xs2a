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

import de.adorsys.psd2.xs2a.web.validator.body.consent.ConsentBodyValidator;
import de.adorsys.psd2.xs2a.web.validator.header.ConsentHeaderValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConsentMethodValidatorImpl extends AbstractMethodValidator {

    private static final String METHOD_NAME = "_createConsent";

    @Autowired
    public ConsentMethodValidatorImpl(List<ConsentHeaderValidator> headerValidators,
                                      List<ConsentBodyValidator> bodyValidators) {
        super(headerValidators, bodyValidators);
    }

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }
}
