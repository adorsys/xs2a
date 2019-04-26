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

package de.adorsys.psd2.xs2a.web.validator.body.payment.type;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PaymentTypeValidatorContextTest {

    @Test
    public void getValidator() {
        List<PaymentTypeValidator> paymentTypeValidators = new ArrayList<>();
        paymentTypeValidators.add(new SinglePaymentTypeValidatorImpl(null, null, null));
        paymentTypeValidators.add(new PeriodicPaymentTypeValidatorImpl(null, null, null));
        paymentTypeValidators.add(new BulkPaymentTypeValidatorImpl(null, null, null));
        PaymentTypeValidatorContext context = new PaymentTypeValidatorContext(paymentTypeValidators);

        Optional<PaymentTypeValidator> paymentTypeValidator = context.getValidator(PaymentType.SINGLE.getValue());
        assertTrue(paymentTypeValidator.isPresent());
        assertTrue(paymentTypeValidator.get() instanceof SinglePaymentTypeValidatorImpl);

        paymentTypeValidator = context.getValidator(PaymentType.PERIODIC.getValue());
        assertTrue(paymentTypeValidator.isPresent());
        assertTrue(paymentTypeValidator.get() instanceof PeriodicPaymentTypeValidatorImpl);

        paymentTypeValidator = context.getValidator(PaymentType.BULK.getValue());
        assertTrue(paymentTypeValidator.isPresent());
        assertTrue(paymentTypeValidator.get() instanceof BulkPaymentTypeValidatorImpl);

        paymentTypeValidator = context.getValidator("");
        assertFalse(paymentTypeValidator.isPresent());

        paymentTypeValidator = context.getValidator(null);
        assertFalse(paymentTypeValidator.isPresent());

        paymentTypeValidator = context.getValidator("unknown method");
        assertFalse(paymentTypeValidator.isPresent());
    }
}
