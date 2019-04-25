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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class MethodValidatorControllerTest {

    @Test
    public void getMethod() {
        List<MethodValidator> methodValidators = new ArrayList<>();
        methodValidators.add(new ConsentMethodValidatorImpl(null, null));
        methodValidators.add(new PaymentMethodValidatorImpl(null, null));
        MethodValidatorController controller = new MethodValidatorController(methodValidators);

        Optional<MethodValidator> methodValidator = controller.getMethod("_createConsent");
        assertTrue(methodValidator.isPresent());
        assertTrue(methodValidator.get() instanceof ConsentMethodValidatorImpl);

        methodValidator = controller.getMethod("_initiatePayment");
        assertTrue(methodValidator.isPresent());
        assertTrue(methodValidator.get() instanceof PaymentMethodValidatorImpl);

        methodValidator = controller.getMethod("");
        assertFalse(methodValidator.isPresent());

        methodValidator = controller.getMethod(null);
        assertFalse(methodValidator.isPresent());

        methodValidator = controller.getMethod("unknown method");
        assertFalse(methodValidator.isPresent());
    }
}
