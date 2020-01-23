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

package de.adorsys.psd2.xs2a.web.validator.body.payment.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationObjectTest {

    @Test
    void defaultValues() {
        ValidationObject validationObject = new ValidationObject();
        assertEquals(0, validationObject.getMaxLength());
        assertEquals(Occurrence.OPTIONAL, validationObject.getUse());
        assertTrue(validationObject.isOptional());
    }

    @Test
    void usages() {
        ValidationObject validationObject = new ValidationObject();
        assertEquals(Occurrence.OPTIONAL, validationObject.getUse());
        assertTrue(validationObject.isOptional());

        validationObject.setUse(Occurrence.REQUIRED);
        assertTrue(validationObject.isRequired());

        validationObject.setUse(Occurrence.SKIP);
        assertTrue(validationObject.isSkipped());

        validationObject.setUse(Occurrence.NONE);
        assertTrue(validationObject.isNone());
    }
}
