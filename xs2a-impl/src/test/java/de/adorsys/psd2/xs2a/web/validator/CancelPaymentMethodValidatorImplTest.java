/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.web.validator.body.cancelpayment.CancelPaymentBodyValidator;
import de.adorsys.psd2.xs2a.web.validator.header.CancelPaymentHeaderValidator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CancelPaymentMethodValidatorImplTest {

    @Test
    void init() {
        List<CancelPaymentHeaderValidator> headerValidators = new ArrayList<>();
        List<CancelPaymentBodyValidator> bodyValidators = new ArrayList<>();
        CancelPaymentMethodValidatorImpl methodValidator = new CancelPaymentMethodValidatorImpl(headerValidators, bodyValidators);

        assertEquals("_cancelPayment", methodValidator.getMethodName());
        assertSame(headerValidators, methodValidator.getValidatorWrapper().getHeaderValidators());
        assertSame(bodyValidators, methodValidator.getValidatorWrapper().getBodyValidators());
        assertTrue(methodValidator.getValidatorWrapper().getQueryParameterValidators().isEmpty());
    }
}
