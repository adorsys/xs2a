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
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ConsentMethodValidatorImplTest {

    @Test
    public void init() {
        List<ConsentHeaderValidator> headerValidators = new ArrayList<>();
        List<ConsentBodyValidator> bodyValidators = new ArrayList<>();
        ConsentMethodValidatorImpl methodValidator = new ConsentMethodValidatorImpl(headerValidators, bodyValidators);

        assertEquals("_createConsent", methodValidator.getMethodName());
        assertSame(headerValidators, methodValidator.getHeaderValidators());
        assertSame(bodyValidators, methodValidator.getBodyValidators());
    }
}
