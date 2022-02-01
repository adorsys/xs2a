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

package de.adorsys.psd2.xs2a.web.validator.body.payment.handler.type;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentTypeValidatorContextTest {

    @Test
    void getValidator() {
        List<PaymentTypeValidator> paymentTypeValidators = new ArrayList<>();
        paymentTypeValidators.add(new SinglePaymentTypeValidatorImpl(null, null, null, null, null, null, null, null));
        paymentTypeValidators.add(new PeriodicPaymentTypeValidatorImpl(null, null, null, null, null, null, null, null));
        paymentTypeValidators.add(new BulkPaymentTypeValidatorImpl(null, null, null, null, null, null, null, null));
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
