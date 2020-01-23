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

package de.adorsys.psd2.xs2a.web.validator.body.payment;

import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.AustriaPaymentBodyFieldsValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.config.AustriaValidationConfigImpl;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.config.DefaultPaymentValidationConfigImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AustriaPaymentBodyFieldsValidatorImplTest {

    private AustriaPaymentBodyFieldsValidatorImpl validator;

    @BeforeEach
    void setUp() {
        validator = new AustriaPaymentBodyFieldsValidatorImpl(null, null);
    }

    @Test
    void checkPaymentValidationConfig() {
        DefaultPaymentValidationConfigImpl validationConfig = validator.createPaymentValidationConfig();
        assertNotNull(validationConfig);
        assertTrue(validationConfig instanceof AustriaValidationConfigImpl);
    }

}
