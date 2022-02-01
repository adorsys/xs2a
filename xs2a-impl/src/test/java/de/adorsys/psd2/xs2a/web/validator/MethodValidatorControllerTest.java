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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MethodValidatorControllerTest {

    @Test
    void getMethod() {
        List<MethodValidator> methodValidators = new ArrayList<>();
        methodValidators.add(new ConsentMethodValidatorImpl(null, null));
        methodValidators.add(new PaymentMethodValidatorImpl(null, null));
        methodValidators.add(new CancelPaymentMethodValidatorImpl(null, null));
        DefaultMethodValidatorImpl defaultMethodValidator = new DefaultMethodValidatorImpl(null);
        MethodValidatorController controller = new MethodValidatorController(methodValidators, defaultMethodValidator);

        MethodValidator methodValidator = controller.getMethod("_createConsent");
        assertTrue(methodValidator instanceof ConsentMethodValidatorImpl);

        methodValidator = controller.getMethod("_initiatePayment");
        assertTrue(methodValidator instanceof PaymentMethodValidatorImpl);

        methodValidator = controller.getMethod("_cancelPayment");
        assertTrue(methodValidator instanceof CancelPaymentMethodValidatorImpl);

        methodValidator = controller.getMethod("");
        assertTrue(methodValidator instanceof DefaultMethodValidatorImpl);

        methodValidator = controller.getMethod(null);
        assertTrue(methodValidator instanceof DefaultMethodValidatorImpl);

        methodValidator = controller.getMethod("unknown method");
        assertNotNull(methodValidator);
    }
}
